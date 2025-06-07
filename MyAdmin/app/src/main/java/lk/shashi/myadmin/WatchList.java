package lk.shashi.myadmin;

import static java.security.AccessController.getContext;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import adapters.MovieAdapter;
import adapters.WatchListAdapter;
import adapters.userMovieAdapter;
import model.Movie;

public class WatchList extends AppCompatActivity {
    private WatchListAdapter watchListAdapter;
    private List<Movie> movieList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_watch_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //load movie
        RecyclerView recyclerView1 = findViewById(R.id.loadwatchlist);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2); // 2 card view
        recyclerView1.setLayoutManager(gridLayoutManager);

        movieList = new ArrayList<>();
        watchListAdapter = new WatchListAdapter(this,movieList);
        recyclerView1.setAdapter(watchListAdapter);
        watchListAdapter.fetchMovies();
    }
}