from django.shortcuts import render, get_object_or_404
from rest_framework import generics, permissions, status
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework_simplejwt.tokens import RefreshToken
from django.contrib.auth.models import User
from mobile_api.models import Requests

class Get_requests(APIView):
    permission_classes = (permissions.IsAuthenticated,)
    def get(self, request):
        user = request.user
        requests = Requests.objects.filter(user_to=user, status='sended').order_by('-id')
        response = []
        if requests.exists():
            status_ = 'ok'
            for r in requests:
                response.append({
                    "id": r.id,
                    "user_from":{
                        "id": r.user_from.id,
                        "name": r.user_from.username
                    },
                    "type": r.type
                })
        else:
            status_ = 'empty'
        return Response({"status": status_, "data": response}, status=status.HTTP_200_OK)
class Cancel_request(APIView):
    permission_classes = (permissions.IsAuthenticated,)
    def post(self, request):
        user = request.user
        rid = request.data.get('rid')
        
        if not rid:
            return Response({"error": "rid required"}, status=status.HTTP_400_BAD_REQUEST)
        
        if Requests.objects.filter(id=rid, user_to=user, status='canceld').exists():
            return Response({"error": "request already canceld"}, status=status.HTTP_400_BAD_REQUEST)
        
        if Requests.objects.filter(id=rid, user_to=user, status='aprooved').exists():
            return Response({"error": "request already aprooved"}, status=status.HTTP_400_BAD_REQUEST)
        
        req = get_object_or_404(Requests, id=rid, user_to = user)
        req.status = 'canceld'
        req.save()
        return Response(status=status.HTTP_201_CREATED)
        
    
class Aproove_request(APIView):
    permission_classes = (permissions.IsAuthenticated,)
    def post(self, request):
        user = request.user
        rid = request.data.get('rid')
        
        if not rid:
            return Response({"error": "rid required"}, status=status.HTTP_400_BAD_REQUEST)
        
        if Requests.objects.filter(id=rid, user_to=user, status='aprooved').exists():
            return Response({"error": "request already aprooved"}, status=status.HTTP_400_BAD_REQUEST)
        
        if Requests.objects.filter(id=rid, user_to=user, status='canceld').exists():
            return Response({"error": "request already canceld"}, status=status.HTTP_400_BAD_REQUEST)
        
        if Requests.objects.filter(id=rid, user_to=user, type='add_friend').exists():
            return Response({"error": "request is not of type 'add_friend'"}, status=status.HTTP_400_BAD_REQUEST)

        req = get_object_or_404(Requests, id=rid, user_to = user)
        req.status = 'aprooved'
        req.save()
        return Response(status=status.HTTP_201_CREATED)
