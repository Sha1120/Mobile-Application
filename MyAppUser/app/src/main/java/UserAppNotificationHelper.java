import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.myuser.MainActivity;
import com.example.myuser.R;
import com.example.myuser.UserLogin;

public class UserAppNotificationHelper {

    public static void showNotification(Context context, String title, String message, int userId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    "A2", "Chanel1", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        // Create an Intent to launch your User activity when notification is clicked
        Intent intent = new Intent(context, MainActivity.class);  // Modify to desired activity
        intent.putExtra("userId", userId);  // Pass userId to the activity
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create the notification
        Notification notification = new NotificationCompat.Builder(context, "A2")
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.notification)
                .setContentIntent(pendingIntent)  // Set the PendingIntent
                .setAutoCancel(true)  // Dismiss notification on click
                .build();

        // Notify with a unique user ID
        notificationManager.notify(userId, notification);
    }
}
