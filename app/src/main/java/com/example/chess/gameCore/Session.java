package com.example.chess.gameCore;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chess.Loading;
import com.example.chess.api.endPoints;
import com.example.chess.data.loadUser;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class Session {
    private WebSocket webSocket;
    private endPoints endpoints;
    private OkHttpClient client;
    private boolean isConnected = false;
    private Context context;

    public Session(Context context){
        this.context = context;
        endpoints = new endPoints();
        client = new OkHttpClient();
    }

    public void setConnected(String channel_id){
        loadUser loadUser = new loadUser();
        loadUser.UserData userData = loadUser.loadUserData(context);
        String token = (userData != null) ? userData.getToken() : "";

        String url = endpoints.getWS_URL() + endpoints.getGAME_SESSION()+ channel_id +"/" + "?token=" + token;
        Request request = new Request.Builder().url(url).build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d("WebSocket", "Connection established");
                isConnected = true;

            }


            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e("WebSocket", "Connection failure", t);
                isConnected = false;
                //showError("Ошибка подключения к серверу: " + t.getMessage());
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.d("WebSocket", "Closing: " + reason);
                isConnected = false;
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d("WebSocket", "Closed: " + reason);
                isConnected = false;
            }

        });
    }

}
