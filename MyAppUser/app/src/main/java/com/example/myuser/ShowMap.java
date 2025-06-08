package com.example.myuser;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ShowMap extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng selectedLocationLatLng;
    private String theaterName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_map);

        // Get the Intent data
        Intent intent = getIntent();
        theaterName = intent.getStringExtra("name");
        String latLngString = intent.getStringExtra("latlan"); // Expecting JSON string {"latitude":6.92, "longitude":79.85}
        Log.d("homehttp", "latlan: " + latLngString);

        if (latLngString != null) {
            try {
                JSONObject jsonObject = new JSONObject(latLngString);
                double latitude = jsonObject.getDouble("latitude");
                double longitude = jsonObject.getDouble("longitude");

                Log.d("homehttp", "Parsed Latitude: " + latitude);
                Log.d("homehttp", "Parsed Longitude: " + longitude);

                // Create LatLng object
                selectedLocationLatLng = new LatLng(latitude, longitude);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Invalid location format!", Toast.LENGTH_SHORT).show();
                selectedLocationLatLng = null;
            }
        }

        // Initialize Map Fragment properly
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);  // Register the callback
        } else {
            Log.e("homehttp", "Map Fragment is null! Check XML file.");
            Toast.makeText(this, "Error loading map!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (selectedLocationLatLng != null) {
            // Add a marker at the given coordinates
            mMap.addMarker(new MarkerOptions()
                    .position(selectedLocationLatLng)
                    .title(theaterName != null ? theaterName : "Theater Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            // Move the camera to the marker
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedLocationLatLng, 15));

            // Get address using Geocoder
            getAddressFromLatLng(selectedLocationLatLng);
        } else {
            // Default Location (Colombo)
            LatLng defaultLocation = new LatLng(6.928988746039056, 79.85242394011613);
            mMap.addMarker(new MarkerOptions()
                    .position(defaultLocation)
                    .title("Default Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
        }
    }

    private void getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        new Thread(() -> {
            try {
                List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addressList != null && !addressList.isEmpty()) {
                    Address address = addressList.get(0);
                    String fullAddress = address.getAddressLine(0);

                    Log.d("homehttp", "Geocoded Address: " + fullAddress);

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Location: " + fullAddress, Toast.LENGTH_LONG).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "No address found!", Toast.LENGTH_SHORT).show();
                        Log.e("homehttp", "Geocoder returned empty result.");
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Geocoder error!", Toast.LENGTH_SHORT).show());
                Log.e("homehttp", "Geocoder error: " + e.getMessage());
            }
        }).start();
    }
}
