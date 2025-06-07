package lk.shashi.myadmin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddDataToFirebaseActivity extends AppCompatActivity {

    private DatabaseReference databaseRef;
    private EditText editCategory, editValue;
    private Button btnAddCategory;
    private TextView statusText;
    private FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_data_to_firebase);

        // Initialize Firebase reference to the 'myapp/reports' node
        databaseRef = FirebaseDatabase.getInstance().getReference("MyApp").child("reports");


        // Initialize UI elements
        editCategory = findViewById(R.id.editCategory);
        editValue = findViewById(R.id.editValue);
        btnAddCategory = findViewById(R.id.btnAddCategory);
        statusText = findViewById(R.id.statusText);

        // Button click listener to add data to Firebase
        btnAddCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("homehttp", "Button clicked");
                String categoryName = editCategory.getText().toString();
                String valueStr = editValue.getText().toString();

                // Check if category name and value are not empty
                if (!categoryName.isEmpty() && !valueStr.isEmpty()) {
                    int value = Integer.parseInt(valueStr);

                    // Add data to Firebase under the 'myapp/reports' node
                    addDataToFirebase(categoryName, value);
                } else {
                    statusText.setText("Please fill both fields.");
                }
            }
        });
    }

    private void addDataToFirebase(String categoryName, int value) {
        // Add category and value to Firebase under 'myapp/reports'
        databaseRef.child(categoryName).setValue(value)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        statusText.setText("Data added successfully!");
                        Log.d("FirebaseData", categoryName + " category added with value: " + value);
                    } else {
                        statusText.setText("Failed to add data.");
                        Log.e("FirebaseData", "Failed to add category: " + categoryName, task.getException());
                    }
                });
    }
}
