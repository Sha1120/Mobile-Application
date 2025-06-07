package adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lk.shashi.myadmin.Config;
import lk.shashi.myadmin.R;
import lk.shashi.myadmin.UpdateCategory;
import model.Theater;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class userTheaterAdapter extends RecyclerView.Adapter<userTheaterAdapter.ViewHolder> { // Add 'extends' here

    private Context context;
    private List<Theater> theaterList;

    public userTheaterAdapter(Context context, List<Theater> theaterList) {
        this.context = context;
        this.theaterList = (theaterList != null) ? theaterList : new ArrayList<>(); // Ensure it's initialized
    }

    @NonNull
    @Override
    public userTheaterAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_site_theater, parent, false);
        return new userTheaterAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull userTheaterAdapter.ViewHolder holder, int position) {
        Theater theater = theaterList.get(position);
        holder.TheaterName.setText(theater.getName());

        holder.btnUpdate.setOnClickListener(v -> {
            // Get the category data from your model or adapter
            String theaterId = String.valueOf(theater.getId());  // Replace with your method to get category ID
            String theaterName = theater.getName();  // Replace with your method to get category name

            // Create an intent and pass the data
            Intent intent = new Intent(context, UpdateCategory.class);
            intent.putExtra("id", theaterId);
            intent.putExtra("name", theaterName);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return theaterList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView TheaterName;
        ImageButton btnUpdate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            TheaterName = itemView.findViewById(R.id.tName);
            btnUpdate = itemView.findViewById(R.id.viewlocation);
        }
    }

    // Fetch category from server
    public void fetchTheater() {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(Config.baseUrl+"/LoadTheaters") // Correct API URL
                    .build();

            try {
                Response response = client.newCall(request).execute();

                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("homehttp", "Server Error: " + response.code());
                    showToast("Error fetching movies from server");
                    return;
                }

                String responseData = response.body().string();
                Log.d("homehttp", "Raw API Response: " + responseData);

                // Parse JSON array
                JsonArray theaterArray = JsonParser.parseString(responseData).getAsJsonArray();

                // Convert the JsonArray into Category objects
                List<Theater> theaters = new ArrayList<>();
                Gson gson = new Gson();
                for (JsonElement theaterElement : theaterArray) {
                    Theater theater = gson.fromJson(theaterElement, Theater.class);
                    theaters.add(theater);
                }

                // Run UI updates on the main thread
                runOnUiThread(() -> {
                    if (theaters != null && !theaters.isEmpty()) {
                        theaterList.clear();
                        theaterList.addAll(theaters);
                        notifyDataSetChanged();
                    } else {
                        showToast("No Categories found.");
                    }
                });
            } catch (IOException | JsonSyntaxException e) {
                Log.e("homehttp", "API Request Failed", e);
                showToast("Error fetching movies");
            }
        }).start();
    }

    private void runOnUiThread(Runnable action) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(action);
        }
    }

    private void showToast(String message) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            );
        }
    }
}

