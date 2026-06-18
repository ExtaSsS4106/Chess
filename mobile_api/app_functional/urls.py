from django.conf.urls.static import static
from django.urls import path
from .views.auth import RegisterView, LogoutView, LoginView
from .views.friends import Get_friends, Add_friend, Delete_from_friends, send_invite_to_friend
from .views.requests import Get_requests, Cancel_request, Aproove_request
from .views.pingpong import PingPong
from .views.home import ActiveGame
from rest_framework_simplejwt.views import TokenObtainPairView, TokenRefreshView

urlpatterns = [
    # ping < - - - > pong
    path('ping', PingPong.as_view(), name="ping_pong"),
    # auth
    path('register', RegisterView.as_view(), name='register'),
    path('logout', LogoutView.as_view(), name='logout'),
    path('login', LoginView.as_view(), name='login'),
    path('token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),
    # friends
    path('friends/get', Get_friends.as_view(), name='get_friends'),
    path('friends/add', Add_friend.as_view(), name='add_friend'),
    path('friends/delete', Delete_from_friends.as_view(), name='delete_friend'),
    path('friends/send_invite', send_invite_to_friend.as_view(), name='send_invite'),
    # requests
    path('requests/get', Get_requests.as_view(), name='get_requests'),
    path('requests/cancel', Cancel_request.as_view(), name='cancel_request'),
    path('requests/aproove', Aproove_request.as_view(), name='aproove_request'),
    #home
    path('active_game', ActiveGame.as_view(), name='get_active_game'),
]