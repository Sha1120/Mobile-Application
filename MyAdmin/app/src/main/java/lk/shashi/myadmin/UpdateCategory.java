package lk.shashi.myadmin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UpdateCategory extends AppCompatActivity {
    private TextInputEditText categoryNameEditText;
    private ImageView categoryImageView;
    private CardView cardView;
    private static final int IMAGE_PICK_CODE = 1000;
    private Uri selectedImageUri;
    private String imagePath;
    private File selecteImageFile;
    private  Button updatebtn , deletebtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_category);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize the UI components
        categoryNameEditText = findViewById(R.id.textInputEditText);  // Make sure to use the correct ID from XML
        categoryImageView = findViewById(R.id.imgGlide);
        cardView = findViewById(R.id.cardView);

        // Get the data passed from the previous activity
        Intent intent = getIntent();
        String categoryId = intent.getStringExtra("id");
        String categoryName = intent.getStringExtra("name");
        String imagePath = intent.getStringExtra("imagePath");  // Receive the image path

        Log.d("homehttp", categoryName);  // Log the received name for debugging

        if (categoryId != null && categoryName != null && imagePath != null) {
            // Set the category name to the TextInputEditText
            categoryNameEditText.setText(categoryName);

            // Load the category image (if image path is passed)
            // You can use Glide or Picasso here to load the image from a URL or local file path
            Glide.with(this)
                    .load(imagePath)  // Assuming imagePath is a valid URL or file path
                    .into(categoryImageView);
        } else {
            // Handle case when data is missing (optional)
            Toast.makeText(this, "Category data is missing", Toast.LENGTH_SHORT).show();
        }


        // update category
        updatebtn = findViewById(R.id.update);
        updatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCategory();
            }
        });

        //delete category
        deletebtn = findViewById(R.id.deletetheater);
        deletebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCategory();
            }
        });

    }

    private void updateCategory() {
        String categoryName = categoryNameEditText.getText() != null ? categoryNameEditText.getText().toString().trim() : "";
        String categoryId = getIntent().getStringExtra("id"); // Retrieve the categoryId from the Intent

        if (categoryName.isEmpty()) {
            Toast.makeText(this, "Enter Category Name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (categoryId == null || categoryId.isEmpty()) {
            Toast.makeText(this, "Category ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        updateCategorytoServer(categoryId, categoryName, imagePath); // Pass categoryId as a parameter
    }

    private void updateCategorytoServer(String categoryId, String categoryName, String imagePath) {


        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();

        // Prepare the multipart request

        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        builder.addFormDataPart("id", categoryId);
        builder.addFormDataPart("name", categoryName);


        // Initialize Handler to post on the main thread
        Handler mainHandler = new Handler(Looper.getMainLooper());


        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(Config.baseUrl+"/UpdateCategory") // Replace with your API URL
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> Toast.makeText(UpdateCategory.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body().string();
                Log.e("homehttp", "Response: " + responseBody);

                JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);

                if (responseJson.has("success") && responseJson.get("success").getAsBoolean()) {
                    if (!isFinishing()) {
                        runOnUiThread(() -> {
                            Toast.makeText(UpdateCategory.this, "Category updated successfully!", Toast.LENGTH_SHORT).show();
                            categoryNameEditText.setText("");
                            ImageView movieImage = findViewById(R.id.imgGlide);
                            movieImage.setImageResource(R.drawable.upload_img);  // Reset the image
                        });
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(UpdateCategory.this, "Category updated failed!", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void deleteCategory() {
        String categoryId = getIntent().getStringExtra("id"); // Get category ID from intent

        if (categoryId == null || categoryId.isEmpty()) {
            Toast.makeText(this, "Category ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        // Call the method to delete the category from the server
        deleteCategoryFromServer(categoryId);
    }

    private void deleteCategoryFromServer(String categoryId) {
        Log.d("homehttp", "category Id : " + categoryId);

        OkHttpClient client = new OkHttpClient();

        // Build the GET request with the category ID as a query parameter
        Request request = new Request.Builder()
                .url(Config.baseUrl+"/DeleteCategory?id=" + categoryId)  // Your GET API URL
                .get()  // Use GET instead of DELETE since you're sending categoryId as a query parameter
                .build();

        // Send the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    // Display error message if request fails
                    Toast.makeText(UpdateCategory.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body().string();
                Log.e("homehttp", "Response: " + responseBody);

                try {
                    // Parse the response JSON
                    JSONObject responseJson = new JSONObject(responseBody);

                    // Check if the deletion was successful
                    if (responseJson.has("success") && responseJson.getBoolean("success")) {
                        // If category deleted successfully
                        runOnUiThread(() -> {
                            Toast.makeText(UpdateCategory.this, "Category deleted successfully!", Toast.LENGTH_SHORT).show();
                            finish();  // Close the activity after deletion
                        });
                    } else {
                        // If deletion failed
                        runOnUiThread(() -> {
                            Toast.makeText(UpdateCategory.this, "Failed to delete category.", Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (JSONException e) {
                    // Error parsing JSON response
                    runOnUiThread(() -> {
                        Toast.makeText(UpdateCategory.this, "Error parsing response.", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }




}