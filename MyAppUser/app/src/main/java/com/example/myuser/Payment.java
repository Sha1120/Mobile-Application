package com.example.myuser;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.myuser.ui.home.HomeFragment;

import org.json.JSONObject;

import java.io.IOException;

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

public class Payment extends AppCompatActivity {

    private EditText editTextNumber;
    private Button paymentButton;
    private int bookId, userId;
    private double amount;
    private int qty = 1;
    private  int confirmBookedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Fix: Disable Autofill
        getWindow().getDecorView().setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);

        editTextNumber = findViewById(R.id.amount);
        amount = getIntent().getDoubleExtra("amount", 0.0);
        bookId = getIntent().getIntExtra("bookingId", -1);
        editTextNumber.setText("LKR " + String.format("%.2f", amount));

        String title = getIntent().getStringExtra("movieTitle");
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String firstName = sharedPreferences.getString("firstName", "Unknown");
        String lastName = sharedPreferences.getString("lastName", "Unknown");
        String mobile = sharedPreferences.getString("mobile", "Unknown");
        String userEmail = sharedPreferences.getString("email", "Unknown");
        userId = sharedPreferences.getInt("userId", 0);
        Log.d("homehttp","userid" + userId);

        paymentButton = findViewById(R.id.button6);
        paymentButton.setOnClickListener(v -> {
            InitRequest req = new InitRequest();

            req.setMerchantSecret("MTM0NjY4NDI3NTI4OTc4MTkxNDYxNzM3NTg0NjkxMTk0OTE2Nzc1Ng==");
            req.setMerchantId("1223795");
            req.setCurrency("LKR");
            req.setAmount(amount);
            req.setOrderId("230000123");
            req.setItemsDescription(title);
            req.setCustom1("This is the custom message 1");
            req.setCustom2("This is the custom message 2");
            req.getCustomer().setFirstName(firstName);
            req.getCustomer().setLastName(lastName);
            req.getCustomer().setEmail(userEmail);
            req.getCustomer().setPhone(mobile);
            req.getCustomer().getAddress().setAddress("No.1, Galle Road");
            req.getCustomer().getAddress().setCity("Colombo");
            req.getCustomer().getAddress().setCountry("Sri Lanka");

            req.getCustomer().getDeliveryAddress().setAddress("No.2, Kandy Road");
            req.getCustomer().getDeliveryAddress().setCity("Kadawatha");
            req.getCustomer().getDeliveryAddress().setCountry("Sri Lanka");
            req.getItems().add(new Item(null, title, 1, amount));

            Intent intent1 = new Intent(Payment.this, PHMainActivity.class);
            intent1.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
            PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
            startActivityForResult(intent1, 1111);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1111 && data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);

            if (resultCode == Activity.RESULT_OK) {
                if (response != null && response.isSuccess()) {
                    showSuccessDialog();
                    savePayment(userId, bookId, amount, qty);
                } else {
                    showWarningDialog("Payment Unsuccessful", "There was an issue with your payment. Please try again.");
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                showWarningDialog("Payment Canceled", "You have canceled the payment. Please try again if needed.");
            }
        }
    }

    private void showSuccessDialog() {
        // Fix: Check if Activity is finishing before showing Dialog
        if (isFinishing()) return;

        runOnUiThread(() -> {
            AlertDialog dialog = new AlertDialog.Builder(Payment.this)
                    .setIcon(R.drawable.correct)
                    .setTitle("Payment Successful")
                    .setMessage("Your payment was successful.")
                    .setPositiveButton("OK", (dialogInterface, which) -> {
                    })
                    .setCancelable(false)
                    .show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(this, R.color.primaryColor));
        });
    }

    private void showWarningDialog(String title, String message) {
        // Fix: Check if Activity is finishing before showing Dialog
        if (isFinishing()) return;

        runOnUiThread(() -> new AlertDialog.Builder(Payment.this)
                .setIcon(R.drawable.warrning)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show());
    }

    private void savePayment(int userId, int bookId, double amount, int qty) {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("userId", String.valueOf(userId))
                .addFormDataPart("bookingId", String.valueOf(bookId))
                .addFormDataPart("price", String.valueOf(amount))
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
                Log.e("homehttp", "Payment data sending failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("homehttp", "Payment data successfully saved.");
                    updateBookingStatus(bookId);
                } else {
                    Log.e("homehttp", "Payment data saving failed: " + response.message());
                }
            }
        });
    }


    private void updateBookingStatus(int bookId) {


        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        try {
            json.put("bookingId", bookId);
            json.put("status", 1);
        } catch (Exception e) {
            e.printStackTrace();
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

                    } else {
                        Log.e("homehttp", "Error updating status: " + responseBody);
                    }
                } else {
                    Log.e("homehttp", "Failed to update booking status: " + response.message());
                }
            }
        });
    }





}
