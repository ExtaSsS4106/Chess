from datetime import timedelta
from django.db import models
from django.contrib.auth.models import User
from django.utils import timezone

# Create your models here.
class Friends(models.Model):
    id = models.AutoField(primary_key=True)
    user_1 = models.ForeignKey(User, on_delete=models.CASCADE, related_name='friends_as_user1')
    user_2 = models.ForeignKey(User, on_delete=models.CASCADE, related_name='friends_as_user2')
    
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    
class Rooms(models.Model):
    TYPE_CHOICES = [('random', 'Random'),('private', 'Private')]
    STATUS_CHOICES = [('created', 'Created'),('playing', 'Playing'), ('waiting', 'Waiting'), ('disabled', 'Disabled')]
    
    id = models.AutoField(primary_key=True)
    channel_id = models.TextField(unique=True, null=False)
    
    user_1 = models.ForeignKey(User, on_delete=models.CASCADE, related_name='rooms_as_user1')
    user_2 = models.ForeignKey(User, on_delete=models.CASCADE, related_name='rooms_as_user2', null=True, blank=True)
    user_1_in = models.BooleanField(default=False, null=True, blank=True)
    user_2_in = models.BooleanField(default=False, null=True, blank=True)
    
    type = models.CharField(max_length=20, choices=TYPE_CHOICES)
    winner = models.ForeignKey(User, on_delete=models.CASCADE, null=True)
    data = models.JSONField(null=True, blank=True)
    status = models.CharField(max_length=20, choices=STATUS_CHOICES)
    info = models.JSONField(null=True)
    
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    
class Requests(models.Model):
    TYPE_CHOICES = [
        ('add_friend', 'Add_friend'),
        ('join_friend_in_game', 'Join_friend_in_game'),
        ('join_random_in_game', 'Join_random_in_game')
    ]
    STATUS_CHOICES = [
        ('sended', 'Sended'),
        ('aprooved', 'Aprooved'),
        ('canceld', 'Canceld')
    ]
    
    id = models.AutoField(primary_key=True)
    user_from = models.ForeignKey(User, on_delete=models.CASCADE, related_name='requests_as_user_from')
    user_to = models.ForeignKey(User, on_delete=models.CASCADE, related_name='requests_as_user_to', null=True, blank=True)
    type = models.CharField(max_length=50, choices=TYPE_CHOICES)
    status = models.CharField(max_length=20, choices=STATUS_CHOICES)  
    
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    
    live_cycle = models.DateTimeField(null=True, blank=True)  
    
    def save(self, *args, **kwargs):
        if not self.pk:
            super().save(*args, **kwargs)
            if self.type in ('join_friend_in_game', 'join_random_in_game'):
                self.live_cycle = self.created_at + timedelta(days=1)
                self.save(update_fields=['live_cycle'])
        else:
            super().save(*args, **kwargs)