package lk.shashi.myadmin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.Category;
import model.Cinema;
import model.Language;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddMovie extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;  // Request code for image selection
    private static final int IMAGE_PICK_CODE = 1000;
    private ImageView movieImageView;
    AutoCompleteTextView autoCompleteTextView;
    OkHttpClient client = new OkHttpClient();
    Handler mainHandler = new Handler(Looper.getMainLooper());
    AutoCompleteTextView theaterAutoComplete;
    AutoCompleteTextView categoryAutoComplete;
    private TextInputEditText title, rating, description, price;
    private AutoCompleteTextView  theater,category;
    private Button createMovieButton;
    private Uri selectedImageUri;
    private String imagePath;

    private File selecteImageFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_movie);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        movieImageView = findViewById(R.id.imgGlide);

        //language
        autoCompleteTextView = findViewById(R.id.language);
        fetchLanguages();

        //theater
        theaterAutoComplete = findViewById(R.id.movietheater);
        fetchTheaters();

        //category
        categoryAutoComplete = findViewById(R.id.movieCategory);
        fetchCategories();

        title = findViewById(R.id.textInputLayout4).findViewById(R.id.title);
        rating = findViewById(R.id.textInputLayout5).findViewById(R.id.rate);
        description = findViewById(R.id.textInputLayout6).findViewById(R.id.description);
        price = findViewById(R.id.textInputLayout7).findViewById(R.id.price);
        //date = findViewById(R.id.textInputLayout8).findViewById(R.id.date);

        //language = findViewById(R.id.autoCompleteTextView);

        //theater = findViewById(R.id.autoCompleteTextView2);
        //category = findViewById(R.id.autoCompleteTextView3);
        createMovieButton = findViewById(R.id.button11);

        //add movie
        createMovieButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadMovie();
            }
        });



        // Set click listener to open gallery for image selection
        movieImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
    }

    //upload movie
    //upload movie
    private void uploadMovie() {

        String movieName = title.getText() != null ? title.getText().toString().trim() : "";
        String rate = rating.getText() != null ? rating.getText().toString().trim() : "";
        String descriptionMovie = description.getText() != null ? description.getText().toString().trim() : "";
        String priceTickets = (price.getText() != null && price.getText().toString().trim().length() > 0) ? price.getText().toString().trim() : "";

        if (movieName.isEmpty()) {
            Toast.makeText(this, "Enter Movie Name", Toast.LENGTH_SHORT).show();
            return;
        }


        if (rate.isEmpty()) {
            Toast.makeText(this, "Enter Movie Rate", Toast.LENGTH_SHORT).show();
            return;
        }

        if (descriptionMovie.isEmpty()) {
            Toast.makeText(this, "Enter Movie Description", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = 0.0;
        if (priceTickets.isEmpty()) {
            Toast.makeText(this, "Enter Movie Ticket Price", Toast.LENGTH_SHORT).show();
            return;
        } else {
            try {
                price = Double.parseDouble(priceTickets);  // Parse the string to double
            } catch (NumberFormatException e) {
                // If the string is not a valid double, handle the exception
                Toast.makeText(this, "Invalid price input", Toast.LENGTH_SHORT).show();
            }
        }

        if (selectedLanguageId == -1) { // Use stored language ID
            Toast.makeText(this, "Select Movie Language", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTheaterId == -1) {
            Toast.makeText(this, "Select Theater", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imagePath == null) {
            Toast.makeText(this, "Select a Movie Image", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCategoryId == -1) {
            Toast.makeText(this, "Select a Movie Category", Toast.LENGTH_SHORT).show();
            return;
        }

        postMovieToServer(movieName, rate, descriptionMovie, imagePath, price, selectedLanguageId, selectedTheaterId, selectedCategoryId);

    }

    //post movie details
    private void postMovieToServer(String movieName, String rate, String descriptionMovie, String imagePath, double price, int selectedLanguageId, int selectedTheaterId, int selectedCategoryId) {
        if (imagePath == null || imagePath.isEmpty()) {
            Toast.makeText(this, "No image file selected", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();

        // Prepare the multipart request
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        builder.addFormDataPart("title", movieName);
        builder.addFormDataPart("rate", rate);
        builder.addFormDataPart("description", descriptionMovie);
        builder.addFormDataPart("price", String.valueOf(price));
        builder.addFormDataPart("language_id", String.valueOf(selectedLanguageId));
        builder.addFormDataPart("cinema_id", String.valueOf(selectedTheaterId));
        builder.addFormDataPart("movie_category_id", String.valueOf(selectedCategoryId));

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
                .url(Config.baseUrl+"/AddMovie") // Update NGROK URL
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> Toast.makeText(AddMovie.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body().string();
                Log.e("homehttp", "Response: " + responseBody);

                JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);

                if (responseJson.has("success") && responseJson.get("success").getAsBoolean()) {
                    if (!isFinishing()) {
                        runOnUiThread(() -> {
                            Toast.makeText(AddMovie.this, "Movie uploaded successfully!", Toast.LENGTH_SHORT).show();
                            resetFeilds();
                        });
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(AddMovie.this, "Movie upload failed!", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private int selectedCategoryId = -1; // Store selected category ID globally
    //load category
    private void fetchCategories() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        RequestBody requestBody = RequestBody.create(gson.toJson(jsonObject), MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(Config.baseUrl+"/LoadMovieCategory")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() ->
                        Toast.makeText(AddMovie.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.body() == null) {
                    mainHandler.post(() ->
                            Toast.makeText(AddMovie.this, "Empty response from server", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                String responseData = response.body().string();
                Log.e("httphome", "Server Response: " + responseData);

                try {
                    JsonObject responseJson = gson.fromJson(responseData, JsonObject.class);
                    boolean success = responseJson.get("success").getAsBoolean();

                    if (!success) {
                        mainHandler.post(() ->
                                Toast.makeText(AddMovie.this, "Backend returned error", Toast.LENGTH_SHORT).show()
                        );
                        return;
                    }

                    JsonArray categoryArray = responseJson.getAsJsonArray("category");
                    List<String> categoryNames = new ArrayList<>();
                    List<Category> categoryList = new ArrayList<>();

                    for (int i = 0; i < categoryArray.size(); i++) {
                        JsonObject categoryObj = categoryArray.get(i).getAsJsonObject();
                        int id = categoryObj.get("id").getAsInt();
                        String categoryName = categoryObj.get("name").getAsString();


                        categoryNames.add(categoryName);
                        categoryList.add(new Category(id, categoryName));
                    }

                    mainHandler.post(() -> {
                        if (!categoryNames.isEmpty()) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    AddMovie.this,
                                    android.R.layout.simple_dropdown_item_1line,
                                    categoryNames
                            );
                            categoryAutoComplete.setAdapter(adapter);

                            // Handle item selection properly
                            categoryAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
                                String selectedCategoryName = (String) parent.getItemAtPosition(position);

                                // Find the selected category by name
                                for (Category category : categoryList) {
                                    if (category.getName().equals(selectedCategoryName)) {
                                        selectedCategoryId = category.getId(); // Store correct ID
                                        break;
                                    }
                                }

                                Toast.makeText(AddMovie.this, "Selected Category ID: " + selectedCategoryId, Toast.LENGTH_SHORT).show();
                            });

                        } else {
                            Toast.makeText(AddMovie.this, "No Categories found", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    mainHandler.post(() ->
                            Toast.makeText(AddMovie.this, "Error processing response", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }


    private int selectedTheaterId = -1; // Store selected theater ID globally
    //load Theaters
    private void fetchTheaters() {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        RequestBody requestBody = RequestBody.create(gson.toJson(jsonObject), MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(Config.baseUrl+"/LoadCinema")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> Toast.makeText(AddMovie.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseData = response.body().string();
                System.out.println("Response Data: " + responseData); // Debugging
                Gson gson = new Gson();

                try {
                    JsonObject responseJson = gson.fromJson(responseData, JsonObject.class);
                    boolean success = responseJson.get("success").getAsBoolean();

                    if (!success) {
                        mainHandler.post(() -> Toast.makeText(AddMovie.this, "Backend returned error", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    JsonArray cinemaArray = responseJson.getAsJsonArray("cinema");
                    List<String> cinemaNames = new ArrayList<>();
                    List<Cinema> cinemaList = new ArrayList<>();

                    for (int i = 0; i < cinemaArray.size(); i++) {
                        JsonObject cinemaObj = cinemaArray.get(i).getAsJsonObject();
                        int id = cinemaObj.get("id").getAsInt();
                        String cinemaName = cinemaObj.get("name").getAsString();
                        //String loacation = cinemaObj.get("location").getAsString();
                        cinemaNames.add(cinemaName);
                        cinemaList.add(new Cinema(id, cinemaName));
                    }

                    mainHandler.post(() -> {
                        if (!cinemaNames.isEmpty()) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(AddMovie.this, android.R.layout.simple_dropdown_item_1line, cinemaNames);
                            theaterAutoComplete.setAdapter(adapter);

                            // Handle item selection properly
                            theaterAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
                                String selectedCinemaName = (String) parent.getItemAtPosition(position);

                                // Find the selected cinema by name
                                for (Cinema cinema : cinemaList) {
                                    if (cinema.getName().equals(selectedCinemaName)) {
                                        selectedTheaterId = cinema.getId(); // Store correct ID
                                        break;
                                    }
                                }

                                Toast.makeText(AddMovie.this, "Selected Theater ID: " + selectedTheaterId, Toast.LENGTH_SHORT).show();
                            });

                        } else {
                            Toast.makeText(AddMovie.this, "No Theaters found", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    mainHandler.post(() -> Toast.makeText(AddMovie.this, "Error processing response", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }


    //load Language
    private int selectedLanguageId = -1; // Store selected language ID globally

    private void fetchLanguages() {
        Gson gson = new Gson();

        JsonObject jsonObject = new JsonObject(); // Empty JSON object
        RequestBody body = RequestBody.create(gson.toJson(jsonObject), MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(Config.baseUrl+"/LoadLanguage")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> Toast.makeText(AddMovie.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                System.out.println("Response Data: " + responseData); // Debugging

                Gson gson = new Gson();
                try {
                    JsonObject responseJson = gson.fromJson(responseData, JsonObject.class);
                    boolean success = responseJson.get("success").getAsBoolean();

                    if (!success) {
                        mainHandler.post(() -> Toast.makeText(AddMovie.this, "Backend returned error", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    JsonArray languagesArray = responseJson.getAsJsonArray("languages");
                    List<Language> languageList = new ArrayList<>();
                    List<String> languageNames = new ArrayList<>();

                    for (int i = 0; i < languagesArray.size(); i++) {
                        JsonObject langObj = languagesArray.get(i).getAsJsonObject();
                        int id = langObj.get("id").getAsInt();
                        String language = langObj.get("language").getAsString();
                        languageNames.add(language);
                        languageList.add(new Language(id, language));
                    }

                    // Update UI on main thread
                    mainHandler.post(() -> {
                        if (!languageNames.isEmpty()) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(AddMovie.this, android.R.layout.simple_dropdown_item_1line, languageNames);
                            autoCompleteTextView.setAdapter(adapter);

                            // Handle item selection
                            autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
                                Language selectedLanguage = languageList.get(position);
                                selectedLanguageId = selectedLanguage.getId(); // Store selected ID

                                Toast.makeText(AddMovie.this, "Selected ID: " + selectedLanguageId, Toast.LENGTH_SHORT).show();
                            });

                        } else {
                            Toast.makeText(AddMovie.this, "No languages found", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    mainHandler.post(() -> Toast.makeText(AddMovie.this, "Error processing response", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }



    // Open gallery to select image
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri =data.getData();

            // Handle image selection
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                movieImageView.setImageBitmap(bitmap);
                imagePath = saveImageToInternalStorage(bitmap, "movie_"+ System.currentTimeMillis());
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Image load failed!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    //save movie img
    public String saveImageToInternalStorage(Bitmap bitmap, String imageName) {
        //ContextWrapper cw = new ContextWrapper(getApplicationContext());

        File directory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),"Images");

        //Create the directory if it does not exist
        if(!directory.exists()){
            directory.mkdir();
        }

        File file = new File(directory, imageName + ".jpg");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        selecteImageFile = file;
        return file.getAbsolutePath();
    }

    //reset feild after upload data
    private void resetFeilds(){

        // Reset AutoCompleteTextViews
        AutoCompleteTextView languageAutoComplete = findViewById(R.id.language);
        AutoCompleteTextView theaterAutoComplete = findViewById(R.id.movietheater);
        AutoCompleteTextView categoryAutoComplete = findViewById(R.id.movieCategory);

        languageAutoComplete.setText("");
        theaterAutoComplete.setText("");
        categoryAutoComplete.setText("");

        // Reset TextInputEditTexts
        TextInputEditText titleEditText = findViewById(R.id.title);
        TextInputEditText rateEditText = findViewById(R.id.rate);
        TextInputEditText descriptionEditText = findViewById(R.id.description);
        TextInputEditText priceEditText = findViewById(R.id.price);

        titleEditText.setText("");
        rateEditText.setText("");
        descriptionEditText.setText("");
        priceEditText.setText("");

        // Reset the image to default
        ImageView movieImage = findViewById(R.id.imgGlide);
        movieImage.setImageResource(R.drawable.upload_img);

        titleEditText.requestFocus();
    }
}

