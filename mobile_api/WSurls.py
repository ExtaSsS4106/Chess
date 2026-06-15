from django.urls import re_path

from mobile_api.app_functional.views.home import GameConsumer

websocket_urlpatterns = [
    re_path(r'^search/game_start/$', GameConsumer.as_asgi()),
]