from rest_framework import generics, permissions, status
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework_simplejwt.tokens import RefreshToken
from django.contrib.auth.models import User

class PingPong(APIView):
    permission_classes = (permissions.IsAuthenticated,)
    def post(self, request):
        query = request.data.get('query')
        if query == "ping":
            return Response({"reponse": "pong"}, status=status.HTTP_200_OK)