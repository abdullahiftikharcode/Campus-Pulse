package com.example.campuspulse;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreateClub extends AppCompatActivity {
    private EditText clubNameEditText, clubDescriptionEditText;
    private Button createClubButton,back;
    private FirebaseDatabase mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_club);

        clubNameEditText = findViewById(R.id.clubName);
        clubDescriptionEditText = findViewById(R.id.clubDescription);
        createClubButton = findViewById(R.id.createClubButton);
       back = findViewById(R.id.button_back);
        mDatabase = FirebaseDatabase.getInstance();

        createClubButton.setOnClickListener(v -> {
            String clubName = clubNameEditText.getText().toString().trim();
            String clubDescription = clubDescriptionEditText.getText().toString().trim();

            if (!clubName.isEmpty() && !clubDescription.isEmpty()) {
                String joinCode = generateUniqueJoinCode();
                String currentUser = getCurrentUserFromSharedPrefs(); // Fetch username from SharedPreferences

                if (currentUser != null) {
                    List<String> owners = new ArrayList<>();
                    owners.add(currentUser); // Add the current user as the first owner

                    Club club = new Club(clubName, owners, joinCode, clubDescription);
                    DatabaseReference clubsRef = mDatabase.getReference("clubs");
                    DatabaseReference usersRef = mDatabase.getReference("users");
                    String clubId = clubsRef.push().getKey();

                    if (clubId != null) {
                        // Add the club data to the "clubs" node
                        clubsRef.child(clubId).setValue(club);

                        // Initialize the fields for total members and total owners
                        Map<String, Object> clubUpdates = new HashMap<>();
                        clubUpdates.put("totalMembers", 1);  // Initially, 1 member (the owner)
                        clubUpdates.put("totalOwners", 1);   // 1 owner (the creator)

                        // Update the club data with these new fields
                        clubsRef.child(clubId).updateChildren(clubUpdates);

                        SharedPreferences sharedPreferences = CreateClub.this.getSharedPreferences("CampusPulsePrefs", Context.MODE_PRIVATE);
                        String userId = sharedPreferences.getString("userId", ""); // Retrieve the current user ID

                        // Get the current date and time
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String currentDateTime = sdf.format(new Date());  // Format the current date and time

                        // Create a map for the current user's data to store in the members node
                        Map<String, Object> memberData = new HashMap<>();
                        memberData.put("userId", userId);
                        memberData.put("username", currentUser);
                        memberData.put("dateTime", currentDateTime); // Add the current date and time

                        // Add the current user as a member in "clubs/clubId/members"
                        clubsRef.child(clubId).child("members").child(userId).setValue(memberData);

                        // Add the club to the user's "joinedClubs" list
                        usersRef.child(userId).child("joinedClubs").child(clubId).setValue(true);

                        Toast.makeText(CreateClub.this, "Club Created Successfully!", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(CreateClub.this, HomeFragment.class);
                        finish();  // Close this activity and return to the previous one
                    } else {
                        Toast.makeText(CreateClub.this, "Error: Unable to generate Club ID.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CreateClub.this, "Error: Unable to fetch username.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CreateClub.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private String generateUniqueJoinCode() {
        Random random = new Random();
        String joinCode;
        boolean isUnique;

        do {
            joinCode = String.format("%06d", random.nextInt(1000000));
            isUnique = checkIfJoinCodeExists(joinCode);
        } while (!isUnique);

        return joinCode;
    }

    private boolean checkIfJoinCodeExists(String joinCode) {
        final boolean[] exists = {false};

        DatabaseReference clubsRef = mDatabase.getReference("clubs");
        clubsRef.orderByChild("join_code").equalTo(joinCode).addListenerForSingleValueEvent(new ValueEventListener() {

            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    exists[0] = true;
                }
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });

        return !exists[0];
    }

    private String getCurrentUserFromSharedPrefs() {
        SharedPreferences sharedPreferences = getSharedPreferences("CampusPulsePrefs", MODE_PRIVATE);
        return sharedPreferences.getString("username", null); // Replace "username" with the actual key used for storing the username
    }
}
