import json
import asyncio
from urllib.parse import parse_qs
from channels.db import database_sync_to_async
from channels.generic.websocket import AsyncWebsocketConsumer
from django.contrib.auth.models import User
from rest_framework_simplejwt.tokens import AccessToken
from mobile_api.models import Rooms

RECONNECT_TIMEOUT = 60  # сек на возвращение соперника


class Session(AsyncWebsocketConsumer):

    async def connect(self):
        self.cleanup_task = None
        self.user = None           # <- инициализируем явно
        self.room_group_name = None  # <- инициализируем явно
        self.channel_id = self.scope['url_route']['kwargs']['channel_id']

        token = self.get_token_from_query()
        self.user = await self.authenticate_user(token)

        if self.user is None:
            await self.close()
            return

        room = await self.get_room(self.channel_id)

        if room is None or room.status == 'disabled':
            await self.close()
            return

        # Присваиваем только после всех проверок
        self.room_group_name = f"game_{self.channel_id}"

        await self.channel_layer.group_add(self.room_group_name, self.channel_name)
        await self.accept()

        room = await self.add_user_and_get_room()

        is_white = room.user_1_id == self.user.id 

        await self.send(text_data=json.dumps({
            'type': 'connected',
            'is_white': is_white,
            'message': 'Вы подключены к игре'
        }))

        await self.channel_layer.group_send(
            self.room_group_name,
            {
                'type': 'opponent_reconnected',
                'user_id': self.user.id,
            }
        )

        if room.user_1 == self.user:
            await self.send(text_data=json.dumps({
                'type': 'waiting',
                'message': 'Ожидание соперника...'
            }))
            if room.user_2_in:
                await self.start_game_for_both()

        elif room.user_2 == self.user:
            await self.send(text_data=json.dumps({
                'type': 'waiting',
                'message': 'Ожидание соперника...'
            }))
            if room.user_1_in:
                await self.start_game_for_both()

    async def disconnect(self, close_code):
        # Защита: если упали до group_add — выходим
        if self.user is None or self.room_group_name is None:
            return

        print(f"Игрок {self.user.username} отключился. Код: {close_code}")

        await self.rm_user()

        if self.cleanup_task and not self.cleanup_task.done():
            self.cleanup_task.cancel()
            self.cleanup_task = None
            await self.disable_room()
            print(f"Комната {self.channel_id} отключена: оба игрока вышли")
        else:
            await self.set_room_waiting()

        await self.channel_layer.group_send(
            self.room_group_name,
            {
                'type': 'opponent_disconnected',
                'user_id': self.user.id,
                'username': self.user.username,
            }
        )

        await self.channel_layer.group_discard(self.room_group_name, self.channel_name)

    # === Хендлеры сообщений группы ===

    async def start_game_for_both(self):
        await self.channel_layer.group_send(
            self.room_group_name,
            {'type': 'start_game', 'message': 'Соперник подключился!'}
        )

    async def start_game(self, event):
        await self.update_room_status_playing()
        await self.send(text_data=json.dumps({
            'type': 'start_game',
            'message': event['message'],
        }))

    async def opponent_disconnected(self, event):
        # своё же сообщение игнорируем
        if event['user_id'] == self.user.id:
            return

        await self.send(text_data=json.dumps({
            'type': 'opponent_disconnected',
            'message': f"Игрок {event['username']} покинул игру. Ожидание возвращения..."
        }))

        # запускаем таймер ожидания, если он ещё не запущен
        if self.cleanup_task is None or self.cleanup_task.done():
            self.cleanup_task = asyncio.create_task(self.wait_for_opponent_reconnect())

    async def opponent_reconnected(self, event):
        if event['user_id'] == self.user.id:
            return

        if self.cleanup_task and not self.cleanup_task.done():
            self.cleanup_task.cancel()
            self.cleanup_task = None
            await self.update_room_status_playing()
            await self.send(text_data=json.dumps({
                'type': 'opponent_reconnected',
                'message': 'Соперник вернулся в игру'
            }))

    async def opponent_move(self, event):
        if event['sender_id'] == self.user.id:
            return
        await self.send(text_data=json.dumps({
            'type': 'opponent_move',
            'desck': event['desck'],
        }))

    # === Таймер ожидания соперника ===

    async def wait_for_opponent_reconnect(self):
        try:
            await asyncio.sleep(RECONNECT_TIMEOUT)
        except asyncio.CancelledError:
            return

        # соперник не вернулся за отведённое время
        await self.disable_room()
        print(f"Комната {self.channel_id} отключена по таймауту")

        await self.send(text_data=json.dumps({
            'type': 'error',
            'message': 'Соперник не вернулся. Игра завершена.'
        }))

        self.cleanup_task = None
        await self.close()

    # === receive ===

    async def receive(self, text_data=None, bytes_data=None):
        if text_data is None: return
        try:
            data = json.loads(text_data)
            await self.save_desk(data)
            await self.channel_layer.group_send(
                self.room_group_name,
                {
                    'type': 'opponent_move',
                    'desck': data, 
                    'sender_id': self.user.id,
                }
            )
        except Exception as e:
            print(f"Error: {e}")
    # === Вспомогательные методы ===

    def get_token_from_query(self):
        query_string = self.scope['query_string'].decode('utf-8')
        params = parse_qs(query_string)
        return params.get('token', [''])[0]

    @database_sync_to_async
    def authenticate_user(self, token):
        try:
            access_token = AccessToken(token)
            user_id = access_token['user_id']
            return User.objects.get(id=user_id)
        except Exception:
            return None

    @database_sync_to_async
    def get_room(self, channel_id):
        try:
            room = Rooms.objects.get(channel_id=channel_id)
            if room.user_1_id == self.user.id or room.user_2_id == self.user.id:
                return room
            return None
        except Rooms.DoesNotExist:
            return None

    @database_sync_to_async
    def add_user_and_get_room(self):
        room = Rooms.objects.select_related('user_1', 'user_2').get(channel_id=self.channel_id)
        if room.user_1_id == self.user.id:
            room.user_1_in = True
            room.save(update_fields=['user_1_in'])
        elif room.user_2_id == self.user.id:
            room.user_2_in = True
            room.save(update_fields=['user_2_in'])
        return room

    @database_sync_to_async
    def rm_user(self):
        try:
            room = Rooms.objects.get(channel_id=self.channel_id)
            if room.user_1_id == self.user.id:
                room.user_1_in = False
                room.save(update_fields=['user_1_in'])
            elif room.user_2_id == self.user.id:
                room.user_2_in = False
                room.save(update_fields=['user_2_in'])
        except Rooms.DoesNotExist:
            pass

    @database_sync_to_async
    def set_room_waiting(self):
        Rooms.objects.filter(channel_id=self.channel_id) \
            .exclude(status='disabled') \
            .update(status='waiting')

    @database_sync_to_async
    def update_room_status_playing(self):
        Rooms.objects.filter(
            channel_id=self.channel_id,
            user_1_in=True,
            user_2_in=True
        ).exclude(status='disabled').update(status='playing')

    @database_sync_to_async
    def disable_room(self):
        Rooms.objects.filter(channel_id=self.channel_id).update(status='disabled')

    @database_sync_to_async
    def save_desk(self, data):
        Rooms.objects.filter(channel_id=self.channel_id).update(data=data)