package com.example.chess;

import android.os.Bundle;
import android.widget.ImageButton;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.chess.main_fragments.friends_fragment;
import com.example.chess.main_fragments.home_fragment;
import com.example.chess.main_fragments.requests_fragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ImageButton home = findViewById(R.id.home_btn);
        ImageButton requests = findViewById(R.id.req_btn);
        ImageButton friends = findViewById(R.id.friends_btn);

        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content, new home_fragment())
                    .commit();
        }

        home.setOnClickListener(v -> replaceFragment(new home_fragment()));
        requests.setOnClickListener(v -> replaceFragment(new requests_fragment()));
        friends.setOnClickListener(v -> replaceFragment(new friends_fragment()));

    }

    private void replaceFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, fragment)
                .commit();
    }
}