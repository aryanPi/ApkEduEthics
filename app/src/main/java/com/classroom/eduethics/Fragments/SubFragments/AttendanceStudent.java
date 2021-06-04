package com.classroom.eduethics.Fragments.SubFragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.Adapter.FragmentAdapter;
import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.GlobalVariables;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AttendanceStudent extends Fragment {


    View root;

    ViewPager2 pager2;
    FragmentAdapter adapter;
    TextView dateTitle;
    int currentMonthItem = 0;
    public static String TODAY = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "-" + Calendar.getInstance().get(Calendar.MONTH);


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_attendance_student, container, false);

        dateTitle = root.findViewById(R.id.dateTitle);
        pager2 = root.findViewById(R.id.pageViewerViewAttendence);
        MainActivity.TO_WHICH_FRAG = LocalConstants.TO_FRAG.TO_HOME_CLASSROOM;
        root.findViewById(R.id.pre).setOnClickListener(v -> {
            if (currentMonthItem != 0)
                pager2.setCurrentItem(currentMonthItem - 1);
        });


        root.findViewById(R.id.forward).setOnClickListener(v -> {
            if (currentMonthItem != 2)
                pager2.setCurrentItem(currentMonthItem + 1);
        });


        runTask();


        return root;
    }

    private void runTask() {
        FirebaseFirestore.getInstance()
                .collection("students")
                .document(GlobalVariables.uid)
                .collection("attendance")
                .document(HomeClassroom.classId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        List<String> datesActive = new ArrayList<>();
                        if (documentSnapshot.exists()) if (documentSnapshot.get("data") != null)
                            datesActive = (List) documentSnapshot.get("data");
                        forward(datesActive);
                    }
                });
    }

    private void forward(List<String> datesActive) {


        List<String> m1 = new ArrayList<>();
        List<String> m2 = new ArrayList<>();
        List<String> m3 = new ArrayList<>();
        List<String> m1sub = new ArrayList<>();
        List<String> m2sub = new ArrayList<>();
        List<String> m3sub = new ArrayList<>();

        addCalendarDates(m1, 0, m1sub);
        addCalendarDates(m2, 1, m2sub);
        addCalendarDates(m3, 2, m3sub);

        FragmentManager fm = getActivity().getSupportFragmentManager();
        ArrayList<Fragment> fragments = new ArrayList<>();

        fragments.add(new innerFrag(m3, datesActive, Calendar.getInstance().get(Calendar.MONTH) - 2));
        fragments.add(new innerFrag(m2, datesActive, Calendar.getInstance().get(Calendar.MONTH) - 1));
        fragments.add(new innerFrag(m1, datesActive, Calendar.getInstance().get(Calendar.MONTH)));

        adapter = new FragmentAdapter(fm, getLifecycle(), fragments);
        pager2.setAdapter(adapter);


        pager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentMonthItem = position;
                dateTitle.setText(LocalConstants.MONTHS[Calendar.getInstance().get(Calendar.MONTH) + position - 2]);
            }
        });

        pager2.setCurrentItem(2);


    }

    private void addCalendarDates(List<String> m1, int j, List<String> m1sub) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) - j, 1);
        int m = 0;
        for (int i = 7; i > 0; i--) {
            if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                m = calendar.get(Calendar.MONTH);
                m1sub.add(calendar.get(Calendar.DAY_OF_MONTH) + "-" + calendar.get(Calendar.MONTH));
            } else break;
        }

        List<String> l2 = new ArrayList<>();
        for (int i = 0; i < 42; i++) {
            m1.add(calendar.get(Calendar.DAY_OF_MONTH) + "-" + calendar.get(Calendar.MONTH));
            if (m1.get(m1.size() - 1).split("-")[1].equals((m + 2) + "")) {
                l2.add(calendar.get(Calendar.DAY_OF_MONTH) + "-" + calendar.get(Calendar.MONTH));
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        m1sub.addAll(l2);

    }


    public static class innerFrag extends Fragment {


        List<String> dates;
        int month;

        List<String> activeYellow = new ArrayList<>(), activeGreen = new ArrayList<>();

        public innerFrag(List<String> dates, List<String> activeDates, int month) {
            this.dates = dates;
            for (String activeDate : activeDates)
                if (activeDate.contains("p"))
                    activeYellow.add(activeDate.split("-")[0] + "-" + activeDate.split("-")[1]);
                else activeGreen.add(activeDate.split("-")[0] + "-" + activeDate.split("-")[1]);
            this.month = month;
        }


        View root;
        TableLayout tableMonth;
        Context context;


        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            root = inflater.inflate(R.layout.month_calandar_single, container, false);
            tableMonth = root.findViewById(R.id.tableMonth);
            context = getContext();

            int counter = 0;
            for (int j = 0; j < 6; j++) {
                TableRow tableRowHead = new TableRow(context);
                tableRowHead.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                tableRowHead.setPadding(0, 8, 0, 8);


                LinearLayout linearLayout = new LinearLayout(context);
                linearLayout.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout.setPadding(0, 8, 0, 8);
                for (int i = 0; i < 7; i++) {
                    TextView textView = new TextView(context);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                    params.gravity = Gravity.CENTER;
                    params.weight = 1;
                    textView.setGravity(Gravity.CENTER);
                    textView.setLayoutParams(params);
                    textView.setText(dates.get(counter).split("-")[0]);
                    if (dates.get(counter).split("-")[1].equals(month + ""))
                        textView.setTextColor(getResources().getColor(R.color.darkgrey));
                    else textView.setTextColor(getResources().getColor(R.color.lightGrey));
                    if (AttendanceStudent.TODAY.trim().equals(dates.get(counter).trim())) {
                        String text = dates.get(counter).split("-")[0];
                        SpannableString ss = new SpannableString(text);
                        ss.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        textView.setText(ss);
                        textView.setBackgroundColor(getResources().getColor(R.color.white));
                        textView.setTextSize(18);
                        textView.setTextColor(Color.BLACK);
                    } else {
                        textView.setTextSize(16);
                        textView.setBackgroundColor(Color.TRANSPARENT);
                    }
                    if (activeYellow.contains(dates.get(counter).trim()))
                        textView.setTextColor(getResources().getColor(R.color.mainCustomPrimary));
                    else if (activeGreen.contains(dates.get(counter).trim()))
                        textView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    linearLayout.addView(textView);
                    counter++;
                }
                tableRowHead.addView(linearLayout);
                tableMonth.addView(tableRowHead);
            }


            return root;
        }
    }
}