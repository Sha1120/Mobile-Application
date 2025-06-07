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
import com.example.myuser.Config;
import com.example.myuser.R;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.Movie;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class userMovieAdapter extends RecyclerView.Adapter<userMovieAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Movie> movieList;
    private List<Integer> watchlistMovieIds = new ArrayList<>();
    private String userId; // Add userId field to store the user ID

    public userMovieAdapter(Context context, ArrayList<Movie> movieList) {
        this.context = context;
        this.movieList = (movieList != null) ? movieList : new ArrayList<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        this.userId = String.valueOf(sharedPreferences.getInt("userId", -1)); // Get user ID as Integer, convert to String
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_movie_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.movieTitle.setText(movie.getTitle());
        holder.rate.setText(movie.getRate());
        holder.price.setText(String.valueOf(movie.getPrice()));

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



    // Method to send the update to the server with both userId and movieId
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

    public List<Movie> getMovieList() {
        return movieList;
    }

    // Update the movie list with the filtered data
    public void updateMovieList(List<Movie> newMovieList) {
        this.movieList = (ArrayList<Movie>) newMovieList;
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    public void filterMovies(String query) {
        List<Movie> filteredList = new ArrayList<>();

        for (Movie movie : getMovieList()) {
            if (movie.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(movie);
            }
        }

        updateMovieList(filteredList);
    }


    // Fetch movies from server
    public void fetchMovies(int userId) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(Config.baseUrl + "/LoadMovie?userId=" + userId) // Pass User ID
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

                JsonArray movieArray = JsonParser.parseString(responseData).getAsJsonArray();
                List<Movie> movies = new ArrayList<>();
                Gson gson = new Gson();

                for (JsonElement movieElement : movieArray) {
                    Movie movie = gson.fromJson(movieElement, Movie.class);
                    movies.add(movie);
                }

                runOnUiThread(() -> {
                    movieList.clear();
                    movieList.addAll(movies);
                    notifyDataSetChanged();
                });
            } catch (IOException | JsonSyntaxException e) {
                Log.e("homehttp", "API Request Failed", e);
                showToast("Error fetching movies");
            }
        }).start();
    }



    public void fetchWatchlist() {
        if (userId.equals("-1")) {
            return; // User not logged in
        }

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

                watchlistMovieIds.clear();
                watchlistMovieIds.addAll(ids);

                // Fetch movies after getting watchlist data
                fetchMovies(Integer.parseInt(userId));
            } catch (IOException | JsonSyntaxException e) {
                Log.e("homehttp", "Failed to fetch watchlist", e);
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

    public boolean isInWatchlist(int movieId) {
        return watchlistMovieIds.contains(movieId);
    }
}
