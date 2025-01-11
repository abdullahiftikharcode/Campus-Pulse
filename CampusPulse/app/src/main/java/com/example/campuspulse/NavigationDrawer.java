package com.example.campuspulse;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

public class NavigationDrawer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    private static final String PREFS_NAME = "CampusPulsePrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        updateNavHeader();
    }

    private void updateNavHeader() {
        SharedPreferences prefs = getSharedPreferences("CampusPulsePrefs", Context.MODE_PRIVATE);
        String savedName = prefs.getString("username", "FULL NAME");
        String savedEmail = prefs.getString("email", "some@example.com");

        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.nav_username);
        TextView navEmail = headerView.findViewById(R.id.nav_email);

        navUsername.setText(savedName);
        navEmail.setText(savedEmail);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.d("NavigationDrawer", "Menu item selected: " + item.getItemId());
        if (item.getItemId() == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_home);
        } else if (item.getItemId() == R.id.nav_profile) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_profile);
        } else if (item.getItemId() == R.id.nav_notifications) {
            saveLastSelectedItem(navigationView.getCheckedItem().getItemId());
            Intent intent = new Intent(this, NotificationActivity.class);
            startActivity(intent);

        } else if (item.getItemId() == R.id.nav_about) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AboutFragment())
                    .commit();
        }
        else if (item.getItemId() == R.id.nav_chatbot) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ChatbotFragment())
                    .commit();
        }
        else if (item.getItemId() == R.id.nav_logout) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            clearStoredCredentials(prefs);
            Toast.makeText(this, "Logout!", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(NavigationDrawer.this, LoginActivity.class);
            startActivity(i);
            finish();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    private void clearStoredCredentials(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_PASSWORD);
        editor.apply();
    }
    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
    private void saveLastSelectedItem(int itemId) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("last_selected_item", itemId);
        editor.apply();
    }
    private void restoreLastSelectedItem() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int lastSelectedItemId = prefs.getInt("last_selected_item", R.id.nav_home);

        // Clear the saved item to prevent reusing it multiple times
        prefs.edit().remove("last_selected_item").apply();

        // Simulate navigation item selection
        MenuItem item = navigationView.getMenu().findItem(lastSelectedItemId);
        if (item != null) {
            onNavigationItemSelected(item);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        restoreLastSelectedItem();
    }
}
