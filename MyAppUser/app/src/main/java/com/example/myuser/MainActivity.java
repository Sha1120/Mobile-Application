package com.example.myuser;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myuser.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import android.Manifest;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel: +94 111 102 222"));
                startActivity(intent);
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
        }

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

        String userEmail = sharedPreferences.getString("userEmail", "");
        int userId = sharedPreferences.getInt("userId", 0); // Default 0 if not found
        checkNotification(userId);
        // Set the email in the Navigation Drawer Header
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            TextView navUserEmail = headerView.findViewById(R.id.nav_user_email);
            if (navUserEmail != null) {
                navUserEmail.setText(userEmail);
            }
        }

       // int Id ;


        // Setup Navigation
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_privacy, R.id.nav_slideshow, R.id.nav_logout,R.id.nav_booking,R.id.nav_watchlist)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        if(getIntent().hasExtra("bookingdetails")){
            navigationView = binding.navView;
            navigationView.setCheckedItem(R.id.nav_booking);

            navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_booking);
        }
    }

    private void checkNotification(int userId) {

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("notification")
                .whereEqualTo("userId",userId).addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                       if(error == null && !value.isEmpty()){
                           Log.d("homehttp","msg"+value.getDocuments().get(0).getString("moviename"));

                           notifyUser(value.getDocuments().get(0));
                       }else{
                           if(error != null){
                               Log.d("homehttp","msg1"+error.getMessage());
                           }
                           Log.d("homehttp","msg");
                       }
                    }
                });
    }

    private void notifyUser(DocumentSnapshot notification) {
        NotificationManager notificationManager = ContextCompat.getSystemService(MainActivity.this, NotificationManager.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    "A2", "Chanel1", NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra("bookingdetails",true);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                MainActivity.this,
                1 ,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(
                R.drawable.notification,
                "Open Bookings",
                pendingIntent
        ).build();

        Notification notification1 = new NotificationCompat.Builder(MainActivity.this, "A2")
                .setContentTitle("Pending Payments Notifications")
                .setSmallIcon(R.drawable.notification)
                .setContentText("Your last day for payment is today. Otherwise, your booking will be cancelled.")
                .addAction(action)
                .build();


        notificationManager.notify(1,notification1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, perform necessary actions
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Update the current intent
        Uri data = intent.getData();
        if (data != null && data.getQueryParameter("userId") != null) {
            String userId = data.getQueryParameter("userId");
            checkUserLogin(userId);
        }
    }

    // Define the checkUserLogin method
    private void checkUserLogin(String userId) {
        // Logic to handle user login or retrieve user info
        if (userId != null && !userId.isEmpty()) {
            // If userId is valid, you can check if the user is logged in or show their profile
            // Example:
            Log.d("MainActivity", "User ID: " + userId);
            Toast.makeText(this, "User ID: " + userId, Toast.LENGTH_SHORT).show();
        } else {
            // Handle case when userId is not found
            Log.e("MainActivity", "Invalid or missing userId");
        }
    }
}
