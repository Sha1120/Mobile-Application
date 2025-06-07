package lk.shashi.myadmin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import lk.shashi.myadmin.databinding.ActivityDashboardBinding;

public class Dashboard extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarDashboard.toolbar);
        binding.appBarDashboard.fab.setOnClickListener(view -> {
            Intent i = new Intent(Dashboard.this, WatchList.class);
            startActivity(i);
        });

        // Get the email passed from the login activity or from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("userEmail", "No Email Found");
        Log.d("homehttp", "User Email: " + userEmail); // Log to check if it's correctly passed

        // Set the email in the Navigation Drawer Header
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        View headerView = navigationView.getHeaderView(0); // Retrieve the header view
        if (headerView != null) {
            TextView navUserEmail = headerView.findViewById(R.id.nav_user_email); // Get the TextView

            if (navUserEmail != null) {
                // Check if the email is not null or empty and set it to the TextView
                if (userEmail != null && !userEmail.isEmpty()) {
                    navUserEmail.setText(userEmail); // Set the email
                } else {
                    navUserEmail.setText("No Email Found"); // Fallback message
                }
            } else {
                Log.e("DashboardError", "nav_user_email TextView not found in header!");
            }
        } else {
            Log.e("DashboardError", "Header view is null!");
        }

        // Setting up the Navigation Drawer
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_dashboard);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_dashboard);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
