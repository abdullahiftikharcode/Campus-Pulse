package com.example.campuspulse;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class members extends Fragment {

    String clubId;
    String title;
    String description;
    int imageResource;
    RecyclerView recyclerView;
    MembersAdapter adapter;
    List<Member> membersList = new ArrayList<>();
    Set<String> ownerUsernames = new HashSet<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_members, container, false);

        // Get arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            clubId = bundle.getString("clubId");
            title = bundle.getString("Title");
            description = bundle.getString("Desc");
            imageResource = bundle.getInt("Image");
        }

        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new MembersAdapter(getContext(),membersList,clubId,this);
        recyclerView.setAdapter(adapter);

        // Load members
        fetchMembers();

        BottomNavigationView bottomNavigationView = rootView.findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.page_2);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.page_1) {
                    selectedFragment = new clubdetails();
                    Bundle bundle = new Bundle();
                    bundle.putString("clubId", clubId);
                    bundle.putString("Title", title);
                    bundle.putString("Desc", description);
                    bundle.putInt("Image", imageResource);
                    selectedFragment.setArguments(bundle);

                }
                else if (item.getItemId() == R.id.page_3){
                    selectedFragment = new ShowEventsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("clubId", clubId);
                    bundle.putString("Title", title);
                    bundle.putString("Desc", description);
                    bundle.putInt("Image", imageResource);
                    selectedFragment.setArguments(bundle);
                }
                else if (item.getItemId() == R.id.page_2) {
                    return true;
                }
                if (selectedFragment != null) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                    return true;
                }
                return false;
            }
        });

        return rootView;
    }

    private void fetchMembers() {
        DatabaseReference clubRef = FirebaseDatabase.getInstance().getReference("clubs").child(clubId);
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Fetch Club Owners first
        clubRef.child("clubOwners").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null) {
                    // The fragment is not attached; skip processing
                    return;
                }
                // Add a divider for "Admins"
                membersList.add(new Member("divider", "Admins", ""));
                adapter.notifyDataSetChanged();  // Notify adapter for the divider

                for (DataSnapshot ownerSnapshot : snapshot.getChildren()) {
                    String username = ownerSnapshot.getValue(String.class); // Get the username directly
                    if (username != null) {
                        // Add the owner username to the set
                        ownerUsernames.add(username);

                        // Search for the user in the users node by username
                        usersRef.orderByChild("username").equalTo(username)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (!isAdded() || getContext() == null) {
                                            // The fragment is not attached; skip processing
                                            return;
                                        }
                                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                            String userId = userSnapshot.getKey();
                                            String ownerName = userSnapshot.child("name").getValue(String.class);

                                            if (userId != null && ownerName != null) {
                                                // Add the owner to the list
                                                membersList.add(new Member(userId, ownerName, username));
                                                adapter.notifyDataSetChanged(); // Update UI
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        // Handle errors
                                    }
                                });
                    }
                }

                fetchClubMembers(clubRef, usersRef);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
            }
        });
    }

    private void fetchClubMembers(DatabaseReference clubRef, DatabaseReference usersRef) {
        clubRef.child("members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null) {
                    // The fragment is not attached; skip processing
                    return;
                }
                // Add a divider for "Members"
                membersList.add(new Member("divider", "Members", ""));
                adapter.notifyDataSetChanged();  // Notify adapter for the divider

                for (DataSnapshot memberSnapshot : snapshot.getChildren()) {
                    String userId = memberSnapshot.getKey(); // Member's user ID
                    if (userId != null) {
                        // Fetch member details
                        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!isAdded() || getContext() == null) {
                                    // The fragment is not attached; skip processing
                                    return;
                                }
                                String memberName = dataSnapshot.child("name").getValue(String.class);
                                String memberUsername = dataSnapshot.child("username").getValue(String.class);

                                if (memberName != null && memberUsername != null) {
                                    // Skip adding member if they are already an owner
                                    if (!ownerUsernames.contains(memberUsername)) {
                                        // Add the member to the list
                                        membersList.add(new Member(userId, memberName, memberUsername));
                                        adapter.notifyDataSetChanged(); // Update UI
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Handle errors
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
            }
        });
    }
    public void reloadFragment() {
        members newFragment = new members();
        Bundle bundle = new Bundle();
        bundle.putString("clubId", clubId);
        bundle.putString("Title", title);
        bundle.putString("Desc", description);
        bundle.putInt("Image", imageResource);
        newFragment.setArguments(bundle);

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, newFragment)
                .commit();
    }


}
