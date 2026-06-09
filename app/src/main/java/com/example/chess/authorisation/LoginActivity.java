package com.example.chess.authorisation;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import com.example.chess.R;
import com.example.chess.authorisation.core.Login;

import com.example.chess.MainActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        Button signUp = findViewById(R.id.btn_switch_to_register);
        Button loginBtn = findViewById(R.id.btn_login);


        EditText login_username = findViewById(R.id.login_username);
        EditText login_password = findViewById(R.id.login_password);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    loginBtn.setEnabled(false);
                    loginBtn.setText("Вход...");

                    String username = String.valueOf(login_username.getText());
                    String password = String.valueOf(login_password.getText());

                    Login login = new Login(LoginActivity.this);
                    login.perfomLogin(username, password);

                    // Переходим на главный экран
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
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
}