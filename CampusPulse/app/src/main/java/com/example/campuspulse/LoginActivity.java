package com.example.campuspulse;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "CampusPulsePrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "name";
    Button registerpage;
    Button loginpage;
    EditText username_;
    EditText password_;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        progressBar = findViewById(R.id.progressBar);
        registerpage = findViewById(R.id.registerpagebutton);
        loginpage = findViewById(R.id.loginButton);
        username_ = findViewById(R.id.username);
        password_ = findViewById(R.id.password);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedUsername = prefs.getString(KEY_USERNAME, null);
        String savedPassword = prefs.getString(KEY_PASSWORD, null);

        if (savedUsername != null && savedPassword != null) {
            progressBar.setVisibility(View.VISIBLE);
            autoLogin(savedUsername, savedPassword, prefs);
        }

        registerpage.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(i);
            finish();
        });

        loginpage.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String username = username_.getText().toString().trim();
            String password = password_.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Both fields are required!", Toast.LENGTH_SHORT).show();
                return;
            }

            validateFromFirebase(username, password, prefs);
        });
    }



    private void validateFromFirebase(String username, String password, SharedPreferences prefs) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users");

        usersRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String dbPassword = snapshot.child("password").getValue(String.class);
                        String userId = snapshot.getKey(); // Get the user ID (Firebase key)
                        String email = snapshot.child("email").getValue(String.class);
                        String name = snapshot.child("name").getValue(String.class);
                        if (dbPassword != null && dbPassword.equals(password)) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(KEY_USERNAME, username);
                            editor.putString(KEY_PASSWORD, password);
                            editor.putString(KEY_USER_ID, userId); // Save user ID
                            editor.putString(KEY_EMAIL, email);
                            editor.putString(KEY_NAME, name);
                            editor.apply();
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            navigateToHome(); // Pass userId to next activity if needed
                            return;
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Incorrect password!", Toast.LENGTH_SHORT).show();
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Username not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void autoLogin(String username, String password, SharedPreferences prefs) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users");

        usersRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String dbPassword = snapshot.child("password").getValue(String.class);
                        String userId = snapshot.getKey(); // Get the user ID (Firebase key)

                        if (dbPassword != null && dbPassword.equals(password)) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(KEY_USERNAME, username);
                            editor.putString(KEY_PASSWORD, password);
                            editor.putString(KEY_USER_ID, userId); // Save user ID
                            editor.apply();
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Auto-login successful!", Toast.LENGTH_SHORT).show();
                            navigateToHome();
                            return;
                        }
                    }
                    clearStoredCredentials(prefs);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Saved credentials incorrect. Please log in again.", Toast.LENGTH_SHORT).show();
                } else {
                    clearStoredCredentials(prefs);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Saved credentials invalid. Please log in again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, NavigationDrawer.class);
        startActivity(intent);
        finish();
    }
    private void clearStoredCredentials(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_PASSWORD);
        editor.apply();
    }
}



