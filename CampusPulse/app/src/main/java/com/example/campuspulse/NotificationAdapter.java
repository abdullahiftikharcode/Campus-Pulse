package com.example.campuspulse;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final Context context;
    private final List<NotificationItem> notificationList;

    public NotificationAdapter(Context context, List<NotificationItem> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationItem item = notificationList.get(position);
        if (item.getEventType().equals("annoucement")){
            holder.eventType.setText("New Annoucement by "+item.announcement.username);
            holder.groupName.setText(item.announcement.text);
        }
        else {
            holder.eventType.setText("New Event by "+ item.event.getUsername());
            holder.groupName.setText(item.event.getTitle());
        }
        holder.imageView.setImageResource(item.getImageResourceId());
        holder.itemView.setOnClickListener(v -> {
            switch (item.getEventType()) {
                case "annoucement":
                    Intent i = new Intent(context,annoucementdetails.class);
                    i.putExtra("announcement",item.announcement);
                    context.startActivity(i);
                    break;
                case "event":
                    Intent intent = new Intent(context, EventDetail.class);
                    intent.putExtra("clubId",item.clubId);
                    intent.putExtra("event", item.event);
                    context.startActivity(intent);
                    break;
                default:
                    Toast.makeText(context, "Unknown event type", Toast.LENGTH_SHORT).show();
            }
        });
        holder.overflowMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.overflowMenu);
            popupMenu.inflate(R.menu.menu_overflow_annoucement);

            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.delete_annoucement) {
                    deleteNotification(item, position);
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });
    }

    private void deleteNotification(NotificationItem item, int position) {
        if (item.NotificationId == null || item.NotificationId.isEmpty()) {
            Toast.makeText(context, "Invalid notification ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reference to the Firebase database
        String userId = context.getSharedPreferences("CampusPulsePrefs", Context.MODE_PRIVATE)
                .getString("userId", null); // Ensure userId retrieval is consistent
        if (userId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference notificationRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("notifications")
                .child(item.NotificationId);

        // Remove the notification from Firebase
        notificationRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Remove the item from the local list and update the adapter
                notificationList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, notificationList.size());
                Toast.makeText(context, "Notification deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to delete notification", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView eventType, groupName;
        ImageView imageView;
        ImageButton overflowMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventType = itemView.findViewById(R.id.eventtype);
            groupName = itemView.findViewById(R.id.groupname);
            imageView = itemView.findViewById(R.id.imageView2);
            overflowMenu= itemView.findViewById(R.id.overflowMenumember);
        }
    }
}
