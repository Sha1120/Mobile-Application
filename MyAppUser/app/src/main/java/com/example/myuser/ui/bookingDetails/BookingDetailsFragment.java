package com.example.myuser.ui.bookingDetails;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myuser.Config;
import com.example.myuser.R;
import com.example.myuser.databinding.FragmentBookingDetailsBinding;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import adapters.BookingDetailsAdapter;
import model.Booking;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BookingDetailsFragment extends Fragment {

    private FragmentBookingDetailsBinding binding;
    private RecyclerView recyclerView;
    private BookingDetailsAdapter adapter;
    private List<Booking> bookingList; // Initialize bookingList here
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Initialize ViewModel and Binding
        BookingDetailsViewModel galleryViewModel =
                new ViewModelProvider(this).get(BookingDetailsViewModel.class);

        binding = FragmentBookingDetailsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Get SharedPreferences to access user ID
        sharedPreferences = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", -1);

        // Initialize RecyclerView and bookingList
        bookingList = new ArrayList<>(); // Initialize the list before using it
        recyclerView = root.findViewById(R.id.recycler_view_booking_details);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Pass the fragment's context and bookingList to the adapter
        adapter = new BookingDetailsAdapter(getContext(), bookingList);
        recyclerView.setAdapter(adapter);

        // Fetch booking details based on user ID
        fetchBookingDetails(userId);

        return root;
    }

    // Method to fetch booking details from backend using OkHttp
    public void fetchBookingDetails(int userId) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(Config.baseUrl + "/LoadBookingHistory?user_id=" + userId)  // URL updated to match servlet endpoint
                    .build();

            try {
                Response response = client.newCall(request).execute();

                // Handle server error or empty response
                if (!response.isSuccessful() || response.body() == null) {
                    getActivity().runOnUiThread(() -> showToast("Error fetching bookings from server"));
                    return;
                }

                String responseData = response.body().string();
                Log.d("BookingDetails", "Response Data: " + responseData);

                if (responseData.isEmpty()) {
                    getActivity().runOnUiThread(() -> showToast("No bookings found."));
                    return;
                }

                // Parse JSON response
                JsonArray bookingsArray = JsonParser.parseString(responseData).getAsJsonArray();
                List<Booking> userBookings = new ArrayList<>();
                Gson gson = new Gson();

                for (JsonElement bookingElement : bookingsArray) {
                    try {
                        Booking booking = gson.fromJson(bookingElement, Booking.class);
                        userBookings.add(booking);
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                    }
                }

                // Update UI on Main Thread
                getActivity().runOnUiThread(() -> {
                    if (!userBookings.isEmpty()) {
                        bookingList.clear();
                        bookingList.addAll(userBookings);
                        adapter.notifyDataSetChanged();
                    } else {
                        showToast("No bookings found.");
                    }
                });

            } catch (IOException | JsonSyntaxException e) {
                getActivity().runOnUiThread(() -> showToast("Error fetching bookings"));
            }
        }).start();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show(); // Fixed Toast context
    }
}
