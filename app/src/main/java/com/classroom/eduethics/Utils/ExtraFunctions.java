package com.classroom.eduethics.Utils;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.classroom.eduethics.Activity.ImagePage;
import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Services.FireMessage;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;

import org.json.JSONArray;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtraFunctions {

    public static void downloadImageAndShow(View progress,String imageurl,Activity activity) {
        progress.setVisibility(View.VISIBLE);
        FirebaseStorage.getInstance().getReference().child(imageurl)
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    progress.setVisibility(View.GONE);
                    if (activity == null) return;
                    if (imageurl.endsWith("pdf")) {
                        Intent intent = new Intent(activity, ImagePage.class);
                        intent.putExtra("urlImage", uri).putExtra("type", "pdf");
                        activity.startActivity(intent);
                    } else if (imageurl.endsWith("png") || imageurl.endsWith("jpg") || imageurl.endsWith("jpeg")) {
                        Intent intent = new Intent(activity, ImagePage.class);

                        intent.putExtra("urlImage", uri).putExtra("type", "image");
                        activity.startActivity(intent);
                    }
                }).addOnFailureListener(e -> {
            if (activity == null) return;
            progress.setVisibility(View.GONE);
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
        });

    }

    public static String getReadableTime(int hourOfDay, int minute) {
        if (hourOfDay > 11) {
            return (((hourOfDay - 12) < 10 ? "0" : "") + (hourOfDay - 12) + ":" + (minute < 10 ? "0" : "") + minute + " PM");
        } else {
            return ((hourOfDay < 10 ? "0" : "") + hourOfDay + ":" + (minute < 10 ? "0" : "") + minute + " AM");
        }
    }


    public static String getDurationFromLong(String s1, String s2, boolean wantInOnlySimple) {
        long l;
        if (s2.equals("-")) return "No Entry";
        else l = Long.parseLong(s2) - Long.parseLong(s1);

        long s = l / 1000;
        long m = s / 60;
        long h = m / 60;
        s %= 60;
        m %= 60;
        if (wantInOnlySimple) {
            return h + ":" + m + ":" + s;
        } else
            return (h == 0 ? "" : h + " hr ") + (m == 0 ? "" : m + " min ") + (s == 0 ? "" : s + " sec");

    }

    public static String getTimeFromLong(String s) {
        /*MM/dd/yyyy-*/
        String s2 = DateFormat.format("kk:mm", new java.sql.Date(Long.parseLong(s))).toString();
        return getReadableTime(Integer.parseInt(s2.split(":")[0]), Integer.parseInt(s2.split(":")[1]));
    }

    public static String getReadableDate(int date, int month, int year) {
        return date + "/" + month + "/" + year;
    }

    public static String getReadableDateInString(int date, int month, int year) {
        return date + " " + LocalConstants.MONTHS[month] + " " + year;
    }

    public static void datePicker(Context context, TextView v, int[] d) {
        DatePickerDialog dialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                v.setText(ExtraFunctions.getReadableDate(dayOfMonth, month + 1, year));
                d[0] = dayOfMonth;
                d[1] = month + 1;
                d[2] = year;
            }
        },
                Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMinDate(new Date().getTime());
        dialog.show();
    }

    public static void timePicker(Context context, TextView v, int[] t) {
        new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                v.setText(ExtraFunctions.getReadableTime(hourOfDay, minute));
                t[0] = hourOfDay;
                t[1] = minute;
            }
        }, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), false).show();
    }

    public static void sendNoti(String id, String title, String body, Context context) {
        FirebaseFirestore.getInstance()
                .collection(GlobalVariables.isStudent ? "teacher" : "students")
                .document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    try {
                        new FireMessage(title, body, context).sendToToken(documentSnapshot.getString("token"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        Map<String, String> map = new HashMap<>();
        map.put("title", title);
        map.put("body", body);
        Map<String, Object> forUpload = new HashMap<>();
        forUpload.put(Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "-" + Calendar.getInstance().getTimeInMillis() + "", map);
        FirebaseFirestore.getInstance()
                .collection(GlobalVariables.isStudent ? "teacher" : "students")
                .document(id)
                .collection("noti")
                .document(Calendar.getInstance().get(Calendar.MONTH) + "-" + Calendar.getInstance().get(Calendar.YEAR))
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            FirebaseFirestore.getInstance()
                                    .collection(GlobalVariables.isStudent ? "teacher" : "students")
                                    .document(id)
                                    .collection("noti")
                                    .document(Calendar.getInstance().get(Calendar.MONTH) + "-" + Calendar.getInstance().get(Calendar.YEAR))
                                    .set(forUpload, SetOptions.merge());
                        } else {
                            FirebaseFirestore.getInstance()
                                    .collection(GlobalVariables.isStudent ? "teacher" : "students")
                                    .document(id)
                                    .collection("noti")
                                    .document(Calendar.getInstance().get(Calendar.MONTH) + "-" + Calendar.getInstance().get(Calendar.YEAR))
                                    .set(forUpload);
                        }
                    }
                });

    }


    public static void sendNoti(List<String> id, String title, String body, Context context) {
        JSONArray mobileTokens = new JSONArray();


        Map<String, String> map = new HashMap<>();
        map.put("title", title);
        map.put("body", body);
        Map<String, Object> forUpload = new HashMap<>();
        forUpload.put(Calendar.getInstance().getTimeInMillis() + "", map);


        for (int i = 0; i < id.size(); i++) {
            FirebaseFirestore.getInstance()
                    .collection(GlobalVariables.isStudent ? "teacher" : "students")
                    .document(id.get(i))
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        mobileTokens.put(documentSnapshot.getString("token"));
                        if (mobileTokens.length() == id.size()) {
                            try {
                                new FireMessage(title, body, context).sendToGroup(mobileTokens);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

            int finalI = i;
            FirebaseFirestore.getInstance()
                    .collection(GlobalVariables.isStudent ? "teacher" : "students")
                    .document(id.get(i))
                    .collection("noti")
                    .document("data")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                FirebaseFirestore.getInstance()
                                        .collection(GlobalVariables.isStudent ? "teacher" : "students")
                                        .document(id.get(finalI))
                                        .collection("noti")
                                        .document("data")
                                        .set(forUpload, SetOptions.merge());
                            } else {
                                FirebaseFirestore.getInstance()
                                        .collection(GlobalVariables.isStudent ? "teacher" : "students")
                                        .document(id.get(finalI))
                                        .collection("noti")
                                        .document("data")
                                        .set(forUpload);
                            }
                        }
                    });
        }


    }


}
