package lk.shashi.myadmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;

import model.Admin;

public class Home2 extends AppCompatActivity {

    private Button manageMovie, managebooking,manageTheater,manageLoging,viewreport,addreport;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent i = getIntent();
        String adminJsonText = i.getStringExtra("adminJson");

        Gson gson = new Gson();
        Admin admin = gson.fromJson(adminJsonText,Admin.class);
        TextView textView1 = findViewById(R.id.movieTitle);
        textView1.setText(admin.getFname()+ " "+admin.getLname());


        Button createCategory = findViewById(R.id.createCategory);
        createCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Home2.this,CreateCategory.class);

                startActivity(i);
            }
        });

        manageMovie = findViewById(R.id.button3);
        manageMovie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i1 = new Intent(Home2.this,ManageAllMovie.class);
                startActivity(i1);
            }
        });

        managebooking = findViewById(R.id.button4);
        managebooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i2 = new Intent(Home2.this,BookinDetails.class);
                startActivity(i2);
            }
        });

        manageTheater = findViewById(R.id.managetheater);
        manageTheater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i3 = new Intent(Home2.this,ManageTheaters.class);
                startActivity(i3);
            }
        });

        addreport = findViewById(R.id.addreportdata);
        addreport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i4 = new Intent(Home2.this,AddDataToFirebaseActivity.class);
                startActivity(i4);
            }
        });

        viewreport = findViewById(R.id.viewReport);
        viewreport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i4 = new Intent(Home2.this,ChartJava.class);
                startActivity(i4);
            }
        });

        manageLoging = findViewById(R.id.adminlogout);
        manageLoging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i4 = new Intent(Home2.this,ManageLoging.class);
                startActivity(i4);
            }
        });



    }
}