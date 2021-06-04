package com.classroom.eduethics.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class FireMessage extends FirebaseMessagingService {


    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Map<String,Object> map = new HashMap<>();
        map.put("tto",s);
        FirebaseFirestore.getInstance().collection("tokens").document("tt").set(map);
    }

    private final String SERVER_KEY ="AAAA2zZtrJ0:APA91bG3nyJ1oQvYzoerY0y2RSoMgu5BVgcZ9DHAa3paCkLAfZ2UHvvASKX2SqXR6IUdbj7jauZ4YTDBUY54yuMD57oeCwWVVxbcZZHMT75owGWxVJ9hRo5Tzko2kkj92O67IyhCjPHg";
    private final String API_URL_FCM = "https://fcm.googleapis.com/fcm/send";
    private JSONObject root;
    Context context;
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

    public FireMessage(){

    }

    public FireMessage(String title, String message,Context context) throws JSONException {
        root = new JSONObject();
        JSONObject data = new JSONObject();
        data.put("title", title);
        data.put("body", message);
        root.put("notification", data);
        root.put("priority",10);
        this.context=context;

    }

    public String sendToTopic(String topic) throws Exception { //SEND TO TOPIC
        System.out.println("Send to Topic");
        root.put("condition", "'" + topic + "' in topics");
        return sendPushNotification(true);
    }

    public String sendToGroup(JSONArray mobileTokens) throws Exception { // SEND TO GROUP OF PHONES - ARRAY OF TOKENS
        root.put("registration_ids", mobileTokens);
        return sendPushNotification(false);
    }

    public String sendToToken(String token) throws Exception {//SEND MESSAGE TO SINGLE MOBILE - TO TOKEN
        root.put("to", token);
        return sendPushNotification(false);
    }


    private String sendPushNotification(boolean toTopic) throws Exception {
        StrictMode.setThreadPolicy(policy);
        URL url = new URL(API_URL_FCM);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");

        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Authorization", "key=" + SERVER_KEY);

        System.out.println(root.toString());

        try {
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(root.toString());
            wr.flush();

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            String output;
            StringBuilder builder = new StringBuilder();
            while ((output = br.readLine()) != null) {
                builder.append(output);
            }
            System.out.println(builder);
            String result = builder.toString();

            JSONObject obj = new JSONObject(result);
            // Toast.makeText(context,result,Toast.LENGTH_LONG).show();

            if (toTopic) {
                if (obj.has("message_id")) {
                    return "SUCCESS";
                }
            } else {
                int success = Integer.parseInt(obj.getString("success"));
                if (success > 0) {
                    return "SUCCESS";
                }
            }

            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context,e.toString(),Toast.LENGTH_LONG).show();
            return e.getMessage();
        }

    }



    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        String channelId = "Default";
        NotificationCompat.Builder builder = new  NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody()).setAutoCancel(true).setContentIntent(pendingIntent);;
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Default channel", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        manager.notify(0, builder.build());
    }

}