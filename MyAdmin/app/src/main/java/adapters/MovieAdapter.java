package adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.google.gson.GsonBuilder;
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
import lk.shashi.myadmin.Update_Movie;
import model.Movie;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    private Context context;
    private List<Movie> movieList;

    public MovieAdapter(Context context, List<Movie> movieList) {
        this.context = context;
        this.movieList = (movieList != null) ? movieList : new ArrayList<>();
    }

    @NonNull
    @Override
    public MovieAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.movie_item, parent, false);
        return new MovieAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieAdapter.ViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.movieTitle.setText(movie.getTitle());

        // Fixed Image URL
        String imageUrl = String.format(Config.baseUrl + "/movies/%s.png", movie.getId());

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.upload_img)
                .error(R.drawable.baseline_running_with_errors_24)
                .into(holder.movieimg);

        holder.btnUpdate.setOnClickListener(v -> {
            // Get the category data from your model or adapter
            String movieId = String.valueOf(movie.getId());  // Replace with your method to get category ID
            String movieName = movie.getTitle();  // Replace with your method to get category name
            String movieRate = movie.getRate();
            String movieDescription = movie.getDescription();
            String moviePrice = String.valueOf(movie.getPrice());
            String movieLanguage = String.valueOf(movie.getLanguage().getLanguage());
            String movieTheater = String.valueOf(movie.getCinema().getName());

            // Check if movie's category is null before accessing its properties
            String categoryname = (movie.getCategoryName() != null && movie.getCategoryName() != null)
                    ? movie.getCategoryName()
                    : "Unknown";


            // Create an intent and pass the data
            Intent intent = new Intent(context, Update_Movie.class);
            intent.putExtra("id", movieId);
            intent.putExtra("name", movieName);
            intent.putExtra("imagePath", imageUrl);  // Pass the image path as well
            intent.putExtra("rate", movieRate);
            intent.putExtra("description", movieDescription);
            intent.putExtra("price",moviePrice);
            intent.putExtra("language",movieLanguage);
            intent.putExtra("category", categoryname);
            intent.putExtra("theater",movieTheater);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView movieimg;
        TextView movieTitle;
        ImageButton btnUpdate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            movieimg = itemView.findViewById(R.id.imageView);
            movieTitle = itemView.findViewById(R.id.movieTitle);
            btnUpdate = itemView.findViewById(R.id.imageButton);
        }
    }

    // Fetch movies from server
    public void fetchMovies() {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(Config.baseUrl+"/LoadAllMovie") // Correct API URL
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
                    String categoryname =  movieElement.getAsJsonObject().get("category").getAsJsonObject().get("category").getAsString();
                    movie.setCategoryName(categoryname);
                    movies.add(movie);
                    //Log.d("homehttp","movie_Category"+movie.getMovie_category().getName());
                }

                // Run UI updates on the main thread
                runOnUiThread(() -> {
                    if (movies != null && !movies.isEmpty()) {
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
