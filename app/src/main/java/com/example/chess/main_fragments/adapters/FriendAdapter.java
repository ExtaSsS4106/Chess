package com.example.chess.main_fragments.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chess.R;
import com.example.chess.main_fragments.core.FriendsCore;
import com.example.chess.main_fragments.objects.Friend;

import java.util.List;

// Адаптер для Friend объектов
public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {
    private List<Friend> friends;  // Теперь список Friend, а не String

    private FriendsCore friendsCore;
    private Context context;

    public FriendAdapter(List<Friend> friends, Context context) {
        this.friends = friends;
        this.friendsCore = new FriendsCore(context);
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Friend friend = friends.get(position);  // Получаем объект Friend
        holder.friendName.setText(friend.getName());  // Показываем имя

        // Можно использовать ID для удаления из БД
        int friendId = friend.getId();

        // Кнопка удаления
        holder.deleteBtn.setOnClickListener(v -> {
            try {
                friendsCore.deleteFriend(friend.getId(), new FriendsCore.DeleteFriendCallback() {
                    @Override
                    public void onSuccess(String message) {
                        // Удаляем из списка и обновляем адаптер
                        friends.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(context, "Ошибка: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView friendName;
        ImageButton deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            friendName = itemView.findViewById(R.id.friendName);
            deleteBtn = itemView.findViewById(R.id.deleteFriendBtn);
        }
    }
}