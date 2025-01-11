package com.example.campuspulse;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class AboutFragment extends Fragment {
    Button instagram, linkedin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_about, container, false);


        instagram = view.findViewById(R.id.instagram);
        linkedin = view.findViewById(R.id.linkedin);


        instagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInstagram();
            }
        });

        linkedin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLinkedIn();
            }
        });

        return view;
    }

    // Method to open Instagram link
    public void openInstagram() {
        String instagramUrl = "https://www.instagram.com/abdullahiftikhar";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(instagramUrl));
        startActivity(intent);
    }

    // Method to open LinkedIn link
    public void openLinkedIn() {
        String linkedInUrl = "https://www.linkedin.com/in/abdullahi-iftikhar";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkedInUrl));
        startActivity(intent);
    }
}
