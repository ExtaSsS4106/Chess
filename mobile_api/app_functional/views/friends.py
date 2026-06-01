from django.shortcuts import render, get_object_or_404
from rest_framework import generics, permissions, status
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework_simplejwt.tokens import RefreshToken
from django.contrib.auth.models import User
from mobile_api.models import Friends, Requests
from django.db.models import Q

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
        return Response(response, status=status.HTTP_200_OK)
    
# добавить в друзья   
class Add_friend(APIView):
    permission_classes = (permissions.IsAuthenticated,)
    def post(self, request):
        user = request.user
        fid = request.data.get('fid')
        
        if not fid:
            return Response({"error": "fid required"}, status=status.HTTP_400_BAD_REQUEST)
        
        if user.id == int(fid):
            return Response({"error": "Cannot add yourself"}, status=status.HTTP_400_BAD_REQUEST)
        
        friend = get_object_or_404(User, id=fid)
        
        user_1, user_2 = (user, friend) if user.id < friend.id else (friend, user)

        try:
            Friends.objects.create(user_1=user_1, user_2=user_2)
            return Response({"status": "friend added"}, status=status.HTTP_201_CREATED)
        except IntegrityError:
            return Response({"status": "friendship already exists"}, status=status.HTTP_409_CONFLICT)
        
class Delete_from_friends(APIView):
    permission_classes = (permissions.IsAuthenticated,)
    def post(self, request):
        user = request.user
        fid = request.data.get('fid')
        
        if not fid:
            return Response({"error": "fid required"}, status=status.HTTP_400_BAD_REQUEST)
        
        if user.id == int(fid):
            return Response({"error": "fid cannot be yourself"}, status=status.HTTP_400_BAD_REQUEST)
        
        friend = get_object_or_404(User, id=fid)
        
        Friends.objects.filter(Q(user_1 = user, user_2 = friend) | Q(user_2 = user, user_1 = friend)).delete()
# отправить запрос в друзья
class send_invite_to_friend(APIView):
    permission_classes = (permissions.IsAuthenticated,)
    def post(self, request):
        user = request.user
        fun = request.data.get('fun')
        
        if not fun:
            return Response({"error": "fun required"}, status=status.HTTP_400_BAD_REQUEST)
        
        if user.username == str(fun):
            return Response({"error": "fun cannot be yourself"}, status=status.HTTP_400_BAD_REQUEST)
        
        friend = get_object_or_404(User, username=fun)
        
        Requests.objects.create(
            user_from = user,
            user_to = friend,
            type = 'add_friend',
            status = 'sended'
        )
        
        return Response({"status": "OK"}, status=status.HTTP_201_CREATED)