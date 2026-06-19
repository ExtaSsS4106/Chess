package com.example.chess.main_fragments.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chess.Lobby;
import com.example.chess.R;
import com.example.chess.main_fragments.core.RequestsCore;
import com.example.chess.main_fragments.objects.RequestOb;


import java.util.List;

public class RequestAdapter  extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {
    private List<RequestOb> requestObs;
    private Context context;
    private RequestsCore requestsCore;

    public RequestAdapter(List<RequestOb> requestObs, Context context) {
        this.requestObs = requestObs;
        this.context = context;
        this.requestsCore = new RequestsCore(context);
    }

    @NonNull
    @Override
    public RequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_request, parent, false);
        return new RequestAdapter.ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull RequestAdapter.ViewHolder holder, int position) {
        RequestOb request = requestObs.get(position);
        holder.friendNameReq.setText(request.getName());
        holder.typeReq.setText(request.getType());
        int RID = request.getId();
        String type = request.getType();
        String data = request.getData();

        holder.cancelBtnReq.setOnClickListener(v -> {
            requestsCore.CancelRequests(RID, new RequestsCore.CancelCallback() {
                @Override
                public void onSuccess(String message) {
                    // Получаем АКТУАЛЬНУЮ позицию элемента в момент нажатия
                    int currentPos = holder.getBindingAdapterPosition();

                    if (currentPos != RecyclerView.NO_POSITION) {
                        requestObs.remove(currentPos); // Удаляем из данных
                        notifyItemRemoved(currentPos); // Анимированно удаляем из UI
                        // Обновляем индексы оставшихся элементов
                        notifyItemRangeChanged(currentPos, requestObs.size());

                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(context, "Ошибка: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });
        holder.saccessBtnReq.setOnClickListener(v -> {
            requestsCore.AprooveRequests(RID, type, new RequestsCore.AprooveCallback() {
                @Override
                public void onSuccess(String message, String data) {
                    // Получаем АКТУАЛЬНУЮ позицию элемента в момент нажатия
                    int currentPos = holder.getBindingAdapterPosition();

                    if (currentPos != RecyclerView.NO_POSITION) {
                        requestObs.remove(currentPos); // Удаляем из данных
                        notifyItemRemoved(currentPos); // Анимированно удаляем из UI
                        // Обновляем индексы оставшихся элементов
                        notifyItemRangeChanged(currentPos, requestObs.size());

                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }
                    if ("join_friend_in_game".equals(type) && data != null && !data.isEmpty()) {
                        Intent intent =
                                new Intent(
                                        context,
                                        Lobby.class
                                );

                        intent.putExtra(
                                "lobbyId",
                                data
                        );



                        context.startActivity(intent);
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(context, "Ошибка: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return requestObs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView friendNameReq;
        TextView typeReq;
        ImageButton saccessBtnReq;
        ImageButton cancelBtnReq;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            friendNameReq = itemView.findViewById(R.id.friendNameReq);
            typeReq = itemView.findViewById(R.id.typeReq);
            saccessBtnReq = itemView.findViewById(R.id.saccessBtnReq);
            cancelBtnReq = itemView.findViewById(R.id.cancelBtnReq);
        }
    }
}
