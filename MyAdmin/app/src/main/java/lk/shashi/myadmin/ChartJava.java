package lk.shashi.myadmin;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.FirebaseApp; // Import FirebaseApp
import java.util.ArrayList;

public class ChartJava extends AppCompatActivity {

    private PieChart pieChart;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this); // Initialize Firebase
            Log.d("Firebase", "Firebase Initialized");
        } else {
            Log.d("Firebase", "Firebase already initialized");
        }

        setContentView(R.layout.activity_chart_java);

        pieChart = findViewById(R.id.pieChart); // PieChart element
        databaseRef = FirebaseDatabase.getInstance().getReference("MyApp").child("reports"); // Firebase reference

        loadDataFromFirebase(); // Fetch data from Firebase
    }

    private void loadDataFromFirebase() {
        databaseRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ArrayList<PieEntry> entries = new ArrayList<>();
                    ArrayList<Integer> colors = new ArrayList<>();

                    // Loop through all categories in Firebase
                    for (DataSnapshot category : snapshot.getChildren()) {
                        String categoryName = category.getKey(); // Get category name (key)
                        Integer value = category.getValue(Integer.class); // Get category value (value)

                        if (categoryName != null && value != null) {
                            Log.d("FirebaseData", "Category: " + categoryName + " Value: " + value);

                            entries.add(new PieEntry(value, categoryName)); // Add PieEntry for each category

                            // Assign random color to each category slice
                            colors.add(Color.rgb((int) (Math.random() * 255),
                                    (int) (Math.random() * 255),
                                    (int) (Math.random() * 255)));
                        }
                    }

                    // Check if we have any data to display
                    if (entries.isEmpty()) {
                        Log.d("homehttp", "No data found for chart.");
                    } else {
                        // Create PieDataSet with entries and colors
                        PieDataSet dataSet = new PieDataSet(entries, "Movie Categories");
                        dataSet.setColors(colors); // Set slice colors
                        dataSet.setValueTextColor(Color.WHITE); // Set text color for values
                        dataSet.setValueTextSize(14f); // Set value text size

                        PieData data = new PieData(dataSet); // Create PieData with dataSet
                        pieChart.setData(data); // Set data to PieChart
                        pieChart.setUsePercentValues(true); // Use percentages for values
                        pieChart.getDescription().setEnabled(false); // Disable description text
                        pieChart.setEntryLabelColor(Color.BLACK); // Set label color
                        pieChart.setEntryLabelTextSize(12f); // Set label text size
                        pieChart.invalidate(); // Refresh the chart
                    }
                } else {
                    Log.d("FirebaseData", "No data available in the database.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Data Fetch Failed: " + error.getMessage());
            }
        });
    }
}
