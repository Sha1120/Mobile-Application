package com.example.myuser;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSessionManager {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public UserSessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public String getUserEmail() {
        return sharedPreferences.getString("userEmail", "");
    }

    public String getFirstName() {
        return sharedPreferences.getString("firstName", "");
    }

    public String getLastName() {
        return sharedPreferences.getString("lastName", "");
    }

    public String getMobile() {
        return sharedPreferences.getString("mobile", "");
    }

    public int getUserID() {
        return sharedPreferences.getInt("userId", -1);  // Retrieve userId as an int
    }
}
