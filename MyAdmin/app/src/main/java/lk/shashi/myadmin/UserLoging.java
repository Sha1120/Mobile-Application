package lk.shashi.myadmin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class UserLoging extends AppCompatActivity {

    private Button userLogin, userRegistred;
    private static final String TAG = "UserLoging";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_loging);

        userRegistred = findViewById(R.id.register);
        userRegistred.setOnClickListener(v -> {
            Intent i = new Intent(UserLoging.this, UserRegistration.class);
            startActivity(i);
        });

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userJson = sharedPreferences.getString("userJson", null);

        if (userJson != null) {
            Intent intent = new Intent(UserLoging.this, Dashboard.class);
            intent.putExtra("userJson", userJson);
            startActivity(intent);
            finish(); // Skip login screen
        }

        userLogin = findViewById(R.id.loginbtn);
        userLogin.setOnClickListener(v -> {
            TextInputEditText textInputEditText1 = findViewById(R.id.useremail);
            TextInputEditText textInputEditText2 = findViewById(R.id.userpassword);

            String email = textInputEditText1.getText() != null ? textInputEditText1.getText().toString().trim() : "";
            String password = textInputEditText2.getText() != null ? textInputEditText2.getText().toString().trim() : "";

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(UserLoging.this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(UserLoging.this, "Server error. Try again later.", Toast.LENGTH_SHORT).show();
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

                            // Save the email to SharedPreferences
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("userEmail", emailValue);  // Save email here
                            editor.putString("userJson", gson.toJson(userObject));
                            editor.apply();

                            Intent i = new Intent(UserLoging.this, Dashboard.class);
                            i.putExtra("email", emailValue); // Passing email to Dashboard
                            runOnUiThread(() -> {
                                startActivity(i);
                                finish();
                            });
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(UserLoging.this, "Invalid User Data", Toast.LENGTH_LONG).show();
                                userLogin.setText("Login");
                                userLogin.setEnabled(true);
                            });
                        }
                    } else {
                        String message = responseJsonObject.has("message") ? responseJsonObject.get("message").getAsString() : "Invalid Details";
                        runOnUiThread(() -> {
                            Toast.makeText(UserLoging.this, message, Toast.LENGTH_LONG).show();
                            userLogin.setText("Login");
                            userLogin.setEnabled(true);
                        });
                    }

                } catch (IOException e) {
                    Log.e(TAG, "homehttp" + e.getMessage(), e);
                    runOnUiThread(() -> {
                        Toast.makeText(UserLoging.this, "Network error. Please try again.", Toast.LENGTH_LONG).show();
                        userLogin.setText("Login");
                        userLogin.setEnabled(true);
                    });
                }
            }).start();
        });
    }
}
