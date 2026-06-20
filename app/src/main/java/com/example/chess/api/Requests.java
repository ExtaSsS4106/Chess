package com.example.chess.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.chess.data.loadUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Requests {

    private String URL;
    private String PING_PONG;
    private String REFRESH;
    private String USER_DATA;
    private RequestQueue requestQueue;
    private endPoints endpoints;

    private loadUser loaduser;
    private Context context;
    public Requests(Context context) {
        this.context = context;
        this.endpoints = new endPoints(context);
        this.URL = this.endpoints.getURL();
        this.PING_PONG = this.endpoints.getPING_PONG();
        this.REFRESH = this.endpoints.getREFRESH();
        this.loaduser = new loadUser();
        this.USER_DATA = loaduser.getFILE();
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public void POST (String path,JSONObject data, final ApiCallback callback ) {
        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.POST, this.URL + path, data, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess(response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError(error.toString());
                    }
                });
        requestQueue.add(request);
    }

    public void GET (String path, final ApiCallback callback ) {
        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, this.URL + path, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess(response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError(error.toString());
                    }
                });
        requestQueue.add(request);
    }

    public void metaPOST (String path,JSONObject data, final ApiCallback callback ) {
        PingPong(new ApiCallback() {
            @Override
            public void onSuccess(String response) {}

            @Override
            public void onError(String error) {}
        });
        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.POST, this.URL + path, data, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            callback.onSuccess(response.toString());
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            callback.onError(error.toString());
                        }
                    }
                )
                    {
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> headers = new HashMap<>();
                            loadUser.UserData userData = loaduser.loadUserData(context);
                            String token = userData.getToken();
                            if (token != null) {
                                headers.put("Authorization", "Bearer " + token);
                            }
                            return headers;
                        }
                    };
        requestQueue.add(request);
    }

    public void metaGET (String path, final ApiCallback callback ) {
        PingPong(new ApiCallback() {
            @Override
            public void onSuccess(String response) {}

            @Override
            public void onError(String error) {}
        });
        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, this.URL + path, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            callback.onSuccess(response.toString());
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            callback.onError(error.toString());
                        }
                    }
                )   {
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> headers = new HashMap<>();
                            loadUser.UserData userData = loaduser.loadUserData(context);
                            String token = userData.getToken();
                            if (token != null) {
                                headers.put("Authorization", "Bearer " + token);
                            }
                            return headers;
                        }
                    };
        requestQueue.add(request);
    }

    public void Refresher(ApiCallback callback){
        loadUser.UserData userData = loaduser.loadUserData(context);
        String token = userData.getRefresh();
        if (token == null || token.isEmpty()) {
            callback.onError("Refresh token not found");
            return;
        }
        JSONObject jsonQ = new JSONObject();
        try {
            jsonQ.put("refresh", token);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.POST, this.URL + this.REFRESH, jsonQ, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            JSONObject json = new JSONObject();

                            json.put("token", response.optString("access"));
                            json.put("refresh", response.optString("refresh"));

                            JSONObject Juser = new JSONObject();
                            Juser.put("username", userData.getUsername());
                            Juser.put("email", userData.getEmail());
                            json.put("user", Juser);

                            FileOutputStream fos = null;
                            fos = context.openFileOutput(USER_DATA, Context.MODE_PRIVATE);
                            fos.write(json.toString().getBytes());
                            fos.close();

                            callback.onSuccess("Token refreshed");
                        } catch (FileNotFoundException e) {
                            callback.onError("FileNotFoundException "+e.getMessage());
                        } catch (JSONException e) {
                            callback.onError("JSONException "+e.getMessage());
                        } catch (IOException e) {
                            callback.onError("IOException "+e.getMessage());
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError(error.toString());
                    }
                });
        requestQueue.add(request);
    }
    public void PingPong(ApiCallback callback){
        JSONObject jsonQ = new JSONObject();
        try {
            jsonQ.put("query", "ping");
        } catch (JSONException e) {
            callback.onError(e.getMessage());
            return;
        }
        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.POST, this.URL + this.PING_PONG, jsonQ, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess(response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null && error.networkResponse.statusCode == 401){
                            Refresher(new ApiCallback() {
                                @Override
                                public void onSuccess(String response) {
                                    PingPong(callback);
                                }

                                @Override
                                public void onError(String error) {
                                    callback.onError("Can`t update token");
                                }
                            });
                        }
                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        loadUser.UserData userData = loaduser.loadUserData(context);
                        String token = userData.getToken();
                        if (token != null) {
                            headers.put("Authorization", "Bearer " + token);
                        }
                        return headers;
                    }
                };
        requestQueue.add(request);
    }
    public interface ApiCallback {
        void onSuccess(String response);
        void onError(String error);
    }
}
