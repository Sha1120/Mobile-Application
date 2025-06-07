package lk.shashi.myadmin;

import static android.app.PendingIntent.getActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import model.Category;
import model.Cinema;
import model.Language;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Update_Movie extends AppCompatActivity {
    private TextInputEditText movieNameText,movieRateText,movieDescriptionText,moviePriceText;
    private AutoCompleteTextView theaterCompleteText,languageCompleteText,categoryCompleteText;
    private ImageView movieImage;
    Handler mainHandler = new Handler(Looper.getMainLooper());
    OkHttpClient client = new OkHttpClient();
    private Button DeleteBtn , UpdateBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_movie);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        movieImage = findViewById(R.id.imgGlide);
        movieNameText = findViewById(R.id.moviename);
        movieRateText = findViewById(R.id.movierate);
        movieDescriptionText = findViewById(R.id.moviedescription);
        moviePriceText = findViewById(R.id.movieprice);
        languageCompleteText = findViewById(R.id.language);
        fetchLanguages();
        theaterCompleteText = findViewById(R.id.movietheater);
        fetchTheaters();
        categoryCompleteText = findViewById(R.id.movieCategory);
        fetchCategories();

        // Get the data passed from the previous activity
        Intent intent = getIntent();
        String movieId = intent.getStringExtra("id");
        String imagePath = intent.getStringExtra("imagePath");
        String movieName = intent.getStringExtra("name");
        String movieRate = intent.getStringExtra("rate");
        String movieDescription = intent.getStringExtra("description");
        String moviePrice = intent.getStringExtra("price");
        String movieLanguage = intent.getStringExtra("language");
        String movieCategory = intent.getStringExtra("category");
        String movieTheater = intent.getStringExtra("theater");

        Log.d("homehttp", movieId);
        Log.d("homehttp", imagePath);
        Log.d("homehttp", movieName);
        Log.d("homehttp",movieRate);
        Log.d("homehttp",movieDescription);
        Log.d("homehttp",moviePrice);
        Log.d("homehttp",movieLanguage);
        Log.d("homehttp",movieCategory);
        Log.d("homehttp",movieTheater);


        if (movieId != null && movieName != null && imagePath != null && movieRate != null && movieDescription != null && moviePrice != null && movieLanguage != null && movieCategory != null && movieTheater != null) {
            // Set the category name to the TextInputEditText
            movieNameText.setText(movieName);
            movieRateText.setText(movieRate);
            movieDescriptionText.setText(movieDescription);
            moviePriceText.setText(moviePrice);
            languageCompleteText.setText(movieLanguage);
            categoryCompleteText.setText(movieCategory);
            theaterCompleteText.setText(movieTheater);


            // Load the category image (if image path is passed)
            // You can use Glide or Picasso here to load the image from a URL or local file path
            Glide.with(this)
                    .load(imagePath)  // Assuming imagePath is a valid URL or file path
                    .into(movieImage);
        } else {
            // Handle case when data is missing (optional)
            Toast.makeText(this, "Movie data is missing", Toast.LENGTH_SHORT).show();
        }

        UpdateBtn = findViewById(R.id.update2);
        UpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //update movie
                updateMovieOnServer(Integer.parseInt(movieId));
            }
        });

        DeleteBtn = findViewById(R.id.delete2);
        DeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 deleteMovieFromServer(Integer.parseInt(movieId));
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
                        Toast.makeText(Update_Movie.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.body() == null) {
                    mainHandler.post(() ->
                            Toast.makeText(Update_Movie.this, "Empty response from server", Toast.LENGTH_SHORT).show()
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
                                Toast.makeText(Update_Movie.this, "Backend returned error", Toast.LENGTH_SHORT).show()
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
                                    Update_Movie.this,
                                    android.R.layout.simple_dropdown_item_1line,
                                    categoryNames
                            );
                            categoryCompleteText.setAdapter(adapter);

                            // Handle item selection properly
                            categoryCompleteText.setOnItemClickListener((parent, view, position, id) -> {
                                String selectedCategoryName = (String) parent.getItemAtPosition(position);

                                // Find the selected category by name
                                for (Category category : categoryList) {
                                    if (category.getName().equals(selectedCategoryName)) {
                                        selectedCategoryId = category.getId(); // Store correct ID
                                        break;
                                    }
                                }

                                Toast.makeText(Update_Movie.this, "Selected Category ID: " + selectedCategoryId, Toast.LENGTH_SHORT).show();
                            });

                        } else {
                            Toast.makeText(Update_Movie.this, "No Categories found", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    mainHandler.post(() ->
                            Toast.makeText(Update_Movie.this, "Error processing response", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

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
                mainHandler.post(() -> Toast.makeText(Update_Movie.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
                        mainHandler.post(() -> Toast.makeText(Update_Movie.this, "Backend returned error", Toast.LENGTH_SHORT).show());
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
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(Update_Movie.this, android.R.layout.simple_dropdown_item_1line, languageNames);
                            languageCompleteText.setAdapter(adapter);

                            // Handle item selection
                            languageCompleteText.setOnItemClickListener((parent, view, position, id) -> {
                                Language selectedLanguage = languageList.get(position);
                                selectedLanguageId = selectedLanguage.getId(); // Store selected ID

                                Toast.makeText(Update_Movie.this, "Selected ID: " + selectedLanguageId, Toast.LENGTH_SHORT).show();
                            });

                        } else {
                            Toast.makeText(Update_Movie.this, "No languages found", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    mainHandler.post(() -> Toast.makeText(Update_Movie.this, "Error processing response", Toast.LENGTH_SHORT).show());
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
                mainHandler.post(() -> Toast.makeText(Update_Movie.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
                        mainHandler.post(() -> Toast.makeText(Update_Movie.this, "Backend returned error", Toast.LENGTH_SHORT).show());
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
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(Update_Movie.this, android.R.layout.simple_dropdown_item_1line, cinemaNames);
                            theaterCompleteText.setAdapter(adapter);

                            // Handle item selection properly
                            theaterCompleteText.setOnItemClickListener((parent, view, position, id) -> {
                                String selectedCinemaName = (String) parent.getItemAtPosition(position);

                                // Find the selected cinema by name
                                for (Cinema cinema : cinemaList) {
                                    if (cinema.getName().equals(selectedCinemaName)) {
                                        selectedTheaterId = cinema.getId(); // Store correct ID
                                        break;
                                    }
                                }

                                Toast.makeText(Update_Movie.this, "Selected Theater ID: " + selectedTheaterId, Toast.LENGTH_SHORT).show();
                            });

                        } else {
                            Toast.makeText(Update_Movie.this, "No Theaters found", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    mainHandler.post(() -> Toast.makeText(Update_Movie.this, "Error processing response", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void updateMovieOnServer(int movieId) { // Changed parameter to int

        String MovieTitle = movieNameText.getText().toString().trim();
        String MovieRate = movieRateText.getText().toString().trim();
        String MovieDescription = movieDescriptionText.getText().toString().trim();
       // String MovieLanguage = languageCompleteText.getText().toString().trim();
       // String MovieTheater = theaterCompleteText.getText().toString().trim();
       // String MovieCategory = categoryCompleteText.getText().toString().trim();


        if (MovieTitle.isEmpty() || MovieRate.isEmpty() || MovieDescription.isEmpty() ||selectedLanguageId ==-1 ||selectedTheaterId ==-1 ||selectedCategoryId ==-1   ) {
//            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            showWarningDialog("Warning","Please fill all fields");
            return;
        }

        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try {
            json.put("movieId",movieId);
            json.put("MovieTitle", MovieTitle);
            json.put("MovieRate", MovieRate);
            json.put("MovieDescription", MovieDescription);
            json.put("MovieLanguage", selectedLanguageId);
            json.put("MovieTheater", selectedTheaterId);
            json.put("MovieCategory", selectedCategoryId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(Config.baseUrl+"/UpdateMovie")
                .put(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
               runOnUiThread(() -> showWarningDialog("Warning","Update Failed"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> showSuccessDialog("Success","Movie Update Successfully") );
                } else {
                    runOnUiThread(() -> showWarningDialog("Warning"," Movie Update Faild"));
                }
            }
        });
    }

    private void deleteMovieFromServer(int movieId) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Config.baseUrl+"/DeleteMovie?id=" + movieId)
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(Update_Movie.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body().string();

                try {
                    JSONObject responseJson = new JSONObject(responseBody);

                    if (responseJson.has("success") && responseJson.getBoolean("success")) {
                        runOnUiThread(() -> {
                           // Toast.makeText(Update_Movie.this, "Movie deleted successfully!", Toast.LENGTH_SHORT).show();
                            showSuccessDialog("Success", "Movie deleted successfully!");
                           // finish();
                        });
                    } else {
                        runOnUiThread(() -> {
                           // Toast.makeText(Update_Movie.this, "Failed to delete movie.", Toast.LENGTH_SHORT).show();
                            showWarningDialog("Warning", "Failed to delete movie.");
                        });
                    }
                } catch (JSONException e) {
                    runOnUiThread(() -> {
                       // Toast.makeText(Update_Movie.this, "Error parsing response.", Toast.LENGTH_SHORT).show();
                        showWarningDialog("Warning", "Error parsing response");
                    });
                }
            }
        });
    }

    private void showSuccessDialog(String title, String message) {
        // Fix: Check if Activity is finishing before showing Dialog
        if (isFinishing()) return;

        runOnUiThread(() -> {
            AlertDialog dialog = new AlertDialog.Builder(Update_Movie.this)
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

        runOnUiThread(() -> new AlertDialog.Builder(Update_Movie.this)
                .setIcon(R.drawable.warrning)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show());
    }

}