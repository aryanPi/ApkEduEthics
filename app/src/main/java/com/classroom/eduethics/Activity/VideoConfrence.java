package com.classroom.eduethics.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.Utils.GlobalVariables;
import com.facebook.react.modules.core.PermissionListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;
import org.jitsi.meet.sdk.JitsiMeetActivityDelegate;
import org.jitsi.meet.sdk.JitsiMeetActivityInterface;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;
import org.jitsi.meet.sdk.JitsiMeetView;
import org.jitsi.meet.sdk.JitsiMeetViewListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class VideoConfrence extends FragmentActivity implements JitsiMeetActivityInterface {
    private JitsiMeetView view;

    String classId;
    String date;
    String time;
    String entryTime;
    boolean isEnded = false;

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        JitsiMeetActivityDelegate.onActivityResult(
                this, requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        JitsiMeetActivityDelegate.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = new JitsiMeetView(this);

        String roomId = getIntent().getStringExtra("roomId");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");
        entryTime = getIntent().getStringExtra("entryTime");


        classId = getIntent().getStringExtra("classId");

        JitsiMeetUserInfo userInfo = new JitsiMeetUserInfo();
        userInfo.setDisplayName(GlobalVariables.name);

        JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                .setRoom("https://meet.jit.si/" + roomId)
                .setUserInfo(userInfo)
                .setSubject(HomeClassroom.model.getSubject())
                .setFeatureFlag("call-integration.enabled", false)
                .setFeatureFlag("add-people.enabled", true)
                .setFeatureFlag("calendar.enabled", false)
                .setFeatureFlag("close-captions.enabled", false)
                .setFeatureFlag("video-share.enabled", false)
                .setFeatureFlag("kick-out.enabled", false)
                .setFeatureFlag("raise-hand.enabled", GlobalVariables.isStudent)
                .setFeatureFlag("invite.enabled", false)
                .setFeatureFlag("live-streaming.enabled", true)
                .setFeatureFlag("meeting-name.enabled", false)
                .setFeatureFlag("recording.enabled", !GlobalVariables.isStudent)
                .setFeatureFlag("pip.enabled", true)
                .setFeatureFlag("help.enabled", false)
                .setFeatureFlag("meeting-password.enabled", !GlobalVariables.isStudent)
                .setWelcomePageEnabled(false)
                .setVideoMuted(GlobalVariables.isStudent)
                .build();


        JitsiMeetViewListener listener = new JitsiMeetViewListener() {

            @Override
            public void onConferenceJoined(Map<String, Object> map) {

            }

            @Override
            public void onConferenceTerminated(Map<String, Object> map) {
                endTask();
            }

            @Override
            public void onConferenceWillJoin(Map<String, Object> map) {

            }
        };

        view.join(options);
        view.setListener(listener);


        setContentView(view);
    }

    private void endTask() {
        if (!isEnded)
            if (!GlobalVariables.isStudent)
                FirebaseFirestore.getInstance()
                        .collection("classroom")
                        .document(HomeClassroom.classId)
                        .collection("liveClasses")
                        .document("status")
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                isEnded = true;
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        isEnded = true;
                        finish();
                    }
                });
            else {
                FirebaseFirestore.getInstance()
                        .collection("attendence")
                        .document(HomeClassroom.classId)
                        .collection(date)
                        .document(time)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Map<String, Object> preData = new HashMap<>();
                                if (documentSnapshot.getData() != null) {
                                    preData = documentSnapshot.getData();
                                } else {
                                    isEnded = true;
                                    finish();
                                }

                                List<Map<String, Object>> ownData = new ArrayList<>();
                                if (preData.size() != 0)
                                    if (preData.get(GlobalVariables.uid) != null)
                                        ownData = (List) preData.get(GlobalVariables.uid);
                                    else {
                                        isEnded = true;
                                        finish();
                                    }
                                else {
                                    isEnded = true;
                                    finish();
                                }


                                for (Map<String, Object> obj : ownData)
                                    if (obj.get("entryTime").toString().equals(entryTime)) {
                                        obj.put("exitTime", Calendar.getInstance().getTimeInMillis() + "");
                                        break;
                                    }

                                preData.put(GlobalVariables.uid, ownData);

                                FirebaseFirestore.getInstance()
                                        .collection("attendence")
                                        .document(HomeClassroom.classId)
                                        .collection(date)
                                        .document(time)
                                        .update(preData)
                                        .addOnSuccessListener(aVoid -> finish()).addOnFailureListener(e -> {
                                    isEnded = true;
                                    finish();
                                });
                            }
                        })
                        .addOnFailureListener(e -> {
                            isEnded = true;
                            finish();
                        });

                FirebaseFirestore.getInstance()
                        .collection("students")
                        .document(GlobalVariables.uid)
                        .collection("attendance")
                        .document(HomeClassroom.classId)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                List<String> att = new ArrayList<>();
                                if (documentSnapshot.exists())
                                    if (documentSnapshot.get("data") != null)
                                        att = (List) documentSnapshot.get("data");
                                if (att != null) for (String s : att)
                                    if (s.split("-")[3].equals("p"))
                                        if (s.substring(0, s.indexOf("p") - 1).equals(date)) {
                                            att.remove(s);
                                            att.add(date + "-f");
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("data", att);
                                            FirebaseFirestore.getInstance()
                                                    .collection("students")
                                                    .document(GlobalVariables.uid)
                                                    .collection("attendance")
                                                    .document(HomeClassroom.classId)
                                                    .set(map);
                                            break;
                                        }
                            }
                        });
            }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        endTask();
        view.dispose();
        view = null;
        JitsiMeetActivityDelegate.onHostDestroy(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        JitsiMeetActivityDelegate.onNewIntent(intent);
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode,
            final String @NotNull [] permissions,
            final int @NotNull [] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        JitsiMeetActivityDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    protected void onResume() {
        super.onResume();

        JitsiMeetActivityDelegate.onHostResume(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        JitsiMeetActivityDelegate.onHostPause(this);
    }


    @Override
    public void requestPermissions(String[] strings, int i, PermissionListener permissionListener) {
        ActivityCompat.requestPermissions(this,
                strings,
                i);

    }
}