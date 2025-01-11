package com.example.campuspulse;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    Button registerpage;
    Button registerbutton;
    EditText email_;
    EditText username_;
    EditText password_;
    EditText name_;
    FirebaseDatabase database;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        registerpage = findViewById(R.id.registerpagebutton);
        name_ = findViewById(R.id.name);
        username_ = findViewById(R.id.username);
        password_ = findViewById(R.id.password);
        email_ = findViewById(R.id.email);
        registerbutton = findViewById(R.id.loginButton);

        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");

        registerpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

        registerbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = name_.getText().toString().trim();
                String username = username_.getText().toString().trim();
                String password = password_.getText().toString().trim();
                String email = email_.getText().toString().trim();

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(email)) {
                    Toast.makeText(RegisterActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (username.contains(" ")) {
                    Toast.makeText(RegisterActivity.this, "Username should not contain spaces!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(RegisterActivity.this, "Enter a valid email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if the username already exists
                usersRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Username already taken
                            Toast.makeText(RegisterActivity.this, "Username already taken, please choose another.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Check if the email already exists
                            usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        // Email already registered
                                        Toast.makeText(RegisterActivity.this, "Email already registered!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Proceed with registration
                                        String userId = usersRef.push().getKey();
                                        HashMap<String, String> userMap = new HashMap<>();
                                        userMap.put("id", userId);
                                        userMap.put("name", name);
                                        userMap.put("username", username);
                                        userMap.put("email", email);
                                        userMap.put("password", password);

                                        assert userId != null;
                                        usersRef.child(userId).setValue(userMap).addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(RegisterActivity.this, "Registration failed!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Toast.makeText(RegisterActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(RegisterActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
