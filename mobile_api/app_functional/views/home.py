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
from mobile_api.models import Rooms, Requests, Friends, Lobby
from rest_framework import generics, permissions, status
from rest_framework.response import Response

import uuid
from django.db.models import Q

class ActiveGame(APIView):
    permission_classes = (permissions.IsAuthenticated,)
    def get(self, request):
        user = request.user
        room = Rooms.objects.filter(
            Q(user_1=user) | Q(user_2=user)
        ).exclude(status='disabled').first()
        
        channel_id = room.channel_id if room else None
        return Response({"channel_id": channel_id}, status=status.HTTP_200_OK)

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


























class PrivateRoom(AsyncWebsocketConsumer):
    
    async def connect(self):
        self.was_sendet_start = False
        self.lobby_id = self.scope['url_route']['kwargs']['lobby_id']

        query_string = self.scope['query_string'].decode('utf-8')
        token = self.get_token_from_query(query_string)

        self.user = await self.authenticate_user(token)

        if self.user is None:
            await self.close()
            return

        self.user_id = self.user.id
        self.room_group_name = f"lobby_{self.lobby_id}"
        self.cleanup_task = None

        await self.set_user_connected()

        lobby = await self.get_lobby()

        await self.channel_layer.group_add(
            self.room_group_name,
            self.channel_name
        )

        await self.accept()

        info = await self.get_lobby_info()


        await self.send(text_data=json.dumps({
            'type': 'connected',

            'user_id': self.user.id,
            'username': self.user.username,

            'user_1_id': info['user_1_id'],
            'user_1_name': info['user_1_name'],

            'user_2_id': info['user_2_id'],
            'user_2_name': info['user_2_name'],

            'user_1_ready': info['user_1_ready'],
            'user_2_ready': info['user_2_ready'],

            'message': 'Подключение выполнено'
        }))

        if lobby.user_1_id == self.user.id:
            await self.send(text_data=json.dumps({
                'type': 'waiting',
                'message': 'Ожидание соперника...'
            }))

            if lobby.user_2_in:
                await self.notify_players()

        elif lobby.user_2_id == self.user.id:
            await self.send(text_data=json.dumps({
                'type': 'waiting',
                'message': 'Ожидание соперника...'
            }))

            if lobby.user_1_in:
                await self.notify_players()
        self.check_req_task = asyncio.create_task(self.check_req())
    async def disconnect(self, close_code):

        if self.user is None:
            return

        print(f"Игрок {self.user.username} отключился. Код: {close_code}")

        await self.rm_user()

        try:
            lobby = await self.get_lobby()
            room = await self.get_room()
            if not lobby.user_1_in or not lobby.user_2_in:
                await self.delete_lobby(room)
                await self.delete_room_own()
                await self.delete_req()
                if self.check_req_task:
                    self.check_req_task.cancel()

        except Lobby.DoesNotExist:
            pass

        await self.channel_layer.group_send(
            self.room_group_name,
            {
                'type': 'opponent_disconnected',
                'user_id': self.user.id,
                'username': self.user.username,
            }
        )

        await self.channel_layer.group_discard(
            self.room_group_name,
            self.channel_name
        )

    async def receive(self, text_data=None, bytes_data=None):

        if text_data is None:
            return

        data = json.loads(text_data)
        message_type = data.get('type')

        if message_type == 'ready':
            await self.set_ready()
            await self.notify_ready()

        elif message_type == 'not_ready':
            await self.set_not_ready()
            await self.notify_notready()
            self.was_sendet_start = False
            
        lobby = await self.get_lobby()
        if (
            lobby.user_1_ready
            and lobby.user_2_ready
            and lobby.status != 'playing'
        ):
            await self.set_status_lobby('playing')
            
            await self.start_game()

    def get_token_from_query(self, query_string):
        params = {}

        for param in query_string.split('&'):
            if '=' in param:
                key, value = param.split('=')
                params[key] = value

        return params.get('token', '')

    async def notify_players(self):
        await self.channel_layer.group_send(
            self.room_group_name,
            {
                'type': 'notify',
                'message': 'Соперник подключился!'
            }
        )

    async def notify(self, event):
        await self.send(text_data=json.dumps({
            'type': 'notify',
            'message': event['message'],
        }))
        
    async def start_game(self):
        room = await self.get_room()
        await self.channel_layer.group_send(
            self.room_group_name,
            {
                'type': 'start',
                'message': 'Запуск',
                'room_id': room.channel_id
            }
        )
        
        await self.delete_lobby(room)

    async def start(self, event):
        await self.send(text_data=json.dumps({
            'type': 'start',
            'message': event['message'],
            'room_id': event['room_id'],
        }))
        
    async def notify_ready(self):
        lobby = await self.get_lobby()
        await self.channel_layer.group_send(
            self.room_group_name,
            {
                'type': 'ready',
                'message': f'Игрок {self.user.username} готов',

                'user_1_id': lobby.user_1_id,
                'user_2_id': lobby.user_2_id,

                'user_1_ready': lobby.user_1_ready,
                'user_2_ready': lobby.user_2_ready,
            }
        )

    async def ready(self, event):
        await self.send(text_data=json.dumps({
            'type': 'ready',
            'message': event['message'],
            'user_1_id': event['user_1_id'],
            'user_2_id': event['user_2_id'],
            'user_1_ready': event['user_1_ready'],
            'user_2_ready': event['user_2_ready'],
        }))

    async def notify_notready(self):
        lobby = await self.get_lobby()
        await self.channel_layer.group_send(
            self.room_group_name,
            {
                'type': 'ready',
                'message': f'Игрок {self.user.username} готов',

                'user_1_id': lobby.user_1_id,
                'user_2_id': lobby.user_2_id,

                'user_1_ready': lobby.user_1_ready,
                'user_2_ready': lobby.user_2_ready,
            }
        )

    async def notready(self, event):
        await self.send(text_data=json.dumps({
            'type': 'notready',
            'message': event['message'],
            'user_1_id': event['user_1_id'],
            'user_2_id': event['user_2_id'],
            'user_1_ready': event['user_1_ready'],
            'user_2_ready': event['user_2_ready'],
        }))
    
    
    @database_sync_to_async
    def get_room(self):
        lobby = Lobby.objects.get(hash=self.lobby_id)
        return lobby.room
    @database_sync_to_async
    def set_user_connected(self):
        lobby = Lobby.objects.get(hash=self.lobby_id)

        if lobby.user_1_id == self.user.id:
            lobby.user_1_in = True

        elif lobby.user_2_id == self.user.id:
            lobby.user_2_in = True

        lobby.save()

    @database_sync_to_async
    def get_lobby(self):
        return Lobby.objects.get(hash=self.lobby_id)

    @database_sync_to_async
    def set_ready(self):
        lobby = Lobby.objects.get(hash=self.lobby_id)

        if lobby.user_1_id == self.user.id:
            lobby.user_1_ready = True

        elif lobby.user_2_id == self.user.id:
            lobby.user_2_ready = True

        lobby.save()

    @database_sync_to_async
    def set_not_ready(self):
        lobby = Lobby.objects.get(hash=self.lobby_id)

        if lobby.user_1_id == self.user.id:
            lobby.user_1_ready = False

        elif lobby.user_2_id == self.user.id:
            lobby.user_2_ready = False

        lobby.save()

    @database_sync_to_async
    def delete_room_own(self):
        Rooms.objects.filter(
            Q(user_1=self.user) | Q(user_2=self.user),
            type='private',
            status='created'
        ).delete()

    @database_sync_to_async
    def delete_lobby(self, room):
        Lobby.objects.filter(
            Q(user_1=self.user) | Q(user_2=self.user),
            room=room
        ).delete()
        
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
        
    @database_sync_to_async
    def rm_user(self):
        try:
            lobby = Lobby.objects.get(hash=self.lobby_id)

            if lobby.user_1_id == self.user.id:
                lobby.user_1_in = False
                lobby.user_1_ready = False
                lobby.save(update_fields=['user_1_in', 'user_1_ready'])

            elif lobby.user_2_id == self.user.id:
                lobby.user_2_in = False
                lobby.user_2_ready = False
                lobby.save(update_fields=['user_2_in', 'user_2_ready'])

        except Lobby.DoesNotExist:
            pass
        
    
    @database_sync_to_async
    def get_requests(self):
        try:
            req = Requests.objects.get(data=self.lobby_id)
        except Requests.DoesNotExist:
            return None
        return req
    
    @database_sync_to_async
    def delete_req(self):
        req = Requests.objects.get(data=self.lobby_id)
        req.delete()
    
    @database_sync_to_async
    def set_status_lobby(self, status):
        try:
            lobby = Lobby.objects.get(hash=self.lobby_id)
            lobby.status = status
            lobby.save()
            return True
        except Lobby.DoesNotExist:
            return False

    
    def stop_loop_check(self):
        if self.check_req_task:
            self.check_req_task.cancel()
            
    
    @database_sync_to_async
    def get_lobby_info(self):
        lobby = Lobby.objects.select_related(
            'user_1',
            'user_2'
        ).get(hash=self.lobby_id)

        return {
            'user_1_id': lobby.user_1.id,
            'user_1_name': lobby.user_1.username,

            'user_2_id': lobby.user_2.id,
            'user_2_name': lobby.user_2.username,

            'user_1_ready': lobby.user_1_ready,
            'user_2_ready': lobby.user_2_ready,
        }
    async def check_req(self):
        
        while True:
            req = await self.get_requests()
            if not req:
                await self.send(text_data=json.dumps({
                    'type': 'request_dose_note_exists',
                    'message': f'Не удалось найти запрос'
                }))
                self.stop_loop_check()
                return
            if req.status == "aprooved":
                await self.send(text_data=json.dumps({
                    'type': 'friend_accepted_req',
                    'message': f'Друг принял приглашение'
                }))
                self.stop_loop_check()
                return
            if req.status == "canceld":
                await self.send(text_data=json.dumps({
                    'type': 'friend_canceld_req',
                    'message': f'Друг отклонил приглашение'
                }))
                self.stop_loop_check()
                return
            await asyncio.sleep(1)
        
    