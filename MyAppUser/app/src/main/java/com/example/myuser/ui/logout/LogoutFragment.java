package com.example.myuser.ui.logout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.myuser.MainActivity;
import com.example.myuser.R;
import com.example.myuser.UserLogin;

public class LogoutFragment extends Fragment {

    private Button logoutButton, loginButton;  // Declare loginButton

    public static LogoutFragment newInstance() {
        return new LogoutFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_logout, container, false);

        // Find the logout and login buttons from the layout
        logoutButton = rootView.findViewById(R.id.logout_button);
        loginButton = rootView.findViewById(R.id.login_button);  // Initialize loginButton

        // Set the logout button click listener
        logoutButton.setOnClickListener(v -> logoutUser());

        // Set the login button click listener
        loginButton.setOnClickListener(v -> loginUser());

        return rootView;
    }

    // Method to log out the user
    private void logoutUser() {
        // Clear the user data from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();  // Clear all data
        editor.apply();  // Apply the changes

        // Start the LoginActivity after logout
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  // Clear activity stack
        startActivity(intent);

        // Finish current activity (MainActivity)
        getActivity().finish();
    }

    private void loginUser() {
        Log.d("homehttp", "Login button clicked"); // Add a log to confirm the button click
        Intent intent = new Intent(getActivity(), UserLogin.class);  // Start UserLogin Activity
        startActivity(intent);
    }

}
