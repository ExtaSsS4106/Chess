package com.example.chess;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chess.core.MainCore;
import com.example.chess.data.loadConf;

public class SettingsActivity extends AppCompatActivity {

    private TextView label_settings;
    private View divider_settings;
    private TextView tv_server_url;
    private EditText et_server_url;
    private Button btn_save;
    private Button btn_back_settings;

    private loadConf conf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        // Инициализация loadConf
        conf = new loadConf();

        // Инициализация View
        label_settings = findViewById(R.id.label_settings);
        divider_settings = findViewById(R.id.divider_settings);
        tv_server_url = findViewById(R.id.tv_server_url);
        et_server_url = findViewById(R.id.et_server_url);
        btn_save = findViewById(R.id.btn_save);
        btn_back_settings = findViewById(R.id.btn_back_settings);

        // Загрузка сохраненного URL
        loadSavedServerUrl();

        // Обработчик кнопки "Сохранить"
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveServerUrl();
            }
        });

        // Обработчик кнопки "Назад"
        btn_back_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Закрываем текущую Activity и возвращаемся назад
            }
        });
    }

    /**
     * Загружает сохраненный URL сервера из файла конфигурации
     */
    private void loadSavedServerUrl() {
        String savedUrl = conf.loadUrl(this);
        if (!savedUrl.isEmpty()) {
            et_server_url.setText(savedUrl);
        } else {
            // Если URL не найден, показываем значение по умолчанию
            et_server_url.setText("192.168.1.100");
        }
    }

    /**
     * Сохраняет URL сервера в файл конфигурации
     */
    private void saveServerUrl() {
        String serverUrl = et_server_url.getText().toString().trim();

        // Валидация: проверяем, что поле не пустое
        if (serverUrl.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, введите адрес сервера", Toast.LENGTH_SHORT).show();
            return;
        }

        // Валидация: проверяем, что URL начинается с http:// или https://
        if (serverUrl.startsWith("http://") && serverUrl.startsWith("https://")) {
            Toast.makeText(this, "Адрес не должен начинаться с http:// или https://", Toast.LENGTH_SHORT).show();
            return;
        }

        // Сохраняем в файл конфигурации
        boolean success = conf.saveUrl(this, serverUrl);

        if (success) {
            Toast.makeText(this, "Настройки сохранены", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Ошибка при сохранении настроек", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Получить сохраненный URL сервера (статический метод для доступа из других Activity)
     */
    public static String getSavedServerUrl(Context context) {
        loadConf conf = new loadConf();
        String url = conf.loadUrl(context);
        return url.isEmpty() ? "http://192.168.1.100:8000" : url;
    }
}