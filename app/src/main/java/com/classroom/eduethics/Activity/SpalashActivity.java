package com.classroom.eduethics.Activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.trusted.splashscreens.SplashScreenParamKey;

import com.classroom.eduethics.R;
import com.classroom.eduethics.Services.FireMessage;
import com.classroom.eduethics.Utils.GlobalVariables;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class SpalashActivity extends AppCompatActivity {

    SharedPreferences prefs;
    private Thread mSplashThread;
    WebView we;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spalash);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        else if (getActionBar() != null) getActionBar().hide();



        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SpalashActivity.this, "Error(220)(Authorize): Please Check your SHA256 on Google Play Console ", Toast.LENGTH_SHORT).show();
                     //   SpalashActivity.this.moveTaskToBack(true);
                    }
                });

            }
        }).start();


        prefs = getApplicationContext().getSharedPreferences("USER_PREF",
                Context.MODE_PRIVATE);


        final Intent[] intent = new Intent[1];
        if (prefs.getString("type", "none").equals("none")) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            intent[0] = new Intent(SpalashActivity.this, LoginActivity.class);
                            intent[0].setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent[0], ActivityOptions.makeSceneTransitionAnimation(SpalashActivity.this).toBundle());
                        }
                    });
                }
            }).start();


        } else {
            intent[0] = new Intent(SpalashActivity.this, MainActivity.class);
            intent[0].setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            notification(intent[0]);
        }


    }

    private void notification(Intent intent) {


        GlobalVariables.uid = prefs.getString("uid", "none");
        if (FirebaseMessaging.getInstance() != null)
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(SpalashActivity.this).toBundle());
                            SpalashActivity.this.finish();
                            return;
                        }

                        Map<String, Object> map = new HashMap<>();
                        map.put("token", task.getResult());

                        FirebaseFirestore.getInstance()
                                .collection(prefs.getString("type", "none").equals("teacher") ? "teacher" : "students")
                                .document(GlobalVariables.uid)
                                .update(map)
                                .addOnSuccessListener(aVoid -> {
                                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(SpalashActivity.this).toBundle());
                                    SpalashActivity.this.finish();
                                }).addOnFailureListener(e -> {
                            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(SpalashActivity.this).toBundle());
                            SpalashActivity.this.finish();
                        });
                    }).addOnFailureListener(e -> {
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(SpalashActivity.this).toBundle());
                SpalashActivity.this.finish();
            });
        else {
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(SpalashActivity.this).toBundle());
        }
    }




}