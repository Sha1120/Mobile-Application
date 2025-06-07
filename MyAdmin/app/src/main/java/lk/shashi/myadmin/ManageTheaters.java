package lk.shashi.myadmin;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import adapters.CategoryAdapter;
import adapters.TheaterAdapter;
import model.Category;
import model.Theater;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ManageTheaters extends AppCompatActivity {

    private Button addlocation,savetheater;

    String Location;
    private TextInputEditText theaterName , location;
    private final int locationPermition = 1;
    Handler mainHandler = new Handler(Looper.getMainLooper());
    private RecyclerView recyclerView;
    private List<Theater> theaterList;
    private TheaterAdapter theaterAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_theaters);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addlocation = findViewById(R.id.addlocation);
        addlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checkLocationPermission()){
                    Intent i = new Intent(ManageTheaters.this,Location.class);
                    startActivity(i);
                }else{
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},locationPermition);
                }
            }
        });

        //check location
        if(getIntent().hasExtra("SelectedLocation")){
            Log.d("homehttp",getIntent().getStringExtra("SelectedLocation"));
        }

        theaterName = findViewById(R.id.theater);
        location = findViewById(R.id.location);
        location.setText(Location);

        savetheater = findViewById(R.id.addTheater);
        savetheater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTheater();
            }
        });

        recyclerView = findViewById(R.id.theaterview);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2); // 2 card view
        recyclerView.setLayoutManager(gridLayoutManager);

        theaterList = new ArrayList<>();
        theaterAdapter = new TheaterAdapter(this,theaterList);
        recyclerView.setAdapter(theaterAdapter);

        fetchTheater();



    }

    private void fetchTheater() {
        theaterAdapter.fetchTheater();

    }

    private boolean checkLocationPermission (){
        if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)  {
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(locationPermition == requestCode ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Intent i = new Intent(ManageTheaters.this,Location.class);
                startActivity(i);
            }else{
                new AlertDialog.Builder(this).setTitle("Warning Message").setMessage("Location Permission Denied. Please Enable Permission to Continue").show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get stored location from SharedPreferences
        String LocationData = getSharedPreferences(getPackageName(), MODE_PRIVATE)
                .getString("SelectedLocation", ""); // Default value is empty string if no location is saved

        // If the intent has "SelectedLocation", override the stored value
        if (getIntent().hasExtra("SelectedLocation")) {
            LocationData = getIntent().getStringExtra("SelectedLocation");
        }
        Log.d("homehttp", LocationData);

        // Check if the location is non-empty, then set the location value
        if (!LocationData.isEmpty()) {
            location.setText(LocationData);  // Only set if LocationData is non-empty
        } else {
            location.setText("");  // Reset location field if no location is found
        }
    }



    private void addTheater() {
        String tName = theaterName.getText() != null ? theaterName.getText().toString().trim() : "";
        String theaterLocation = location.getText() != null ? location.getText().toString().trim() : "";

        if (tName.isEmpty()) {
            new AlertDialog.Builder(this).setTitle("Warning Message").setMessage("Please Enter Theater Name").show();
            return; // Stop execution if name is empty
        }

        if (theaterLocation.isEmpty()) {
            new AlertDialog.Builder(this).setTitle("Warning Message").setMessage("Please Select Location").show();
            return; // Stop execution if location is empty
        }

        postTheaterToServer(tName, theaterLocation);
    }


    private void postTheaterToServer(String tName, String theaterLocation){
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();

        // Prepare the multipart request
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        builder.addFormDataPart("theatername", tName);
        builder.addFormDataPart("theaterlocation", theaterLocation);

        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(Config.baseUrl + "/AddTheater") // Update NGROK URL
                .post(requestBody)
                .build();

        Log.d("homehttp", "tname: " + tName);
        Log.d("homehttp", "location: " + theaterLocation);
        Log.d("homehttp", "POST URL: " + Config.baseUrl + "/AddTheater");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("homehttp", "Network Error: " + e.getMessage()); // Log error
                mainHandler.post(() -> Toast.makeText(ManageTheaters.this, "Network Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body().string();

                Log.e("homehttp", "Raw API Response: " + responseBody); // Full response eka print karanawa

                // Response eka empty da kiyala balanna
                if (responseBody == null || responseBody.isEmpty()) {
                    runOnUiThread(() ->
                           // Toast.makeText(ManageTheaters.this, "Server eken empty response ekak awa!", Toast.LENGTH_SHORT).show()
                            showWarningDialog("Empty response.")
                    );
                    return;
                }

                try {
                    JsonObject responseJson = new Gson().fromJson(responseBody, JsonObject.class);

                    if (responseJson.has("success") && responseJson.get("success").getAsBoolean()) {
                        runOnUiThread(() -> {
                            //Toast.makeText(ManageTheaters.this, "Theater upload una!", Toast.LENGTH_SHORT).show();
                            showSuccessDialog( "Theater upload successfully");
                            theaterName.setText("");
                            location.setText("");
                            getSharedPreferences(getPackageName(), MODE_PRIVATE).edit()
                                    .remove("SelectedLocation")
                                    .apply();
                        });
                    } else {
                        String errorMessage = responseJson.has("message") ? responseJson.get("message").getAsString() : "Unknown error";
                        runOnUiThread(() ->
                                //Toast.makeText(ManageTheaters.this, "Fail una: " + errorMessage, Toast.LENGTH_SHORT).show()
                                showWarningDialog("Theater upload faild.")
                        );
                    }
                } catch (Exception e) {
                    Log.e("homehttp", "JSON Parsing Error: " + e.getMessage());
                    runOnUiThread(() ->
                           // Toast.makeText(ManageTheaters.this, "Server response eka parse karanna bari una!", Toast.LENGTH_SHORT).show()
                            showWarningDialog("Server not response.")
                    );
                }
            }



        });
    }

    private void showSuccessDialog( String message) {
        // Fix: Check if Activity is finishing before showing Dialog
        if (isFinishing()) return;

        runOnUiThread(() -> {
            AlertDialog dialog = new AlertDialog.Builder(ManageTheaters.this)
                    .setIcon(R.drawable.correct)
                    .setTitle("Success")
                    .setMessage(message)
                    .setPositiveButton("OK", (dialogInterface, which) -> {
                    })
                    .setCancelable(false)
                    .show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(this, R.color.primaryColor));
        });
    }

    private void showWarningDialog(String message) {
        // Fix: Check if Activity is finishing before showing Dialog
        if (isFinishing()) return;

        runOnUiThread(() -> new AlertDialog.Builder(ManageTheaters.this)
                .setIcon(R.drawable.warrning)
                .setTitle("Waring")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show());
    }

}