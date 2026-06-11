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
import com.example.chess.main_fragments.adapters.FriendAdapter;
import com.example.chess.main_fragments.objects.Friend;

import java.util.ArrayList;
import java.util.List;

public class friends_fragment extends Fragment {
    private EditText textAreaFriends;
    private ImageButton req_btn;
    private RecyclerView recyclerViewFriends;

    private FriendAdapter adapter;
    private List<Friend> friendsList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friends, container, false);
        textAreaFriends = view.findViewById(R.id.textAreaFriends);
        req_btn = view.findViewById(R.id.req_btn);
        recyclerViewFriends = view.findViewById(R.id.recyclerViewFriends);

        // Создаем список
        friendsList = new ArrayList<>();

        // ✅ ДОБАВЛЯЕМ ОДИН ОБЪЕКТ ПРИ СТАРТЕ
        friendsList.add(new Friend(1, "Friend 1"));
        friendsList.add(new Friend(2, "Friend 2"));
        friendsList.add(new Friend(3, "Friend 3"));
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




}