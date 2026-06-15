from django.urls import re_path, include
from .views.home import GameConsumer

websocket_urlpatterns = [
    re_path(r'^game_start/', GameConsumer.as_asgi()),
]