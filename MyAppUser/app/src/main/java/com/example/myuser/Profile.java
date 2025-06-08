package com.example.myuser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Profile extends AppCompatActivity {

    private TextInputEditText firstName, lastName, mobile, email, password;
    private Button updateProfileButton;
    private ImageView profileImageView;
    private OkHttpClient client;
    private SharedPreferences sharedPreferences;
    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        client = new OkHttpClient();
        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);

        // Initialize Views
        firstName = findViewById(R.id.fname);
        lastName = findViewById(R.id.lname);
        mobile = findViewById(R.id.mobile);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        updateProfileButton = findViewById(R.id.button5);
        profileImageView = findViewById(R.id.imageView2);  // ImageView for profile picture

        // Initialize data from SharedPreferences
        String first = sharedPreferences.getString("firstName", "");
        String last = sharedPreferences.getString("lastName", "");
        String phone = sharedPreferences.getString("mobile", "");
        String userEmail = sharedPreferences.getString("email", "");
        String userPass = sharedPreferences.getString("password", "");

        // Set data to UI fields
        firstName.setText(first);
        lastName.setText(last);
        mobile.setText(phone);
        email.setText(userEmail);
        password.setText(userPass);

        // Load saved profile image from SharedPreferences
        String encodedImage = sharedPreferences.getString("profileImage", null);
        if (encodedImage != null) {
            byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap decodedImage = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            profileImageView.setImageBitmap(decodedImage);
        }

        // Update profile button click listener
        updateProfileButton.setOnClickListener(v -> updateUserProfile());

        // Set click listener for ImageView to open gallery
        profileImageView.setOnClickListener(v -> openGallery());
    }

    // Convert Bitmap image to Base64 string
    private String encodeImageToBase64(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    // Open gallery to select image
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE);
    }

    // Handle the result from the gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                profileImageView.setImageBitmap(selectedImage);  // Set the image to the ImageView

                // Convert image to Base64
                String encodedImage = encodeImageToBase64(selectedImage);

                // Save to SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("profileImage", encodedImage);  // Save encoded image string
                editor.apply();

                Toast.makeText(this, "Image saved!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateUserProfile() {
        // Retrieve the updated data from the TextInputEditText fields
        String updatedFirstName = firstName.getText().toString().trim();
        String updatedLastName = lastName.getText().toString().trim();
        String updatedMobile = mobile.getText().toString().trim();
        String updatedEmail = email.getText().toString().trim();
        String updatedPassword = password.getText().toString().trim();

        // Check if any fields are empty
        if (TextUtils.isEmpty(updatedFirstName) || TextUtils.isEmpty(updatedLastName) ||
                TextUtils.isEmpty(updatedMobile) || TextUtils.isEmpty(updatedEmail) ||
                TextUtils.isEmpty(updatedPassword)) {
            // Show a message if any field is empty
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save the updated data locally in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("firstName", updatedFirstName);
        editor.putString("lastName", updatedLastName);
        editor.putString("mobile", updatedMobile);
        editor.putString("email", updatedEmail);
        editor.putString("password", updatedPassword);
        editor.apply();

        // You can also update the profile image here if needed
        // (e.g., if you want to upload the image to your server)

        // Optionally: Send the updated data to the server (API request)
        JSONObject json = new JSONObject();
        try {
            json.put("firstName", updatedFirstName);
            json.put("lastName", updatedLastName);
            json.put("mobile", updatedMobile);
            json.put("email", updatedEmail);
            json.put("password", updatedPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Send update request to backend (API call)
        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(Config.baseUrl+"/UpdateProfile")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(Profile.this, "Failed to update!", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(Profile.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(Profile.this, "Update Failed!", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

}
