package com.example.campuspulse;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class clubdetails extends Fragment {

    private ImageView clubImage;
    private TextView clubTitle, clubDesc;
    private Button copyjoincode;
    private String clubID;
    LinearLayout layout;
    ImageButton overflowMenu ;
    private View mainFabBtn, transparentBg, annoucementbutton, eventbutton;
    private View annoucementxt,eventtxt;
    private String selectedAnnouncementId;
    BottomNavigationView  bottomNavigationView;
    private boolean isExpanded = false;

    private Animation fromBottomFabAnim;
    private Animation toBottomFabAnim;
    private Animation rotateClockWiseFabAnim;
    private Animation rotateAntiClockWiseFabAnim;
    private Animation fromBottomBgAnim;
    private Animation toBottomBgAnim;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment layout
        View rootView = inflater.inflate(R.layout.fragment_clubdetails, container, false);


        // Initialize the UI components
        clubImage = rootView.findViewById(R.id.recImage);
        clubTitle = rootView.findViewById(R.id.recTitle);
        clubDesc = rootView.findViewById(R.id.recDesc);
        layout = rootView.findViewById(R.id.append_annoucement);

        mainFabBtn = rootView.findViewById(R.id.mainFabBtn);
        transparentBg = rootView.findViewById(R.id.transparentBg);
        annoucementbutton = rootView.findViewById(R.id.createannoucement);
        eventbutton = rootView.findViewById(R.id.createevent);

        annoucementxt = rootView.findViewById(R.id.annoucementtxt);
        eventtxt = rootView.findViewById(R.id.eventtxt);

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
         annoucementbutton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 showAnnouncementDialog();
             }
         });
         eventbutton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 SharedPreferences sharedPreferences = getActivity().getSharedPreferences("CampusPulsePrefs", Context.MODE_PRIVATE);

                 String currentUsername = sharedPreferences.getString("username", ""); // Assuming username is stored

                 // Firebase reference to the club's owners list
                 DatabaseReference clubRef = FirebaseDatabase.getInstance().getReference("clubs").child(clubID).child("clubOwners");

                 clubRef.addListenerForSingleValueEvent(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot snapshot) {
                         if (!isAdded() || getContext() == null) {
                             // The fragment is not attached; skip processing
                             return;
                         }
                         boolean isOwner = false;

                         // Loop through each club owner in the clubOwners list
                         for (DataSnapshot ownerSnapshot : snapshot.getChildren()) {
                             String ownerUsername = ownerSnapshot.getValue(String.class);


                             // Check if the current user matches any of the club owners
                             if (currentUsername.equals(ownerUsername)) {
                                 isOwner = true;
                                 break;
                             }
                         }
                         if (isOwner) {
                 DialogFragment dialog = FullscreenDialog.newInstance();
                 Bundle args = new Bundle();
                 args.putString("clubId", clubID);
                 dialog.setArguments(args);
                 dialog.show(getFragmentManager(), "fullscreen_dialog_tag");
                         } else {
                             // Show an error message if the user is not the club owner
                             Toast.makeText(getContext(), "You must be the club owner to create an event", Toast.LENGTH_SHORT).show();
                         }
                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError error) {
                         Toast.makeText(getContext(), "Failed to check club owner", Toast.LENGTH_SHORT).show();
                     }
                 });
             }
         });
        transparentBg.setOnClickListener(v -> shrinkFab());
        // Get the data passed from the adapter
        if (getArguments() != null) {
            String title = getArguments().getString("Title");
            String description = getArguments().getString("Desc");
            int imageResource = getArguments().getInt("Image");
            clubID = getArguments().getString("clubId");
            // Set the data to the UI components
            clubTitle.setText(title);
            clubDesc.setText(description);
            clubImage.setImageResource(imageResource);
        }

        copyjoincode = rootView.findViewById(R.id.joincode);

        DatabaseReference clubRef = FirebaseDatabase.getInstance().getReference("clubs").child(clubID).child("joinCode");
        clubRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null) {
                    // The fragment is not attached; skip processing
                    return;
                }
                if (snapshot.exists()) {
                    String joinCode = snapshot.getValue(String.class);
                    copyjoincode.setText("Join Code: "+joinCode);
                } else {
                    copyjoincode.setText("Join Code Not Available");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to fetch join code", Toast.LENGTH_SHORT).show();
            }
        });

        copyjoincode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonText = copyjoincode.getText().toString();

                // Remove "Join Code: " prefix
                if (buttonText.startsWith("Join Code: ")) {
                    String joinCode = buttonText.replace("Join Code: ", "");
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("Join Code", joinCode);
                    clipboard.setPrimaryClip(clip);

                    Toast.makeText(getContext(), "Join Code Copied to Clipboard", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "No Join Code to Copy", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Load announcements
        loadAnnouncements();
       bottomNavigationView = rootView.findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.page_1);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                if (item.getItemId() == R.id.page_1) {
                   return true;

                }
                else  if (item.getItemId() == R.id.page_2) {

                    selectedFragment = new members();
                    Bundle bundle = new Bundle();
                    bundle.putString("clubId", clubID);
                    bundle.putString("Title",  clubTitle.getText().toString());
                    bundle.putString("Desc", clubDesc.getText().toString());
                    bundle.putInt("Image",  getArguments().getInt("Image"));
                    selectedFragment.setArguments(bundle);
                }
                else if (item.getItemId() == R.id.page_3){
                    selectedFragment = new ShowEventsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("clubId", clubID);
                    bundle.putString("Title",  clubTitle.getText().toString());
                    bundle.putString("Desc", clubDesc.getText().toString());
                    bundle.putInt("Image",  getArguments().getInt("Image"));
                    selectedFragment.setArguments(bundle);
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

        return rootView;
    }

    private void showAnnouncementDialog() {
        // Fetch the current user's ID and username from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("CampusPulsePrefs", Context.MODE_PRIVATE);

        String currentUsername = sharedPreferences.getString("username", ""); // Assuming username is stored

        // Firebase reference to the club's owners list
        DatabaseReference clubRef = FirebaseDatabase.getInstance().getReference("clubs").child(clubID).child("clubOwners");

        clubRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null) {
                    // The fragment is not attached; skip processing
                    return;
                }
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
                    // User is the club owner, show the announcement dialog
                    LayoutInflater inflater = getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.dialog_announcement, null);

                    final EditText editTextAnnouncement = dialogView.findViewById(R.id.editTextAnnouncement);

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Create Announcement")
                            .setView(dialogView)
                            .setPositiveButton("Publish", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String announcement = editTextAnnouncement.getText().toString();
                                    if (!announcement.isEmpty()) {
                                        saveAnnouncement(announcement);
                                    } else {
                                        Toast.makeText(getContext(), "Announcement cannot be empty", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", null);

                    builder.create().show();
                } else {
                    // Show an error message if the user is not the club owner
                    Toast.makeText(getContext(), "You must be the club owner to create an announcement", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to check club owner", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadAnnouncements() {
        // Firebase reference to the specific club's announcements
        DatabaseReference clubRef = FirebaseDatabase.getInstance().getReference("clubs").child(clubID).child("announcements");
        clubRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null) {
                    // The fragment is not attached; skip processing
                    return;
                }
                layout.removeAllViews(); // Clear previous announcements

                // Create a list to hold the announcements in reverse order
                List<Announcement> announcements = new ArrayList<>();

                // Iterate through each announcement in Firebase and add to the list
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String announcementId = childSnapshot.getKey(); // Get the announcement ID
                    Announcement announcement = childSnapshot.getValue(Announcement.class);
                    if (announcement != null) {
                        announcement.id = announcementId; // Set the announcement ID
                        announcements.add(announcement);
                    }
                }

                // Reverse the list of announcements to display latest first
                Collections.reverse(announcements);

                // Add each announcement to the layout in reverse order
                for (Announcement announcement : announcements) {
                    addAnnouncementToLayout(announcement);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load announcements", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void addAnnouncementToLayout(Announcement announcement) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View announcementView = inflater.inflate(R.layout.announcement_item, layout, false);
        overflowMenu = announcementView.findViewById(R.id.overflowMenuAnnoucement);

        // Set the announcement ID as a tag so it can be retrieved later
        announcementView.setTag(announcement.id);

        overflowMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOverflowMenu(v, announcement.id);
            }
        });

        // Bind data to the custom layout
        TextView usernameTextView = announcementView.findViewById(R.id.membername);
        TextView dateTimeTextView = announcementView.findViewById(R.id.memberusername);
        TextView announcementTextView = announcementView.findViewById(R.id.announcementText);

        // Set data from the announcement object
        usernameTextView.setText(announcement.username);
        dateTimeTextView.setText(announcement.date + " " + announcement.time);
        announcementTextView.setText(announcement.text);
        announcementView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String announcementId = (String) v.getTag();
                navigateToAnnouncementDetails(announcement);
            }
        });

        layout.addView(announcementView);
    }
    private void navigateToAnnouncementDetails(Announcement announcement) {
        Intent i = new Intent(getContext(),annoucementdetails.class);
        i.putExtra("announcement", announcement);
        startActivity(i);
    }
    private void shrinkFab() {
        if (!isExpanded) return; // Avoid running animations when already collapsed.

        transparentBg.startAnimation(toBottomBgAnim);
        mainFabBtn.startAnimation(rotateAntiClockWiseFabAnim);
        annoucementbutton.startAnimation(toBottomFabAnim);
        eventbutton.startAnimation(toBottomFabAnim);
        annoucementxt.startAnimation(toBottomFabAnim);
        eventtxt.startAnimation(toBottomFabAnim);

        annoucementbutton.setVisibility(View.INVISIBLE);
        eventbutton.setVisibility(View.INVISIBLE);
        annoucementxt.setVisibility(View.INVISIBLE);
        eventtxt.setVisibility(View.INVISIBLE);

        transparentBg.setVisibility(View.GONE);
        transparentBg.setClickable(false);

        isExpanded = false;
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
    private void expandFab() {
        if (isExpanded) return; // Avoid running animations when already expanded.

        transparentBg.startAnimation(fromBottomBgAnim);
        mainFabBtn.startAnimation(rotateClockWiseFabAnim);
        annoucementbutton.startAnimation(fromBottomFabAnim);
        eventbutton.startAnimation(fromBottomFabAnim);
        annoucementxt.startAnimation(fromBottomFabAnim);
        eventtxt.startAnimation(fromBottomFabAnim);

        annoucementbutton.setVisibility(View.VISIBLE);
        eventbutton.setVisibility(View.VISIBLE);
        annoucementxt.setVisibility(View.VISIBLE);
        eventtxt.setVisibility(View.VISIBLE);

        transparentBg.setVisibility(View.VISIBLE);
        transparentBg.setClickable(true);

        isExpanded = true;
    }


    public static class Announcement  implements Serializable {
        public String id;
        public String text;
        public String date;
        public String time;
        public String username;
        public String name;


        public Announcement() {
        }

        public Announcement(String id, String text, String date, String time, String username,String name) {
            this.id = id;
            this.text = text;
            this.date = date;
            this.time = time;
            this.username = username;
            this.name=name;
        }

        @Override
        public String toString() {
            return username + " (" + date + " " + time + "): " + text;
        }
    }
    public static class Announcement2 {

        public String text;
        public String date;
        public String time;
        public String username;
        public String name;

        public Announcement2() { }

        public Announcement2(String text, String date, String time, String username,String name) {
            this.text = text;
            this.date = date;
            this.time = time;
            this.username = username;
            this.name=name;
        }

        @Override
        public String toString() {
            return username + " (" + date + " " + time + "): " + text;
        }
    }



    private void saveAnnouncement(String text) {
        // Get current date and time
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        // Fetch username from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("CampusPulsePrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "DefaultUser");
        String name = sharedPreferences.getString("name", "DefaultName");
        // Create announcement object
        Announcement2 announcement = new Announcement2(text, date, time, username,name);

        // Save announcement in Firebase under the specific club
        DatabaseReference clubRef = FirebaseDatabase.getInstance().getReference("clubs").child(clubID).child("announcements");
        clubRef.push().setValue(announcement).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Announcement published", Toast.LENGTH_SHORT).show();
                Announcement announcement2= new Announcement();
                announcement2.date=announcement.date;
                announcement2.time=announcement.time;
                announcement2.name=announcement.name;
                announcement2.username=announcement.username;
                announcement2.text=announcement.text;
                notifyClubMembers(announcement2);
            } else {
                Toast.makeText(getContext(), "Failed to publish announcement", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showOverflowMenu(View view, String announcementId) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_overflow_annoucement, popupMenu.getMenu()); // Inflate the menu

        popupMenu.show();

        // Set item click listener
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.delete_annoucement) {
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("CampusPulsePrefs", Context.MODE_PRIVATE);

                String currentUsername = sharedPreferences.getString("username", ""); // Assuming username is stored

                // Firebase reference to the club's owners list
                DatabaseReference clubRef = FirebaseDatabase.getInstance().getReference("clubs").child(clubID).child("clubOwners");
                // Call delete method with the selected announcement ID
                clubRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!isAdded() || getContext() == null) {
                            // The fragment is not attached; skip processing
                            return;
                        }
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
                             deleteAnnouncement(announcementId);
                        } else {
                            // Show an error message if the user is not the club owner
                            Toast.makeText(getContext(), "You must be the club owner to delete announcement", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to check club owner", Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            }
            return false;
        });
    }
    private void deleteAnnouncement(String announcementId) {
        if (announcementId != null) {
            DatabaseReference clubRef = FirebaseDatabase.getInstance().getReference("clubs").child(clubID).child("announcements").child(announcementId);
            clubRef.removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Announcement deleted successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete announcement", Toast.LENGTH_SHORT).show());
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.page_1);
    }


    private void notifyClubMembers(Announcement announcement) {
        // Get the reference to the 'clubs' node in Firebase
        DatabaseReference clubsRef =  FirebaseDatabase.getInstance().getReference("clubs");

        // Query the 'members' for the specific club by using the clubId
        clubsRef.child(clubID).child("members").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Loop through the member userIds in the club's member list
                for (DataSnapshot memberSnapshot : task.getResult().getChildren()) {
                    String userId = memberSnapshot.getKey();  // Assuming the userId is stored as a string in the members list
                    if (userId != null) {
                        // Create a notification for the user
                        clubdetails.Notification notification = new  clubdetails.Notification("annoucement", announcement);

                        // Save the notification to the user's notifications node
                        DatabaseReference userNotificationsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("notifications");
                        userNotificationsRef.push().setValue(notification)
                                .addOnCompleteListener(notificationTask -> {
                                    if (!notificationTask.isSuccessful()) {
                                        // Handle notification failure (optional)
                                        Toast.makeText(getContext(), "Failed to send notification to " + userId, Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            } else {
                // Handle failure in retrieving members
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to retrieve club members!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    class Notification {
        public String type;
        public Announcement announcement;
        public Notification(String type,Announcement announcement) {
            this.type=type;
            this.announcement = announcement;
        }
    }
}
