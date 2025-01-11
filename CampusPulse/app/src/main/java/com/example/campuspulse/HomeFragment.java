package com.example.campuspulse;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<DataClass> dataList;
    private MyAdapter adapter;
    private DataClass androidData;

    private View mainFabBtn, transparentBg, joinclubbutton, createclubbutton;
    private View joinclub, createclub;
    private boolean isExpanded = false;

    private Animation fromBottomFabAnim;
    private Animation toBottomFabAnim;
    private Animation rotateClockWiseFabAnim;
    private Animation rotateAntiClockWiseFabAnim;
    private Animation fromBottomBgAnim;
    private Animation toBottomBgAnim;

    private FirebaseDatabase database;

    private DatabaseReference usersRef, clubsRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);
        dataList = new ArrayList<>();
        adapter = new MyAdapter(getContext(), dataList);
        recyclerView.setAdapter(adapter);

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        clubsRef = database.getReference("clubs");
        loadUserClubs();
        // Initialize FAB Views
        mainFabBtn = view.findViewById(R.id.mainFabBtn);
        transparentBg = view.findViewById(R.id.transparentBg);
        joinclubbutton = view.findViewById(R.id.joinclub);
        createclubbutton = view.findViewById(R.id.createClub);

        joinclub = view.findViewById(R.id.jointxt);
        createclub = view.findViewById(R.id.createtxt);

        // Initialize Animations
        fromBottomFabAnim = AnimationUtils.loadAnimation(getContext(), R.anim.from_bottom_fab);
        toBottomFabAnim = AnimationUtils.loadAnimation(getContext(), R.anim.to_bottom_fab);
        rotateClockWiseFabAnim = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_clock_wise);
        rotateAntiClockWiseFabAnim = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_anti_clock_wise);
        fromBottomBgAnim = AnimationUtils.loadAnimation(getContext(), R.anim.from_bottom_anim);
        toBottomBgAnim = AnimationUtils.loadAnimation(getContext(), R.anim.to_bottom_anim);

        // Main FAB Click Listener
        mainFabBtn.setOnClickListener(v -> {
            if (isExpanded) {
                shrinkFab();
            } else {
                expandFab();
            }
        });

        // Individual FAB Click Listeners
        joinclubbutton.setOnClickListener(v -> onJoinClicked());
        createclubbutton.setOnClickListener(v -> onCreateClicked());

        // Background Click Listener
        transparentBg.setOnClickListener(v -> shrinkFab());

        return view;
    }
    private void loadUserClubs() {
        // Get the user ID from SharedPreferences
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("CampusPulsePrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", ""); // Retrieve the current user ID

        if (userId.isEmpty()) {
            Toast.makeText(getContext(), "Error retrieving user information. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch the user's joined clubs in real-time
        usersRef.child(userId).child("joinedClubs").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot userClubsSnapshot) {
                if (!isAdded() || getContext() == null) {
                    // The fragment is not attached; skip processing
                    return;
                }
                dataList.clear(); // Clear the list to avoid duplication
                adapter.notifyDataSetChanged();

                if (userClubsSnapshot.exists()) {
                    for (DataSnapshot clubSnapshot : userClubsSnapshot.getChildren()) {
                        String clubId = clubSnapshot.getKey();
                        clubsRef.child(clubId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot clubDetailsSnapshot) {
                                if (clubDetailsSnapshot.exists()) {
                                    String clubName = clubDetailsSnapshot.child("clubName").getValue(String.class);
                                    String clubDescription = clubDetailsSnapshot.child("clubDescription").getValue(String.class);

                                    // Create DataClass with clubId included
                                    DataClass clubData = new DataClass(clubName, clubDescription, R.drawable.recycleimage, clubId);

                                    // Add the club data to the list
                                    dataList.add(clubData);
                                    adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(getContext(), "Error loading club details: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Toast.makeText(getContext(), "No clubs joined yet.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onJoinClicked() {
        // Inflate the custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_join, null);

        // Find the input and button in the custom layout
        TextInputEditText joinCodeInput = dialogView.findViewById(R.id.code_input);
        Button submitButton = dialogView.findViewById(R.id.submitButton);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Set the button's click listener
        submitButton.setOnClickListener(v -> {
            String joinCode = joinCodeInput.getText().toString().trim();
            if (joinCode.isEmpty()) {
                Toast.makeText(getContext(), "Join code cannot be empty!", Toast.LENGTH_SHORT).show();
            } else {
                dialog.dismiss(); // Close the dialog
                checkJoinCode(joinCode); // Handle join code
            }
        });

        dialog.show();
    }



    private void checkJoinCode(String joinCode) {
        // Get the user ID and username from SharedPreferences
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("CampusPulsePrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        String userId = sharedPreferences.getString("userId", ""); // Retrieve the current user ID

        if (userId.isEmpty() || username.isEmpty()) {
            Toast.makeText(getContext(), "Error retrieving user information. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the current date and time
        String currentDateAndTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Query the clubs to find the one matching the join code
        clubsRef.orderByChild("joinCode").equalTo(joinCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Club found, proceed with joining
                    DataSnapshot clubSnapshot = dataSnapshot.getChildren().iterator().next();
                    String clubId = clubSnapshot.getKey(); // Get the club ID

                    // Check if the user is already in the club
                    usersRef.child(userId).child("joinedClubs").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot userClubsSnapshot) {
                            if (userClubsSnapshot.hasChild(clubId)) {
                                Toast.makeText(getContext(), "You are already a member of this club.", Toast.LENGTH_SHORT).show();
                            } else {
                                // Add the club to the user's joinedClubs list (as a simple value)
                                usersRef.child(userId).child("joinedClubs").child(clubId).setValue(true);

                                // Add the user to the club's members list with both userId, username, and the current date/time
                                DatabaseReference clubMembersRef = clubsRef.child(clubId).child("members");
                                Member member = new Member(userId, username, currentDateAndTime); // Add the user along with the join date/time
                                clubMembersRef.child(userId).setValue(member); // Add the user as a member

                                // Increment the totalMembers count for the club
                                clubsRef.child(clubId).child("totalMembers").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot totalMembersSnapshot) {
                                        long totalMembers = totalMembersSnapshot.getValue(Long.class);
                                        clubsRef.child(clubId).child("totalMembers").setValue(totalMembers + 1);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Toast.makeText(getContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                                Toast.makeText(getContext(), "Successfully joined the club!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(getContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Invalid join code. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void onCreateClicked() {
        Intent intent = new Intent(getContext(), CreateClub.class);
        startActivity(intent);
    }



    private void shrinkFab() {
        if (!isExpanded) return; // Avoid running animations when already collapsed.

        transparentBg.startAnimation(toBottomBgAnim);
        mainFabBtn.startAnimation(rotateAntiClockWiseFabAnim);
        joinclubbutton.startAnimation(toBottomFabAnim);
        createclubbutton.startAnimation(toBottomFabAnim);
        joinclub.startAnimation(toBottomFabAnim);
        createclub.startAnimation(toBottomFabAnim);

        joinclubbutton.setVisibility(View.INVISIBLE);
        createclubbutton.setVisibility(View.INVISIBLE);
        joinclub.setVisibility(View.INVISIBLE);
        createclub.setVisibility(View.INVISIBLE);

        transparentBg.setVisibility(View.GONE);
        transparentBg.setClickable(false);

        isExpanded = false;
    }

    private void expandFab() {
        if (isExpanded) return; // Avoid running animations when already expanded.

        transparentBg.startAnimation(fromBottomBgAnim);
        mainFabBtn.startAnimation(rotateClockWiseFabAnim);
        joinclubbutton.startAnimation(fromBottomFabAnim);
        createclubbutton.startAnimation(fromBottomFabAnim);
        joinclub.startAnimation(fromBottomFabAnim);
        createclub.startAnimation(fromBottomFabAnim);

        joinclubbutton.setVisibility(View.VISIBLE);
        createclubbutton.setVisibility(View.VISIBLE);
        joinclub.setVisibility(View.VISIBLE);
        createclub.setVisibility(View.VISIBLE);

        transparentBg.setVisibility(View.VISIBLE);
        transparentBg.setClickable(true);

        isExpanded = true;
    }


    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && isExpanded) {
            Rect outRect = new Rect();
            ViewGroup parent = (ViewGroup) mainFabBtn.getParent();
            parent.getGlobalVisibleRect(outRect);
            if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                shrinkFab();
            }
        }
        return super.getActivity().dispatchTouchEvent(event);
    }
    public class Member {
        private String userId;
        private String username;
        private String joinDate; // Added field for join date and time

        // Constructor
        public Member(String userId, String username, String joinDate) {
            this.userId = userId;
            this.username = username;
            this.joinDate = joinDate;
        }

        // Getter methods
        public String getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getJoinDate() {
            return joinDate;
        }

        // Setter methods (if needed)
        public void setUserId(String userId) {
            this.userId = userId;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setJoinDate(String joinDate) {
            this.joinDate = joinDate;
        }
    }

}
