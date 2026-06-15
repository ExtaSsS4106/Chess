import json
from datetime import datetime

from channels.db import database_sync_to_async
from channels.generic.websocket import AsyncWebsocketConsumer
from django.contrib.auth.models import User
from rest_framework import permissions
from rest_framework.permissions import IsAuthenticated
from rest_framework.views import APIView
from rest_framework_simplejwt.tokens import AccessToken

class GameConsumer(AsyncWebsocketConsumer):
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
            'message': 'Вы подключены к WebSocket серверу'
        }))
    
    async def disconnect(self, close_code):
        print(f"User {self.user.username if self.user else 'Unknown'} disconnected")
    
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
            await self.close()
            return
        data = json.loads(text_data)
        action = data.get('action')
        message = data.get('message', '')
        
        # Простое эхо: отправляем обратно то же сообщение
        await self.send(text_data=json.dumps({
            'type': 'echo',
            'action': action,
            'your_message': message,
            'from_user': self.user.username,
            'timestamp': str(datetime.now())
        }))
    
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
    
class PrivateRoom(APIView):
    permission_classes = (IsAuthenticated,)

    def post(self, request):
        return None