package adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
import model.Movie;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WatchListAdapter extends RecyclerView.Adapter<WatchListAdapter.ViewHolder>{

    private Context context;
    private List<Movie> movieList;

    public WatchListAdapter(Context context, List<Movie> movieList) {
        this.context = context;
        this.movieList = (movieList != null) ? movieList : new ArrayList<>();
    }

    @NonNull
    @Override
    public WatchListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_movie_view, parent, false);
        return new WatchListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WatchListAdapter.ViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.movieTitle.setText(movie.getTitle());
        holder.rate.setText(movie.getRate());
        holder.price.setText(String.valueOf(movie.getPrice()));

        // Fixed Image URL
        String imageUrl = String.format(Config.baseUrl + "/movies/%s.png", movie.getId());

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.upload_img)
                .error(R.drawable.baseline_running_with_errors_24)
                .into(holder.movieimg);

        // Variable to track the button click state
        int[] isRed = {0};

        holder.btnbooking.setOnClickListener(v -> {
            if (isRed[0] == 1) {
                // Reset color to default and reset icon
                holder.btnbooking.clearColorFilter();
                holder.btnbooking.setImageResource(R.drawable.baseline_favorite_border_24); // Set the original icon
                isRed[0] = 0;


            } else {
                // Change the icon color to red and change the icon
                holder.btnbooking.setColorFilter(context.getResources().getColor(R.color.red)); // Change the color to red
                holder.btnbooking.setImageResource(R.drawable.baseline_favorite_24); // Set the red-colored icon (your custom red icon)
                isRed[0] = 1;

            }
        });



    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView movieimg;
        TextView movieTitle,rate, price;
        ImageButton btnbooking;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            movieimg = itemView.findViewById(R.id.imageView9);
            movieTitle = itemView.findViewById(R.id.title);
            btnbooking = itemView.findViewById(R.id.booking);
            rate = itemView.findViewById(R.id.rateview1);
            price = itemView.findViewById(R.id.priceview);
        }
    }

    // Fetch movies from server
    public void fetchMovies() {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(Config.baseUrl + "/LoadMovie")
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
                JsonArray movieArray = JsonParser.parseString(responseData).getAsJsonArray();

                // Convert the JsonArray into Movie objects
                List<Movie> movies = new ArrayList<>();
                Gson gson = new Gson();
                for (JsonElement movieElement : movieArray) {
                    Movie movie = gson.fromJson(movieElement, Movie.class);

                    // Ensure category is properly parsed
                    String categoryname = "Unknown";
                    if (movieElement.getAsJsonObject().has("category")) {
                        JsonElement categoryElement = movieElement.getAsJsonObject().get("category");
                        if (categoryElement.isJsonObject() && categoryElement.getAsJsonObject().has("category")) {
                            categoryname = categoryElement.getAsJsonObject().get("category").getAsString();
                        }
                    }
                    movie.setCategoryName(categoryname);
                    movies.add(movie);
                }

                // Run UI updates on the main thread
                runOnUiThread(() -> {
                    if (!movies.isEmpty()) {
                        movieList.clear();
                        movieList.addAll(movies);
                        notifyDataSetChanged();
                    } else {
                        showToast("No movies found.");
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
