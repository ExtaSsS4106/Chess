package com.example.chess.main_fragments.core;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.chess.api.Requests;
import com.example.chess.api.endPoints;
import com.example.chess.main_fragments.objects.RequestOb;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RequestsCore {
    private String get;
    private String cancel;
    private String aproove;
    private String add_friend;
    private String join_fr;
    private Requests requests;
    private Context context;
    private endPoints endPoints;
    public RequestsCore(Context context){
        this.endPoints = new endPoints();
        this.requests = new Requests(context);
        this.context = context;

        this.get = endPoints.getGET_REQUESTS();
        this.cancel = endPoints.getCANCEL_REQUEST();
        this.aproove = endPoints.getAPROOVE_REQUEST();
        this.add_friend = endPoints.getADD_FRIEND();
    }
    public void GetRequests(RequestsCallback callback){
        try {
            requests.metaGET(get, new Requests.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    JSONObject jsonResponse = null;
                    try {
                        jsonResponse = new JSONObject(response);
                        JSONArray reqArray = jsonResponse.getJSONArray("data");

                        List<RequestOb> reqList = new ArrayList<>();
                        for (int i = 0; i < reqArray.length(); i++){
                            JSONObject reqdObj = reqArray.getJSONObject(i);
                            // Проверяем наличие ключей, чтобы избежать ошибок
                            int id = reqdObj.has("id") ? reqdObj.getInt("id") : -1;
                            JSONObject USER_FROM = reqdObj.getJSONObject("user_from");
                            String name = USER_FROM.getString("name");
                            String type = reqdObj.getString("type");
                            String data = reqdObj.getString("data");
                            RequestOb requestOb = new RequestOb(id, name, type, data);
                            reqList.add(requestOb);
                        }
                        if (callback != null) {
                            callback.onSuccess(reqList);
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
                        callback.onError("ERROR GetRequests: " + error);
                    }
                }
            });
        }catch (Exception e){

        }
    }
    public void CancelRequests(Integer RID, CancelCallback callback){
        try {
            JSONObject data = new JSONObject();
            data.put("rid", RID);

            requests.metaPOST(cancel, data, new Requests.ApiCallback() {
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
                        callback.onError("ERROR CancelRequests: " + error);
                    }
                }
            });
        }catch (Exception e){

        }
    }
    public void AprooveRequests(Integer RID, String type, String data_, AprooveCallback callback){
        try {
            JSONObject data = new JSONObject();
            data.put("rid", RID);
            String path;
            if (Objects.equals(type, "add_friend")) {
                path = add_friend;
            }
            if (Objects.equals(type, "join_friend_in_game")){
                path = join_fr;
            }else{
                path = aproove;
            }
            requests.metaPOST(path, data, new Requests.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    JSONObject jsonResponse = null;
                    try {
                        jsonResponse = new JSONObject(response);
                        if (jsonResponse.has("status") && jsonResponse.getString("status").equals("ok")) {
                            Toast.makeText(context, "Успешно", Toast.LENGTH_SHORT).show();
                            Log.d("Response", "Code: " + jsonResponse.getString("code"));
                            if (callback != null)
                                callback.onSuccess("Успешно удалено", null); // <--- БЕЗ ЭТОЙ СТРОКИ АДАПТЕР НЕ УЗНАЕТ ОБ УСПЕХЕ
                        }else if (jsonResponse.has("status") && jsonResponse.getString("status").equals("join_friend_in_game")) {
                            Toast.makeText(context, "Успешно", Toast.LENGTH_SHORT).show();
                            Log.d("Response", "Code: " + jsonResponse.getString("code"));
                            if (callback != null)
                                callback.onSuccess("Успешно",jsonResponse.getString("lobby_hash")); // <--- БЕЗ ЭТОЙ СТРОКИ АДАПТЕР НЕ УЗНАЕТ ОБ УСПЕХЕ
                        } else if (jsonResponse.has("status") && jsonResponse.getString("status").equals("error")) {
                            Toast.makeText(context, "Ошибка" + jsonResponse.getString("code"), Toast.LENGTH_SHORT).show();
                            Log.d("Response", "Code: " + jsonResponse.getString("code"));
                        }else {
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
                        callback.onError("ERROR AprooveRequests: " + error);
                    }
                }
            });
        }catch (Exception e){

        }
    }




    // Callback интерфейсы
    public interface RequestsCallback {
        void onSuccess(List<RequestOb> requestObList);
        void onError(String error);
    }

    public interface CancelCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface AprooveCallback {
        void onSuccess(String message, String data);
        void onError(String error);
    }
}
