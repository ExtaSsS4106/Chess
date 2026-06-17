package com.example.chess;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chess.api.Requests;
import com.example.chess.api.endPoints;
import com.example.chess.data.loadUser;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class Loading extends AppCompatActivity {
    private WebSocket webSocket;
    private endPoints endpoints;
    private OkHttpClient client;
    private boolean isConnected = false;
    private TextView labelL;
    private Handler handler = new Handler();
    private Requests requests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requests = new Requests(this);
        requests.PingPong(new Requests.ApiCallback() {
            @Override
            public void onSuccess(String response) {}

            @Override
            public void onError(String error) {}
        });
        setContentView(R.layout.loading);

        endpoints = new endPoints();
        client = new OkHttpClient();

        labelL = findViewById(R.id.label_l);

        Button btn_back_l = findViewById(R.id.btn_back_l);
        btn_back_l.setOnClickListener(v -> {
            if (webSocket != null && isConnected) {
                Log.d("WebSocket", "Sending close command...");
                boolean sent = webSocket.send("close");
                Log.d("WebSocket", "Send result: " + sent);
                
                // Даем серверу время обработать сообщение и прислать 'search_stopped'
                // Если через 1 секунду мы все еще здесь - закрываем принудительно
                handler.postDelayed(() -> {
                    if (!isFinishing()) {
                        Log.d("WebSocket", "Fallback: finishing activity after timeout");
                        finish();
                    }
                }, 1000);
            } else {
                finish();
            }
        });

        connectToWebSocket();

    }

    private void connectToWebSocket() {
        loadUser loadUser = new loadUser();
        loadUser.UserData userData = loadUser.loadUserData(this);
        String token = (userData != null) ? userData.getToken() : "";

        String url = endpoints.getWS_URL() + endpoints.getSEARCH_ROOM() + "?token=" + token;

        Log.d("WebSocket", "Connecting to: " + url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d("WebSocket", "Connection established");
                isConnected = true;

            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d("WebSocket", "Received: " + text);
                try {
                    JSONObject json = new JSONObject(text);
                    String type = json.optString("type");

                    if ("opponent_found".equals(type)) {
                        String channel_id = json.optString("channel_id");
                        updateStatus(json.optString("message", "Соперник найден!"));
                        startGame(channel_id);
                    } else if ("waiting_for_opponent".equals(type)) {
                        updateStatus(json.optString("message", "Ожидание соперника..."));
                    } else if ("search_timeout".equals(type)) {
                        showError(json.optString("message", "Время поиска истекло"));
                    } else if ("search_stopped".equals(type)) {
                        updateStatus("Поиск остановлен");
                        showError("Поиск остановлен");
                    } else if ("connected".equals(type)) {
                        updateStatus(json.optString("message", "Подключено к серверу"));
                        Log.d("WebSocket", "Authenticated as " + json.optString("username"));
                    } else if ("error".equals(type)) {
                        showError(json.optString("message", "Ошибка"));
                    }

                } catch (JSONException e) {
                    Log.e("WebSocket", "Error parsing message", e);
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e("WebSocket", "Connection failure", t);
                isConnected = false;
                showError("Ошибка подключения к серверу: " + t.getMessage());
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



    private void startGame(String roomId) {
        runOnUiThread(() -> {
            Intent intent = new Intent(Loading.this, GameActivity.class);
            intent.putExtra("room_id", roomId);
            startActivity(intent);
            finish();
        });
    }

    private void updateStatus(String message) {
        runOnUiThread(() -> {
            if (labelL != null) {
                labelL.setText(message);
            }
        });
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(Loading.this, message, Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (webSocket != null) {
            webSocket.close(1000, "Activity destroyed");
        }
    }
}