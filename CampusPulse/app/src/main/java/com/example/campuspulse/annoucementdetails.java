package com.example.campuspulse;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.divider.MaterialDivider;

public class annoucementdetails extends AppCompatActivity {
     Button b ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annoucementdetails);
        clubdetails.Announcement announcement = (clubdetails.Announcement) getIntent().getSerializableExtra("announcement");
        TextView memberName = findViewById(R.id.membername);
        TextView memberUsername = findViewById(R.id.memberusername);
        TextView timeDate = findViewById(R.id.time);
        TextView announcementText = findViewById(R.id.announcementText);
        ImageView imageView = findViewById(R.id.imageView2);
        b = findViewById(R.id.button_back);
        if (announcement != null) {
            memberName.setText(announcement.name);
            memberUsername.setText(announcement.username);
            String date = announcement.date;
            String time = announcement.time;
            timeDate.setText(date+" "+time);
            announcementText.setText(announcement.text);

            imageView.setImageResource(R.drawable.aklogo);
        }
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });
    }
}
