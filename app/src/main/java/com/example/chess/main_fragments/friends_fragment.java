package com.example.chess.main_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chess.R;

import java.util.ArrayList;
import java.util.List;

public class friends_fragment extends Fragment {
    private EditText textAreaFriends;
    private ImageButton req_btn;
    private RecyclerView recyclerViewFriends;

    private FriendAdapter adapter;
    private List<String> friendsList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friends, container, false);
        textAreaFriends = view.findViewById(R.id.textAreaFriends);
        req_btn = view.findViewById(R.id.req_btn);
        recyclerViewFriends = view.findViewById(R.id.recyclerViewFriends);

        // Создаем список
        friendsList = new ArrayList<>();

        // ✅ ДОБАВЛЯЕМ ОДИН ОБЪЕКТ ПРИ СТАРТЕ
        friendsList.add("Friend 1");

        // Настройка адаптера
        adapter = new FriendAdapter(friendsList);
        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewFriends.setAdapter(adapter);
        req_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


        return view;
    }



    // Адаптер
    class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {
        private List<String> friends;

        public FriendAdapter(List<String> friends) {
            this.friends = friends;
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
            String friendName = friends.get(position);
            holder.friendName.setText(friendName);

            // Кнопка удаления
            holder.deleteBtn.setOnClickListener(v -> {

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
}