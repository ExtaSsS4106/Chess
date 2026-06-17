package com.example.chess.core;

import android.content.Context;

import com.example.chess.api.Requests;
import com.example.chess.api.endPoints;
import com.example.chess.main_fragments.core.FriendsCore;
import com.example.chess.main_fragments.objects.Friend;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MainCore {
    private String get;
    private Requests requests;
    private endPoints endpoints;
    private Context context;

    public MainCore(Context context) {
        this.context = context;
        this.requests = new Requests(context);
        this.endpoints = new endPoints();

        this.get = endpoints.getACTIVE_GAME();

    }

    public void getChannelID(MainCore.ChannelCallback callback){
        requests.metaGET(get, new Requests.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    // Проверяем наличие channel_id и то, что он не пустой
                    if (jsonResponse.has("channel_id") && !jsonResponse.isNull("channel_id")) {
                        String ID = jsonResponse.getString("channel_id");
                        if (!ID.isEmpty() && !ID.equals("null")) {
                            callback.onSuccess(ID);
                            return;
                        }
                    }
                    callback.onSuccess(null);
                } catch (JSONException e) {
                    callback.onError("Ошибка парсинга: " + e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    public interface ChannelCallback {
        void onSuccess(String ID);
        void onError(String error);
    }
}
