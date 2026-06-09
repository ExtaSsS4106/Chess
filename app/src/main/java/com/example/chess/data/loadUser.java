package com.example.chess.data;

import android.content.Context;

import org.json.JSONObject;

import java.io.FileInputStream;

public class loadUser {

    public UserData loadUserData(Context context) {
        try {
            FileInputStream fis = context.openFileInput("user_data.json");
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();

            String jsonStr = new String(buffer);
            JSONObject json = new JSONObject(jsonStr);

            String refresh = json.getString("refresh");
            String token = json.getString("token");
            JSONObject user = json.getJSONObject("user");
            String username = user.getString("username");
            String email = user.getString("email");

            return new UserData(token, refresh, username, email);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class UserData {
        private String token;
        private String refresh;
        private String username;
        private String email;

        public UserData(String token, String refresh, String username, String email) {
            this.token = token;
            this.refresh = refresh;
            this.username = username;
            this.email = email;
        }

        public String getToken() { return token; }
        public String getRefresh() { return refresh; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
    }
}