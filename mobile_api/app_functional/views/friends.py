from django.shortcuts import render, get_object_or_404
from rest_framework import generics, permissions, status
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework_simplejwt.tokens import RefreshToken
from django.contrib.auth.models import User
from mobile_api.models import Friends, Requests, Rooms, Lobby
from django.db.models import Q
import uuid

from mobile_api.app_functional.status_codes import StatusCodes
# Create your views here.
class Get_friends(APIView):
    permission_classes = (permissions.IsAuthenticated,)
    def get(self, request):
        user = request.user
        friendships = Friends.objects.filter(Q(user_1 = user) | Q(user_2 = user)).distinct().order_by('-id')
        
        response = []
        for friendship in friendships:
            friend = friendship.user_1 if friendship.user_2 == user else friendship.user_2
            
            response.append({
                "id": friend.id,
                "name": friend.username,
            })
        return Response({ "friends": response }, status=status.HTTP_200_OK)
# добавить в друзья   
class Add_friend(APIView):
    permission_classes = (permissions.IsAuthenticated,)
    def post(self, request):
        user = request.user
        rid = request.data.get('rid')
        
        if rid:
            if Requests.objects.filter(id=rid, user_to=user, status='canceld').exists():
                return Response(StatusCodes.REQUEST_ALREADY_CANCELED, status=status.HTTP_400_BAD_REQUEST)
            if Requests.objects.filter(id=rid, user_to=user, status='aprooved').exists():
                return Response(StatusCodes.REQUEST_ALREADY_APPROVED, status=status.HTTP_400_BAD_REQUEST)
            if Requests.objects.filter(id=rid, user_to=user, status='sended').exists():
                req = get_object_or_404(Requests, id=rid, user_to = user)
                req.status = 'aprooved'
                req.save()
            else:
                return Response(StatusCodes.REQUEST_ALREADY_SENDED, status=status.HTTP_400_BAD_REQUEST)
        else:
            return Response(StatusCodes.RID_REQUIRED, status=status.HTTP_400_BAD_REQUEST)
            
       
        friend = get_object_or_404(User, id=req.user_from.id)
        
        user_1, user_2 = (user, friend) if user.id < friend.id else (friend, user)

        try:
            Friends.objects.create(user_1=user_1, user_2=user_2)
            return Response(StatusCodes.FRIEND_ADDED, status=status.HTTP_201_CREATED)
        except IntegrityError:
            return Response(StatusCodes.FRIENDSHIP_EXISTS, status=status.HTTP_409_CONFLICT)
        
class Delete_from_friends(APIView):
    permission_classes = (permissions.IsAuthenticated,)
    def post(self, request):
        user = request.user
        fid = request.data.get('fid')
        
        if not fid:
            return Response(StatusCodes.FID_REQUIRED, status=status.HTTP_400_BAD_REQUEST)
        
        if user.id == int(fid):
            return Response(StatusCodes.FID_CANNOT_BE_YOURSELF, status=status.HTTP_400_BAD_REQUEST)
        
        friend = get_object_or_404(User, id=fid)
        try:
            Friends.objects.filter(Q(user_1 = user, user_2 = friend) | Q(user_2 = user, user_1 = friend)).delete()
        except Friends.DoesNotExist:
            return Response(StatusCodes.FRIENDSHIP_EXISTS, status=status.HTTP_400_BAD_REQUEST)
        return Response(StatusCodes.FRIEND_DELETED, status=status.HTTP_200_OK)
# отправить запрос в друзья
class send_invite_to_friend(APIView):
    permission_classes = (permissions.IsAuthenticated,)
    def post(self, request):
        user = request.user
        fun = request.data.get('fun')
        
        
        if Requests.objects.filter(user_from=user, user_to__username=fun, status='sended').exists():
            return Response(StatusCodes.INVITE_ALREADY_SENDED, status=status.HTTP_400_BAD_REQUEST)
        
        if not fun:
            return Response(StatusCodes.FUN_REQUIRED, status=status.HTTP_400_BAD_REQUEST)
        
        if user.username == str(fun):
            return Response(StatusCodes.FUN_CANNOT_BE_YOURSELF, status=status.HTTP_400_BAD_REQUEST)
        
        if Friends.objects.filter(Q(user_1=user, user_2__username=fun) | Q(user_2=user, user_1__username=fun)).exists():
            return Response(StatusCodes.FRIENDSHIP_EXISTS, status=status.HTTP_400_BAD_REQUEST)
        
        if Requests.objects.filter(Q(user_from=user, user_to__username=fun) | Q(user_to=user, user_from__username=fun), status='sended').exists():
            return Response(StatusCodes.INVITE_ALREADY_SENDED, status=status.HTTP_400_BAD_REQUEST)
        friend = get_object_or_404(User, username=fun)
        
        Requests.objects.create(
            user_from = user,
            user_to = friend,
            type = 'add_friend',
            status = 'sended'
        )
        
        return Response(StatusCodes.INVITE_SENT, status=status.HTTP_201_CREATED)
    
