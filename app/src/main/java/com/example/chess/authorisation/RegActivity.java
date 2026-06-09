package com.example.chess.authorisation;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chess.MainActivity;
import com.example.chess.R;
import com.example.chess.authorisation.core.Login;
import com.example.chess.authorisation.core.Register;



public class RegActivity extends AppCompatActivity {
    private EditText editTextUsername;
    private EditText editTextEmail;
    private EditText editTextPassword1;
    private EditText editTextPassword2;
    private Button registerButton;
    private Register register;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registr);
        Button login = findViewById(R.id.btn_swich);

        editTextUsername = findViewById(R.id.reg_username);
        editTextEmail = findViewById(R.id.reg_email);
        editTextPassword1 = findViewById(R.id.reg_password);
        editTextPassword2 = findViewById(R.id.reg_confirm_password);
        registerButton = findViewById(R.id.btn_register);



        Register register = new Register(RegActivity.this);
        registerButton.setOnClickListener(v -> attemptRegistration());

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }










    private void attemptRegistration() {
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password1 = editTextPassword1.getText().toString().trim();
        String password2 = editTextPassword2.getText().toString().trim();

        if (!validateFields(username, email, password1, password2)) {
            return;
        }

        if (!password1.equals(password2)){
            Toast.makeText(RegActivity.this, "Пароли не совподают!", Toast.LENGTH_LONG).show();
            return;
        }

        registerButton.setEnabled(false);
        registerButton.setText("Регистрация...");

        register.performRegister(username, email, password1, password2, new Register.RegisterCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    Toast.makeText(RegActivity.this, "Регистрация успешна!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(RegActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    registerButton.setEnabled(true);
                    registerButton.setText("Зарегистрироваться");
                    Toast.makeText(RegActivity.this, "Ошибка: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private boolean validateFields(String username, String email, String password1, String password2) {
        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("Введите имя пользователя");
            editTextUsername.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Введите email");
            editTextEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Введите корректный email");
            editTextEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password1)) {
            editTextPassword1.setError("Введите пароль");
            editTextPassword1.requestFocus();
            return false;
        }

        if (password1.length() < 8) {
            editTextPassword1.setError("Пароль должен быть не менее 8 символов");
            editTextPassword1.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password2)) {
            editTextPassword2.setError("Подтвердите пароль");
            editTextPassword2.requestFocus();
            return false;
        }

        if (!password1.equals(password2)) {
            editTextPassword2.setError("Пароли не совпадают");
            editTextPassword2.requestFocus();
            return false;
        }

        return true;
    }
}












