package Service;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;

public class MyFirebaseInstanceIDService extends FirebaseMessagingService {

    public static final String TOKEN_BROADCAST = "myfcmtokenbroadcast";
    @Override
    public void onNewToken(@NonNull String refeshtoken) {
        Log.d("homehttp", "Refreshed token: " + refeshtoken);

        // If you want to send messages to this application instance or
        // manage this app's subscriptions on the server side, send the
        // FCM registration token to your app server.
        //sendRegistrationToServer(token);

        getApplicationContext().sendBroadcast(new Intent(TOKEN_BROADCAST));
        storeToken(refeshtoken);
    }

    private void storeToken(String token){
        SharedPrefManager.getInstance(getApplicationContext()).storeToken(token);

    }
}
