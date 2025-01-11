package com.example.campuspulse;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MembersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<Member> membersList;
    private String clubId;
    private Fragment fragment;
    private static final int TYPE_TITLE = 0;
    private static final int TYPE_MEMBER = 1;

    public MembersAdapter(Context context,List<Member> membersList, String clubId,Fragment fragment) {
        this.context = context;
        this.membersList = membersList != null ? membersList : new ArrayList<>();  // Ensure list is not null
        this.clubId = clubId;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_TITLE) {
            // title layout
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.title_divider, parent, false);
            return new TitleViewHolder(view);
        } else {
            // member layout
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member, parent, false);
            return new MembersViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Check if the current position is valid
        if (position < 0 || position >= membersList.size()) {
            return;
        }

        if (getItemViewType(position) == TYPE_TITLE) {
            TitleViewHolder titleViewHolder = (TitleViewHolder) holder;
            Member member = membersList.get(position);
            titleViewHolder.titleTextView.setText(member.getMemberName());
        } else {
            MembersViewHolder membersViewHolder = (MembersViewHolder) holder;
            Member member = membersList.get(position);

            // Bind data to the TextViews
            membersViewHolder.memberNameTextView.setText(member.getMemberName());
            membersViewHolder.usernameTextView.setText(member.getUsername());
            membersViewHolder.overflowMenuButton.setOnClickListener(view -> {
                // Create a PopupMenu
                PopupMenu popupMenu = new PopupMenu(view.getContext(), membersViewHolder.overflowMenuButton);
                popupMenu.inflate(R.menu.menu_overflow_member);
                // Set click listener for menu items
                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId()==R.id.make_admin){
                        handleMakeAdmin(member, clubId);
                        return true;
                    }
                    else if (item.getItemId()==R.id.remove_admin){
                        handleRemoveAdmin(member);
                        return true;
                    }
                    else if ( item.getItemId()==R.id.remove_member){
                        handleRemoveMember(member);
                        return true;
                    }
                    else {
                        return false;
                    }
                });
                popupMenu.show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return membersList.size();
    }

    @Override
    public int getItemViewType(int position) {
        // Check if the position corresponds to a divider
        if ("divider".equals(membersList.get(position).getUserId())) {
            return TYPE_TITLE;
        } else {
            return TYPE_MEMBER;
        }
    }
    private void handleMakeAdmin(Member member, String clubId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("CampusPulsePrefs", Context.MODE_PRIVATE);
        String currentUserId = sharedPreferences.getString("username", "No password");

        DatabaseReference clubRef = FirebaseDatabase.getInstance().getReference("clubs").child(clubId);

        // Fetch the club data
        clubRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(context, "Failed to fetch club data.", Toast.LENGTH_SHORT).show();
                return;
            }

            DataSnapshot clubSnapshot = task.getResult();
            if (clubSnapshot == null) {
                Toast.makeText(context, "Club data is missing.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the list of club owners
            List<String> clubOwners = (List<String>) clubSnapshot.child("clubOwners").getValue();
            if (clubOwners == null) {
                clubOwners = new ArrayList<>();
            }

            if (!clubOwners.contains(currentUserId)) {
                Toast.makeText(context, "Only a club owner can promote another member.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the selected member is already a club owner
            if (clubOwners.contains(member.getUsername())) {
                Toast.makeText(context, member.getUsername() + " is already a club owner.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Promote the member to club owner
            clubOwners.add(member.getUsername());
            clubRef.child("clubOwners").setValue(clubOwners)
                    .addOnSuccessListener(aVoid -> {
                        // Update the total number of owners
                        clubRef.child("totalOwners").get().addOnCompleteListener(countTask -> {
                            if (countTask.isSuccessful() && countTask.getResult().exists()) {
                                Long currentTotalOwners = countTask.getResult().getValue(Long.class);
                                if (currentTotalOwners == null) currentTotalOwners = 0L;
                                clubRef.child("totalOwners").setValue(currentTotalOwners + 1)
                                        .addOnSuccessListener(updateTask -> {
                                            Toast.makeText(context, "Promoted " + member.getUsername() + " to club owner. Total owners updated.", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(context, "Failed to update total owners: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                clubRef.child("totalOwners").setValue(1); // Initialize if not present
                                Toast.makeText(context, "Promoted " + member.getUsername() + " to club owner. Total owners initialized to 1.", Toast.LENGTH_SHORT).show();
                            }
                            notifyParentFragment();
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to promote member: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }



    private void handleRemoveAdmin(Member member) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("CampusPulsePrefs", Context.MODE_PRIVATE);
        String currentUserId = sharedPreferences.getString("username", "No password");

        DatabaseReference clubRef = FirebaseDatabase.getInstance().getReference("clubs").child(clubId);

        // Fetch the club data
        clubRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(context, "Failed to fetch club data.", Toast.LENGTH_SHORT).show();
                return;
            }

            DataSnapshot clubSnapshot = task.getResult();
            if (clubSnapshot == null) {
                Toast.makeText(context, "Club data is missing.", Toast.LENGTH_SHORT).show();
                return;
            }


            List<String> clubOwners = (List<String>) clubSnapshot.child("clubOwners").getValue();
            if (clubOwners == null) {
                clubOwners = new ArrayList<>();
            }

            // Check if the current user is a club owner
            if (!clubOwners.contains(currentUserId)) {
                Toast.makeText(context, "Only a club owner can remove another club owner.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the selected member is a club owner
            if (!clubOwners.contains(member.getUsername())) {
                Toast.makeText(context, member.getUsername() + " is not a club owner.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check the total number of owners
            List<String> finalClubOwners = clubOwners;
            clubRef.child("totalOwners").get().addOnCompleteListener(countTask -> {
                if (countTask.isSuccessful() && countTask.getResult().exists()) {
                    Long currentTotalOwners = countTask.getResult().getValue(Long.class);
                    if (currentTotalOwners != null && currentTotalOwners > 1) {
                        // Remove the member from club owners
                        finalClubOwners.remove(member.getUsername());
                        clubRef.child("clubOwners").setValue(finalClubOwners)
                                .addOnSuccessListener(aVoid -> {
                                    // Update the total number of owners
                                    clubRef.child("totalOwners").setValue(currentTotalOwners - 1)
                                            .addOnSuccessListener(updateTask -> {
                                                Toast.makeText(context, "Removed " + member.getUsername() + " as a club owner. Total owners updated.", Toast.LENGTH_SHORT).show();
                                                notifyParentFragment();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(context, "Failed to update total owners: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to remove member: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(context, "Cannot remove the last club owner.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Error fetching total owners count.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }



    private void handleRemoveMember(Member member) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("CampusPulsePrefs", Context.MODE_PRIVATE);
        String currentUserId = sharedPreferences.getString("username", "No username");

        DatabaseReference clubRef = FirebaseDatabase.getInstance().getReference("clubs").child(clubId);
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(member.getUserId());

        clubRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(context, "Failed to fetch club data.", Toast.LENGTH_SHORT).show();
                return;
            }

            DataSnapshot clubSnapshot = task.getResult();
            if (clubSnapshot == null) {
                Toast.makeText(context, "Club data is missing.", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> clubOwners = (List<String>) clubSnapshot.child("clubOwners").getValue();
            if (clubOwners == null) clubOwners = new ArrayList<>();

            // Ensure the current user is an owner
            if (!clubOwners.contains(currentUserId)) {
                Toast.makeText(context, "Only club owners can remove members.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ensure the owner does not remove themselves
            if (currentUserId.equals(member.getUsername())) {
                Toast.makeText(context, "You cannot remove yourself from the club.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isOwner = clubOwners.contains(member.getUsername());
            DatabaseReference memberRef = clubRef.child("members").child(member.getUserId());

            if (isOwner) {
                // Remove from clubOwners
                clubOwners.remove(member.getUsername());
                clubRef.child("clubOwners").setValue(clubOwners)
                        .addOnSuccessListener(aVoid -> {
                            // Decrement totalOwners
                            clubRef.child("totalOwners").get().addOnCompleteListener(countTask -> {
                                if (countTask.isSuccessful() && countTask.getResult().exists()) {
                                    Long currentTotalOwners = countTask.getResult().getValue(Long.class);
                                    clubRef.child("totalOwners").setValue(currentTotalOwners - 1);
                                }
                            });

                            // Remove from clubMembers
                            memberRef.removeValue().addOnSuccessListener(aVoid1 -> {
                                // Decrement totalMembers
                                clubRef.child("totalMembers").get().addOnCompleteListener(memberTask -> {
                                    if (memberTask.isSuccessful() && memberTask.getResult().exists()) {
                                        Long currentTotalMembers = memberTask.getResult().getValue(Long.class);
                                        clubRef.child("totalMembers").setValue(currentTotalMembers - 1);
                                    }
                                });

                                // Remove club from user's joinedClubs
                                userRef.child("joinedClubs").child(clubId).removeValue()
                                        .addOnSuccessListener(aVoid2 -> {
                                            Toast.makeText(context, "Removed " + member.getMemberName() + " from the club as an owner.", Toast.LENGTH_SHORT).show();
                                        });
                                notifyParentFragment();
                            });
                        });
            } else {
                // Remove from clubMembers
                memberRef.removeValue().addOnSuccessListener(aVoid -> {
                    // Decrement totalMembers
                    clubRef.child("totalMembers").get().addOnCompleteListener(memberTask -> {
                        if (memberTask.isSuccessful() && memberTask.getResult().exists()) {
                            Long currentTotalMembers = memberTask.getResult().getValue(Long.class);
                            clubRef.child("totalMembers").setValue(currentTotalMembers - 1);
                        }
                    });

                    // Remove club from user's joinedClubs
                    userRef.child("joinedClubs").child(clubId).removeValue()
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(context, "Removed " + member.getMemberName() + " from the club.", Toast.LENGTH_SHORT).show();
                            });
                });
            }
        });
    }

    static class MembersViewHolder extends RecyclerView.ViewHolder {
        TextView memberNameTextView, usernameTextView;
        ImageButton overflowMenuButton;
        public MembersViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize TextViews from your custom layout
            memberNameTextView = itemView.findViewById(R.id.membername);
            usernameTextView = itemView.findViewById(R.id.memberusername);
            overflowMenuButton = itemView.findViewById(R.id.overflowMenumember);
        }
    }

    static class TitleViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;

        public TitleViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize the title TextView
            titleTextView = itemView.findViewById(R.id.text_box);
        }
    }
    private void notifyParentFragment() {
        if (fragment instanceof members) {
            ((members) fragment).reloadFragment();
        }
    }
}

