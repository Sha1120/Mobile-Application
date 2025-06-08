package com.example.myuser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserLogin extends AppCompatActivity {
    private Button userLogin,userRegistred;
    private static final String TAG = "UserLoging";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        // Show Fingerprint Authentication Dialog
        FingerprintAuth.showBiometricPrompt(this, new FingerprintAuth.AuthCallback() {
            @Override
            public void onAuthenticationSuccess() {
                Toast.makeText(UserLogin.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                // Redirect to Home Screen
                startActivity(new Intent(UserLogin.this, MainActivity.class));
                finish();
            }

            @Override
            public void onAuthenticationFailed() {
                Toast.makeText(UserLogin.this, "Authentication Failed!", Toast.LENGTH_SHORT).show();
            }
        });

        userRegistred = findViewById(R.id.register);
        userRegistred.setOnClickListener(v -> {
            Intent i = new Intent(UserLogin.this, UserRegistration.class);
            startActivity(i);
        });

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userJson = sharedPreferences.getString("userJson", null);

        // If the user is already logged in, skip login screen
        if (userJson != null) {
            Intent intent = new Intent(UserLogin.this, MainActivity.class);
            intent.putExtra("userJson", userJson);
            startActivity(intent);
            finish();
        }

        userLogin = findViewById(R.id.loginbtn);
        userLogin.setOnClickListener(v -> {
            TextInputEditText emailField = findViewById(R.id.useremail);
            TextInputEditText passwordField = findViewById(R.id.userpassword);

            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(UserLogin.this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            userLogin.setText("Loading...");
            userLogin.setEnabled(false);

            new Thread(() -> {
                Gson gson = new Gson();
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("email", email);
                jsonObject.addProperty("password", password);

                OkHttpClient okHttpClient = new OkHttpClient();
                RequestBody requestBody = RequestBody.create(gson.toJson(jsonObject), MediaType.get("application/json"));
                Request request = new Request.Builder()
                        .url(Config.baseUrl + "/UserLogin") // Make sure Config.baseUrl is defined
                        .post(requestBody)
                        .build();

                try {
                    Response response = okHttpClient.newCall(request).execute();

                    if (!response.isSuccessful() || response.body() == null) {
                        Log.e(TAG, "homehttp: " + response);
                        runOnUiThread(() -> {
                            Toast.makeText(UserLogin.this, "Server error. Try again later.", Toast.LENGTH_SHORT).show();
                            userLogin.setText("Login");
                            userLogin.setEnabled(true);
                        });
                        return;
                    }

                    String responseText = response.body().string();
                    Log.d(TAG, "homehttp: " + responseText);

                    JsonObject responseJsonObject = gson.fromJson(responseText, JsonObject.class);
                    if (responseJsonObject.get("success").getAsBoolean()) {
                        if (responseJsonObject.has("user") && !responseJsonObject.get("user").isJsonNull()) {
                            JsonObject userObject = responseJsonObject.getAsJsonObject("user");

                            // Extract user details
                            String emailValue = userObject.has("email") ? userObject.get("email").getAsString() : "";
                            String fname = userObject.has("fname") ? userObject.get("fname").getAsString() : "";
                            String lname = userObject.has("lname") ? userObject.get("lname").getAsString() : "";
                            String mobile = userObject.has("mobile") ? userObject.get("mobile").getAsString() : "";
                            String userpassword = userObject.has("password")?userObject.get("password").getAsString():"";
                            int userid = userObject.has("id") ? userObject.get("id").getAsInt() : 0;

                            // Save the email to SharedPreferences
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("userEmail", emailValue);  // Save email here
                            editor.putString("firstName",fname);//fname
                            editor.putString("lastName", lname);  // lname
                            editor.putString("mobile",mobile);//mobile
                            editor.putInt("userId", userid);  // Save userId as int
                            editor.putString("password",userpassword);
                            editor.putString("userJson", gson.toJson(userObject));
                            editor.apply();

                            Intent i = new Intent(UserLogin.this, MainActivity.class);
                            i.putExtra("email", emailValue);  // Passing email to Dashboard
                            i.putExtra("firstName", fname);  // Passing fname
                            i.putExtra("lastName", lname);   // Passing lname
                            i.putExtra("mobile", mobile);    // Passing mobile
                            i.putExtra("password",userpassword); //passing password
                            i.putExtra("userId",userid);
                            startActivity(i);
                            finish();

                            runOnUiThread(() -> {
                                startActivity(i);
                                finish();
                            });
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(UserLogin.this, "Invalid User Data", Toast.LENGTH_LONG).show();
                                userLogin.setText("Login");
                                userLogin.setEnabled(true);
                            });
                        }
                    } else {
                        String message = responseJsonObject.has("message") ? responseJsonObject.get("message").getAsString() : "Invalid Details";
                        runOnUiThread(() -> {
                            Toast.makeText(UserLogin.this, message, Toast.LENGTH_LONG).show();
                            userLogin.setText("Login");
                            userLogin.setEnabled(true);
                        });
                    }

                } catch (IOException e) {
                    Log.e(TAG, "homehttp" + e.getMessage(), e);
                    runOnUiThread(() -> {
                        Toast.makeText(UserLogin.this, "Network error. Please try again.", Toast.LENGTH_LONG).show();
                        userLogin.setText("Login");
                        userLogin.setEnabled(true);
                    });
                }
            }).start();
        });
    }
}
