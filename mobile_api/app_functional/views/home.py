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


class GameConsumer(AsyncWebsocketConsumer):
    searching: bool = True
    stop_searching: bool = False
    search_task = None
    
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
            'message': 'Поиск игроков запущен'
        }))
        
        # ЗАПУСКАЕМ ПОИСК В ФОНЕ, чтобы connect завершился и receive работал
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
            print(f"User {self.user.username} requested to close the connection")
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
            access_token = AccessToken(token)
            user_id = access_token['user_id']
            user = User.objects.get(id=user_id)
            print(f"User authenticated: {user.username}")
            return user
        except Exception as e:
            print(f"Authentication error: {e}")
            return None
    
    async def find_opponent_loop(self):
        """Фоновый цикл поиска соперника"""
        timeout = 180
        elapsed = 0
        waiting_message_sent = False
        
        try:
            while self.searching and elapsed < timeout:
                if self.stop_searching:
                    return
                
                # 1. Ищем чужую свободную комнату
                free_room_id = await self.get_free_room_id()
                if free_room_id:
                    if await self.set_status_for_random_room(free_room_id):
                        room_data = await self.get_room_data(free_room_id)
                        if room_data:
                            await self.send(text_data=json.dumps({
                                'type': 'opponent_found',
                                'room_id': free_room_id,
                                'message': f'Найден соперник: {room_data["user_1_username"]}'
                            }))
                            return  # Выходим из поиска
                
                # 2. Проверяем свою комнату (не зашел ли кто-то)
                my_room_id = await self.get_own_room_id()
                if my_room_id:
                    room_data = await self.get_room_data(my_room_id)
                    if room_data and room_data.get('user_2_id'):
                        await self.send(text_data=json.dumps({
                            'type': 'opponent_found',
                            'room_id': my_room_id,
                            'message': f'Соперник {room_data["user_2_username"]} подключился!'
                        }))
                        return
                    elif not waiting_message_sent:
                        # Отправляем сообщение об ожидании только один раз
                        await self.send(text_data=json.dumps({
                            'type': 'waiting_for_opponent',
                            'room_id': my_room_id,
                            'message': 'Ожидание соперника...'
                        }))
                        waiting_message_sent = True
                else:
                    # Если своей нет - создаем
                    my_room_id = await self.create_room()
                    if my_room_id:
                        waiting_message_sent = False
                
                if elapsed % 15 == 0 and elapsed > 0:
                    print(f"[DEBUG] User {self.user.username} searching... {elapsed}s")
                
                await asyncio.sleep(3)
                elapsed += 3
            
            # Таймаут
            if elapsed >= timeout:
                await self.send(text_data=json.dumps({
                    'type': 'search_timeout',
                    'message': 'Поиск игроков завершён по таймауту'
                }))
                await self.close()
                
        except asyncio.CancelledError:
            print(f"[DEBUG] Search task cancelled for {self.user.username}")
        except Exception as e:
            print(f"[ERROR] in find_opponent_loop: {e}")
            await self.close()
    
    @database_sync_to_async
    def get_own_room_id(self):
        """Получаем ID собственной комнаты пользователя (включая статус 'playing')"""
        try:
            # Ищем комнату, где мы создатель, в любом активном статусе
            room = Rooms.objects.filter(
                type='random', 
                user_1=self.user
            ).filter(
                models.Q(status='created') | models.Q(status='playing')
            ).first()
            return room.id if room else None
        except Exception as e:
            print(f"Error in get_own_room_id: {e}")
            return None
    
    @database_sync_to_async
    def get_free_room_id(self):
        """Ищем свободную комнату для подключения"""
        try:
            room = Rooms.objects.filter(
                type='random', 
                status='created', 
                user_2__isnull=True
            ).exclude(user_1=self.user).first()
            return room.id if room else None
        except Exception as e:
            print(f"Error in get_free_room_id: {e}")
            return None
    
    @database_sync_to_async
    def get_room_data(self, room_id):
        """Возвращает СЛОВАРЬ с данными, не объект"""
        try:
            room = Rooms.objects.get(id=room_id)
            return {
                'id': room.id,
                'user_1_id': room.user_1.id,
                'user_1_username': room.user_1.username,
                'user_2_id': room.user_2.id if room.user_2 else None,
                'user_2_username': room.user_2.username if room.user_2 else None,
                'status': room.status,
                'type': room.type
            }
        except Rooms.DoesNotExist:
            return None
        except Exception as e:
            print(f"Error in get_room_data: {e}")
            return None
    
    @database_sync_to_async
    def delete_room_own(self):
        """Удаляем свою комнату"""
        try:
            # Удаляем комнаты где пользователь создатель
            Rooms.objects.filter(type='random', user_1=self.user).delete()
            # Освобождаем комнаты, где пользователь был вторым
            Rooms.objects.filter(type='random', user_2=self.user).update(user_2=None)
        except Exception as e:
            print(f"Error in delete_room_own: {e}")
        return None
    
    @database_sync_to_async
    def set_status_for_random_room(self, room_id):
        """Устанавливаем статус для случайной комнаты"""
        try:
            room = Rooms.objects.get(id=room_id)
            if room.status != 'created':
                return False
            if room.type != 'random':
                return False
            room.status = 'playing'
            room.user_2 = self.user
            room.save()
            return True
        except Rooms.DoesNotExist:
            return False
        except Exception as e:
            print(f"Error in set_status_for_random_room: {e}")
            return False
    
    @database_sync_to_async
    def create_room(self):
        """Создаём комнату для случайной игры"""
        try:
            room = Rooms.objects.create(type='random', status='created', user_1=self.user)
            return room.id
        except Exception as e:
            print(f"Error in create_room: {e}")
            return None
    



