package lk.shashi.myadmin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import adapters.BookingAdapter;
import adapters.MovieAdapter;
import model.Booking;
import model.Movie;


public class ManageAllMovie extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private List<Movie> movieList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_all_movie);

        // Handle window insets for Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FloatingActionButton addMovieButton = findViewById(R.id.floatingActionButton);
        addMovieButton.setOnClickListener(v -> {
            Intent i = new Intent(ManageAllMovie.this, AddMovie.class);
            startActivity(i);
        });

        recyclerView = findViewById(R.id.movie);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2); // 2 card view
        recyclerView.setLayoutManager(gridLayoutManager);

        movieList = new ArrayList<>();
        movieAdapter = new MovieAdapter(this,movieList);
        recyclerView.setAdapter(movieAdapter);

        fetchMovies();

    }

    private void fetchMovies() {
        movieAdapter.fetchMovies();
    }

}
