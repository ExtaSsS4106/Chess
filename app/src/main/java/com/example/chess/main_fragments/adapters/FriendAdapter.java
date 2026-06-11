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
    private List<Friend> friends;
    private FriendsCore friendsCore;
    private Context context;
    private OnFriendDeletedListener onFriendDeletedListener;  // Добавляем listener

    // Интерфейс для уведомления об удалении
    public interface OnFriendDeletedListener {
        void onFriendDeleted(int position, Friend friend);
    }

    public FriendAdapter(List<Friend> friends, Context context) {
        this.friends = friends;
        this.friendsCore = new FriendsCore(context);
        this.context = context;
    }

    // Метод для установки listener
    public void setOnFriendDeletedListener(OnFriendDeletedListener listener) {
        this.onFriendDeletedListener = listener;
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
        Friend friend = friends.get(position);
        holder.friendName.setText(friend.getName());

        // Кнопка удаления
        holder.deleteBtn.setOnClickListener(v -> {
            final int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) {
                return; // Элемент уже удален
            }

            try {
                friendsCore.deleteFriend(friend.getId(), new FriendsCore.DeleteFriendCallback() {
                    @Override
                    public void onSuccess(String message) {
                        // Удаляем из списка
                        friends.remove(currentPosition);
                        notifyItemRemoved(currentPosition);
                        notifyItemRangeChanged(currentPosition, friends.size() - currentPosition);
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                        // Уведомляем фрагмент об удалении
                        if (onFriendDeletedListener != null) {
                            onFriendDeletedListener.onFriendDeleted(currentPosition, friend);
                        }
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

    // Метод для обновления всего списка
    public void updateList(List<Friend> newFriends) {
        this.friends.clear();
        this.friends.addAll(newFriends);
        notifyDataSetChanged();
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