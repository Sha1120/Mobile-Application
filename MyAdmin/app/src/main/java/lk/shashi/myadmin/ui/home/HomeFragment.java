package lk.shashi.myadmin.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import adapters.ImageSliderAdapter;
import adapters.userMovieAdapter;
import adapters.userTheaterAdapter;
import lk.shashi.myadmin.R;
import lk.shashi.myadmin.databinding.FragmentHomeBinding;
import model.Movie;
import model.Theater;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private userTheaterAdapter theaterAdapter;
    private  userMovieAdapter movieAdapter;
    private ImageSliderAdapter imageSliderAdapter;
    private Handler handler;
    private Runnable runnable;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Setup RecyclerView with horizontal layout manager
        RecyclerView recyclerView = binding.recyclerView2;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        theaterAdapter = new userTheaterAdapter(getContext(), new ArrayList<Theater>());
        recyclerView.setAdapter(theaterAdapter);
        theaterAdapter.fetchTheater();

        //load movie
        RecyclerView recyclerView1 = binding.loadmovie;
        recyclerView1.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columns per row
        movieAdapter = new userMovieAdapter(getContext(), new ArrayList<Movie>());
        recyclerView1.setAdapter(movieAdapter);
        movieAdapter.fetchMovies();

        // List of drawable images instead of URLs
        List<Integer> imageResources = Arrays.asList(
                R.drawable.movies1,
                R.drawable.movies5,
                R.drawable.movies3
        );

        // Setup ViewPager2 with ImageSliderAdapter
        ViewPager2 viewPager = binding.viewPager;
        imageSliderAdapter = new ImageSliderAdapter(getContext(), imageResources);
        viewPager.setAdapter(imageSliderAdapter);

        // Auto-slide functionality
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = viewPager.getCurrentItem();
                int nextItem = (currentItem + 1) % imageResources.size();  // Loop back to the first image
                viewPager.setCurrentItem(nextItem, true);
                handler.postDelayed(this, 3000); // Change image every 3 seconds
            }
        };

        handler.postDelayed(runnable, 3000); // Start auto-slide after 3 seconds


        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}