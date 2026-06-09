package com.example.chess.main_fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.chess.authorisation.LoginActivity;

import androidx.fragment.app.Fragment;

import com.example.chess.R;
import com.example.chess.authorisation.core.Login;
public class home_fragment extends Fragment {

    private Button startB;
    private Button startWFB;
    private Button logOut;
    private Login login;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home, container, false);

        startB = view.findViewById(R.id.start_home);
        startWFB = view.findViewById(R.id.start2_home);
        logOut = view.findViewById(R.id.logout_home);
        login = new Login(requireContext());
        startB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        startWFB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        logOut.setOnClickListener(v -> {
            login.logout();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish(); // закрываем главный экран
        });

        return view;
    }
}
