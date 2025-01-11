package com.example.campuspulse;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private Context context;
    private List<DataClass> dataList;

    public MyAdapter(Context context, List<DataClass> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        DataClass data = dataList.get(position);

        holder.recImage.setImageResource(data.getDataImage());
        holder.recTitle.setText(data.getDataTitle());
        holder.recDesc.setText(data.getDataDesc());

        // Card click listener
        holder.recCard.setOnClickListener(v -> {
            if (context instanceof NavigationDrawer) {
                NavigationDrawer activity = (NavigationDrawer) context;


                clubdetails clubDetailsFragment = new clubdetails();

                // Prepare data to pass to the fragment
                Bundle bundle = new Bundle();
                bundle.putInt("Image", data.getDataImage());
                bundle.putString("Title", data.getDataTitle());
                bundle.putString("Desc", data.getDataDesc());
                bundle.putString("clubId", data.getClubId());
                clubDetailsFragment.setArguments(bundle);

                // Begin the fragment transaction
                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                // Replace the current fragment and add to backstack
                fragmentTransaction.replace(R.id.fragment_container, clubDetailsFragment); // Replace `R.id.fragment_container` with your actual container ID
                fragmentTransaction.addToBackStack(null); // Add this transaction to the backstack
                fragmentTransaction.commit();
            }
        });

        // Overflow menu setup
        holder.overflowMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.overflowMenu);
            popupMenu.inflate(R.menu.overflow_menu); // Inflate your menu resource file
            popupMenu.setOnMenuItemClickListener(item -> handleMenuItemClick(item, position));
            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    private boolean handleMenuItemClick(MenuItem item, int position) {
        DataClass data = dataList.get(position);
        if (item.getItemId()==R.id.leave_group){
            leave_society(data);
            return true;
        }
        else if (item.getItemId()==R.id.delete_group){
            delete_group(data);
            return true;
        }
        else {
            return false;
        }
    }
    private void delete_group(DataClass data) {
        // Get the userId from SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("CampusPulsePrefs", Context.MODE_PRIVATE);
        String currentUsername = sharedPreferences.getString("username", null); // Default is null if not found

        if (currentUsername == null) {
            Toast.makeText(context, "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String clubId = data.getClubId(); // Get the club ID
        DatabaseReference clubRef = FirebaseDatabase.getInstance().getReference("clubs").child(clubId);
        DatabaseReference membersRef = clubRef.child("members");
        DatabaseReference clubOwnersRef = clubRef.child("clubOwners");

        // Check if the current user is an owner of the club
        clubOwnersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isOwner = false;

                // Check if the current user is in the clubOwners list
                for (DataSnapshot ownerSnapshot : snapshot.getChildren()) {
                    String ownerUsername = ownerSnapshot.getValue(String.class);
                    if (currentUsername.equals(ownerUsername)) {
                        isOwner = true;
                        break;
                    }
                }

                if (!isOwner) {
                    Toast.makeText(context, "You must be an owner to delete this society.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Show confirmation dialog
                new androidx.appcompat.app.AlertDialog.Builder(context)
                        .setTitle("Delete Society")
                        .setMessage("Are you sure you want to delete this society? This action cannot be undone.")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Remove the society from joinedClubs of all members
                            membersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot membersSnapshot) {
                                    for (DataSnapshot member : membersSnapshot.getChildren()) {
                                        String memberId = member.getKey();
                                        DatabaseReference joinedClubsRef = FirebaseDatabase.getInstance()
                                                .getReference("users")
                                                .child(memberId)
                                                .child("joinedClubs");
                                        joinedClubsRef.child(clubId).removeValue();
                                    }

                                    // Delete the club from Firebase
                                    clubRef.removeValue().addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(context, "Society deleted successfully.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(context, "Failed to delete society.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(context, "Failed to remove society from members' joinedClubs.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to check club ownership.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void leave_society(DataClass data) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("CampusPulsePrefs", Context.MODE_PRIVATE);
        String currentUsername = sharedPreferences.getString("username", null); // Default is null if not found
        String userId = sharedPreferences.getString("userId", null); // Default is null if not found

        if ( currentUsername == null) {
            Toast.makeText(context, "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String clubId = data.getClubId();

        // Reference to the club's members and the club data
        DatabaseReference membersRef = FirebaseDatabase.getInstance().getReference("clubs").child(clubId).child("members");
        DatabaseReference clubDataRef = FirebaseDatabase.getInstance().getReference("clubs").child(clubId); // To update total members and owners
        DatabaseReference clubOwnersRef = FirebaseDatabase.getInstance().getReference("clubs").child(clubId).child("clubOwners"); // To check if user is owner
        DatabaseReference joinedClubsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("joinedClubs");

        // First, check if the user is an owner by checking if their username is in the clubOwners list
        clubOwnersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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

                // If the user is an owner, proceed to remove them and replace with another owner if necessary
                if (isOwner) {
                    // Decrement both totalOwners and totalMembers
                    clubDataRef.child("totalOwners").get().addOnCompleteListener(ownerCountTask -> {
                        if (ownerCountTask.isSuccessful()) {
                            int totalOwners = ownerCountTask.getResult().getValue(Integer.class);
                            clubDataRef.child("totalOwners").setValue(totalOwners - 1);
                        }
                    });

                    clubDataRef.child("totalMembers").get().addOnCompleteListener(memberCountTask -> {
                        if (memberCountTask.isSuccessful()) {
                            int totalMembers = memberCountTask.getResult().getValue(Integer.class);
                            clubDataRef.child("totalMembers").setValue(totalMembers - 1);
                        }
                    });

                    // Remove the current user from the club's owners list
                    clubOwnersRef.get().addOnCompleteListener(ownerTask -> {
                        if (ownerTask.isSuccessful()) {
                            // Remove the current owner's username from the list
                            for (DataSnapshot ownerSnapshot : ownerTask.getResult().getChildren()) {
                                String ownerUsername = ownerSnapshot.getValue(String.class);
                                if (currentUsername.equals(ownerUsername)) {
                                    clubOwnersRef.child(ownerSnapshot.getKey()).removeValue(); // Remove by key
                                    break;
                                }
                            }

                            // If no owners are left, promote a new owner from the members list (just the first member in the list)
                            if (ownerTask.getResult().getChildrenCount() == 1) {
                                membersRef.get().addOnCompleteListener(memberTask -> {
                                    if (memberTask.isSuccessful() && memberTask.getResult().getChildrenCount() > 0) {
                                        // Get the first member and promote them to owner (no sorting, just the first available member)
                                        DataSnapshot memberSnapshot = memberTask.getResult().getChildren().iterator().next();
                                        String newOwnerUsername = memberSnapshot.child("username").getValue(String.class);

                                       // Get the next available index in the clubOwners list
                                        clubOwnersRef.get().addOnCompleteListener(ownerListTask -> {
                                            if (ownerListTask.isSuccessful()) {
                                                int newIndex = (int) ownerListTask.getResult().getChildrenCount(); // Get the current size of the list

                                                // Set the new owner at the next available index
                                                clubOwnersRef.child(String.valueOf(newIndex)).setValue(newOwnerUsername).addOnCompleteListener(pushTask -> {
                                                    if (pushTask.isSuccessful()) {
                                                        // Successfully added the new owner
                                                        // Update totalOwners count
                                                        clubDataRef.child("totalOwners").get().addOnCompleteListener(updateOwnerCount -> {
                                                            if (updateOwnerCount.isSuccessful()) {
                                                                int updatedOwnerCount = updateOwnerCount.getResult().getValue(Integer.class);
                                                                clubDataRef.child("totalOwners").setValue(updatedOwnerCount + 1);
                                                            }
                                                        });
                                                    } else {
                                                        Toast.makeText(context, "Failed to add new owner.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else {
                                                Toast.makeText(context, "Failed to get club owners list.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                } else {
                    // User is just a member, so we only decrement totalMembers
                    clubDataRef.child("totalMembers").get().addOnCompleteListener(memberCountTask -> {
                        if (memberCountTask.isSuccessful()) {
                            int totalMembers = memberCountTask.getResult().getValue(Integer.class);
                            clubDataRef.child("totalMembers").setValue(totalMembers - 1);
                        }
                    });
                }

                // Remove user from the club's members
                membersRef.child(userId).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // If successful, remove the club from the user's joinedClubs
                        joinedClubsRef.child(clubId).removeValue().addOnCompleteListener(removeTask -> {
                            if (removeTask.isSuccessful()) {
                                Toast.makeText(context, "You have left the society.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Failed to update user data.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(context, "Failed to leave the society.", Toast.LENGTH_SHORT).show();
                    }
                });
            }


            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to check club ownership.", Toast.LENGTH_SHORT).show();
            }
        });
    }





    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView recImage, overflowMenu;
        TextView recTitle, recDesc;
        CardView recCard;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            recImage = itemView.findViewById(R.id.recImage);
            recTitle = itemView.findViewById(R.id.recTitle);
            recDesc = itemView.findViewById(R.id.recDesc);
            recCard = itemView.findViewById(R.id.recCard);
            overflowMenu = itemView.findViewById(R.id.overflowMenu);
        }
    }
}
