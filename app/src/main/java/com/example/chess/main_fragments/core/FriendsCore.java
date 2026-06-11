package com.example.chess.main_fragments.core;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.chess.api.Requests;
import com.example.chess.api.endPoints;
import com.example.chess.main_fragments.objects.Friend;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FriendsCore {
    private String get;
    private String add;
    private String delete;
    private String send;
    private Requests requests;
    private endPoints endpoints;
    private Context context;

    public FriendsCore(Context context) {
        this.context = context;
        this.requests = new Requests(context);
        this.endpoints = new endPoints();

        this.get = endpoints.getGET_FRIENDS();
        this.add = endpoints.getADD_FRIEND();
        this.delete = endpoints.getDELETE_FRIEND();
        this.send = endpoints.getSEND_IVITE();
    }
    public void getFriends(FriendsCallback callback){
        requests.metaGET(get, new Requests.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d("FriendsCore", "Response: " + response);
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(response);
                    JSONArray friendsArray = jsonResponse.getJSONArray("friends");

                    List<Friend> friendsList = new ArrayList<>();
                    for (int i = 0; i < friendsArray.length(); i++){
                        JSONObject friendObj = friendsArray.getJSONObject(i);
                        // Проверяем наличие ключей, чтобы избежать ошибок
                        int id = friendObj.has("id") ? friendObj.getInt("id") : -1;
                        String name = friendObj.has("name") ? friendObj.getString("name") : 
                                     (friendObj.has("username") ? friendObj.getString("username") : "Unknown");
                        
                        Friend friend = new Friend(id, name);
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
                    callback.onError("ERROR getFriends: " + error);
                }
            }
        });
    }




    public void addFriend(Integer RID, AddFriendCallback callback) throws JSONException {
        JSONObject data = new JSONObject();
        data.put("rid", RID);
        requests.metaPOST(add, data, new Requests.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(response);
                    if (jsonResponse.has("status") && jsonResponse.getString("status").equals("ok")) {
                        Toast.makeText(context, "Успешно", Toast.LENGTH_SHORT).show();
                        Log.d("Response", "Code: " + jsonResponse.getString("code"));
                    } else if (jsonResponse.has("status") && jsonResponse.getString("status").equals("error")) {
                        Toast.makeText(context, "Ошибка" + jsonResponse.getString("code"), Toast.LENGTH_SHORT).show();
                        Log.d("Response", "Code: " + jsonResponse.getString("code"));
                    } else {
                        Toast.makeText(context, "Ошибка ответа", Toast.LENGTH_SHORT).show();
                        Log.d("Response", "Code: " + jsonResponse.getString("code"));
                    }
                }catch (Exception e){
                    Log.e("Response", "Critical error: " + e.getMessage(), e);
                    Toast.makeText(context, "Критическая ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError("ERROR addFriend: " + error);
                }
            }
        });
    }
    public void deleteFriend(Integer FID, DeleteFriendCallback callback) throws JSONException {
        JSONObject data = new JSONObject();
        data.put("fid", FID);
        requests.metaPOST(delete, data, new Requests.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(response);
                    if (jsonResponse.has("status") && jsonResponse.getString("status").equals("ok")) {
                        Toast.makeText(context, "Успешно", Toast.LENGTH_SHORT).show();
                        Log.d("Response", "Code: " + jsonResponse.getString("code"));
                        if (callback != null)
                            callback.onSuccess("Успешно удалено"); // <--- БЕЗ ЭТОЙ СТРОКИ АДАПТЕР НЕ УЗНАЕТ ОБ УСПЕХЕ
                    }else if (jsonResponse.has("status") && jsonResponse.getString("status").equals("error")) {
                        Toast.makeText(context, "Ошибка" + jsonResponse.getString("code"), Toast.LENGTH_SHORT).show();
                        Log.d("Response", "Code: " + jsonResponse.getString("code"));
                    } else {
                        Toast.makeText(context, "Ошибка ответа", Toast.LENGTH_SHORT).show();
                        Log.d("Response", "Code: " + jsonResponse.getString("code"));
                    }
                }catch (Exception e){
                    Log.e("Response", "Critical error: " + e.getMessage(), e);
                    Toast.makeText(context, "Критическая ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError("ERROR deleteFriend: " + error);
                }
            }
        });
    }
    public void sendInvite(String userName, SendInviteCallback callback) throws JSONException {
        JSONObject data = new JSONObject();
        data.put("fun", userName);
        requests.metaPOST(send, data, new Requests.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(response);
                    if (jsonResponse.has("status") && jsonResponse.getString("status").equals("ok")) {
                        Toast.makeText(context, "Успешно", Toast.LENGTH_SHORT).show();
                        Log.d("Response", "Code: " + jsonResponse.getString("code"));
                    } else if (jsonResponse.has("status") && jsonResponse.getString("status").equals("error")) {
                        Toast.makeText(context, "Ошибка" + jsonResponse.getString("code"), Toast.LENGTH_SHORT).show();
                        Log.d("Response", "Code: " + jsonResponse.getString("code"));
                    } else {
                        Toast.makeText(context, "Ошибка ответа", Toast.LENGTH_SHORT).show();
                        Log.d("Response", "Code: " + jsonResponse.getString("code"));
                    }
                }catch (Exception e){
                    Log.e("Response", "Critical error: " + e.getMessage(), e);
                    Toast.makeText(context, "Критическая ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                if (callback != null) {
                    callback.onError("ERROR deleteFriend: " + error);
                }
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
