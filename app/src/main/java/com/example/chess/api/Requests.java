package com.example.chess.api;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class Requests {

    private String URL;
    private RequestQueue requestQueue;
    private endPoints endpoints;

    public Requests(Context context) {
        this.endpoints = new endPoints();
        this.URL = this.endpoints.getURL();
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

    public interface ApiCallback {
        void onSuccess(String response);
        void onError(String error);
    }
}