class invite_to_game(APIView):
    permission_classes = (permissions.IsAuthenticated,)
    
    def post(self, request):
        user = request.user
        fuid = request.data.get('fuid')
        
        if not fuid:
            print(1)
            return Response(
                {"status": "error", "code": "FUID_REQUIRED", "message": "ID друга обязателен"},
                status=status.HTTP_400_BAD_REQUEST
            )
            
        
        try:
            friend = User.objects.get(id=fuid)
        except User.DoesNotExist:
            print(2)
            return Response(
                {"status": "error", "code": "USER_NOT_FOUND", "message": "Пользователь не найден"},
                status=status.HTTP_404_NOT_FOUND
            )
        
        # Проверяем, что пользователи друзья
        if not Friends.objects.filter(
            Q(user_1=user, user_2=friend) |
            Q(user_2=user, user_1=friend)
        ).exists():
            print(3)
            return Response(
                {"status": "error", "code": "NOT_FRIENDS", "message": "Пользователи не являются друзьями"},
                status=status.HTTP_400_BAD_REQUEST
            )
        
        # Проверяем, что нет активного приглашения
        if Requests.objects.filter(
            Q(user_from=user, user_to=friend, type='join_friend_in_game', status='sended') |
            Q(user_from=friend, user_to=user, type='join_friend_in_game', status='sended')
        ).exists():
            print(4)
            return Response(
                {"status": "error", "code": "INVITE_ALREADY_SENDED", "message": "Приглашение уже отправлено"},
                status=status.HTTP_400_BAD_REQUEST
            )
        
        # Проверяем, что нет активного лобби
        existing_lobby = Lobby.objects.filter(
            Q(user_1=user, user_2=friend) |
            Q(user_2=user, user_1=friend)
        ).first()
        
        if existing_lobby:
            
            if existing_lobby.room and existing_lobby.room.status != 'created':
                print(5)
                return Response(
                    {"status": "error", "code": "LOBBY_ALREADY_EXISTS", "message": "Игра уже существует"},
                    status=status.HTTP_400_BAD_REQUEST
                )
            else:
                # Если комната disabled, удаляем старый lobby
                existing_lobby.delete()
        
        # Проверяем, что у пользователя нет активной игры
        if Rooms.objects.filter(
            Q(user_1=user) | Q(user_2=user),
            status='playing'
        ).exists():
            print(6)
            return Response(
                {"status": "error", "code": "USER_IN_GAME", "message": "Вы уже в игре"},
                status=status.HTTP_400_BAD_REQUEST
            )
        
        if Rooms.objects.filter(
            Q(user_1=friend) | Q(user_2=friend),
            status='playing'
        ).exists():
            print(7)
            return Response(
                {"status": "error", "code": "FRIEND_IN_GAME", "message": "Друг уже в игре"},
                status=status.HTTP_400_BAD_REQUEST
            )
        
        # Создаем комнату
        room = self.create_room(friend, user.id)
        if not room:
            print(8)
            return Response(
                {"status": "error", "code": "ROOM_CREATION_FAILED", "message": "Не удалось создать комнату"},
                status=status.HTTP_500_INTERNAL_SERVER_ERROR
            )
        
        # Создаем лобби
        lobby_hash = self.generate_hash()
        lobby = Lobby.objects.create(
            hash=lobby_hash,
            user_1=user,
            user_2=friend,
            status='created',
            room=room,
            user_1_in=True,  # Создатель сразу в игре
            user_2_in=False,  # Друг еще не подключился
            user_1_ready=False,
            user_2_ready=False
        )
        
        # Создаем запрос
        req = Requests.objects.create(
            user_from=user,
            user_to=friend,
            type='join_friend_in_game',
            status='sended',
            data=lobby_hash
        )
        
        return Response(
            {
                "status": "ok",
                "lobby_hash": lobby_hash,
                "request_id": req.id,
                "room_id": room.id,
                "channel_id": room.channel_id,
                "message": "Приглашение отправлено"
            },
            status=status.HTTP_200_OK
        )
    
    def generate_hash(self):
        while True:
            lobby_hash = str(uuid.uuid4())[:25]
            if not Lobby.objects.filter(hash=lobby_hash).exists():
                return lobby_hash
            
    def create_room(self, friend, user_id):
        try:
            channel_id = self.generate_unique_channel_id()
            user = User.objects.get(id=user_id)
            room = Rooms.objects.create(
                type='private',
                status='created',
                user_1=user,
                user_2=friend,
                channel_id=channel_id,
                user_1_in=True,
                user_2_in=False
            )
            print(f"[DEBUG] Created room {room.id} with channel {channel_id} for user {user.username}")
            return room
        except Exception as e:
            print(9)
            print(f"Error in create_room: {e}")
            return None
    
    def generate_unique_channel_id(self):
        while True:
            channel_id = str(uuid.uuid4())[:25]
            if not Rooms.objects.filter(channel_id=channel_id).exists():
                return channel_id