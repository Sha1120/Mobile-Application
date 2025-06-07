package adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.myuser.BuyTicketActivity;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.myuser.Config;
import com.example.myuser.R;
import model.Movie;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WatchListAdapter extends RecyclerView.Adapter<WatchListAdapter.ViewHolder> {

    private Context context;
    private List<Movie> movieList = new ArrayList<>();
    private List<Integer> watchlistMovieIds = new ArrayList<>();
    private String userId;

    public WatchListAdapter(Context context, List<Movie> movieList) {
        this.context = context;
        this.movieList = (movieList != null) ? movieList : new ArrayList<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int storedUserId = sharedPreferences.getInt("userId", -1);
        if (storedUserId == -1) {
            // Handle the case where user is not logged in
            return;
        }
        this.userId = String.valueOf(storedUserId);
    }

    @NonNull
    @Override
    public WatchListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_movie_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WatchListAdapter.ViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.movieTitle.setText(movie.getTitle());
        holder.rate.setText(movie.getRate());
        holder.price.setText(String.valueOf(movie.getPrice()));

        // Set Image URL
        String imageUrl = String.format(Config.baseUrl + "/movies/%s.png", movie.getId());

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.upload_img)
                .error(R.drawable.baseline_running_with_errors_24)
                .into(holder.movieimg);

        if (isInWatchlist(movie.getId())) {
            holder.btnbooking.setColorFilter(context.getResources().getColor(R.color.red));
            holder.btnbooking.setImageResource(R.drawable.baseline_favorite_24);
        } else {
            holder.btnbooking.clearColorFilter();
            holder.btnbooking.setImageResource(R.drawable.baseline_favorite_border_24);
        }

        holder.btnbooking.setOnClickListener(v -> {
            if (watchlistMovieIds.contains(movie.getId())) {
                removeMovieWatchlist(movie.getId());
                watchlistMovieIds.remove(Integer.valueOf(movie.getId()));
                holder.btnbooking.clearColorFilter();
                holder.btnbooking.setImageResource(R.drawable.baseline_favorite_border_24);
            } else {
                addMovieWatchlist(movie.getId());
                watchlistMovieIds.add(movie.getId());
                holder.btnbooking.setColorFilter(context.getResources().getColor(R.color.red));
                holder.btnbooking.setImageResource(R.drawable.baseline_favorite_24);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BuyTicketActivity.class);
            intent.putExtra("movieId", movie.getId());
            intent.putExtra("title", movie.getTitle());
            intent.putExtra("rate", movie.getRate());
            intent.putExtra("price", String.valueOf(movie.getPrice()));
            intent.putExtra("imageUrl", imageUrl);
            intent.putExtra("description", movie.getDescription());
            intent.putExtra("cinemaId", movie.getCinema().getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView movieimg;
        TextView movieTitle, rate, price;
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

    public void fetchWatchlist() {
        if (userId.equals("-1")) {
            showToast("User not logged in.");
            return;
        }
        Log.d("homehttp", "User ID: " + userId);

        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            String url = Config.baseUrl + "/LoadWatchList?userId=" + userId;
            Request request = new Request.Builder().url(url).build();

            try {
                Response response = client.newCall(request).execute();

                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("homehttp", "Error fetching watchlist: " + response.code());
                    return;
                }

                String responseData = response.body().string();
                JsonArray watchlistArray = JsonParser.parseString(responseData).getAsJsonArray();

                List<Integer> ids = new ArrayList<>();
                for (JsonElement element : watchlistArray) {
                    ids.add(element.getAsJsonObject().get("movieId").getAsInt());
                }
                fetchMovies(Integer.valueOf(userId));
                watchlistMovieIds.clear();
                watchlistMovieIds.addAll(ids);

            } catch (IOException | JsonSyntaxException e) {
                Log.e("homehttp", "Failed to fetch watchlist", e);
            }
        }).start();
    }


    private void fetchMovies(int userId) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(Config.baseUrl + "/LoadMovie?userId=" + userId) // Endpoint to fetch all movies
                    .build();

            try {
                Response response = client.newCall(request).execute();

                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("homehttp", "Error fetching movies: " + response.code());
                    return;
                }

                String responseData = response.body().string();
                Log.d("homehttp", "Raw API Response: " + responseData);

                JsonArray movieArray = JsonParser.parseString(responseData).getAsJsonArray();

                List<Movie> allMovies = new ArrayList<>();
                Gson gson = new Gson();
                for (JsonElement movieElement : movieArray) {
                    Movie movie = gson.fromJson(movieElement, Movie.class);
                    allMovies.add(movie);
                }

                // Filter movies that are in the watchlist
                List<Movie> filteredMovies = new ArrayList<>();
                for (Movie movie : allMovies) {
                    if (watchlistMovieIds.contains(movie.getId())) {
                        filteredMovies.add(movie);
                    }
                }

                // Debug log for filtered movies
                Log.d("FilteredMovies", "Filtered movies count: " + filteredMovies.size());

                // Now update the UI with the filtered movies
                runOnUiThread(() -> {
                    if (!filteredMovies.isEmpty()) {
                        movieList.clear();
                        movieList.addAll(filteredMovies);
                        notifyDataSetChanged(); // Notify adapter after updating data
                    } else {
                        showToast("No movies found in your watchlist.");
                    }
                });

            } catch (IOException | JsonSyntaxException e) {
                Log.e("homehttp", "API Request Failed", e);
                showToast("Error fetching movies");
            }
        }).start();
    }

    private void addMovieWatchlist(int movieId) {
        if (userId.equals("-1")) {
            showToast("User not logged in.");
            return;
        }

        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            String url = Config.baseUrl + "/AddedWatchList?id=" + movieId + "&userId=" + userId;
            Request request = new Request.Builder().url(url).build();

            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    Log.e("homehttp", "Server Error: " + response.code());
                    showToast("Error saving movie to watchlist");
                }
            } catch (IOException e) {
                Log.e("homehttp", "API Request Failed", e);
                showToast("Error saving movie to watchlist");
            }
        }).start();
    }

    private void removeMovieWatchlist(int movieId) {
        if (userId.equals("-1")) {
            showToast("User not logged in.");
            return;
        }

        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            String url = Config.baseUrl + "/RemoveWatchList?id=" + movieId + "&userId=" + userId;
            Request request = new Request.Builder().url(url).build();

            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    Log.e("homehttp", "Server Error: " + response.code());
                    showToast("Error removing movie from watchlist");
                }
            } catch (IOException e) {
                Log.e("homehttp", "API Request Failed", e);
                showToast("Error removing movie from watchlist");
            }
        }).start();
    }

    private boolean isInWatchlist(int movieId) {
        return watchlistMovieIds.contains(movieId);
    }

    private void showToast(String message) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
        }
    }

    private void runOnUiThread(Runnable action) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(action);
        }
    }
}
