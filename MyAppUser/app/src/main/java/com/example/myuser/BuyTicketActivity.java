package com.example.myuser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

public class BuyTicketActivity extends AppCompatActivity {

    private TextView titleTextView, rateTextView, priceTextView, descriptionTextView;
    private ImageView movieImageView;
    private String title, rate, price, description, imageUrl;
    private int movieid,cinemaid;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_buy_ticket);

        // Apply insets for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        titleTextView = findViewById(R.id.titleView);
        rateTextView = findViewById(R.id.rateView);
        priceTextView = findViewById(R.id.priceView);
        descriptionTextView = findViewById(R.id.descriptionView);
        movieImageView = findViewById(R.id.movieView);

        // Get intent data
        Intent intent = getIntent();
        if (intent != null) {
            title = intent.getStringExtra("title");
            rate = intent.getStringExtra("rate");
            price = intent.getStringExtra("price");
            description = intent.getStringExtra("description");
            imageUrl = intent.getStringExtra("imageUrl");
            movieid = intent.getIntExtra("movieId",-1);
            cinemaid = intent.getIntExtra("cinemaId",-1);

            // Set data to views
            titleTextView.setText(title);
            rateTextView.setText(rate);
            priceTextView.setText(price);
            descriptionTextView.setText(description);

            // Load image using Glide
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.upload_img)
                    .error(R.drawable.baseline_running_with_errors_24)
                    .into(movieImageView);
        }

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences bookpreferences = getSharedPreferences("booking_prefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = bookpreferences.edit();

                editor.putString("title", title);
                editor.putString("price", price);
                editor.putString("movieid", String.valueOf(movieid));
                editor.putString("cinemaid", String.valueOf(cinemaid));
                editor.apply(); // Save changes asynchronously

                // Create intent to start BookTicket activity
                Intent i = new Intent(BuyTicketActivity.this, BookTicket.class);
                startActivity(i);

                // Log values for debugging
                Log.d("homehttp", "moviename: " + title);
                Log.d("homehttp", "price: " + price);
                Log.d("homehttp", "movieid: " + movieid);
                Log.d("homehttp", "cinemaid: " + cinemaid);

            }
        });
    }
}
