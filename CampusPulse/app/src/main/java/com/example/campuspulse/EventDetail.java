package com.example.campuspulse;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EventDetail extends AppCompatActivity {
    Button b, mark;
    DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Retrieve the Event object and clubId passed from the previous activity
        EventAdapter.Event event = (EventAdapter.Event) getIntent().getSerializableExtra("event");
        String clubId = getIntent().getStringExtra("clubId");

        // Initialize the views
        TextView membername = findViewById(R.id.membername);
        TextView memberusername = findViewById(R.id.memberusername);
        TextView title_event = findViewById(R.id.title_event);
        TextView statusevent = findViewById(R.id.statusevent);
        TextView Start = findViewById(R.id.Start);
        TextView End = findViewById(R.id.End);
        TextView textView6 = findViewById(R.id.textView6);
        TextView announcementText = findViewById(R.id.announcementText);
        b = findViewById(R.id.button_back);
        mark = findViewById(R.id.markbutton);

        database = FirebaseDatabase.getInstance().getReference("clubs").child(clubId).child("events");
        // Go back to the previous activity
        b.setOnClickListener(v -> finish());
        mark.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("CampusPulsePrefs", Context.MODE_PRIVATE);

            String currentUsername = sharedPreferences.getString("username", ""); // Assuming username is stored

            // Firebase reference to the club's owners list
            DatabaseReference clubRef = FirebaseDatabase.getInstance().getReference("clubs").child(clubId).child("clubOwners");

            clubRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    boolean isOwner = false;

                    // Loop through each club owner in the clubOwners list
                    for (DataSnapshot ownerSnapshot : snapshot.getChildren()) {
                        String ownerUsername = ownerSnapshot.getValue(String.class);


                        // Check if the current user matches any of the club owners
                        if (currentUsername.equals(ownerUsername)) {
                            isOwner = true;
                            break; // Exit the loop as we've found the owner
                        }
                    }
                    if (isOwner) {
                        if (event != null) {
                            if (event.getEventId() == null) {
                                // Query database to find eventId based on other attributes
                                DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("clubs").child(clubId).child("events");
                                eventsRef.get().addOnCompleteListener(task -> {
                                    if (task.isSuccessful() && task.getResult() != null) {
                                        for (DataSnapshot snapshot2 : task.getResult().getChildren()) {
                                            EventAdapter.Event dbEvent = snapshot2.getValue(EventAdapter.Event.class);
                                            if (dbEvent != null &&
                                                    dbEvent.getName().equals(event.getName()) &&
                                                    dbEvent.getTitle().equals(event.getTitle()) &&
                                                    dbEvent.getStartDate().equals(event.getStartDate())) {
                                                event.setEventId(snapshot2.getKey());
                                                break;
                                            }
                                        }

                                        if (event.getEventId() != null) {
                                            markEventAsEnded(event, statusevent);
                                        } else {
                                            Toast.makeText(EventDetail.this, "Event not found in the database", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(EventDetail.this, "Failed to retrieve events from the database", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                // Use existing eventId to mark the event
                                markEventAsEnded(event, statusevent);
                            }
                        } else {
                            Toast.makeText(EventDetail.this, "Invalid event data", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        // Show an error message if the user is not the club owner
                        Toast.makeText(EventDetail.this, "You must be the club owner mark an event Done", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(EventDetail.this, "Failed to check club owner", Toast.LENGTH_SHORT).show();
                }
            });
        });


        // Set the event data to the respective views
        if (event != null) {
            membername.setText(event.getName());
            memberusername.setText(event.getUsername());
            title_event.setText("Title :" + event.getTitle());
            statusevent.setText("Status : " + event.getStatus());
            if (event.getStatus().equals("Upcoming")) {
                statusevent.setTextColor(ContextCompat.getColor(this, R.color.teal_200));
            } else {
                statusevent.setTextColor(ContextCompat.getColor(this, R.color.red));
            }
            Start.setText("Start Date & Time : " + event.getStartDate() + " " + event.getStartTime());
            End.setText("End Date & Time : " + event.getEndDate() + " " + event.getEndTime());
            textView6.setText("Location : " + event.getLocation());
            announcementText.setText("Description :" + event.getDescription());
        }
    }

    private void markEventAsEnded(EventAdapter.Event event, TextView statusevent) {
        if (event.getStatus().equals("Upcoming")) {
            // Update the status in the event object
            event.setStatus("Ended");

            // Use the correct database reference with eventId
            database.child(event.getEventId()).child("status").setValue("Ended")
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(EventDetail.this, "Event marked as Ended", Toast.LENGTH_SHORT).show();
                            statusevent.setText("Status : Ended");
                            statusevent.setTextColor(ContextCompat.getColor(EventDetail.this, R.color.red));
                        } else {
                            Toast.makeText(EventDetail.this, "Failed to update event status", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Show a message if the event is already ended or not ongoing
            Toast.makeText(EventDetail.this, "Event already marked as Ended", Toast.LENGTH_SHORT).show();
        }
    }

}
