package com.example.chess.main_fragments.core;

import android.content.Context;
import android.util.Log;

import com.example.chess.api.Requests;
import com.example.chess.api.endPoints;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class friends {
    private String get;
    private String add;
    private String delete;
    private String send;
    private Requests requests;
    private endPoints endpoints;
    private Context context;

    public friends(Context context) {
        this.context = context;
        this.requests = new Requests(context);
        this.endpoints = new endPoints();

        this.add = endpoints.getADD_FRIEND();
        this.delete = endpoints.getDELETE_FRIEND();
        this.send = endpoints.getSEND_IVITE();
    }
    public void getFriends(FriendsCallback callback){
        requests.metaGET(get, new Requests.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(response);
                    JSONArray friendsArray = jsonResponse.getJSONArray("friends");

                    List<Friend> friendsList = new ArrayList<>();
                    for (int i = 0; i < friendsArray.length(); i++){
                        JSONObject friendObj = friendsArray.getJSONObject(i);
                        Friend friend = new Friend();
                        friend.setId(friendObj.getInt("id"));
                        friend.setName(friendObj.getString("name"));
                        friendsList.add(friend);
                    }
                    if (callback != null) {
                        callback.onSuccess(friendsList);
                    }
                } catch (Exception e) {
                    Log.e("Friends", "Error parsing friends: " + e.getMessage());
                    if (callback != null) {
                        callback.onError("Ошибка обработки данных: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError("Ошибка добавления друга: " + error);
                }
            }
        });
    }

    public static class Friend {
        private int id;
        private String name;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }


    public void addFriend(){
        JSONObject data = new JSONObject();
        requests.metaPOST(add, data, new Requests.ApiCallback() {
            @Override
            public void onSuccess(String response) {

            }

            @Override
            public void onError(String error) {

            }
        });
    }
    public void deleteFriend(){
        JSONObject data = new JSONObject();
        requests.metaPOST(delete, data, new Requests.ApiCallback() {
            @Override
            public void onSuccess(String response) {

            }

            @Override
            public void onError(String error) {

            }
        });
    }
    public void sendInvite(){
        JSONObject data = new JSONObject();
        requests.metaPOST(send, data, new Requests.ApiCallback() {
            @Override
            public void onSuccess(String response) {

            }

            @Override
            public void onError(String error) {

            }
        });
    }




    // Callback интерфейсы
    public interface FriendsCallback {
        void onSuccess(List<Friend> friends);
        void onError(String error);
    }

    public interface AddFriendCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface DeleteFriendCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface SendInviteCallback {
        void onSuccess(String message);
        void onError(String error);
    }

}
