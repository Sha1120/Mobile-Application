package lk.shashi.myadmin;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import adapters.CategoryAdapter;
import adapters.MovieAdapter;
import model.Category;
import model.Movie;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateCategory extends AppCompatActivity {

    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView categoryImageView;
    private Uri selectedImageUri;
    private String imagePath;
    Handler mainHandler = new Handler(Looper.getMainLooper());
    private TextInputEditText categoryNameEditText;
    private Button saveCategoryButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_category);

        // Initialize views
        categoryImageView = findViewById(R.id.imgGlide);
        categoryNameEditText = findViewById(R.id.categoryName);
        saveCategoryButton = findViewById(R.id.button2);


        recyclerView = findViewById(R.id.recyclerViewCategories);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2); // 2 card view
        recyclerView.setLayoutManager(gridLayoutManager);

        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(this,categoryList);
        recyclerView.setAdapter(categoryAdapter);

        fetchCategory();


        // Check permissions before opening the gallery
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }

        // Open gallery to select an image
        findViewById(R.id.button8).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        // Save category
        saveCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadCategory();
            }
        });
    }

    private void fetchCategory() {
        categoryAdapter.fetchCategory();

    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                categoryImageView.setImageBitmap(bitmap);
                imagePath = saveImageToInternalStorage(bitmap, "category_" + System.currentTimeMillis());
                Log.d("ImagePath", "Image saved to: " + imagePath);
            } catch (IOException e) {
                e.printStackTrace();
               // Toast.makeText(this, "Image load failed!", Toast.LENGTH_SHORT).show();
                showWarningDialog("Image load failed!", "Please try again ");
            }
        }
    }


    public String saveImageToInternalStorage(Bitmap bitmap, String imageName) {
        File directory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Images");

        // Create the directory if it does not exist
        if (!directory.exists()) {
            directory.mkdir();
        }

        File file = new File(directory, imageName + ".jpg");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return file.getAbsolutePath();
    }

    private void uploadCategory() {
        String categoryName = categoryNameEditText.getText() != null ? categoryNameEditText.getText().toString().trim() : "";

        if (categoryName.isEmpty()) {
           // Toast.makeText(this, "Enter Category Name", Toast.LENGTH_SHORT).show();
            showWarningDialog("Required", "Enter Category Name");
            return;
        }

        if (imagePath == null || imagePath.isEmpty()) {
            //Toast.makeText(this, "Select an Image", Toast.LENGTH_SHORT).show();
            showWarningDialog("Required", "Select an Image");
            return;
        }

        postImagePathToServer(categoryName, imagePath);
    }

    private void postImagePathToServer(String categoryName, String imagePath) {

        if (imagePath == null || imagePath.isEmpty()) {
            Toast.makeText(this, "No image file selected", Toast.LENGTH_SHORT).show();
            showWarningDialog("Warning", "No image file selected");
            return;
        }

        OkHttpClient okHttpClient = new OkHttpClient();
        Gson gson = new Gson();

        // Initialize Handler to post on the main thread
        Handler mainHandler = new Handler(Looper.getMainLooper());

        // Prepare the multipart request
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        builder.addFormDataPart("category_name", categoryName);

        // Adding the image file to the request
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            Toast.makeText(this, "Image file does not exist", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
        builder.addFormDataPart("image", imageFile.getName(), imageBody);

        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(Config.baseUrl+"/CreateCategory")  // Replace with your actual URL
                .post(requestBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> Toast.makeText(CreateCategory.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body().string();
                Log.e("homehttp", "Response: " + responseBody);

                JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);

                if (responseJson.has("success") && responseJson.get("success").getAsBoolean()) {
                    if (!isFinishing()) {
                        runOnUiThread(() -> {
                            //Toast.makeText(CreateCategory.this, "Category uploaded successfully!", Toast.LENGTH_SHORT).show();
                            showSuccessDialog("Success", "Category uploaded successfully!");
                            categoryNameEditText.setText("");
                            ImageView movieImage = findViewById(R.id.imgGlide);
                            movieImage.setImageResource(R.drawable.upload_img);  // Reset the image
                        });
                    }
                } else {
                    runOnUiThread(() ->
                            //Toast.makeText(CreateCategory.this, "Category upload failed!", Toast.LENGTH_SHORT).show()
                            showWarningDialog("Something went wrong", "Category upload failed!")
                    );
                }
            }
        });
    }

    private void showSuccessDialog(String title, String message) {
        // Fix: Check if Activity is finishing before showing Dialog
        if (isFinishing()) return;

        runOnUiThread(() -> {
            AlertDialog dialog = new AlertDialog.Builder(CreateCategory.this)
                    .setIcon(R.drawable.correct)
                    .setTitle(title)
                    .setMessage(message)
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

        runOnUiThread(() -> new AlertDialog.Builder(CreateCategory.this)
                .setIcon(R.drawable.warrning)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show());
    }
}
