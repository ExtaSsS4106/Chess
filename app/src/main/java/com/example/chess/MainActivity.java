package com.example.chess;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.chess.authorisation.LoginActivity;
import com.example.chess.core.MainCore;
import com.example.chess.main_fragments.friends_fragment;
import com.example.chess.main_fragments.home_fragment;
import com.example.chess.main_fragments.requests_fragment;
import com.example.chess.data.loadUser;
import com.example.chess.data.loadUser.UserData;

public class MainActivity extends AppCompatActivity {
    private MainCore mainCore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainCore = new MainCore(this);

        try {
            loadUser loadUser = new loadUser();
            UserData userData = loadUser.loadUserData(this);


            if (userData != null) {
                setContentView(R.layout.main);

                TextView userName = findViewById(R.id.user_name);

                userName.setText(userData.getUsername());

                loadActiveGame();

                ImageButton home = findViewById(R.id.home_btn);
                ImageButton requests = findViewById(R.id.req_btn);
                ImageButton friends = findViewById(R.id.friends_btn);

                if (savedInstanceState == null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content, new home_fragment())
                            .commit();
                }

                home.setOnClickListener(v -> replaceFragment(new home_fragment()));
                requests.setOnClickListener(v -> replaceFragment(new requests_fragment()));
                friends.setOnClickListener(v -> replaceFragment(new friends_fragment()));

            } else {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
    private void loadActiveGame(){
        mainCore.getChannelID(new MainCore.ChannelCallback() {
            @Override
            public void onSuccess(String ID) {
                if (ID != null) {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(MainActivity.this, GameActivity.class);
                        intent.putExtra("room_id", ID);
                        startActivity(intent);
                        finish();
                    });
                }
            }

            @Override
            public void onError(String error) {

            }
        });
    }
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, fragment)
                .commit();
    }
}