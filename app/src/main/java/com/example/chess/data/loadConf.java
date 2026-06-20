package com.example.chess.data;

import android.content.Context;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class loadConf {
    private static final String FILE = "conf.json";

    public String getFILE() {
        return FILE;
    }

    public String loadUrl(Context context) {
        try {
            FileInputStream fis = context.openFileInput(FILE);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            fis.close();

            JSONObject json = new JSONObject(sb.toString());
            return json.optString("url", "192.168.31.229");

        } catch (Exception e) {
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
