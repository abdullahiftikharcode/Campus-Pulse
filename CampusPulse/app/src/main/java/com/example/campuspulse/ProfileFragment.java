package com.example.campuspulse;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {
    Button editprofile;
    TextView name, name2, username, username2, email, password, joinclubs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        editprofile = view.findViewById(R.id.editButton);
        name = view.findViewById(R.id.titleName);
        name2 = view.findViewById(R.id.profileName);
        username = view.findViewById(R.id.profileUsername);
        username2 = view.findViewById(R.id.titleUsername);
        email = view.findViewById(R.id.profileEmail);
        password = view.findViewById(R.id.profilePassword);
        joinclubs = view.findViewById(R.id.joinedclubs);

        // Load profile data
        reloadProfileData();

        // Handle Edit Profile button click
        editprofile.setOnClickListener(v -> {
            // Create a new Dialog instance
            Dialog dialog = new Dialog(requireContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // Remove the title bar
            dialog.setContentView(R.layout.dialog_circular); // Set the custom layout
            dialog.setCancelable(true); // Allow dismissal on touch outside
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

            // Get references to dialog views
            EditText passwordInput = dialog.findViewById(R.id.password);
            Button submitButton = dialog.findViewById(R.id.submitButton);

            // Set the submit button listener
            submitButton.setOnClickListener(v1 -> {
                String inputPassword = passwordInput.getText().toString();
                SharedPreferences sharedPreferences = requireContext().getSharedPreferences("CampusPulsePrefs", Context.MODE_PRIVATE);
                String storedPassword = sharedPreferences.getString("password", "No password");

                if (inputPassword.equals(storedPassword)) {
                    dialog.dismiss(); // Dismiss the dialog
                    Intent i = new Intent(requireContext(), Edit_Profile.class);
                    startActivity(i);
                } else {
                    passwordInput.setError("Incorrect password");
                }
            });

            dialog.show();
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload profile data whenever the fragment is visible again
        reloadProfileData();
    }

    private void reloadProfileData() {
        // Retrieve data from SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("CampusPulsePrefs", Context.MODE_PRIVATE);
        String fullName = sharedPreferences.getString("name", "No Name");
        String userName = sharedPreferences.getString("username", "No Username");
        String emailAddress = sharedPreferences.getString("email", "No Email");
        String userId = sharedPreferences.getString("userId", "No UserId");

        // Update UI
        name.setText(fullName);
        name2.setText(fullName);
        username.setText(userName);
        username2.setText(userName);
        email.setText(emailAddress);
        password.setText("********");

        // Fetch joined clubs from Firebase
        if (!userId.equals("No UserId")) {
            DatabaseReference joinedClubsRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("joinedClubs");

            joinedClubsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!isAdded() || getContext() == null) {
                        // The fragment is not attached; skip processing
                        return;
                    }
                    long clubCount = snapshot.getChildrenCount();
                    joinclubs.setText(String.valueOf(clubCount));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    joinclubs.setText("Error fetching data");
                }
            });
        } else {
            joinclubs.setText("No UserId Found");
        }
    }
}
