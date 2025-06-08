package com.example.myuser;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserRegistration extends AppCompatActivity {

    private Button loginUser, registerUser;
    private TextInputEditText firstName, lastName, email, phone, password, confirmPassword;
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_registration);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        loginUser = findViewById(R.id.login);
        registerUser = findViewById(R.id.button5);

        firstName = findViewById(R.id.firstname);
        lastName = findViewById(R.id.lastname);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.mobile);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.coformpassword);

        loginUser.setOnClickListener(v -> {
            Intent i = new Intent(UserRegistration.this, UserLogin.class);
            startActivity(i);
        });

        registerUser.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String fName = firstName.getText().toString().trim();
        String lName = lastName.getText().toString().trim();
        String userEmail = email.getText().toString().trim();
        String userPhone = phone.getText().toString().trim();
        String userPassword = password.getText().toString().trim();
        String userConfirmPassword = confirmPassword.getText().toString().trim();

        if (fName.isEmpty() || lName.isEmpty() || userEmail.isEmpty() || userPhone.isEmpty() || userPassword.isEmpty() || userConfirmPassword.isEmpty()) {
            showAlertDialog("All fields are required.");
            return;
        }

        if (!userPassword.equals(userConfirmPassword)) {
            showAlertDialog("Passwords do not match.");
            return;
        }

        if (!isValidEmail(userEmail)) {
            showAlertDialog("Invalid email format.");
            return;
        }

        if (!isValidPhone(userPhone)) {
            showAlertDialog("Invalid phone number format.");
            return;
        }

        if (!isValidPassword(userPassword)) {
            showAlertDialog("Password must be at least 8 characters long and include a mix of upper and lowercase letters, numbers, and special characters.");
            return;
        }

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("first_name", fName)
                .addFormDataPart("last_name", lName)
                .addFormDataPart("email", userEmail)
                .addFormDataPart("phone", userPhone)
                .addFormDataPart("password", userPassword)
                .build();

        Request request = new Request.Builder()
                .url(Config.baseUrl + "/UserRegistration")
                .post(requestBody)
                .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : "";

                    if (responseBody.contains("\"success\":true")) {
                        runOnUiThread(() -> showAlertDialogsuccess("Registration Successful"));
                    } else if (responseBody.contains("Email is already registered.")) {
                        runOnUiThread(() -> showAlertDialog("Already added user"));
                    } else {
                        runOnUiThread(() -> showAlertDialog("Registration Failed: " + responseBody));
                    }
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    if (errorBody.contains("Email is already registered.")) {
                        runOnUiThread(() -> showAlertDialog("Already added user"));
                    } else {
                        runOnUiThread(() -> showAlertDialog("Registration Failed: " + errorBody));
                    }
                }
            } catch (IOException e) {
                runOnUiThread(() -> showAlertDialog("Something went wrong. Please try again later."));
            } catch (Exception e) {
                runOnUiThread(() -> showAlertDialog("An unexpected error occurred. Please contact support."));
            }
        }).start();
    }


    // Helper Methods for Validation
    private boolean isValidEmail(String email) {
        return email.matches("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("^07[012345678]{1}[0-9]{7}$");
    }

    private boolean isValidPassword(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).{8,}$");
    }

    // AlertDialog Method for validation errors
    private void showAlertDialog(String message) {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.warrning)
                .setTitle("Validation Error")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // AlertDialog Method for success
    // AlertDialog Method for success
    private void showAlertDialogsuccess(String message) {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.correct)
                .setTitle("Registration Success")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    // After user clicks "OK", navigate to the login page
                    Intent intent = new Intent(UserRegistration.this, UserLogin.class);
                    startActivity(intent);
                    finish(); // Optional: Close the registration activity
                })
                .show();
    }

}
