package com.example.myuser;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getNotification() != null) {

            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            Log.d("FCM", "Notification received: " + title + " - " + body);
        }


        if (remoteMessage.getData().size() > 0) {

            Log.d("FCM", "Data received: " + remoteMessage.getData());
        }
    }


    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d("FCM", "New token: " + token);

    }
}
