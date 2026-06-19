from django.urls import re_path

from mobile_api.app_functional.views.home import GameConsumer, PrivateRoom
from mobile_api.game_functional.views.gameSession import Session
websocket_urlpatterns = [
    re_path(r'^search/game_start/$', GameConsumer.as_asgi()),
    re_path(r'^session/(?P<channel_id>[\w-]+)/$', Session.as_asgi()),
    re_path(r'^lobby/(?P<lobby_id>[\w-]+)/$', PrivateRoom.as_asgi()),
]