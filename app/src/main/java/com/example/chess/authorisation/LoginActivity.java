package com.example.chess.authorisation;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.chess.R;
import com.example.chess.api.Requests;
import com.example.chess.authorisation.core.Login;

import com.example.chess.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText login_username;
    private EditText login_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        Button signUp = findViewById(R.id.btn_switch_to_register);
        Button loginBtn = findViewById(R.id.btn_login);


        login_username = findViewById(R.id.login_username);
        login_password = findViewById(R.id.login_password);

        loginBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                try{


                    String username = String.valueOf(login_username.getText());
                    String password = String.valueOf(login_password.getText());
                    if (!validateFields(username, password)){
                        return;
                    }
                    loginBtn.setEnabled(false);
                    loginBtn.setText("Вход...");
                    Login login = new Login(LoginActivity.this);
                    login.perfomLogin(username, password, new Requests.ApiCallback() {
                        @Override
                        public void onSuccess(String response) {
                            // Переходим на главный экран только после успешного входа
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            Toast.makeText(LoginActivity.this,
                                    "Добро пожаловать, " + username + "!",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(String error) {
                            loginBtn.setEnabled(true);
                            loginBtn.setText("Войти");
                            Log.d("Login Error", error);
                            Toast.makeText(LoginActivity.this,
                                    "Ошибка входа, проверьте имя или пароль",
                                    Toast.LENGTH_SHORT).show();
                            login_username.setText("");
                            login_password.setText("");
                        }
                    });

                }catch (Exception e){
                    Log.d("Error ", String.valueOf(e));
                    loginBtn.setEnabled(true);
                    loginBtn.setText("Войти");
                }
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private boolean validateFields(String username, String password) {
        if (TextUtils.isEmpty(username)) {
            login_username.setError("Введите имя пользователя");
            login_username.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            login_password.setError("Введите пароль");
            login_password.requestFocus();
            return false;
        }

        return true;
    }
}