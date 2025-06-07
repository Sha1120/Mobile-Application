package adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myuser.Config;
import com.example.myuser.R;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.BookedSeat;
import model.Theater;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.SeatViewHolder> {

    private List<BookedSeat> bookedSeatList;
    private Context context;

    public SeatAdapter(Context context, List<BookedSeat> bookedSeatList) {
        this.context = context;
        this.bookedSeatList = bookedSeatList;
    }

    @NonNull
    @Override
    public SeatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each seat
        View view = LayoutInflater.from(context).inflate(R.layout.seat_item, parent, false);
        return new SeatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeatViewHolder holder, int position) {
        BookedSeat bookedSeat = bookedSeatList.get(position);
        //Log.d("SeatAdapter", "Seat Number from API: " + bookedSeat.getNumber());
        holder.seatno.setText(bookedSeat.getNumber());

        // Check if the seat is booked (status = 1) and update background color
        if (bookedSeat.getStatus() == 1) {

            holder.card.setCardBackgroundColor(Color.RED);
            holder.seatButton.setEnabled(false); // This will disable the seat button so it cannot be clicked
        } else if(bookedSeat.getStatus() == 2){
            holder.card.setCardBackgroundColor(Color.YELLOW);
            holder.seatButton.setEnabled(false);
        }else  {

            holder.card.setCardBackgroundColor(Color.GRAY); // Default color for available seats
            holder.seatButton.setEnabled(true);

            holder.seatButton.setOnClickListener(view -> {

                if (holder.card.getCardBackgroundColor().getDefaultColor() == Color.GRAY) {
                    holder.card.setCardBackgroundColor(Color.BLUE);
                    saveSelectedSeat(bookedSeat.getId(), bookedSeat.getNumber());
                } else {
                    holder.card.setCardBackgroundColor(Color.GRAY);
                    removeSelectedSeat(bookedSeat.getId(), bookedSeat.getNumber());
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return bookedSeatList.size();
    }

    static class SeatViewHolder extends RecyclerView.ViewHolder {
        ImageButton seatButton;
        CardView card;
        TextView seatno;

        public SeatViewHolder(@NonNull View itemView) {
            super(itemView);
            seatButton = itemView.findViewById(R.id.seatbtn);  // Ensure this ID matches your layout
            card = itemView.findViewById(R.id.cardView);
            seatno = itemView.findViewById(R.id.textView3);  // Ensure this ID matches your layout
        }
    }

    public void fetchSeates() {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(Config.baseUrl + "/LoadSeats")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("homehttp", "Server Error: " + response.code());
                    showToast("Error fetching seats from server");
                    return;
                }

                String responseData = response.body().string();
                Log.d("homehttp", "Raw API Response: " + responseData);

                // Validate JSON
                if (responseData.isEmpty()) {
                    showToast("Empty response from server");
                    return;
                }

                // Parse JSON
                JsonArray seatsArray = JsonParser.parseString(responseData).getAsJsonArray();
                List<BookedSeat> seats = new ArrayList<>();
                Gson gson = new Gson();

                for (JsonElement seatElement : seatsArray) {
                    try {
                        BookedSeat bookedSeat = gson.fromJson(seatElement, BookedSeat.class);
                        seats.add(bookedSeat);
                    } catch (JsonSyntaxException e) {
                        Log.e("homehttp", "JSON Parse Error", e);
                    }
                }

                // Update UI on Main Thread
                runOnUiThread(() -> {
                    if (!seats.isEmpty()) {
                        bookedSeatList.clear();
                        bookedSeatList.addAll(seats);
                        notifyDataSetChanged();
                    } else {
                        showToast("No seats found.");
                    }
                });

            } catch (IOException | JsonSyntaxException e) {
                Log.e("homehttp", "API Request Failed", e);
                showToast("Error fetching seats");
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

    // Add this method to the SeatAdapter class
    private void saveSelectedSeat(int seatId, String seatNumber) {
        SharedPreferences preferences = context.getSharedPreferences("selected_seats", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Get the existing selected seats from SharedPreferences
        Set<String> selectedSeats = preferences.getStringSet("seats", new HashSet<>());

        // Add the selected seat ID and number to the set
        selectedSeats.add(seatId + ":" + seatNumber);

        // Save the updated set back to SharedPreferences
        editor.putStringSet("seats", selectedSeats);
        editor.apply();
    }

    private void removeSelectedSeat(int seatId, String seatNumber) {
        SharedPreferences preferences = context.getSharedPreferences("selected_seats", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Get the existing selected seats from SharedPreferences
        Set<String> selectedSeats = preferences.getStringSet("seats", new HashSet<>());

        // Remove the selected seat ID and number from the set
        selectedSeats.remove(seatId + ":" + seatNumber);

        // Save the updated set back to SharedPreferences
        editor.putStringSet("seats", selectedSeats);
        editor.apply();
    }


}
