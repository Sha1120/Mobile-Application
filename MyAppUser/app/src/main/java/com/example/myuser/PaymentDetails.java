package com.example.myuser;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.Item;
import lk.payhere.androidsdk.model.StatusResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PaymentDetails extends AppCompatActivity {
    private TextInputEditText userFirstNameTextView, userLastNameTextView, userEmailTextView, userMobileTextView, selectDate, movieTitleTextView, selectedSeatsTextView,moviePriceView;
    private TextView totalView;
    private Button time1, time2, time3, PaymentBook ,BookedMovie;
    private int Total = 0;
    private int selectedYear, selectedMonth, selectedDay;
    private boolean isTimeSelected = false;
    private Button selectedTimeButton = null;
    private int seatCount,MovieId,CinemaId,userId;
    private float totalPrice;
    private String userEmail,firstName,lastName,mobile;
    private UserSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Save user data in SharedPreferences
        sessionManager = new UserSessionManager(this); // Pass 'this' as context

        // Get stored user details
        userEmail = sessionManager.getUserEmail();
        firstName = sessionManager.getFirstName();
        lastName = sessionManager.getLastName();
        mobile = sessionManager.getMobile();
        userId = sessionManager.getUserID();

        // Retrieve the selected seats from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("selected_seats", Context.MODE_PRIVATE);
        Set<String> selectedSeats = preferences.getStringSet("seats", new HashSet<>());

        if (selectedSeats != null && !selectedSeats.isEmpty()) {
            // Count of selected seats
            seatCount = selectedSeats.size();
            Log.d("homehttp", "Total Seats Selected: " + seatCount);

            // Process the selected seats
            for (String seat : selectedSeats) {
                // Split the seat data into Seat ID and Seat Number
                String[] seatDetails = seat.split(":");
                String seatId = seatDetails[0];  // Seat ID
                String seatNumber = seatDetails[1];  // Seat Number

                // Log or use the seat data
                Log.d("homehttp", "Seat ID: " + seatId + ", Seat Number: " + seatNumber);
            }

        } else {
            // Handle case when no seats are selected
            Toast.makeText(this, "No seats selected", Toast.LENGTH_SHORT).show();
        }

        //get booking data
        SharedPreferences bookpreferences = getSharedPreferences("booking_prefs", Context.MODE_PRIVATE);
        String title = bookpreferences.getString("title", "");
        String price = bookpreferences.getString("price", "");
        String movieid = bookpreferences.getString("movieid", "");
        String cinemaid = bookpreferences.getString("cinemaid", "");

        // Debug logs
        Log.d("bookhttp", "moviename: " + title);
        Log.d("bookhttp", "price: " + price);
        Log.d("bookhttp", "movieid: " + movieid);
        Log.d("bookhttp", "cinemaid: " + cinemaid);

        if (price != null) {
            try {
                // Convert movie price to float or double (for handling decimal values)
                float mPrice = Float.parseFloat(price);

                // Multiply by seat count
                totalPrice = mPrice * seatCount;
                Log.d("homehttp", "total :" + totalPrice);

            } catch (NumberFormatException e) {
                // Handle error if the movie price is not a valid number
                Log.e(TAG, "Invalid movie price format", e);
                Toast.makeText(this, "Invalid movie price format", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle case where movie price is not passed in the intent
            Toast.makeText(this, "Movie price not available", Toast.LENGTH_SHORT).show();
        }

        userFirstNameTextView = findViewById(R.id.fname);
        userLastNameTextView = findViewById(R.id.lname);
        userEmailTextView = findViewById(R.id.email);
        userMobileTextView = findViewById(R.id.mobile);
        selectDate = findViewById(R.id.date);
        moviePriceView = findViewById(R.id.moviePrice);

        // Set up the DatePickerDialog when the user clicks on the TextInputEditText
        selectDate.setOnClickListener(view -> showDatePickerDialog());
        movieTitleTextView = findViewById(R.id.movie);
        totalView = findViewById(R.id.total);
        totalView.setText(String.valueOf(totalPrice));
        selectedSeatsTextView = findViewById(R.id.tickets);

        time1 = findViewById(R.id.button2);
        time2 = findViewById(R.id.button3);
        time3 = findViewById(R.id.button4);

        if (title != null) {
            movieTitleTextView.setText(title);
        }

        if (price != null) {
            moviePriceView.setText(price);

        }
        selectedSeatsTextView.setText(String.valueOf(seatCount));



        // Set click listeners for time buttons
        time1.setOnClickListener(view -> selectTimeSlot(time1));
        time2.setOnClickListener(view -> selectTimeSlot(time2));
        time3.setOnClickListener(view -> selectTimeSlot(time3));

        // Display user details in the UI
        userFirstNameTextView.setText(firstName);
        userLastNameTextView.setText(lastName);
        userMobileTextView.setText(mobile);
        userEmailTextView.setText(userEmail);

        //book
        BookedMovie = findViewById(R.id.payment);
        BookedMovie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isFieldsValid() && isTimeSelected){

                    // save data from database
                    bookedSeats();
                    confirmBooking();
                   // showSuccessDialog();
                }else {
                    if (!isTimeSelected) {
                        showWarningDialog("Time Slot Not Selected", "Please select a time slot before proceeding.");
                    } else {
                        showWarningDialog("Incomplete Fields", "Please fill all the required fields before proceeding.");
                    }
                }
            }
        });

        //payment
        PaymentBook = findViewById(R.id.paymentnow);
        PaymentBook.setOnClickListener(v -> {

                InitRequest req = new InitRequest();

                req.setMerchantSecret("MTM0NjY4NDI3NTI4OTc4MTkxNDYxNzM3NTg0NjkxMTk0OTE2Nzc1Ng==");
                req.setMerchantId("1223795");       // Merchant ID
                req.setCurrency("LKR");             // Currency code LKR/USD/GBP/EUR/AUD
                req.setAmount(totalPrice);             // Final Amount to be charged

                req.setOrderId("230000123");        // Unique Reference ID
                req.setItemsDescription(title);  // Item description title
                req.setCustom1("This is the custom message 1");
                req.setCustom2("This is the custom message 2");
                req.getCustomer().setFirstName(firstName);
                req.getCustomer().setLastName(lastName);
                req.getCustomer().setEmail(userEmail);
                req.getCustomer().setPhone(mobile);
                req.getCustomer().getAddress().setAddress("No.1, Galle Road");
                req.getCustomer().getAddress().setCity("Colombo");
                req.getCustomer().getAddress().setCountry("Sri Lanka");
                Log.d("homehttp","username"+firstName);

//Optional Params
                //req.setNotifyUrl(“xxxx”);           // Notifiy Url
                req.getCustomer().getDeliveryAddress().setAddress("No.2, Kandy Road");
                req.getCustomer().getDeliveryAddress().setCity("Kadawatha");
                req.getCustomer().getDeliveryAddress().setCountry("Sri Lanka");
                req.getItems().add(new Item(null, title, seatCount, totalPrice));

                Intent intent1 = new Intent(this, PHMainActivity.class);
                intent1.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
                PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
                startActivityForResult(intent1, 1111); //unique request ID e.g. "11001"

        });

    }

    // Method to check if all required fields are filled
    private boolean isFieldsValid() {
        // Check if user details and movie details are filled
        return !userFirstNameTextView.getText().toString().isEmpty() &&
                !userLastNameTextView.getText().toString().isEmpty() &&
                !userEmailTextView.getText().toString().isEmpty() &&
                !userMobileTextView.getText().toString().isEmpty() &&
                !moviePriceView.getText().toString().isEmpty() &&
                !selectDate.getText().toString().isEmpty() &&
                totalPrice > 0; // Check if totalPrice is greater than 0
    }


    // Method to show the DatePickerDialog
    private void showDatePickerDialog() {
        // Get current date
        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog and set a listener
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                PaymentDetails.this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                dateSetListener,
                selectedYear, selectedMonth, selectedDay);

        datePickerDialog.show();
    }

    // Listener for the DatePickerDialog to handle date selection
    private final DatePickerDialog.OnDateSetListener dateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
        // Month is 0-indexed, so add 1 to the month
        String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
        selectDate.setText(selectedDate);  // Set the selected date in the TextInputEditText
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1111 && data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);

            if (resultCode == Activity.RESULT_OK) {
                String msg;
                if (response != null) {
                    if (response.isSuccess()) {
                        msg = "Activity result: " + response.getData().toString();
                        // Show success dialog
                        savePaymentsForConfirmedBookings();
                    } else {
                        msg = "Result: " + response.toString();
                        // Show warning dialog for unsuccessful payment
                        showWarningDialog("Payment Unsuccessful", "There was an issue with your payment. Please try again.");
                    }
                } else {
                    msg = "Result: no response";
                    showWarningDialog("Payment Failed", "No response from payment gateway. Please try again.");
                }
                Log.d(TAG, msg);
                //Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            } else if (resultCode == Activity.RESULT_CANCELED) {
//                if (response != null) {
//                    //Toast.makeText(this, response.toString(), Toast.LENGTH_LONG).show();
//                } else {
//                    //Toast.makeText(this, "User canceled the request", Toast.LENGTH_LONG).show();
//                }
                // Show warning dialog for canceled payment
                showWarningDialog("Payment Canceled", "You have canceled the payment. Please try again if needed.");
            }
        }
    }

    private void showWarningDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.warrning) // You can use a custom warning icon here if you prefer
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss(); // Close the dialog and allow the user to proceed
                })
                .setCancelable(false) // Prevent closing by tapping outside
                .show();
    }


    // Method to show the success dialog
    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.correct) // Use your custom success icon (e.g., a checkmark or check circle)
                .setTitle("Booking Successful")
                .setMessage("Your booking was successful.")
                .setPositiveButton("OK", (dialog, which) -> {
                })
                .setCancelable(false) // Prevent closing by tapping outside
                .setOnDismissListener(dialog -> {
                    // Optional: Add any action upon closing the dialog, like animations
                })
                .show()
                .getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.primaryColor)); // Change the button color to match your theme
    }

    private void showSuccessDialog2() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.correct) // Use your custom success icon (e.g., a checkmark or check circle)
                .setTitle("Payment Successful")
                .setMessage("Your payment was successful.")
                .setPositiveButton("OK", (dialog, which) -> {
                    updateBookingStatusesForConfirmedPayments();
                })
                .setCancelable(false) // Prevent closing by tapping outside
                .setOnDismissListener(dialog -> {
                    // Optional: Add any action upon closing the dialog, like animations
                })
                .show()
                .getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.primaryColor)); // Change the button color to match your theme
    }

    // Method to handle time slot selection
    private void selectTimeSlot(Button selectedButton) {
        // If a time slot is already selected, reset the color of the previous button
        if (selectedTimeButton != null) {
            resetButtonColor(selectedTimeButton);
        }

        // Update the selected button color
        selectedTimeButton = selectedButton;
        selectedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.purple_200)); // Highlight selected button

        // Mark time as selected
        isTimeSelected = true;
    }

    // Method to reset the color of a button
    private void resetButtonColor(Button button) {
        button.setBackgroundColor(ContextCompat.getColor(this, R.color.black)); // Reset to original color
    }

    private void bookedSeats() {

        SharedPreferences preferences = getSharedPreferences("selected_seats", Context.MODE_PRIVATE);
        Set<String> selectedSeats = preferences.getStringSet("seats", new HashSet<>());

        if (selectedSeats != null && !selectedSeats.isEmpty()) {

            StringBuilder seatIds = new StringBuilder();
            for (String seat : selectedSeats) {
                String[] seatDetails = seat.split(":");
                String seatId = seatDetails[0];  // Seat ID
                seatIds.append(seatId).append(",");
            }

            // Remove the trailing comma
            if (seatIds.length() > 0) {
                seatIds.setLength(seatIds.length() - 1);  // Remove the last comma
            }

            // Make the GET request with OkHttp
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(Config.baseUrl+"/UpdateSeats?seatIds="+seatIds)
                    .build();

            // Asynchronous request to update seat status in the database
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    // Handle failure (network issues, etc.)
                    runOnUiThread(() ->
                            //Toast.makeText(PaymentDetails.this, "Failed to update seat status", Toast.LENGTH_SHORT).show()
                            showWarningDialog("Failed to update seat status", "Please try again later")
                    );
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        // Successful response - process if needed
                        runOnUiThread(() -> {
                           // Toast.makeText(PaymentDetails.this, "Seats booked successfully", Toast.LENGTH_SHORT).show();
                           // showSuccessDialog();
                        });
                    } else {
                        // Error response from the server
                        runOnUiThread(() ->
                                //Toast.makeText(PaymentDetails.this, "Error booking seats", Toast.LENGTH_SHORT).show()
                                showWarningDialog("Error booking seats", "Your Internet is not stable")
                        );

                    }
                }
            });
        } else {
            // Handle case where no seats were selected
           // Toast.makeText(this, "No seats selected", Toast.LENGTH_SHORT).show();
            showWarningDialog("Something went wrong", "No seats selected");
        }
    }

    private void confirmBooking() {
        SharedPreferences preferences = getSharedPreferences("selected_seats", Context.MODE_PRIVATE);
        Set<String> selectedSeats = preferences.getStringSet("seats", new HashSet<>());

        if (selectedSeats != null && !selectedSeats.isEmpty()) {
            StringBuilder seatIds = new StringBuilder();
            for (String seat : selectedSeats) {
                String[] seatDetails = seat.split(":");
                if (seatDetails.length > 0) {
                    String seatId = seatDetails[0];  // Seat ID
                    seatIds.append(seatId).append(",");
                }
            }

            // Remove the trailing comma
            if (seatIds.length() > 0) {
                seatIds.setLength(seatIds.length() - 1);
            }

            SharedPreferences bookpreferences = getSharedPreferences("booking_prefs", Context.MODE_PRIVATE);
            String title = bookpreferences.getString("title", "");
            String price = bookpreferences.getString("price", "");
            String movieid = bookpreferences.getString("movieid", "");
            String cinemaid = bookpreferences.getString("cinemaid", "");

            String userId = String.valueOf(sessionManager.getUserID());

            if (movieid.isEmpty() || cinemaid.isEmpty()) {
               // Toast.makeText(this, "Invalid movie or cinema details", Toast.LENGTH_SHORT).show();
                showWarningDialog("Invalid movie or cinema details","Please check again");
                return;
            }

            String SeatIds = seatIds.toString();
            String timeSlot = selectedTimeButton.getText().toString();
            String bookedDate = selectDate.getText().toString();

            if (timeSlot.isEmpty() || bookedDate.isEmpty()) {
                //Toast.makeText(this, "Time or date is missing", Toast.LENGTH_SHORT).show();
                showWarningDialog("Time or date is missing","Please check again");
                return;
            }

            OkHttpClient client = new OkHttpClient();
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            builder.addFormDataPart("user_id", userId);
            builder.addFormDataPart("movie_id", movieid);
            builder.addFormDataPart("cinema_id", cinemaid);
            builder.addFormDataPart("seat_ids", SeatIds);
            builder.addFormDataPart("time_slot", timeSlot);
            builder.addFormDataPart("booked_date", bookedDate);

            RequestBody requestBody = builder.build();
            Request request = new Request.Builder()
                    .url(Config.baseUrl+"/ConfirmBooking")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                       // Toast.makeText(PaymentDetails.this, "Failed to confirm booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d("homehttp","Failed to confirm booking: " + e.getMessage());
                        showWarningDialog("Failed to confirm booking",e.getMessage());
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        try {
                            JSONObject jsonResponse = new JSONObject(responseData);
                            boolean success = jsonResponse.getBoolean("success");

                            if (success) {
                                JSONArray bookingIds = jsonResponse.getJSONArray("booking_ids");
                                StringBuilder bookingIdString = new StringBuilder();
                                for (int i = 0; i < bookingIds.length(); i++) {
                                    bookingIdString.append(bookingIds.getInt(i)).append(", ");
                                }
                                if (bookingIdString.length() > 0) {
                                    bookingIdString.setLength(bookingIdString.length() - 2);
                                }

                                // Save booking IDs to SharedPreferences
                                SharedPreferences preferences = getSharedPreferences("booking_prefs", Context.MODE_PRIVATE);
                                preferences.edit().putString("booking_ids", bookingIdString.toString()).apply();

                                String message = "Booking confirmed! Booking IDs: " + bookingIdString.toString();
                                runOnUiThread(() -> {
                                    //Toast.makeText(PaymentDetails.this, message, Toast.LENGTH_LONG).show();
                                    Log.d("homehttp", message);
                                    showSuccessDialog();
                                });
                            } else {
                                String errorMessage = jsonResponse.optString("message", "Error confirming booking");
                                runOnUiThread(() -> {
                                    //Toast.makeText(PaymentDetails.this, errorMessage, Toast.LENGTH_SHORT).show();
                                    showWarningDialog("Error confirming booking",errorMessage);
                                });
                            }
                        } catch (JSONException e) {
                            runOnUiThread(() -> {
                               // Toast.makeText(PaymentDetails.this, "Response parsing error", Toast.LENGTH_SHORT).show();
                                showWarningDialog("Error confirming booking","Response parsing error");
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            //Toast.makeText(PaymentDetails.this, "Error confirming booking: " + response.message(), Toast.LENGTH_SHORT).show();
                            showWarningDialog("Error confirming booking", response.message());
                        });
                    }
                }
            });
        } else {
            //Toast.makeText(this, "No seats selected", Toast.LENGTH_SHORT).show();
            showWarningDialog("No seats selected","Please select seat");
        }
    }

    private void savePaymentsForConfirmedBookings() {
        SharedPreferences preferences = getSharedPreferences("booking_prefs", Context.MODE_PRIVATE);
        String bookingIdsStr = preferences.getString("booking_ids", ""); // Get saved booking IDs

        if (!bookingIdsStr.isEmpty()) {
            String[] bookingIdsArray = bookingIdsStr.split(", ");
            int userId = sessionManager.getUserID();
            double ticketPrice = Double.parseDouble(preferences.getString("price", "0"));
            int qty = bookingIdsArray.length;

            // Calculate total amount (ticketPrice * qty)
            double totalAmount = ticketPrice * qty;

            for (String bookIdStr : bookingIdsArray) {
                int bookId = Integer.parseInt(bookIdStr);
                savePayment(userId, bookId, totalAmount, qty);
            }
        } else {
            runOnUiThread(() ->
                    //Toast.makeText(this, "No confirmed booking IDs found!", Toast.LENGTH_SHORT).show()
                    Log.d("homehttp","No confirmed booking IDs found!")
            );
        }
    }

    private void savePayment(int userId, int bookId, double totalAmount, int qty) {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("userId", String.valueOf(userId))
                .addFormDataPart("bookingId", String.valueOf(bookId))
                .addFormDataPart("price", String.valueOf(totalAmount))
                .addFormDataPart("qty", String.valueOf(qty))
                .build();

        Request request = new Request.Builder()
                .url(Config.baseUrl + "/SavePayment")
                .post(requestBody)
                .build();
        Log.d("homehttp","data send" );
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                       // Toast.makeText(PaymentDetails.this, "Payment data sending failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        showWarningDialog("Payment data sending failed",e.getMessage())
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() ->
                            //Toast.makeText(PaymentDetails.this, "Payment data successfully saved.", Toast.LENGTH_SHORT).show()
                            showSuccessDialog2()

                    );
                } else {
                    runOnUiThread(() ->
                            //Toast.makeText(PaymentDetails.this, "Payment failed: " + response.message(), Toast.LENGTH_SHORT).show()
                            showWarningDialog("Payment failed",response.message())
                    );
                }
            }

        });
    }

    private void updateBookingStatusesForConfirmedPayments() {
        SharedPreferences preferences = getSharedPreferences("booking_prefs", Context.MODE_PRIVATE);
        String bookingIdsStr = preferences.getString("booking_ids", ""); // Get saved booking IDs

        if (!bookingIdsStr.isEmpty()) {
            String[] bookingIdsArray = bookingIdsStr.split(", ");

            Log.d("homehttp", "updateBookingStatus");
            for (String bookIdStr : bookingIdsArray) {
                int bookId = Integer.parseInt(bookIdStr);
                Log.d("homehttp", "updateBookingStatus"+bookId);
                updateBookingStatus(bookId);
                bookedSeatStatusUpdated();
            }
        } else {
            runOnUiThread(() ->
                    //Toast.makeText(this, "No confirmed booking IDs found!", Toast.LENGTH_SHORT).show()
                    Log.d("homehttp","No confirmed booking IDs found!")
            );
        }
    }

    private void updateBookingStatus(int bookId) {


        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        try {
            json.put("bookingId", bookId);
            json.put("status", 1);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("homehttp", "Request Body",e);
        }
        RequestBody requestBody = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        // Log the request body and other info
        Log.d("homehttp", "Request Body: bookingId=" + bookId + ", status=1");

        // Creating the request
        Request request = new Request.Builder()
                .url(Config.baseUrl+"/UpdateBookingStatus")
                .post(requestBody)
                .build();

        Log.d("homehttp", "Sending request to: " + Config.baseUrl + "/UpdateBookingStatus");

        // Making the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("homehttp", "Failed to update booking status: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Parse the response body
                    String responseBody = response.body().string();
                    Log.d("homehttp", "Booking status updated to 1 (Paid). Response: " + responseBody);

                    // Optional: Handle the response further based on success/failure
                    if (responseBody.contains("\"success\": true")) {
                        Log.d("homehttp", "Status updated successfully!");
                       // updateSeatStatus(bookId);

                    } else {
                        Log.e("homehttp", "Error updating status: " + responseBody);
                    }
                } else {
                    Log.e("homehttp", "Failed to update booking status: " + response.message());
                }
            }
        });
    }

    private void bookedSeatStatusUpdated(){
        SharedPreferences preferences = getSharedPreferences("selected_seats", Context.MODE_PRIVATE);
        Set<String> selectedSeats = preferences.getStringSet("seats", new HashSet<>());

        if (selectedSeats != null && !selectedSeats.isEmpty()) {

            StringBuilder seatIds = new StringBuilder();
            for (String seat : selectedSeats) {
                String[] seatDetails = seat.split(":");
                String seatId = seatDetails[0];  // Seat ID
                seatIds.append(seatId).append(",");
            }

            // Remove the trailing comma
            if (seatIds.length() > 0) {
                seatIds.setLength(seatIds.length() - 1);  // Remove the last comma
            }

            // Make the GET request with OkHttp
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(Config.baseUrl+"/updateSeatStatus?seatIds="+seatIds)
                    .build();

            // Asynchronous request to update seat status in the database
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    // Handle failure (network issues, etc.)
                    runOnUiThread(() ->
                            //Toast.makeText(PaymentDetails.this, "Failed to update seat status", Toast.LENGTH_SHORT).show()
                            showWarningDialog("Failed to update seat status", "Please try again later")
                    );
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        // Successful response - process if needed
                        runOnUiThread(() -> {
                            // Toast.makeText(PaymentDetails.this, "Seats booked successfully", Toast.LENGTH_SHORT).show();
                            // showSuccessDialog();

                            Intent intent = new Intent(PaymentDetails.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        });
                    } else {
                        // Error response from the server
                        runOnUiThread(() ->
                                //Toast.makeText(PaymentDetails.this, "Error booking seats", Toast.LENGTH_SHORT).show()
                                showWarningDialog("Error booking seats", "Your Internet is not stable")
                        );

                    }
                }
            });
        } else {
            // Handle case where no seats were selected
            // Toast.makeText(this, "No seats selected", Toast.LENGTH_SHORT).show();
            showWarningDialog("Something went wrong", "No seats selected");
        }
    }


}
