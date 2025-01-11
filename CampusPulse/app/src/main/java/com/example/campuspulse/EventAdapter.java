package com.example.campuspulse;

import static com.example.campuspulse.R.*;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private Context context;
    String ClubId;


    public EventAdapter(List<Event> eventList, Context context,String clubid) {
        this.eventList = eventList;
        this.context = context;
        this.ClubId=clubid;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.eventName.setText(event.getTitle());
        holder.eventDescription.setText(event.getDescription());
        if (Objects.equals(event.getStatus(), "Upcoming")){
            holder.eventStatus.setTextColor(ContextCompat.getColor(context, color.teal_200));
        }
        else {
            holder.eventStatus.setTextColor(ContextCompat.getColor(context, color.red));
        }
        holder.eventStatus.setText("Status : "+event.getStatus());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EventDetail.class);
                intent.putExtra("clubId",ClubId);
                intent.putExtra("event", event);
                context.startActivity(intent);
            }
        });
        holder.overflowMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.overflowMenu);
            popupMenu.inflate(menu.menu_overflow_annoucement);

            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.delete_annoucement) {
                    // Handle delete action
                    deleteEvent(event, position);
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });

    }
    private void deleteEvent(Event event, int position) {
        if (event != null) {
            DatabaseReference clubRef = FirebaseDatabase.getInstance().getReference("clubs").child(ClubId).child("events").child(event.getEventId());
            clubRef.removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Announcement deleted successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete announcement", Toast.LENGTH_SHORT).show());
        }
        eventList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, eventList.size());
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, eventDescription, eventStatus;
        ImageButton overflowMenu;
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.Titleevent);
            eventDescription = itemView.findViewById(R.id.eventdescription);
            eventStatus = itemView.findViewById(R.id.status);
            overflowMenu = itemView.findViewById(R.id.overflowMenu);
        }
    }

    public static class Event implements Serializable {
        private String eventId;  // New field for event ID
        private String title;
        private String description;
        private String startDate;
        private String startTime;
        private String endDate;
        private String endTime;
        private String location;
        private String status;
        private String name; // Organizer's name
        private String username;

        // Default constructor (required for Firebase)
        public Event() {}

        // Parameterized constructor
        public Event(String eventId, String title, String description, String startDate, String startTime,
                     String endDate, String endTime, String location, String status, String name, String username) {
            this.eventId = eventId;
            this.title = title;
            this.description = description;
            this.startDate = startDate;
            this.startTime = startTime;
            this.endDate = endDate;
            this.endTime = endTime;
            this.location = location;
            this.status = status;
            this.name = name;
            this.username = username;
        }

        // Getters and setters
        public String getEventId() {
            return eventId;
        }

        public void setEventId(String eventId) {
            this.eventId = eventId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

}

