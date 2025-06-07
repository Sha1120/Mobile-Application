package lk.shashi.myadmin;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateTheater extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_theater);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        TextInputEditText teatername  = findViewById(R.id.textInputEditText);
        Button deletebtn = findViewById(R.id.deletetheater);

        Intent intent = getIntent();
        String theaterId = intent.getStringExtra("id");
        String theaterName = intent.getStringExtra("name");

        teatername.setText(theaterName);
        deletebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assert theaterId != null;
                deleteTheater(Integer.parseInt(theaterId));
            }
        });
    }

    private void deleteTheater(int theaterId){
        //String theaterId = intent.getStringExtra("id");
        OkHttpClient client = new OkHttpClient();

        // Build the GET request with the category ID as a query parameter
        Request request = new Request.Builder()
                .url(Config.baseUrl+"/DeleteTheater?id=" + theaterId)  // Your GET API URL
                .get()  // Use GET instead of DELETE since you're sending categoryId as a query parameter
                .build();

        // Send the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    // Display error message if request fails
                    Toast.makeText(UpdateTheater.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                            //Toast.makeText(UpdateTheater.this, "Theater deleted successfully!", Toast.LENGTH_SHORT).show();
                            //finish();  // Close the activity after deletion
                            showSuccessDialog("Success", "Theater deleted successfully!");

                        });
                    } else {
                        // If deletion failed
                        runOnUiThread(() -> {
                            //Toast.makeText(UpdateTheater.this, "Failed to delete category.", Toast.LENGTH_SHORT).show();
                            showWarningDialog("Warning", "Failed to delete category.");
                        });
                    }
                } catch (JSONException e) {
                    // Error parsing JSON response
                    runOnUiThread(() -> {
                        //Toast.makeText(UpdateTheater.this, "Error parsing response.", Toast.LENGTH_SHORT).show();
                        showWarningDialog("Warning", "Error parsing response.");
                    });
                }
            }
        });

    }

    private void showSuccessDialog(String title, String message) {
        // Fix: Check if Activity is finishing before showing Dialog
        if (isFinishing()) return;

        runOnUiThread(() -> {
            AlertDialog dialog = new AlertDialog.Builder(UpdateTheater.this)
                    .setIcon(R.drawable.correct)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("OK", (dialogInterface, which) -> {
                        finish();
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

        runOnUiThread(() -> new AlertDialog.Builder(UpdateTheater.this)
                .setIcon(R.drawable.warrning)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show());
    }
}