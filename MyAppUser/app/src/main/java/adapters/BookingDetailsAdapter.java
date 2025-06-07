package adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myuser.Config;
import com.example.myuser.Payment;
import com.example.myuser.R;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import model.Booking;
import model.Seat;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BookingDetailsAdapter extends RecyclerView.Adapter<BookingDetailsAdapter.SeatViewHolder> {

    private List<Booking> bookingList;
    private Context context;

    private Activity activity;

    public BookingDetailsAdapter(Context context, List<Booking> bookingList) {
        this.context = context;
        this.bookingList = bookingList;

        // Ensure activity is set correctly
        if (context instanceof Activity) {
            this.activity = (Activity) context;
        }
    }

    @NonNull
    @Override
    public SeatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking_details, parent, false);
        return new SeatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeatViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        holder.movieTitle.setText(booking.getMovie() != null ? booking.getMovie().getTitle() : "Unknown Movie");
        holder.seatNumber.setText(booking.getSeat() != null ? booking.getSeat().getNumber() : "Unknown Seat");
        holder.TheaterName.setText(booking.getCinema() != null ? booking.getCinema().getName() : "Unknown Theater");
        holder.BookingDate.setText(booking.getDate() != null ? booking.getDate() : "Unknown Date");
        holder.TimeSlot.setText(booking.getSlot() != null ? booking.getSlot() : "Unknown Time");
        holder.Price.setText(booking.getMovie() != null ? String.valueOf(booking.getMovie().getPrice()) : "Unknown Price");

        int seatId = booking.getSeat().getId();
        Double price = booking.getMovie().getPrice();
        String movieTitle = booking.getMovie().getTitle();
        String seatNumber = booking.getSeat() != null ? booking.getSeat().getNumber() : "Unknown Seat";
        String theaterName = booking.getCinema() != null ? booking.getCinema().getName() : "Unknown Theater";
        String bookingDate = booking.getDate() != null ? booking.getDate() : "Unknown Date";
        String timeSlot = booking.getSlot() != null ? booking.getSlot() : "Unknown Time";
        int bookingId = booking.getId();
        Log.d("homehttp", "seatno" + seatId);

        // Check if the booking status is 1 (paid)
        if (booking.getStatus() == 1) {
            // Set Payment Button Text to 'Paid' and disable it
            holder.payBtn.setText("Paid");
            holder.payBtn.setTextColor(context.getResources().getColor(android.R.color.white)); // White text color
            holder.payBtn.setBackgroundColor(context.getResources().getColor(android.R.color.holo_red_dark)); // Red background color

            holder.payBtn.setEnabled(false);

            // Disable the Delete button
            holder.delete.setEnabled(false);
        } else {
            // Enable the Payment Button if not Paid
            holder.payBtn.setText("Payment");
            holder.payBtn.setEnabled(true);

            // Enable the Delete button
            holder.delete.setEnabled(true);
        }

        holder.NotyBtn.setOnClickListener(v -> {
            showBookingNotification(movieTitle, seatNumber, theaterName, bookingDate, timeSlot, price);
        });

        holder.payBtn.setOnClickListener(v -> {
            // Only allow payment if the booking is not paid
            if (booking.getStatus() != 1) {
                Intent paymentIntent = new Intent(context, Payment.class);
                paymentIntent.putExtra("amount", price);
                paymentIntent.putExtra("movieTitle", movieTitle);
                paymentIntent.putExtra("bookingId", bookingId);
                context.startActivity(paymentIntent);
            }
        });

        holder.delete.setOnClickListener(v -> {
            // Check seatId here, it is always initialized
            if (seatId != -1) {
                new AlertDialog.Builder(context)
                        .setIcon(R.drawable.comfirm)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete this booking?")
                        .setPositiveButton("OK", (dialog, which) -> {
                            deleteBookingFromBackend(bookingId, seatId, position);
                            Log.d("homehttp", "seatno" + seatId);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            } else {
                Log.e("BookingDetailsAdapter", "Seat ID is invalid or missing!");
            }
        });



    }

    private void deleteBookingFromBackend(int bookingId, int seatId, int position)            {
        OkHttpClient client = new OkHttpClient();

        // First, update the seat status to 0 (available)
        Request updateSeatRequest = new Request.Builder()
                .url(Config.baseUrl + "/UpdateSeateStatus?id=" + seatId) // Update seat status to 0 (available)
                .get() // Make sure this is a GET request to your backend
                .build();
        Log.d("homehttp","seatno"+ seatId);
        client.newCall(updateSeatRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                activity.runOnUiThread(() ->
                        Toast.makeText(activity, "Failed to update seat status", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Seat status updated successfully, now proceed with deleting the booking
                    Request deleteBookingRequest = new Request.Builder()
                            .url(Config.baseUrl + "/deleteBooking?id=" + bookingId)
                            .get() // Ensure the servlet uses GET requests for deletion
                            .build();

                    client.newCall(deleteBookingRequest).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                            activity.runOnUiThread(() ->
                                    Toast.makeText(activity, "Failed to delete booking", Toast.LENGTH_SHORT).show()
                            );
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                String responseBody = response.body().string();
                                activity.runOnUiThread(() -> {
                                    try {
                                        JSONObject jsonResponse = new JSONObject(responseBody);
                                        boolean success = jsonResponse.getBoolean("success");

                                        if (success) {
                                            // Remove the item from RecyclerView
                                            bookingList.remove(position);
                                            notifyItemRemoved(position);

                                            // Show success dialog
                                            new AlertDialog.Builder(context)
                                                    .setIcon(R.drawable.correct)
                                                    .setTitle("Success")
                                                    .setMessage("Booking deleted successfully.")
                                                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                                    .show();
                                        } else {
                                            String message = jsonResponse.getString("message");
                                            Toast.makeText(activity, "Failed: " + message, Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Toast.makeText(activity, "Error parsing response", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                activity.runOnUiThread(() ->
                                        Toast.makeText(activity, "Failed to delete booking", Toast.LENGTH_SHORT).show()
                                );
                            }
                        }
                    });
                } else {
                    activity.runOnUiThread(() ->
                            Toast.makeText(activity, "Failed to update seat status", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }





    @Override
    public int getItemCount() {
        return bookingList.size();
    }


    static class SeatViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        TextView movieTitle, seatNumber, TheaterName, BookingDate, TimeSlot, Price;
        ImageButton NotyBtn , delete;

        Button payBtn;

        public SeatViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardView);
            movieTitle = itemView.findViewById(R.id.moviename);
            seatNumber = itemView.findViewById(R.id.seatnumber);
            TheaterName = itemView.findViewById(R.id.theatername);
            BookingDate = itemView.findViewById(R.id.bookingdate);
            TimeSlot = itemView.findViewById(R.id.timeslot);
            Price = itemView.findViewById(R.id.price);
            NotyBtn = itemView.findViewById(R.id.notify);
            delete = itemView.findViewById(R.id.delete);
            payBtn = itemView.findViewById(R.id.paymentbtn);
        }
    }

    private void showBookingNotification(String movieTitle, String seatNumber, String theaterName, String bookingDate, String timeSlot, Double price) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "payment_notification_channel";

        // Creating the notification channel for devices running Android Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Payment Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Constructing the notification content
        String notificationContent = "Movie: " + movieTitle + "\n" +
                "Seat: " + seatNumber + "\n" +
                "Theater: " + theaterName + "\n" +
                "Date: " + bookingDate + "\n" +
                "Time: " + timeSlot + "\n" +
                "Price: LKR " + String.format("%.2f", price);

        // Creating the notification
        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.notification) // Customize your icon here
                .setContentTitle("Booking Details")
                .setContentText("Tap for more details!")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(notificationContent))  // Allows more text to be visible
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)  // Dismiss the notification once tapped
                .build();

        // Displaying the notification
        notificationManager.notify(0, notification);
    }
}
