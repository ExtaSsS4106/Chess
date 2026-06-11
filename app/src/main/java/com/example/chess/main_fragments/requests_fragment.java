package com.example.chess.main_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chess.R;
import com.example.chess.main_fragments.adapters.FriendAdapter;
import com.example.chess.main_fragments.adapters.RequestAdapter;
import com.example.chess.main_fragments.core.FriendsCore;
import com.example.chess.main_fragments.core.RequestsCore;
import com.example.chess.main_fragments.objects.Friend;
import com.example.chess.main_fragments.objects.RequestOb;

import java.util.ArrayList;
import java.util.List;

public class requests_fragment extends Fragment {
    private TextView textView2;
    private RecyclerView recyclerViewReq;
    private List<RequestOb> requestsList;
    private RequestAdapter adapter;
    private RequestsCore requestsCore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.requests, container, false);
        recyclerViewReq = view.findViewById(R.id.recyclerViewReq);

        requestsList = new ArrayList<>();
        requestsCore = new RequestsCore(getContext());

        // АДАПТЕР
        adapter = new RequestAdapter(requestsList, getContext());
        recyclerViewReq.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewReq.setAdapter(adapter);
        return view;
    }
    private void loadRequestsFromServer() {
        requestsCore.GetRequests(new RequestsCore.RequestsCallback() {
            @Override
            public void onSuccess(List<RequestOb> requestObs) {
                requireActivity().runOnUiThread(() -> {
                    requestsList.clear();
                    requestsList.addAll(requestObs);
                    adapter.notifyDataSetChanged();  // ОБНОВЛЯЕМ СПИСОК
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
}
