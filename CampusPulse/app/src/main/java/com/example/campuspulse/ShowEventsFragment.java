package com.example.campuspulse;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ShowEventsFragment extends Fragment {

    String clubId;
    String title;
    String description;
    int imageResource;
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<EventAdapter.Event> eventList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_show_events, container, false);
        BottomNavigationView bottomNavigationView = rootView.findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.page_3);
        Bundle bundle = getArguments();
        if (bundle != null) {
            clubId = bundle.getString("clubId");
            title = bundle.getString("Title");
            description = bundle.getString("Desc");
            imageResource = bundle.getInt("Image");
        }
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList, getContext(),clubId);
        recyclerView.setAdapter(eventAdapter);
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
                else if (item.getItemId() == R.id.page_2){
                    selectedFragment = new members();
                    Bundle bundle = new Bundle();
                    bundle.putString("clubId", clubId);
                    bundle.putString("Title", title);
                    bundle.putString("Desc", description);
                    bundle.putInt("Image", imageResource);
                    selectedFragment.setArguments(bundle);
                }
                else if (item.getItemId() == R.id.page_3) {
                    return true;
                }
                if (selectedFragment != null) {
                    // Perform the fragment transaction
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                    return true;
                }
                return false;
            }
        });

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("clubs").child(clubId).child("events");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null) {
                    // The fragment is not attached; skip processing
                    return;
                }
                eventList.clear();
                eventAdapter.notifyDataSetChanged();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    EventAdapter.Event event = dataSnapshot.getValue(EventAdapter.Event.class);
                    assert event != null;
                    event.setEventId(dataSnapshot.getKey());
                    eventList.add(event);
                }
                Collections.reverse(eventList);
                eventAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        return rootView;
    }
}