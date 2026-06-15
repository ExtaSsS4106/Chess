import json
from datetime import datetime
import time 
from channels.db import database_sync_to_async
from channels.generic.websocket import AsyncWebsocketConsumer
from django.contrib.auth.models import User
from rest_framework import permissions
from rest_framework.permissions import IsAuthenticated
from rest_framework.views import APIView
from rest_framework_simplejwt.tokens import AccessToken
from mobile_api.models import Rooms, Requests, Friends

class GameConsumer(AsyncWebsocketConsumer):
    searching: bool = True
    stop_searching: bool = False
    async def connect(self):
        # Получаем токен из query string
        query_string = self.scope['query_string'].decode('utf-8')
        token = self.get_token_from_query(query_string)
        
        # Аутентифицируем пользователя по токену
        self.user = await self.authenticate_user(token)
        
        if self.user is None:
            await self.close()
            return
        
        # Принимаем соединение
        await self.accept()
        
        # Отправляем приветствие
        await self.send(text_data=json.dumps({
            'type': 'connected',
            'user_id': self.user.id,
            'username': self.user.username,
            'message': 'Поиск играков'
        }))
        room = await self.find_opponent()
        if room == "timeout":
            await self.send(text_data=json.dumps({
                'type': 'search_timeout',
                'message': 'Поиск игроков завершён по таймауту'
            }))
            await self.close()
            return
        if room is None:
            await self.send(text_data=json.dumps({
                'type': 'search_stopped',
                'message': 'Поиск остановлен'
            }))
            await self.close()
            return
        if room.user_1 == self.user:
            if await self.check_room_access(room):
                await self.send(text_data=json.dumps({
                    'type': 'waiting_for_opponent',
                    'room_id': room.id,
                    'message': 'Ожидание соперника'
                }))
        else:
            if await self.set_status_for_random_room(room):
                await self.send(text_data=json.dumps({
                    'type': 'opponent_found',
                    'room_id': room.id,
                    'message': f'Найдён соперник: {room.user_1.username}'
                }))
            else:
                await self.send(text_data=json.dumps({
                    'type': 'error',
                    'message': 'Ошибка при подключении к комнате'
                }))
                await self.close()
                return
        
        
        
    
    async def disconnect(self, close_code):
        print(f"User {self.user.username if self.user else 'Unknown'} disconnected")
        await self.delete_room_own()
    
    async def receive(self, text_data=None, bytes_data=None):
        if text_data is None:
            return
        
        if text_data == 'ping':
            await self.send(text_data=json.dumps({
                'type': 'pong',
                'timestamp': str(datetime.now())
            }))
            return
        
        if text_data == 'close':
            self.stop_searching = True
            await self.send(text_data=json.dumps({
                'type': 'search_stopped',
                'message': 'Поиск остановлен'
            }))
            await self.close()
            return
        
        
    
    def get_token_from_query(self, query_string):
        """Извлекаем токен из query string"""
        params = {}
        for param in query_string.split('&'):
            if '=' in param:
                key, value = param.split('=')
                params[key] = value
        return params.get('token', '')
    
    @database_sync_to_async
    def authenticate_user(self, token):
        """Аутентификация по JWT токену"""
        if not token:
            print("No token provided")
            return None
        
        try:
            # Декодируем токен
            access_token = AccessToken(token)
            user_id = access_token['user_id']
            user = User.objects.get(id=user_id)
            print(f"User authenticated: {user.username}")
            return user
        except Exception as e:
            print(f"Authentication error: {e}")
            return None
    @database_sync_to_async    
    def find_opponent(self):
        """Ищем соперника для игры"""
        timeout = 180  # Максимальное время поиска в секундах
        time_elapsed = 0
        try:
            while self.searching:
                # Whish to stop searching?
                if self.stop_searching or time_elapsed >= timeout:
                    # Have your own room?
                    room = Rooms.objects.get(type='random', status='created', user_1=self.user)
                    if room.exists():
                        room.delete()
                    self.searching = False
                    self.stop_searching = False
                    if time_elapsed >= timeout:
                        return "timeout"
                    return None
                # Any free room exists?
                room = Rooms.objects.filter(type='random', status='created', user_2__isnull=True).first()
                if room:
                    if room.user_1 != self.user:
                        # Have your own room?
                        if Rooms.objects.filter(type='random', status='created', user_1=self.user).exists():
                            # Is any one in your room?
                            if Rooms.objects.filter(type='random', status='created', user_1=self.user, user_2__isnull=False).exists():
                                # Connect to your room
                                room = Rooms.objects.get(type='random', status='created', user_1=self.user)
                                self.searching = False
                                return room
                            else:
                                Rooms.objects.filter(type='random', status='created', user_1=self.user).first().delete()
                                self.searching = False
                                return room
                        
                        else:
                            # Connect to the free room
                            self.searching = False
                            return room
                # Have your own room?
                if Rooms.objects.filter(type='random', status='created', user_1=self.user).exists():
                    # Is any one in your room?
                    if Rooms.objects.filter(type='random', status='created', user_1=self.user, user_2__isnull=False).exists():
                        # Connect to your room
                        room = Rooms.objects.get(type='random', status='created', user_1=self.user)
                        self.searching = False
                        return room
                else:
                    Rooms.objects.create(type='random', status='created', user_1=self.user)
                time.sleep(1)
                time_elapsed += 1
        except Exception as e:
            room = Rooms.objects.filter(type='random', status='created', user_1=self.user)
            if room.exists():
                room.first().delete()
            print(f"Error in find_opponent: {e}\n\ncode row: {e.__traceback__.tb_lineno}")
            return None
        
    @database_sync_to_async
    def delete_room_own(self):
        room = Rooms.objects.filter(type='random', status='created', user_1=self.user)
        if room.exists():
            room.first().delete()
        return None
    @database_sync_to_async
    def set_status_for_random_room(self, room):
        """Устанавливаем статус для случайной комнаты"""
        if not room:
            return False
        if room.status != 'created':
            return False
        if room.type != 'random':
            return False
        room.status = 'playing'
        room.user_2 = self.user
        room.save()
        return True
    
    @database_sync_to_async
    def check_room_access(self, room):
        if not room.exists():
            return False
        if room.status != 'created':
            return False
        if not (room.user_1 == self.user or room.user_2 == self.user):
            return False
        if room.user_1__isnull and room.user_2__isnull:
            return False
        return True
class PrivateRoom(APIView):
    permission_classes = (IsAuthenticated,)

    def post(self, request):
        return None