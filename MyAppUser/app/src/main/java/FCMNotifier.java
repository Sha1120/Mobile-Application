import okhttp3.*;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class FCMNotifier {

    public static void sendNotification(String fcmToken, String message) throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        JSONObject messageJson = new JSONObject();
        JSONObject notification = new JSONObject();

        notification.put("title", "Payment Reminder");
        notification.put("body", message);

        messageJson.put("token", fcmToken);
        messageJson.put("notification", notification);

        json.put("message", messageJson);

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://fcm.googleapis.com/v1/projects/myapp-cfcfa/messages:send")
                .post(body)
                .addHeader("Authorization", "Bearer " + getAccessToken()) // Get Token from Service Account
                .addHeader("Content-Type", "application/json")
                .build();

        Response response = client.newCall(request).execute();
        System.out.println(response.body().string());
    }

    public static String getAccessToken() throws IOException {
        // Service Account JSON File path
        String serviceAccountPath = "C:\\Users\\USER\\AndroidStudioProjects\\MyUser\\app\\myapp-cfcfa-firebase-adminsdk-fbsvc-cf26a442ff.json";

        ProcessBuilder processBuilder = new ProcessBuilder(
                "gcloud", "auth", "application-default", "print-access-token");
        processBuilder.environment().put("GOOGLE_APPLICATION_CREDENTIALS", serviceAccountPath);

        Process process = processBuilder.start();
        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
        return reader.readLine().trim();
    }
}
