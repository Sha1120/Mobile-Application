package com.example.myuser;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import adapters.BookingDetailsAdapter;
import model.Booking;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BookingDetails extends AppCompatActivity {
    private RecyclerView recyclerView;
    private BookingDetailsAdapter adapter;
    private List<Booking> bookingList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_booking_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize the RecyclerView and adapter
        recyclerView = findViewById(R.id.recyclerView);
        bookingList = new ArrayList<>();
        adapter = new BookingDetailsAdapter(this, bookingList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Fetch bookings for a specific user (for example, user ID 1)
        fetchBookingsForUser(1);

    }

    // Fetch bookings for a specific user
    public void fetchBookingsForUser(int userId) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(Config.baseUrl + "/LoadBookingDetails?user_id=" + userId)  // URL updated to match servlet endpoint
                    .build();

            try {
                Response response = client.newCall(request).execute();

                // Handle server error or empty response
                if (!response.isSuccessful() || response.body() == null) {
                    runOnUiThread(() -> showToast("Error fetching bookings from server"));
                    return;
                }

                String responseData = response.body().string();
                if (responseData.isEmpty()) {
                    runOnUiThread(() -> showToast("No bookings found."));
                    return;
                }

                // Parse JSON response
                JsonArray bookingsArray = JsonParser.parseString(responseData).getAsJsonArray();
                List<Booking> userBookings = new ArrayList<>();
                Gson gson = new Gson();

                for (JsonElement bookingElement : bookingsArray) {
                    try {
                        Booking booking = gson.fromJson(bookingElement, Booking.class);
                        userBookings.add(booking);
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                    }
                }

                // Update UI on Main Thread
                runOnUiThread(() -> {
                    if (!userBookings.isEmpty()) {
                        bookingList.clear();
                        bookingList.addAll(userBookings);
                        adapter.notifyDataSetChanged();
                    } else {
                        showToast("No bookings found.");
                    }
                });

            } catch (IOException | JsonSyntaxException e) {
                runOnUiThread(() -> showToast("Error fetching bookings"));
            }
        }).start();
    }

    // Show Toast on the UI thread
    private void showToast(String message) {
        Toast.makeText(BookingDetails.this, message, Toast.LENGTH_SHORT).show();
    }


}