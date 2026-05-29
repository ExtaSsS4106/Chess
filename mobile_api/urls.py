from django.urls import path, include

urlpatterns = [
    path('app_functional/',  include('mobile_api.app_functional.urls')),
    path('game_functional/',  include('mobile_api.game_functional.urls')),
]
