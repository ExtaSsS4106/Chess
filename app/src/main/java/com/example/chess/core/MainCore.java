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
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(response);
                    String ID = jsonResponse.optString("channel_id");

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(String error) {

            }
        });
    }
    public interface ChannelCallback {
        void onSuccess(String ID);
        void onError(String error);
    }
}
