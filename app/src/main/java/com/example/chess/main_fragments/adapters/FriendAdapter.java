package com.example.chess.main_fragments.adapters;

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

import org.json.JSONException;

import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {
    private List<Friend> friends;
    private Context context;
    private FriendsCore friendsCore;

    public FriendAdapter(List<Friend> friends, Context context) {
        this.friends = friends;
        this.context = context;
        this.friendsCore = new FriendsCore(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Friend friend = friends.get(position);
        holder.friendName.setText(friend.getName());
        int friendId = friend.getId();

        holder.deleteBtn.setOnClickListener(v -> {
            try {
                friendsCore.deleteFriend(friendId, new FriendsCore.DeleteFriendCallback() {
                    @Override
                    public void onSuccess(String message) {
                        // Получаем АКТУАЛЬНУЮ позицию элемента в момент нажатия
                        int currentPos = holder.getBindingAdapterPosition();

                        if (currentPos != RecyclerView.NO_POSITION) {
                            friends.remove(currentPos); // Удаляем из данных
                            notifyItemRemoved(currentPos); // Анимированно удаляем из UI
                            // Обновляем индексы оставшихся элементов
                            notifyItemRangeChanged(currentPos, friends.size());

                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(context, "Ошибка: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
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