from django.shortcuts import render, get_object_or_404
from rest_framework import generics, permissions, status
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework_simplejwt.tokens import RefreshToken
from django.contrib.auth.models import User
from mobile_api.models import Friends, Requests
from django.db.models import Q

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