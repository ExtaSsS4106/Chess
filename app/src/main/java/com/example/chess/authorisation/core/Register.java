package com.example.chess.authorisation.core;

import android.content.Context;
import android.util.Log;

import com.example.chess.api.Requests;
import com.example.chess.api.endPoints;

import org.json.JSONObject;

public class Register {
    private String path;
    private Requests requests;
    private endPoints endpoints;
    private Context context;
    private String USER_DATA = "user_data.json";

    public Register(Context context) {
        this.context = context;
        this.requests = new Requests(context);
        this.endpoints = new endPoints(context);
        this.path = endpoints.getREGISTERPath();
    }

    public void performRegister(String username, String email, String password1, String password2, RegisterCallback callback) {
        try {
            JSONObject registerData = new JSONObject();
            registerData.put("username", username);
            registerData.put("email", email);
            registerData.put("password", password1);
            registerData.put("password2", password2);

            requests.POST(path, registerData, new Requests.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    Log.d("Register", "Регистрация успешна: " + response);
                    if (callback != null) {
                        callback.onSuccess(response);
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e("Register", "Ошибка регистрации: " + error);
                    if (callback != null) {
                        callback.onError(error);
                    }
                }
            });
        } catch (Exception e) {
            Log.e("Register", "Ошибка выполнения запроса", e);
            if (callback != null) {
                callback.onError(e.getMessage());
            }
        }
    }

    // Интерфейс для callback
    public interface RegisterCallback {
        void onSuccess(String response);
        void onError(String error);
    }
}