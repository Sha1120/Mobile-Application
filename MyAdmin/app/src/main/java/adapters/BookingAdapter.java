package adapters;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lk.shashi.myadmin.Config;
import lk.shashi.myadmin.R;
import model.Booking;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

    private Context context;
    private List<Booking> bookingList;

    public BookingAdapter(Context context, List<Booking> bookingList) {
        this.context = context;
        this.bookingList = (bookingList != null) ? bookingList : new ArrayList<>(); // Ensure it's initialized
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.booking_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        holder.movieTitle.setText(booking.getMovie().getTitle());
        holder.mobilenumber.setText(booking.getUser().getMobile());
        holder.email.setText(booking.getUser().getEmail());
        holder.slot.setText(booking.getSlot());
        holder.theater.setText(booking.getCinema().getName());
        holder.date.setText(String.valueOf(booking.getDate()));
        holder.price.setText(String.valueOf((int) booking.getMovie().getPrice()));
        holder.nooftickets.setText(String.valueOf(1));


        // Set initial button text and color
        if (booking.getStatus() == 0) {
            holder.bookingStatus.setText("Pending");
            holder.bookingStatus.setBackgroundColor(Color.BLACK);
        } else {
            holder.bookingStatus.setText("Confirm");
            holder.bookingStatus.setBackgroundColor(Color.GRAY);
        }

        // Add Click Listener to Change Status
        holder.bookingStatus.setOnClickListener(v -> {
            if ("Pending".equalsIgnoreCase(holder.bookingStatus.getText().toString())) {
                holder.bookingStatus.setText("Send Warning....");
                holder.bookingStatus.setBackgroundColor(Color.BLUE);
                holder.bookingStatus.setEnabled(false);

                int userId = booking.getUser().getId();
                String movieName = booking.getMovie().getTitle();
                sendNotification(userId,movieName);

            }
        });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView movieTitle, mobilenumber, email, slot, theater, date, price, nooftickets;
        Button bookingStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            movieTitle = itemView.findViewById(R.id.movieTitle);
            mobilenumber = itemView.findViewById(R.id.mobilenumber);
            email = itemView.findViewById(R.id.email);
            slot = itemView.findViewById(R.id.slot);
            theater = itemView.findViewById(R.id.theater);
            date = itemView.findViewById(R.id.date);
            price = itemView.findViewById(R.id.price);
            nooftickets = itemView.findViewById(R.id.nooftickets);
            bookingStatus = itemView.findViewById(R.id.bookingStatus);
        }
    }

    // Method to fetch bookings from server
    public void fetchBookings() {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(Config.baseUrl+"/LoadBookingDetails")
                    .build();

            try {
                Response response = client.newCall(request).execute();

                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("homehttp", "Server Error: " + response.code());
                    showToast("Error fetching products from server");
                    return;
                }

                String responseData = response.body().string();
                Log.d("homehttp", "Raw API Response: " + responseData);

                if (!responseData.trim().startsWith("[")) {
                    Log.e("homehttp", "Unexpected response format: " + responseData);
                    showToast("Invalid response format from server");
                    return;
                }

                Gson gson = new Gson();
                Type productListType = new TypeToken<List<Booking>>() {}.getType();
                List<Booking> bookings = gson.fromJson(responseData, productListType);

                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() -> {
                        if (bookings != null && !bookings.isEmpty()) {
                            bookingList.clear();
                            bookingList.addAll(bookings);
                            notifyDataSetChanged();
                        } else {
                            showToast("No products found.");
                        }
                    });
                }
            } catch (IOException e) {
                Log.e("homehttp", "API Request Failed", e);
                showToast("Error fetching products");
            }
        }).start();
    }

    // Helper method to show toast from background thread
    private void showToast(String message) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            );
        }
    }


    private void sendNotification(int userId, String movieName){

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("userId",userId);
        hashMap.put("moviename",movieName);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("notification").add(hashMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("homehttp","send success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("homehttp","send failed");
                    }
                });

    }
}
