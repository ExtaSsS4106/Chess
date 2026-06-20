package com.example.chess.authorisation.core;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.example.chess.api.Requests.ApiCallback;
import com.example.chess.api.endPoints;
import com.example.chess.api.Requests;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;

public class Login {

    private String path;
    private Requests requests;
    private endPoints endpoints;
    private Context context;
    private String USER_DATA = "user_data.json";
    public Login(Context context) {
        this.context = context;
        this.requests = new Requests(context);
        this.endpoints = new endPoints(context);
        this.path = endpoints.getLOGINPath();
    }

    public void logout() {
        try{
            boolean deleted = context.deleteFile(USER_DATA);
            if (deleted) {
                Log.d("Login", "Пользователь вышел из системы, данные удалены");
            } else {
                Log.w("Login", "Файл данных не найден");
            }

        }catch (Exception e){
            Log.e("Login", "Ошибка при выходе: " + e.getMessage());
        }
    }
    public void perfomLogin (String username, String password, Requests.ApiCallback callback){
        try {
            JSONObject loginData = new JSONObject();
            loginData.put("username", username);
            loginData.put("password", password);

            requests.POST(path, loginData, new ApiCallback(){


                @Override
                public void onSuccess(String response) {

                    JSONObject jsonResponse = null;
                    try {
                        jsonResponse = new JSONObject(response);
                        String token = jsonResponse.getString("token");
                        String refreshToken = jsonResponse.getString("refresh");
                        JSONObject user = jsonResponse.getJSONObject("user");

                        String username = user.getString("username");
                        String email = user.getString("email");
                        try {
                            JSONObject json = new JSONObject();
                            json.put("token", token);
                            json.put("refresh", refreshToken);
                            JSONObject Juser = new JSONObject();
                            Juser.put("username", username);
                            Juser.put("email", email);
                            json.put("user", Juser);

                            FileOutputStream fos = context.openFileOutput(USER_DATA, Context.MODE_PRIVATE);
                            fos.write(json.toString().getBytes());
                            fos.close();
                            Log.d("Storage", "Данные пользователя сохранены");

                            callback.onSuccess(response);
                        }catch (Exception e){
                            Log.e("Storage", "Ошибка сохранения", e);
                            callback.onError("Ошибка сохранения данных");
                        }



                    } catch (JSONException e) {
                        callback.onError("Ошибка парсинга ответа");
                    }
                }
                @Override
                public void onError(String error) {
                    Log.e("Login", "Ошибка авторизации: " + error);
                    callback.onError(error);
                }
            });
        } catch (Exception e) {
            Log.e("Storage", "Ошибка выполнения запроса", e);
            callback.onError(e.getMessage());
        }
    }

}
