package com.example.myuser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import adapters.SeatAdapter;
import adapters.userMovieAdapter;
import model.BookedSeat;
import model.Movie;
import model.Seat;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BookTicket extends AppCompatActivity {

    private LinearProgressIndicator progressIndicator;
    private TextView stepTitle;
    private Button nextButton, confromBtn;
    private RecyclerView seatRecyclerView;
    private SeatAdapter seatAdapter;
    private List<BookedSeat> bookedSeatList;
    Handler mainHandler = new Handler(Looper.getMainLooper());

    private int movieId, cinemaId;
    private Context context;
    private String title,price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_ticket);

        progressIndicator = findViewById(R.id.progressIndicator);
        stepTitle = findViewById(R.id.stepTitle);
        confromBtn = findViewById(R.id.confrom);
        seatRecyclerView = findViewById(R.id.recycler_seats);
        nextButton = findViewById(R.id.nextButton);

        context = this;

        // In BookTicket Activity:
        Intent intent = getIntent();
        if (intent != null) {
            title = intent.getStringExtra("title");
            price = intent.getStringExtra("price");
            movieId = intent.getIntExtra("movieid", -1);  // Default value -1 if not found
            cinemaId = intent.getIntExtra("cinemaid", -1);  // Default value -1 if not found

            Log.d("homehttp", "Movie ID: " + movieId);
            Log.d("homehttp", "Cinema ID: " + cinemaId);
            Log.d("homehttp", " price: " + price);
        }

        // Initialize seats (This should be dynamic in real app)
        bookedSeatList = new ArrayList<>();

        // Setup RecyclerView
        seatRecyclerView.setLayoutManager(new GridLayoutManager(this, 5)); // 5 seats per row
        seatAdapter = new SeatAdapter(this, bookedSeatList);
        seatRecyclerView.setAdapter(seatAdapter);
        seatAdapter.fetchSeates();


        confromBtn.setOnClickListener(view -> {
            cancelBooking();
            // Retrieve the selected seats from SharedPreferences
            SharedPreferences preferences = getSharedPreferences("selected_seats", Context.MODE_PRIVATE);
            Set<String> selectedSeats = preferences.getStringSet("seats", new HashSet<>());

            if (selectedSeats != null && !selectedSeats.isEmpty()) {
                // Handle the selected seats (e.g., show them in a summary or process them)
                StringBuilder selectedSeatsSummary = new StringBuilder();

                for (String seat : selectedSeats) {
                    String[] seatDetails = seat.split(":");
                    String seatId = seatDetails[0];  // Seat ID
                    String seatNumber = seatDetails[1];  // Seat Number

                    selectedSeatsSummary.append("Seat ID: ").append(seatId)
                            .append(", Seat Number: ").append(seatNumber).append("\n");
                }

                // Show a toast or use the selected seats information as needed
                Log.d("homehttp", "Selected Seats:\n" + selectedSeatsSummary.toString());

                // Optionally, you can send this data to the server or proceed with the booking.
            } else {
                Toast.makeText(BookTicket.this, "No seats selected", Toast.LENGTH_SHORT).show();
            }
        });

        //next button
        nextButton.setOnClickListener(view -> {

            SharedPreferences preferences = getSharedPreferences("selected_seats", Context.MODE_PRIVATE);
            Set<String> selectedSeats = preferences.getStringSet("seats", new HashSet<>());

            if (selectedSeats.isEmpty()) {
                showWarningDialog();
            } else {
                proceedToBooking();
            }
        });

    }

    // Method to send the seat selection data to the server
    private void sendSeatSelectionToServer(int userId, int movieId, int cinemaId, String seats) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Gson gson = new Gson();

            // Prepare the multipart request
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            builder.addFormDataPart("userId", String.valueOf(userId));
            builder.addFormDataPart("movieId", String.valueOf(movieId));
            builder.addFormDataPart("cinema_id", String.valueOf(cinemaId));  // Fixed parameter name to match backend
            builder.addFormDataPart("seats", seats);

            RequestBody requestBody = builder.build();

            Request request = new Request.Builder()
                    .url(Config.baseUrl + "/BookSeats")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    mainHandler.post(() -> Toast.makeText(BookTicket.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseBody = response.body().string();
                    Log.e("homehttp", "Response: " + responseBody);

                    JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);

                    if (responseJson.has("success") && responseJson.get("success").getAsBoolean()) {
                        runOnUiThread(() -> {
                            Toast.makeText(BookTicket.this, "Seats confirmed successfully!", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(BookTicket.this, "Seats booking failed!", Toast.LENGTH_SHORT).show());
                    }
                }
            });

        }).start();
    }

    // Show a toast message on the UI thread
    private void showToast(String message) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            );
        }
    }

    // Method to show a warning dialog
    private void showWarningDialog() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.warrning)
                .setTitle("Warning")
                .setMessage("You need to select at least one seat before proceeding.")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();  // Dismiss the warning dialog
                    // After dismissing the warning dialog, show the cancel booking dialog
                    cancelBooking();
                })
                .setCancelable(false)
                .show();
    }

    // Method to proceed to the booking screen
    private void proceedToBooking() {
        Log.d("homehttp", " price: " + price);
        Intent intent = new Intent(BookTicket.this, PaymentDetails.class);
        intent.putExtra("movieID",movieId);
        intent.putExtra("cinemaId",cinemaId);
        intent.putExtra("movieTitle", title);
        intent.putExtra("moviePrice", price);
        startActivity(intent);
    }

    private void cancelBooking() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.comfirm)
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel the booking?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Clear all the selected seats data from SharedPreferences
                    SharedPreferences preferences = getSharedPreferences("selected_seats", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();  // Clear the selected seats data
                    editor.apply();

                    // Show a toast message confirming that the booking has been canceled
                    Toast.makeText(this, "Booking canceled.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())  // Dismiss the dialog if the user selects No
                .setCancelable(false)  // Prevent the dialog from being canceled by touching outside
                .show();
    }

}
