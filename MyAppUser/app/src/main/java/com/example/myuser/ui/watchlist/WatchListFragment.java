package com.example.myuser.ui.watchlist;

import androidx.lifecycle.ViewModelProvider;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myuser.R;

import java.util.ArrayList;
import java.util.List;

import adapters.WatchListAdapter;
import model.Movie;

public class WatchListFragment extends Fragment {

    private WatchListViewModel mViewModel;
    private RecyclerView recyclerView;
    private WatchListAdapter watchListAdapter;
    private List<Movie> movieList = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private int userId;

    public static WatchListFragment newInstance() {
        return new WatchListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_watch_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(WatchListViewModel.class);
        // TODO: Use the ViewModel

        sharedPreferences = getActivity().getSharedPreferences("UserSession", getActivity().MODE_PRIVATE);
        userId = sharedPreferences.getInt("userId", -1);

        recyclerView = getView().findViewById(R.id.recyclerViewWatchList);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2); // 2 items per row
        recyclerView.setLayoutManager(gridLayoutManager);

        // Create the WatchListAdapter and set it to RecyclerView
        watchListAdapter = new WatchListAdapter(getContext(), movieList);
        recyclerView.setAdapter(watchListAdapter);

        // Fetch movies from the server
        watchListAdapter.fetchWatchlist();

    }

}