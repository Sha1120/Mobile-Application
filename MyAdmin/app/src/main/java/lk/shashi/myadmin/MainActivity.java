package lk.shashi.myadmin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.annotation.Nonnull;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        FirebaseMessaging.getInstance().subscribeToTopic("booking_app").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                String msg = "Done";
                if(!task.isSuccessful()){
                    msg ="Failed";
                }
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String adminJson = sharedPreferences.getString("adminJson", null);

        if (adminJson != null) {
            Intent intent = new Intent(MainActivity.this, Home2.class);
            intent.putExtra("adminJson", adminJson);
            startActivity(intent);
            finish(); // Login screen skip karanawa
        }


        Button button1 = findViewById(R.id.button);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TextInputEditText textInputEditText1 = findViewById(R.id.email);
                TextInputEditText textInputEditText2 = findViewById(R.id.password);

                String email = textInputEditText1.getText() != null ? textInputEditText1.getText().toString().trim() : "";
                String password = textInputEditText2.getText() != null ? textInputEditText2.getText().toString().trim() : "";

                button1.setText("Loading");

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Gson gson = new Gson();
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("email", email);
                        jsonObject.addProperty("password", password);

                        OkHttpClient okHttpClient = new OkHttpClient();
                        RequestBody requestBody = RequestBody.create(gson.toJson(jsonObject), MediaType.get("application/json"));
                        Request request = new Request.Builder()
                                .url(Config.baseUrl+"/AdminLogin")
                                .post(requestBody)
                                .build();

                        try {
                            Response response = okHttpClient.newCall(request).execute();
                            String responseTest = response.body().string();
                            Log.e("homehttp","hii hii2");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    button1.setText("Login");
                                }
                            });
                            Log.e("homehttp","hii hii3");
                                JsonObject responseJsonObject = gson.fromJson(responseTest, JsonObject.class);
                                if (responseJsonObject.get("success").getAsBoolean()) {
                                    if (responseJsonObject.has("admin") && !responseJsonObject.get("admin").isJsonNull()) {

                                        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("adminJson", gson.toJson(responseJsonObject.get("admin").getAsJsonObject()));
                                        editor.apply();

                                        Intent i = new Intent(MainActivity.this, Home2.class);
                                        i.putExtra("adminJson", gson.toJson(responseJsonObject.get("admin").getAsJsonObject()));
                                        runOnUiThread(() -> startActivity(i));
                                        Log.e("homehttp", "hii");
                                    } else {
                                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Invalid Admin Data", Toast.LENGTH_LONG).show());
                                    }
                                } else {
                                    String message = responseJsonObject.has("message") ? responseJsonObject.get("message").getAsString() : "Invalid Details";
                                    runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show());
                                }

                        } catch (Exception e) {
                            Log.e("homehttp", "Error: " + e.getMessage());
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Network error. Please try again.", Toast.LENGTH_LONG).show());
                        }
                    }
                }).start();
            }
        });

    }
}