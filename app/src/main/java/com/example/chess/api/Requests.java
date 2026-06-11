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

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Requests {

    private String URL;
    private RequestQueue requestQueue;
    private endPoints endpoints;

    private loadUser loaduser;
    private Context context;
    public Requests(Context context) {
        this.context = context;
        this.endpoints = new endPoints();
        this.URL = this.endpoints.getURL();
        this.requestQueue = Volley.newRequestQueue(context);
        this.loaduser = new loadUser();
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

    public interface ApiCallback {
        void onSuccess(String response);
        void onError(String error);
    }
}
