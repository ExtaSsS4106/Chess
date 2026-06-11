package com.example.chess.main_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chess.R;
import com.example.chess.main_fragments.adapters.FriendAdapter;
import com.example.chess.main_fragments.core.FriendsCore;
import com.example.chess.main_fragments.objects.Friend;

import java.util.ArrayList;
import java.util.List;

public class friends_fragment extends Fragment {
    private EditText textAreaFriends;
    private ImageButton req_btn;
    private RecyclerView recyclerViewFriends;
    private FriendAdapter adapter;
    private List<Friend> friendsList;
    private FriendsCore friendsCore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friends, container, false);

        textAreaFriends = view.findViewById(R.id.textAreaFriends);
        req_btn = view.findViewById(R.id.req_btn);
        recyclerViewFriends = view.findViewById(R.id.recyclerViewFriends);

        // Инициализация
        friendsList = new ArrayList<>();
        friendsCore = new FriendsCore(getContext());

        // Настройка адаптера
        adapter = new FriendAdapter(friendsList, getContext());
        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewFriends.setAdapter(adapter);

        // Устанавливаем listener для обновления после удаления
        adapter.setOnFriendDeletedListener((position, friend) -> {
            Toast.makeText(getContext(), "Друг удален: " + friend.getName(), Toast.LENGTH_SHORT).show();
        });

        // Загружаем друзей
        loadFriends();

        req_btn.setOnClickListener(v -> {
            String userName = textAreaFriends.getText().toString().trim();
            if (!userName.isEmpty()) {
                sendInvite(userName);
            } else {
                Toast.makeText(getContext(), "Введите имя пользователя", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void loadFriends() {
        friendsCore.getFriends(new FriendsCore.FriendsCallback() {
            @Override
            public void onSuccess(List<Friend> friends) {
                requireActivity().runOnUiThread(() -> {
                    friendsList.clear();
                    friendsList.addAll(friends);
                    adapter.updateList(friendsList);

                    if (friendsList.isEmpty()) {
                        Toast.makeText(getContext(), "У вас пока нет друзей", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Ошибка: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void sendInvite(String userName) {
        try {
            friendsCore.sendInvite(userName, new FriendsCore.SendInviteCallback() {
                @Override
                public void onSuccess(String message) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        textAreaFriends.setText("");
                        loadFriends();
                    });
                }

                @Override
                public void onError(String error) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Ошибка: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}