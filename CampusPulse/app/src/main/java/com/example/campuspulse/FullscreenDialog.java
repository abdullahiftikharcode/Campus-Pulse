package com.example.campuspulse;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class FullscreenDialog extends DialogFragment implements View.OnClickListener {
    private TextView startDateText, startTimeText, EndDateText, EndTimeText, emailtxt;
    private EditText title, location, description;
    private Callback callback;
    private String email, username, name, clubId;

    // Firebase Database reference
    private FirebaseDatabase database;
    private DatabaseReference eventRef;

    private Context mContext; // Store context

    static FullscreenDialog newInstance() {
        return new FullscreenDialog();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Ensure context is not null and store it
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialogTheme);

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fullscreen_dialog, container, false);
        ImageButton close = view.findViewById(R.id.fullscreen_dialog_close);
        TextView action = view.findViewById(R.id.fullscreen_dialog_action);
        clubId = getArguments().getString("clubId");
        close.setOnClickListener(this);
        action.setOnClickListener(this);
        startDateText = view.findViewById(R.id.startDateText);
        startTimeText = view.findViewById(R.id.startTimeText);
        EndDateText = view.findViewById(R.id.EndDateText);
        EndTimeText = view.findViewById(R.id.EndTimeText);
        emailtxt = view.findViewById(R.id.textView);
        title = view.findViewById(R.id.editText);
        location = view.findViewById(R.id.editText2);
        description = view.findViewById(R.id.editTextTextMultiLine);

        // Retrieve user information from SharedPreferences
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("CampusPulsePrefs", Context.MODE_PRIVATE);
        email = sharedPreferences.getString("email", "No email");
        emailtxt.setText(email);
        username = sharedPreferences.getString("username", "No email");
        name = sharedPreferences.getString("name", "No Name");

        // Set onClick listeners for the date and time pickers
        startDateText.setOnClickListener(v -> showDatePickerDialog(v));
        EndDateText.setOnClickListener(v -> showDatePickerDialog2(v));
        startTimeText.setOnClickListener(v -> showTimePickerDialog(v));
        EndTimeText.setOnClickListener(v -> showTimePickerDialog2(v));

        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.fullscreen_dialog_close) {
            dismiss();
        } else if (id == R.id.fullscreen_dialog_action) {
            saveEvent();
            if (callback != null) {
                callback.onActionClick("Event Saved");
            }
            dismiss();
        }
    }

    // Method to save event to Firebase
    private void saveEvent() {
        // Retrieve event details
        String startDate = startDateText.getText().toString();
        String startTime = startTimeText.getText().toString();
        String endDate = EndDateText.getText().toString();
        String endTime = EndTimeText.getText().toString();
        String titletxt = title.getText().toString();
        String locationtxt = location.getText().toString();
        String descriptiontxt = description.getText().toString();

        // Create Event object
        Event event = new Event(startDate, startTime, endDate, endTime,
                username, name, titletxt, locationtxt, descriptiontxt);

        String eventId = database.getReference("clubs").child(clubId).child("events").push().getKey();

        if (eventId != null) {
            eventRef = database.getReference("clubs").child(clubId).child("events").child(eventId);
            eventRef.setValue(event).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (mContext != null) {
                        notifyClubMembers(event);
                        Toast.makeText(mContext, "Event saved successfully!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (mContext != null) {
                        Toast.makeText(mContext, "Failed to save event!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // Callback interface for handling action click
    public interface Callback {
        void onActionClick(String name);
    }

    // Methods for showing date and time pickers
    public void showDatePickerDialog(View view) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getActivity(),
                (view1, year1, monthOfYear, dayOfMonth) -> startDateText.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1),
                year, month, day);
        datePickerDialog.show();
    }

    public void showDatePickerDialog2(View view) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getActivity(),
                (view1, year1, monthOfYear, dayOfMonth) -> EndDateText.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1),
                year, month, day);
        datePickerDialog.show();
    }

    public void showTimePickerDialog(View view) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getActivity(),
                (view1, hourOfDay, minute1) -> startTimeText.setText(hourOfDay + ":" + minute1),
                hour, minute, true);
        timePickerDialog.show();
    }

    public void showTimePickerDialog2(View view) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getActivity(),
                (view1, hourOfDay, minute1) -> EndTimeText.setText(hourOfDay + ":" + minute1),
                hour, minute, true);
        timePickerDialog.show();
    }
    class Event {
        public String startDate, startTime, endDate, endTime, username, name, title, location, description,status;

        public Event(String startDate, String startTime, String endDate, String endTime,
                     String username, String name, String title, String location, String description) {
            this.startDate = startDate;
            this.startTime = startTime;
            this.endDate = endDate;
            this.endTime = endTime;
            this.username = username;
            this.name = name;
            this.title = title;
            this.location = location;
            this.description = description;
            this.status = "Upcoming";
        }
    }
    private void notifyClubMembers(Event event) {
        // Get the reference to the 'clubs' node in Firebase
        DatabaseReference clubsRef = database.getReference("clubs");

        // Query the 'members' for the specific club by using the clubId
        clubsRef.child(clubId).child("members").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Loop through the member userIds in the club's member list
                for (DataSnapshot memberSnapshot : task.getResult().getChildren()) {
                    String userId = memberSnapshot.getKey();  // Assuming the userId is stored as a string in the members list
                    if (userId != null) {
                        // Create a notification for the user
                        Notification notification = new Notification("event",clubId, event);

                        // Save the notification to the user's notifications node
                        DatabaseReference userNotificationsRef = database.getReference("users").child(userId).child("notifications");
                        userNotificationsRef.push().setValue(notification)
                                .addOnCompleteListener(notificationTask -> {
                                    if (!notificationTask.isSuccessful()) {
                                        // Handle notification failure (optional)
                                        Toast.makeText(mContext, "Failed to send notification to " + userId, Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            } else {
                // Handle failure in retrieving members
                if (mContext != null) {
                    Toast.makeText(mContext, "Failed to retrieve club members!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    class Notification {
        public String type;
        public String clubId;
        public Event event;

        public Notification(String type,String clubId, Event event) {
            this.type=type;
            this.clubId = clubId;
            this.event = event;
        }
    }
}
