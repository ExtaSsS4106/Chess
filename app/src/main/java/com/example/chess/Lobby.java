package com.example.chess;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chess.api.endPoints;
import com.example.chess.data.loadUser;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class Lobby extends AppCompatActivity {

    private WebSocket webSocket;
    private OkHttpClient client;
    private endPoints endpoints;

    private String lobbyId;

    private boolean isConnected = false;
    private boolean isReady = false;

    private int myId = -1;

    private TextView tvLobbyTitle;
    private TextView tvMyName;
    private TextView tvEnemyName;
    private TextView tvMyReady;
    private TextView tvEnemyReady;

    private Button btnReady;
    private Button btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inviting_friend);

        endpoints = new endPoints();
        client = new OkHttpClient();

        lobbyId = getIntent().getStringExtra("lobbyId");

        tvLobbyTitle = findViewById(R.id.tvLobbyTitle);

        tvMyName = findViewById(R.id.tvMyName);
        tvEnemyName = findViewById(R.id.tvEnemyName);

        tvMyReady = findViewById(R.id.tvMyReady);
        tvEnemyReady = findViewById(R.id.tvEnemyReady);

        btnReady = findViewById(R.id.LobtnReady);
        btnExit = findViewById(R.id.LobtnExit);

        btnReady.setOnClickListener(v -> toggleReady());

        btnExit.setOnClickListener(v -> finish());

        connectToWebSocket();
    }

    private void connectToWebSocket() {

        loadUser loader = new loadUser();
        loadUser.UserData userData = loader.loadUserData(this);

        String token =
                userData != null
                        ? userData.getToken()
                        : "";

        String url =
                endpoints.getWS_URL()
                        + endpoints.getLOBBY()
                        + lobbyId
                        + "/?token="
                        + token;

        Log.d("LOBBY", url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {

            @Override
            public void onOpen(WebSocket webSocket, Response response) {

                isConnected = true;

                runOnUiThread(() ->
                        tvLobbyTitle.setText("Подключено"));
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {

                Log.d("LOBBY_WS", text);

                try {

                    JSONObject json = new JSONObject(text);

                    String type = json.optString("type");

                    switch (type) {

                        case "connected":
                            handleConnected(json);
                            break;

                        case "notify":
                            updateTitle(
                                    json.optString("message")
                            );
                            break;

                        case "ready":
                        case "notready":
                            updateReadyStatus(json);
                            break;

                        case "start":

                            startGame(
                                    json.optString("room_id")
                            );

                            break;

                        case "opponent_disconnected":

                            showToast(
                                    "Соперник вышел"
                            );

                            finish();

                            break;

                        case "friend_accepted_req":

                            showToast(
                                    "Друг принял приглашение"
                            );

                            break;

                        case "friend_canceld_req":

                            showToast(
                                    "Друг отклонил приглашение"
                            );

                            finish();

                            break;

                        case "request_dose_note_exists":

                            showToast(
                                    "Приглашение больше не существует"
                            );

                            finish();

                            break;
                    }

                } catch (Exception e) {

                    Log.e(
                            "LOBBY_WS",
                            "Parse error",
                            e
                    );
                }
            }

            @Override
            public void onFailure(
                    WebSocket webSocket,
                    Throwable t,
                    Response response
            ) {

                isConnected = false;

                runOnUiThread(() -> {

                    Toast.makeText(
                            Lobby.this,
                            "Ошибка соединения",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                });
            }

            @Override
            public void onClosed(
                    WebSocket webSocket,
                    int code,
                    String reason
            ) {
                isConnected = false;
            }
        });
    }

    private void handleConnected(JSONObject json)
            throws JSONException {

        myId = json.getInt("user_id");

        int user1Id = json.getInt("user_1_id");
        int user2Id = json.getInt("user_2_id");

        String user1Name =
                json.getString("user_1_name");

        String user2Name =
                json.getString("user_2_name");

        boolean user1Ready =
                json.getBoolean("user_1_ready");

        boolean user2Ready =
                json.getBoolean("user_2_ready");

        runOnUiThread(() -> {

            if (myId == user1Id) {

                tvMyName.setText(user1Name);
                tvEnemyName.setText(user2Name);

                setReadyLabels(
                        user1Ready,
                        user2Ready
                );

            } else {

                tvMyName.setText(user2Name);
                tvEnemyName.setText(user1Name);

                setReadyLabels(
                        user2Ready,
                        user1Ready
                );
            }
        });
    }

    private void updateReadyStatus(JSONObject json)
            throws JSONException {

        int user1Id =
                json.getInt("user_1_id");

        boolean user1Ready =
                json.getBoolean("user_1_ready");

        boolean user2Ready =
                json.getBoolean("user_2_ready");

        runOnUiThread(() -> {

            if (myId == user1Id) {

                setReadyLabels(
                        user1Ready,
                        user2Ready
                );

            } else {

                setReadyLabels(
                        user2Ready,
                        user1Ready
                );
            }
        });
    }

    private void setReadyLabels(
            boolean myReady,
            boolean enemyReady
    ) {

        tvMyReady.setText(
                myReady
                        ? "ГОТОВ"
                        : "НЕ ГОТОВ"
        );

        tvEnemyReady.setText(
                enemyReady
                        ? "ГОТОВ"
                        : "НЕ ГОТОВ"
        );
    }

    private void toggleReady() {

        if (!isConnected || webSocket == null)
            return;

        try {

            JSONObject json = new JSONObject();

            if (!isReady) {

                json.put(
                        "type",
                        "ready"
                );

                webSocket.send(
                        json.toString()
                );

                isReady = true;

                btnReady.setText(
                        "НЕ ГОТОВ"
                );

            } else {

                json.put(
                        "type",
                        "not_ready"
                );

                webSocket.send(
                        json.toString()
                );

                isReady = false;

                btnReady.setText(
                        "ГОТОВ"
                );
            }

        } catch (JSONException e) {

            Log.e(
                    "LOBBY",
                    "Ready error",
                    e
            );
        }
    }

    private void startGame(String roomId) {

        runOnUiThread(() -> {

            Intent intent =
                    new Intent(
                            Lobby.this,
                            GameActivity.class
                    );

            intent.putExtra(
                    "room_id",
                    roomId
            );

            startActivity(intent);

            finish();
        });
    }

    private void updateTitle(String text) {

        runOnUiThread(() ->
                tvLobbyTitle.setText(text)
        );
    }

    private void showToast(String text) {

        runOnUiThread(() ->
                Toast.makeText(
                        Lobby.this,
                        text,
                        Toast.LENGTH_SHORT
                ).show()
        );
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (webSocket != null) {

            webSocket.close(
                    1000,
                    "Lobby closed"
            );
        }
    }
}