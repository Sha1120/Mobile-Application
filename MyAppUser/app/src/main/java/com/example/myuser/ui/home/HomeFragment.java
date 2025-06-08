package com.example.myuser.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myuser.Profile;
import com.example.myuser.R;
import com.example.myuser.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import adapters.ImageSliderAdapter;
import adapters.userMovieAdapter;
import adapters.userTheaterAdapter;

import androidx.appcompat.widget.SearchView;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private userTheaterAdapter theaterAdapter;
    private userMovieAdapter movieAdapter;
    private ImageSliderAdapter imageSliderAdapter;
    private Handler handler;
    private Runnable runnable;
    private SearchView searchView;

    private int userId;
    private ImageView profileIcon;
    private SharedPreferences sharedPreferences;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        profileIcon = root.findViewById(R.id.imageView5);
        sharedPreferences = getActivity().getSharedPreferences("UserSession", getActivity().MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", -1);

        // Retrieve the profile image from SharedPreferences
        String encodedImage = sharedPreferences.getString("profileImage", null);

        if (encodedImage != null) {
            // Decode the Base64 string back to a Bitmap
            byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap decodedImage = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            // Set the image to the ImageView
            profileIcon.setImageBitmap(decodedImage);
        }
        // SearchView setup
        searchView = binding.searchView1; // Ensure ID matches XML
        if (searchView != null) {
            searchView.setQueryHint("Search Movies...");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (movieAdapter != null) { // Prevent NullPointerException
                        movieAdapter.filterMovies(newText);
                    }
                    return true;
                }
            });
        }

        // Setup RecyclerView with horizontal layout manager
        RecyclerView recyclerView = binding.recyclerView2;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        theaterAdapter = new userTheaterAdapter(getContext(), new ArrayList<>());
        recyclerView.setAdapter(theaterAdapter);
        theaterAdapter.fetchTheater();

        // Load movies
        RecyclerView recyclerView1 = binding.loadmovie;
        recyclerView1.setLayoutManager(new GridLayoutManager(getContext(), 2));

        movieAdapter = new userMovieAdapter(getContext(), new ArrayList<>());
        recyclerView1.setAdapter(movieAdapter);

        movieAdapter.fetchWatchlist();
        movieAdapter.fetchMovies(userId);

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
        runnable = () -> {
            int currentItem = viewPager.getCurrentItem();
            int nextItem = (currentItem + 1) % imageResources.size();
            viewPager.setCurrentItem(nextItem, true);
            handler.postDelayed(runnable, 3000);
        };

        handler.postDelayed(runnable, 3000);

        // Set OnClickListener to imageView5
        View imageView = binding.imageView5;
        if (imageView != null) { // Prevent NullPointerException
            imageView.setOnClickListener(v -> openProfileUpdate());
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
        binding = null;
    }

    private void openProfileUpdate() {
        Intent intent = new Intent(getActivity(), Profile.class);
        startActivity(intent);
    }
}
