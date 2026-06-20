package com.example.chess.data;

import android.content.Context;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class loadConf {
    private static final String FILE = "conf.json";

    public String getFILE() {
        return FILE;
    }


    public String loadUrl(Context context) {
        try {
            FileInputStream fis = context.openFileInput(FILE);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();

            String jsonStr = new String(buffer);
            JSONObject json = new JSONObject(jsonStr);

            return json.optString("url");

        } catch (Exception e) {
            e.printStackTrace();
            return "192.168.31.229";
        }

    }


    public boolean saveUrl(Context context, String url) {
        try {
            JSONObject json = new JSONObject();
            json.put("url", url);

            FileOutputStream fos = context.openFileOutput(FILE, Context.MODE_PRIVATE);
            fos.write(json.toString().getBytes());
            fos.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean confFileExists(Context context) {
        try {
            FileInputStream fis = context.openFileInput(FILE);
            fis.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}