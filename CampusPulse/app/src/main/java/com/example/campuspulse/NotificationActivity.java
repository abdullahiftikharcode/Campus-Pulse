package com.example.campuspulse;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationItem> notificationList;

    private FirebaseDatabase database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_notification); // Rename layout file appropriately


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        database = FirebaseDatabase.getInstance();

        notificationList = new ArrayList<>();
        loadNotifications();

        adapter = new NotificationAdapter(this, notificationList);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.fullscreen_dialog_close).setOnClickListener(this);
        findViewById(R.id.fullscreen_dialog_action).setOnClickListener(this);
        loadNotifications();
    }

    private String getCurrentUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("CampusPulsePrefs", MODE_PRIVATE);
        return sharedPreferences.getString("userId", null); // Adjust the key as per your implementation
    }

    private void loadNotifications() {
        String userId = getCurrentUserId();

        if (userId == null) {
            // Handle the case where userId is null (e.g., redirect to login or show error)
            return;
        }

        DatabaseReference notificationsRef = database.getReference("users").child(userId).child("notifications");

        notificationsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                notificationList.clear(); // Clear the list to avoid duplication

                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    // Retrieve NotificationId and details
                    String notificationId = snapshot.getKey();
                    NotificationItem notification = new NotificationItem();
                    notification.NotificationId=notificationId;
                    notification.eventType = snapshot.child("type").exists() ? snapshot.child("type").getValue(String.class) : "Default Event";
                    if (notification.eventType.equals("annoucement")){
                        notification.announcement = snapshot.child("announcement").getValue(clubdetails.Announcement.class);
                    }
                    else {
                        notification.clubId=snapshot.child("clubId").exists() ? snapshot.child("clubId").getValue(String.class) : "Default Event";
                        notification.event = snapshot.child("event").getValue(EventAdapter.Event.class);
                    }
                    if (notification != null && notificationId != null) {
                        notification.imageResourceId=R.drawable.aklogo;
                        notificationList.add(notification);
                    }
                }
                Collections.reverse(notificationList);
                adapter.notifyDataSetChanged();
            } else {

                Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.fullscreen_dialog_close) {
            finish();
        } else if (id == R.id.fullscreen_dialog_action) {
            showOverflowMenu(v);
        }
    }
    private void showOverflowMenu(View anchor) {
        androidx.appcompat.widget.PopupMenu popupMenu = new androidx.appcompat.widget.PopupMenu(this, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.menu_delete_notification_box, popupMenu.getMenu());

        // Handle menu item clicks
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.delete_box) {
                deleteAllNotifications();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }
    private void deleteAllNotifications() {
        String userId = getCurrentUserId();

        if (userId == null) {
            Toast.makeText(this, "User not logged in. Cannot delete notifications.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference notificationsRef = database.getReference("users").child(userId).child("notifications");

        // Remove all notifications
        notificationsRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                notificationList.clear(); // Clear the local list
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "All notifications deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to delete notifications", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
