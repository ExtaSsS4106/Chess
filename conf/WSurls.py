from channels.routing import URLRouter
from django.urls import re_path

from mobile_api.WSurls import websocket_urlpatterns as mobile_ws

websocket_urlpatterns = [
    re_path(r'^mobile_api/', URLRouter(mobile_ws)),
]