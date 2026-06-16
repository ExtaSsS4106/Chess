import json
from datetime import datetime
import asyncio
from channels.db import database_sync_to_async
from channels.generic.websocket import AsyncWebsocketConsumer
from django.contrib.auth.models import User
from django.db import models
from rest_framework import permissions
from rest_framework.permissions import IsAuthenticated
from rest_framework.views import APIView
from rest_framework_simplejwt.tokens import AccessToken
from mobile_api.models import Rooms, Requests, Friends
import uuid

class GameConsumer(AsyncWebsocketConsumer):
    searching: bool = True
    stop_searching: bool = False
    search_task = None
    user_id = None  # Сохраняем ID пользователя отдельно
    
    async def connect(self):
        query_string = self.scope['query_string'].decode('utf-8')
        token = self.get_token_from_query(query_string)
        
        self.user = await self.authenticate_user(token)
        
        if self.user is None:
            await self.close()
            return
        
        # Сохраняем ID пользователя
        self.user_id = self.user.id
        
        await self.accept()
        
        await self.send(text_data=json.dumps({
            'type': 'connected',
            'user_id': self.user.id,
            'username': self.user.username,
            'message': 'Поиск игроков запущен'
        }))
        
        self.searching = True
        self.search_task = asyncio.create_task(self.find_opponent_loop())
    
    async def disconnect(self, close_code):
        print(f"User {self.user.username if self.user else 'Unknown'} disconnected with code: {close_code}")
        self.searching = False
        if self.search_task:
            self.search_task.cancel()
        await self.delete_room_own()
    
    async def receive(self, text_data=None, bytes_data=None):
        if text_data is None:
            return
        
        print(f"[DEBUG] Received from {self.user.username}: '{text_data}'")
        
        if text_data == 'ping':
            await self.send(text_data=json.dumps({
                'type': 'pong',
                'timestamp': str(datetime.now())
            }))
            return
        
        if text_data == 'close':
            print(f"User {self.user.username} requested to close")
            self.searching = False
            self.stop_searching = True
            if self.search_task:
                self.search_task.cancel()
            await self.delete_room_own()
            await self.send(text_data=json.dumps({
                'type': 'search_stopped',
                'message': 'Поиск остановлен'
            }))
            await asyncio.sleep(0.1)
            await self.close()
            return
    
    def get_token_from_query(self, query_string):
        params = {}
        for param in query_string.split('&'):
            if '=' in param:
                key, value = param.split('=')
                params[key] = value
        return params.get('token', '')
    
    @database_sync_to_async
    def authenticate_user(self, token):
        if not token:
            print("No token provided")
            return None
        try:
            access_token = AccessToken(token)
            user_id = access_token['user_id']
            user = User.objects.get(id=user_id)
            print(f"User authenticated: {user.username}")
            return user
        except Exception as e:
            print(f"Authentication error: {e}")
            return None
    
    async def find_opponent_loop(self):
        timeout = 180
        elapsed = 0
        waiting_message_sent = False

        try:
            while self.searching and elapsed < timeout:
                if self.stop_searching:
                    return

                # 1. Проверяем свою комнату
                my_room_pk, channel_id = await self.get_own_room_id()
                if my_room_pk:
                    room_data = await self.get_room_data(my_room_pk)
                    if room_data and room_data.get('user_2_id'):
                        await self.send(text_data=json.dumps({
                            'type': 'opponent_found',
                            'channel_id': channel_id,  # <- channel_id для клиента
                            'message': f'Соперник {room_data["user_2_username"]} подключился!'
                        }))
                        return

                # 2. Ищем чужую свободную комнату
                free_room_pk, channel_id = await self.get_free_room_id()
                if free_room_pk:
                    print(f"[DEBUG] Found free room {free_room_pk}, trying to join...")
                    success = await self.set_status_for_random_room(free_room_pk)
                    if success:
                        print(f"[DEBUG] Successfully joined room {free_room_pk}")
                        await self.delete_room_own()
                        room_data = await self.get_room_data(free_room_pk)
                        await self.send(text_data=json.dumps({
                            'type': 'opponent_found',
                            'channel_id': channel_id,  # <- channel_id для клиента
                            'message': f'Найден соперник: {room_data["user_1_username"]}'
                        }))
                        return
                    else:
                        print(f"[DEBUG] Failed to join room {free_room_pk}")

                # 3. Создаем свою комнату если нет
                if not my_room_pk:
                    my_room_pk = await self.create_room()
                    waiting_message_sent = False
                    if my_room_pk:
                        print(f"[DEBUG] Created own room {my_room_pk}")

                if my_room_pk and not waiting_message_sent:
                    # channel_id для waiting сообщения берём из БД
                    _, my_channel_id = await self.get_own_room_id()
                    await self.send(text_data=json.dumps({
                        'type': 'waiting_for_opponent',
                        'room_id': my_channel_id,
                        'message': 'Ожидание соперника...'
                    }))
                    waiting_message_sent = True

                await asyncio.sleep(1)
                elapsed += 1

            await self.send(text_data=json.dumps({'type': 'search_timeout'}))
            await self.close()

        except asyncio.CancelledError:
            pass
        except Exception as e:
            print(f"Error in find_opponent_loop: {e}")
            await self.close()
    
    @database_sync_to_async
    def get_own_room_id(self):
        try:
            room = Rooms.objects.filter(
                type='random', 
                user_1_id=self.user_id
            ).filter(
                models.Q(status='created') | models.Q(status='playing')
            ).first()
            if room:
                return room.id, room.channel_id
            else:
                return None, None
        except Exception as e:
            print(f"Error in get_own_room_id: {e}")
            return None
    
    @database_sync_to_async
    def get_free_room_id(self):
        try:
            room = Rooms.objects.filter(
                type='random', 
                status='created', 
                user_2__isnull=True
            ).exclude(user_1_id=self.user_id).first()
            if room:
                return room.id, room.channel_id
            else:
                return None, None
        except Exception as e:
            print(f"Error in get_free_room_id: {e}")
            return None
    
    @database_sync_to_async
    def get_room_data(self, room_id):
        try:
            room = Rooms.objects.get(id=room_id)
            return {
                'id': room.id,
                'user_1_id': room.user_1.id,
                'user_1_username': room.user_1.username,
                'user_2_id': room.user_2.id if room.user_2 else None,
                'user_2_username': room.user_2.username if room.user_2 else None,
                'status': room.status,
                'type': room.type,
                'channel_id': room.channel_id
            }
        except Rooms.DoesNotExist:
            return None
        except Exception as e:
            print(f"Error in get_room_data: {e}")
            return None
    
    @database_sync_to_async
    def delete_room_own(self):
        try:
            Rooms.objects.filter(type='random', status="created", user_1_id=self.user_id).delete()
            print(f"[DEBUG] Cleaned up rooms for user {self.user_id}")
        except Exception as e:
            print(f"Error in delete_room_own: {e}")
        return None
    
    @database_sync_to_async
    def set_status_for_random_room(self, room_id):
        try:
            room = Rooms.objects.get(id=room_id)
            print(f"[DEBUG] Room {room_id}: status={room.status}, type={room.type}, user_2={room.user_2}")
            
            if room.status != 'created':
                print(f"[DEBUG] Room status is {room.status}, not 'created'")
                return False
            if room.type != 'random':
                print(f"[DEBUG] Room type is {room.type}, not 'random'")
                return False
            
            user = User.objects.get(id=self.user_id)
            print(room.user_2)
            room.user_2 = user
            print(room.user_2)
            room.status = 'playing'
            room.save()
            print(f"[DEBUG] SUCCESS! User {user.username} (id={self.user_id}) assigned as user_2 to room {room_id}")
            print(f"[DEBUG] After save: user_2_id={room.user_2.id if room.user_2 else None}")
            return True
        except Rooms.DoesNotExist:
            print(f"[DEBUG] Room {room_id} does not exist")
            return False
        except Exception as e:
            print(f"Error in set_status_for_random_room: {e}")
            return False
    
    @database_sync_to_async
    def create_room(self):
        try:
            channel_id = self.generate_unique_channel_id()
            user = User.objects.get(id=self.user_id)
            room = Rooms.objects.create(type='random', status='created', user_1=user, channel_id=channel_id)
            print(f"[DEBUG] Created room {room.id} for user {user.username}")
            return room.id
        except Exception as e:
            print(f"Error in create_room: {e}")
            return None
        
    def generate_unique_channel_id(self):
        while True:
            channel_id = str(uuid.uuid4())[:25]  # Генерируем ID
            if not Rooms.objects.filter(channel_id=channel_id).exists():
                return channel_id

class PrivateRoom(APIView):
    permission_classes = (IsAuthenticated,)

    def post(self, request):
        return None