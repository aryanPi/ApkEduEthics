package com.classroom.eduethics.Fragments.SubFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.Adapter.AttendanceAdapters;
import com.classroom.eduethics.Adapter.FragmentAdapter;
import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.Fragments.TeacherFragments.TeachersFeeStudentView;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendenceTeacher extends Fragment {

    TabLayout tabLayout;
    ViewPager2 pager2;
    FragmentAdapter adapter;

    //  date         (time)           UID-s   List    entry,exit
    Map<String, Map<String, Map<String, List<Map<String, String>>>>> data = new HashMap<>();

    View root;
    List<String> dates;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_attendence_teacher, container, false);
        // root.findViewById(R.id.progress).setVisibility(View.VISIBLE);

        tabLayout = root.findViewById(R.id.tab_layout);
        pager2 = root.findViewById(R.id.pageViewerViewAttendence);

        MainActivity.TO_WHICH_FRAG = LocalConstants.TO_FRAG.TO_HOME_CLASSROOM;
        runTask();


        return root;
    }

    private void runTask() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        FirebaseFirestore.getInstance()
                .collection("attendence")
                .document(HomeClassroom.classId)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                dates = (List) documentSnapshot.get("dates");
                if (dates != null) {
                    if (dates.size() != 0) {
                        for (String date : dates) {
                            FirebaseFirestore.getInstance()
                                    .collection("attendence")
                                    .document(HomeClassroom.classId)
                                    .collection(date)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                            if (queryDocumentSnapshots.getDocuments().size() != 0) {
                                                //time
                                                Map<String, Map<String, List<Map<String, String>>>> timeData = new HashMap<>();
                                                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                                    Map<String, List<Map<String, String>>> singleStudent = new HashMap<>();
                                                    Map<String, Object> documentData = document.getData();
                                                    for (Map.Entry<String, Object> objectEntry : documentData.entrySet()) {
                                                        List<Map<String, String>> singleStudentData = (List) objectEntry.getValue();
                                                        singleStudent.put(objectEntry.getKey(), singleStudentData);
                                                    }
                                                    timeData.put(document.getId(), singleStudent);
                                                }

                                                data.put(date, timeData);
                                                countAndRun("done");
                                            } else countAndRun("not " + date);
                                        }
                                    });
                        }
                    } else {
                        root.findViewById(R.id.empty).setVisibility(View.VISIBLE);
                        ((TextView) root.findViewById(R.id.emptyMessage)).setText("No Attendence Yet!");
                    }
                } else {
                    root.findViewById(R.id.empty).setVisibility(View.VISIBLE);
                    ((TextView) root.findViewById(R.id.emptyMessage)).setText("No Attendence Yet!");
                }
            }
        });


    }


    int counter = 0;

    private void countAndRun(String sta) {
        counter++;
        Log.d("TAG", "countAndRun: " + sta);
        if (counter == dates.size()) {
            forward();
            Toast.makeText(getContext(), "done" + counter, Toast.LENGTH_SHORT).show();
        }
    }

    private void forward() {


        FragmentManager fm = getActivity().getSupportFragmentManager();
        ArrayList<Fragment> fragments = new ArrayList<>();

        Calendar calendar2 = Calendar.getInstance();

        calendar2.set(calendar2.get(Calendar.YEAR), calendar2.get(Calendar.MONTH), calendar2.get(Calendar.DAY_OF_MONTH));

        for (int i = 0; i < 28; i++) {
            Log.d("TAG", "forward: " + (calendar2.get(Calendar.DAY_OF_MONTH) + "-" + calendar2.get(Calendar.MONTH) + "-" + calendar2.get(Calendar.YEAR)));
            fragments.add(new innerAttTeacher(data.get("" + (calendar2.get(Calendar.DAY_OF_MONTH) + "-" + calendar2.get(Calendar.MONTH) + "-" + calendar2.get(Calendar.YEAR)))));
            Log.d("TAG", "forward: " + "" + (calendar2.get(Calendar.DAY_OF_MONTH) + "-" + calendar2.get(Calendar.MONTH) + "-" + calendar2.get(Calendar.YEAR)) + "   :::: " + data.size());

            tabLayout.addTab(tabLayout.newTab().setText("" + (calendar2.get(Calendar.DAY_OF_MONTH) + "\n" + LocalConstants.MONTHS[(calendar2.get(Calendar.MONTH))].substring(0, 3))));
            calendar2.add(Calendar.DAY_OF_MONTH, -1);

        }

        adapter = new FragmentAdapter(fm, getLifecycle(), fragments);
        pager2.setAdapter(adapter);

        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        pager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });


        pager2.setCurrentItem(0);


    }

    public static class innerAttTeacher extends Fragment {

        //(time)           UID-s   List    entry,exit
        Map<String, Map<String, List<Map<String, String>>>> innderData;

        public innerAttTeacher(Map<String, Map<String, List<Map<String, String>>>> innderData) {
            this.innderData = innderData;
        }

        View _root;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            _root = inflater.inflate(R.layout.recycler_view, container, false);

            if (innderData != null) {
                RecyclerView recyclerView = _root.findViewById(R.id.RECYCLER_VIEW);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(new AttendanceAdapters(innderData, this));
            } else {
                _root.findViewById(R.id.empty).setVisibility(View.VISIBLE);
                ((TextView) _root.findViewById(R.id.emptyMessage)).setText("No Class Taken");
            }

            return _root;
        }
    }

}



