package lk.shashi.myadmin;

import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;

public class AdminNotificationService {

    private static final String SERVER_KEY = "AIzaSyA0OAAd-8W5FfusFN2DuP8UDQnZ8rYvV7Q"; // Firebase console -> Cloud Messaging -> Server key
    private static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";

    public static void sendPushNotification(String token, String title, String body, int userId) {
        try {
            // JSON object for FCM payload
            JSONObject json = new JSONObject();
            json.put("to", token);

            JSONObject notification = new JSONObject();
            notification.put("title", title);
            notification.put("body", body);
            json.put("notification", notification);

            JSONObject data = new JSONObject();
            data.put("userId", userId);
            json.put("data", data);

            // OkHttp Request
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(FCM_URL)
                    .post(requestBody)
                    .addHeader("Authorization", "key=" + SERVER_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    System.out.println("Notification sent: " + response.body().string());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
