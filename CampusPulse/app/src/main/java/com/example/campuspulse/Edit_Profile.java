package com.example.campuspulse;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Edit_Profile extends AppCompatActivity {
    Button saveProfile;
    EditText name, username, email, password;
    DatabaseReference databaseReference;
    String oldUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize UI elements
        name = findViewById(R.id.editName);
        username = findViewById(R.id.editUsername);
        email = findViewById(R.id.editEmail);
        password = findViewById(R.id.password);
        saveProfile = findViewById(R.id.saveButton);

        // Initialize Firebase Database Reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Retrieve values from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("CampusPulsePrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", ""); // Retrieve userID
        String storedName = sharedPreferences.getString("name", "");
        String storedUsername = sharedPreferences.getString("username", "");
        String storedEmail = sharedPreferences.getString("email", "");
        String storedPassword = sharedPreferences.getString("password", "");
         oldUsername=storedUsername;
        // Set values to EditText fields
        name.setText(storedName);
        username.setText(storedUsername);
        email.setText(storedEmail);
        password.setText(storedPassword);

        // Save updated values to SharedPreferences and Firebase when save button is clicked
        saveProfile.setOnClickListener(v -> {
            String updatedName = name.getText().toString().trim();
            String updatedUsername = username.getText().toString().trim();
            String updatedEmail = email.getText().toString().trim();
            String updatedPassword = password.getText().toString().trim();

            if (validateFields(updatedName, updatedUsername, updatedEmail, updatedPassword)) {
                // Check if the username already exists
                databaseReference.orderByChild("username").equalTo(updatedUsername)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                // Check if username exists and is not the current user's username
                                if (dataSnapshot.exists() && !dataSnapshot.hasChild(userId)) {
                                    Toast.makeText(Edit_Profile.this, "Username already exists! Please choose a different one.", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Username is unique or belongs to the current user; proceed with saving locally and in Firebase
                                    saveToLocalAndFirebase(userId, updatedName, updatedUsername, updatedEmail, updatedPassword);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(Edit_Profile.this, "Error checking username: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void saveToLocalAndFirebase(String userId, String name, String username, String email, String password) {
        // Save to SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("CampusPulsePrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.putString("username", username);
        editor.putString("email", email);
        editor.putString("password", password);
        editor.apply();

        // Update in Firebase
        updateFirebaseProfile(userId, name, username, email, password);

        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
        finish(); // Close the activity
    }

    private void updateFirebaseProfile(String userID, String name, String username, String email, String password) {
        if (userID == null || userID.isEmpty()) {
            Toast.makeText(this, "User ID is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userReference = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userID);

        // Update user fields in "users"
        userReference.child("name").setValue(name)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseUpdate", "Name updated successfully");
                    } else {
                        Log.e("FirebaseUpdate", "Failed to update name", task.getException());
                    }
                });

        userReference.child("username").setValue(username)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseUpdate", "Username updated successfully");
                        updateClubMemberUsernames(userID, username); // Update username in clubs
                    } else {
                        Log.e("FirebaseUpdate", "Failed to update username", task.getException());
                    }
                });

        userReference.child("email").setValue(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseUpdate", "Email updated successfully");
                    } else {
                        Log.e("FirebaseUpdate", "Failed to update email", task.getException());
                    }
                });

        userReference.child("password").setValue(password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirebaseUpdate", "Password updated successfully");
                    } else {
                        Log.e("FirebaseUpdate", "Failed to update password", task.getException());
                    }
                });
    }

    private void updateClubMemberUsernames(String userID, String newUsername) {
        DatabaseReference userClubsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userID)
                .child("joinedClubs");

        userClubsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Fetch the old username from the "users" node
                    DatabaseReference userRef = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(userID);

                    userRef.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot usernameSnapshot) {


                            for (DataSnapshot clubSnapshot : snapshot.getChildren()) {
                                String clubId = clubSnapshot.getKey();
                                if (clubId != null) {
                                    // Update the username in the "members" list
                                    DatabaseReference clubMemberRef = FirebaseDatabase.getInstance()
                                            .getReference("clubs")
                                            .child(clubId)
                                            .child("members")
                                            .child(userID)
                                            .child("username");

                                    clubMemberRef.setValue(newUsername)
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    Log.d("ClubUpdate", "Updated username in club members: " + clubId);
                                                } else {
                                                    Log.e("ClubUpdate", "Failed to update username in club members: " + clubId, task.getException());
                                                }
                                            });

                                    // Check and update the username in the "owners" list
                                    DatabaseReference clubOwnersRef = FirebaseDatabase.getInstance()
                                            .getReference("clubs")
                                            .child(clubId)
                                            .child("clubOwners");

                                    clubOwnersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot ownersSnapshot) {
                                            if (ownersSnapshot.exists()) {
                                                for (DataSnapshot ownerSnapshot : ownersSnapshot.getChildren()) {
                                                    String ownerId = ownerSnapshot.getKey();
                                                    String ownerUsername = ownerSnapshot.getValue(String.class);

                                                    if (ownerUsername != null && ownerUsername.equals(oldUsername)) {
                                                        // Update the old username to the new username
                                                        clubOwnersRef.child(ownerId).setValue(newUsername)
                                                                .addOnCompleteListener(task -> {
                                                                    if (task.isSuccessful()) {
                                                                        Log.d("ClubUpdate", "Updated username in club owners: " + clubId);
                                                                    } else {
                                                                        Log.e("ClubUpdate", "Failed to update username in club owners: " + clubId, task.getException());
                                                                    }
                                                                });
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.e("ClubUpdate", "Error fetching club owners: " + error.getMessage());
                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("ClubUpdate", "Error fetching old username: " + error.getMessage());
                        }
                    });
                } else {
                    Log.d("ClubUpdate", "User is not part of any clubs.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ClubUpdate", "Error fetching joinedClubs: " + error.getMessage());
            }
        });
    }

    private boolean validateFields(String name, String username, String email, String password) {
        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields must be filled!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
