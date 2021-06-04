package com.classroom.eduethics.Fragments.SubFragments;

import android.os.Bundle;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.Adapter.FragmentAdapter;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.ExtraFunctions;
import com.classroom.eduethics.Utils.GlobalVariables;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewTimeTable extends Fragment {


    View root;
    List<Map<String, Object>> classRoomsData;


    TabLayout tabLayout;
    ViewPager2 pager2;
    FragmentAdapter adapter;

    public ViewTimeTable(List<Map<String, Object>> classRoomsData) {
        this.classRoomsData = classRoomsData;
    }




    TextView dateText, timesText;
    int[] t = new int[2];
    int[] d = new int[3];

    androidx.appcompat.widget.AppCompatSpinner spinner;
    String[] idsClassrrom;
    String[] spinnerData;


    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setEnterTransition(inflater.inflateTransition(R.transition.slide_right));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_view_time_table, container, false);


        if (GlobalVariables.isStudent) root.findViewById(R.id.floatBtn).setVisibility(View.GONE);
        else {

            dateText = root.findViewById(R.id.dateText);
            timesText = root.findViewById(R.id.timeText);
            Calendar dateTime = Calendar.getInstance();
            d[0] = dateTime.get(Calendar.DAY_OF_MONTH);
            d[1] = dateTime.get(Calendar.MONTH) + 1;
            d[2] = dateTime.get(Calendar.YEAR);
            t[0] = dateTime.get(Calendar.HOUR_OF_DAY);
            t[1] = dateTime.get(Calendar.MINUTE);
            timesText.setText(ExtraFunctions.getReadableTime(t[0], t[1]));
            dateText.setText(ExtraFunctions.getReadableDate(d[0], d[1], d[2]));
            spinner = root.findViewById(R.id.classSpinner);
            spinnerData = new String[classRoomsData.size()];
            idsClassrrom = new String[classRoomsData.size()];

            for (Map<String, Object> classRoomsDatum : classRoomsData) {
                spinnerData[classRoomsData.indexOf(classRoomsDatum)] = classRoomsDatum.get("name").toString();
                idsClassrrom[classRoomsData.indexOf(classRoomsDatum)] = classRoomsDatum.get("id").toString();
            }


            ArrayAdapter aa = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, spinnerData);
            aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinner.setAdapter(aa);
            spinner.setSelection(0);

            root.findViewById(R.id.floatBtn).setOnClickListener(v -> {
                TransitionManager.beginDelayedTransition(root.findViewById(R.id.MAIN));
                root.findViewById(R.id.FIRST).setVisibility(View.GONE);
                root.findViewById(R.id.CREATE).setVisibility(View.VISIBLE);
            });
            root.findViewById(R.id.saveAndUpload).setOnClickListener(v -> uploadToFirebase());


            root.findViewById(R.id.date).setOnClickListener(v -> ExtraFunctions.datePicker(getContext(), dateText, d));
            root.findViewById(R.id.time).setOnClickListener(v -> ExtraFunctions.timePicker(getContext(), timesText, t));

        }


        runTask();

        MainActivity.actionBarTitle.setText("Time Table");

        return root;
    }


    private void uploadToFirebase() {

        root.findViewById(R.id.progress).setVisibility(View.VISIBLE);

        boolean allDone = true;
        if (((TextInputEditText) root.findViewById(R.id.mobileNoMainPage)).getText().toString().equals("")) {
            ((TextInputEditText) root.findViewById(R.id.mobileNoMainPage)).setError("Required !");
            allDone = false;
        }
        if ((d[0] == Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) && (d[1] - 1 == Calendar.getInstance().get(Calendar.MONTH)) && (d[2] == Calendar.getInstance().get(Calendar.YEAR))) {
            if (((Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60) + Calendar.getInstance().get(Calendar.MINUTE)) > (t[0] * 60 + t[1])) {
                ((LinearLayout) root.findViewById(R.id.time)).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.error_time, null));
                Toast.makeText(getContext(), "Please Set Valid Time for deadline !", Toast.LENGTH_SHORT).show();
                allDone = false;
            } else {
                ((LinearLayout) root.findViewById(R.id.time)).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.background_blocks_12dp, null));
            }
        } else {
            Toast.makeText(getContext(), "not equal" + (d[0] == Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) + " : " + (d[1] - 1 == Calendar.getInstance().get(Calendar.MONTH)) + " : " + (d[2] == Calendar.getInstance().get(Calendar.YEAR)), Toast.LENGTH_SHORT).show();
        }

        if (allDone) {
            String id = "";
            for (int i = 0; i < spinnerData.length; i++) {
                if (spinner.getSelectedItem().toString().equals(spinnerData[i])) {
                    id = idsClassrrom[i];
                    break;
                }
            }


            String finalId = id;
            FirebaseFirestore.getInstance()
                    .collection("timetable")
                    .document(id)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Map<String, Object> forUpload = new HashMap<>();
                            Map<String, Object> data = new HashMap<>();
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(Calendar.MINUTE, t[1]);
                            calendar.set(Calendar.HOUR_OF_DAY, t[0]);
                            data.put("time", calendar.getTimeInMillis() + "");
                            data.put("subject", spinner.getSelectedItem().toString());
                            data.put("type", "events");
                            if (getContext() == null) return;
                            data.put("title", ((TextInputEditText) root.findViewById(R.id.mobileNoMainPage)).getText().toString());
                            data.put("dec", ((TextInputEditText) root.findViewById(R.id.decText)).getText().toString());
                            List<Map<String, Object>> preData = new ArrayList<>();

                            if (documentSnapshot.get(d[0] + "" + d[1] + "" + d[2]) != null) {
                                preData = (List) documentSnapshot.get(dateText.getText().toString());
                            }

                            preData.add(data);
                            forUpload.put(d[0] + "-" + d[1] + "-" + d[2], preData);
                            FirebaseFirestore.getInstance()
                                    .collection("timetable")
                                    .document(finalId)
                                    .update(forUpload)
                                    .addOnSuccessListener(aVoid -> {
                                        if (getContext() == null) return;
                                        root.findViewById(R.id.progress).setVisibility(View.GONE);
                                    }).addOnFailureListener(e -> {
                                FirebaseFirestore.getInstance()
                                        .collection("timetable")
                                        .document(finalId)
                                        .set(forUpload)
                                        .addOnSuccessListener(aVoid -> {
                                            if (getContext() == null) return;
                                            root.findViewById(R.id.progress).setVisibility(View.GONE);
                                        });
                            });
                        }
                    });

        }
    }

    private void runTask() {
        root.findViewById(R.id.progress).setVisibility(View.VISIBLE);

        tabLayout = root.findViewById(R.id.tab_layout);
        pager2 = root.findViewById(R.id.pageViewerViewTimeTable);

        List<Map<String, Object>> data = new ArrayList<>();
        for (Map<String, Object> classRoomsDatum : classRoomsData) {
            FirebaseFirestore.getInstance()
                    .collection("timetable")
                    .document(classRoomsDatum.get("id").toString())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Map<String, Object> dataTemp = documentSnapshot.getData();
                            if (dataTemp == null) {
                                passData2(new ArrayList<>());
                                return;
                            }

                            for (Map.Entry<String, Object> stringObjectEntry : dataTemp.entrySet()) {
                                Map<String, Object> t = new HashMap<>();
                                t.put("date", stringObjectEntry.getKey());
                                t.put("data", stringObjectEntry.getValue());
                                data.add(t);
                            }
                            passData2(data);
                        }
                    }).addOnFailureListener(e -> {
                if (getContext() == null) return;
                Toast.makeText(getContext(), "Network Error!", Toast.LENGTH_SHORT).show();
            });


        }
    }


    private void passData2(List<Map<String, Object>> data) {
        //  Date      Type   Data(Single Element)
        Map<String, Map<String, List<Map<String, String>>>> dataForSend = new HashMap<>();

        for (Map<String, Object> datum : data) {
            String date = datum.get("date").toString();
            List<Map<String, Object>> insideData = (List) datum.get("data");
            for (Map<String, Object> INSIDE : insideData) {
                Map<String, String> temp3 = new HashMap<>();
                temp3.put("dec", INSIDE.get("dec").toString());
                temp3.put("title", INSIDE.get("title").toString());
                temp3.put("subject", INSIDE.get("subject").toString());
                temp3.put("time", INSIDE.get("time").toString());
                temp3.put("type", INSIDE.get("type").toString());
                if (dataForSend.containsKey(date)) {
                    if (dataForSend.get(date).containsKey(temp3.get("type"))) {
                        dataForSend.get(date).get(temp3.get("type")).add(temp3);
                    } else {
                        List<Map<String, String>> INSIDE_3 = new ArrayList<>();
                        INSIDE_3.add(temp3);
                        dataForSend.get(date).put(temp3.get("type"), INSIDE_3);
                    }
                } else {
                    Map<String, List<Map<String, String>>> INSIDE_2 = new HashMap<>();
                    List<Map<String, String>> INSIDE_3 = new ArrayList<>();
                    INSIDE_3.add(temp3);
                    INSIDE_2.put(temp3.get("type"), INSIDE_3);
                    dataForSend.put(date, INSIDE_2);
                }
            }
        }

        if (getActivity() == null) return;
        FragmentManager fm = getActivity().getSupportFragmentManager();
        ArrayList<Fragment> fragments = new ArrayList<>();

        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(calendar2.get(Calendar.YEAR), calendar2.get(Calendar.MONTH), calendar2.get(Calendar.DAY_OF_MONTH));


        for (int i = 0; i < 28; i++) {
            fragments.add(new viewTimeTableInnerFragment(dataForSend.get("" + (calendar2.get(Calendar.DAY_OF_MONTH) + "-" + (calendar2.get(Calendar.MONTH) + 1) + "-" + calendar2.get(Calendar.YEAR))), this, root.findViewById(R.id.MAIN)));
            tabLayout.addTab(tabLayout.newTab().setText("" + (calendar2.get(Calendar.DAY_OF_MONTH) + "\n" + LocalConstants.MONTHS[(calendar2.get(Calendar.MONTH))].substring(0, 3))));
            calendar2.add(Calendar.DAY_OF_MONTH, 1);
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


        root.findViewById(R.id.progress).setVisibility(View.GONE);
    }

    public static class viewTimeTableInnerFragment extends Fragment {


        TextView assignmentTitleTV, testDueTV, eventsTV;
        RecyclerView classesAndEventsRecyclerView, testDueRecyclerView, assignmentRecyclerView;
        ConstraintLayout MAIN_CONS;


        Map<String, List<Map<String, String>>> data;
        Fragment fragment;
        ConstraintLayout constraintLayout;

        public viewTimeTableInnerFragment(Map<String, List<Map<String, String>>> data, Fragment fragment, ConstraintLayout constraintLayout) {
            this.constraintLayout = constraintLayout;
            this.fragment = fragment;
            this.data = data;
        }

        View root;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


            root = inflater.inflate(R.layout.time_table_single_frag_recycler_view, container, false);

            this.assignmentTitleTV = root.findViewById(R.id.temp1);
            this.testDueTV = root.findViewById(R.id.temp3);
            this.eventsTV = root.findViewById(R.id.temp5);

            if (data == null) {
                eventsTV.setText("0 Events");
                testDueTV.setText("0 Test Due");
                assignmentTitleTV.setText("0 Assignment Due");
            } else {


                this.MAIN_CONS = root.findViewById(R.id.MAIN_SINGLE_ITEM_REC);
                this.assignmentRecyclerView = root.findViewById(R.id.assignmentRecyclerView);
                this.testDueRecyclerView = root.findViewById(R.id.testDueRecyclerView);
                this.classesAndEventsRecyclerView = root.findViewById(R.id.ClassesAndEventsRecyclerView);

                testDueTV.setOnClickListener(v -> {
                    TransitionManager.beginDelayedTransition(constraintLayout);
                    testDueRecyclerView.setVisibility(testDueRecyclerView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                });

                assignmentTitleTV.setOnClickListener(v -> {
                    TransitionManager.beginDelayedTransition(constraintLayout);
                    assignmentRecyclerView.setVisibility(assignmentRecyclerView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                });

                eventsTV.setOnClickListener(v -> {
                    TransitionManager.beginDelayedTransition(constraintLayout);
                    classesAndEventsRecyclerView.setVisibility(classesAndEventsRecyclerView.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                });


                assignmentRecyclerView.setLayoutManager(new LinearLayoutManager(fragment.getContext()));
                if (data.get("assignment") != null) {
                    assignmentRecyclerView.setAdapter(new insiderClassV(data.get("assignment")));
                    assignmentTitleTV.setText(data.get("assignment").size() + " Assignments Due");
                } else {
                    assignmentTitleTV.setText("0 Assignments Due");
                }

                testDueRecyclerView.setLayoutManager(new LinearLayoutManager(fragment.getContext()));
                if (data.get("test") != null) {
                    testDueRecyclerView.setAdapter(new insiderClassV(data.get("test")));
                    testDueTV.setText(data.get("test").size() + " Test Due");
                } else {
                    testDueTV.setText("0 Test Due");
                }

                classesAndEventsRecyclerView.setLayoutManager(new LinearLayoutManager(fragment.getContext()));
                if (data.get("events") != null) {
                    classesAndEventsRecyclerView.setAdapter(new insiderClassV(data.get("events")));
                    eventsTV.setText(data.get("events").size() + " Events");
                } else {
                    eventsTV.setText("0 Events");
                }
            }
            return root;
        }
    }

    static class insiderClassV extends RecyclerView.Adapter<insiderClassV.vhInsider> {

        List<Map<String, String>> dataInsider;

        public insiderClassV(List<Map<String, String>> dataInsider) {
            this.dataInsider = dataInsider;
        }


        @NonNull
        @Override
        public vhInsider onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new vhInsider(LayoutInflater.from(parent.getContext()).inflate(R.layout.single_item_event, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull vhInsider holder, int position) {
            holder.className.setText(dataInsider.get(position).get("subject"));
            holder.title.setText(dataInsider.get(position).get("title"));
            holder.time.setText(ExtraFunctions.getTimeFromLong(dataInsider.get(position).get("time")));
            if (!dataInsider.get(position).get("dec").equals(""))
                ((TextView) holder.itemView.findViewById(R.id.decSingleItem)).setText(dataInsider.get(position).get("dec"));
            else holder.itemView.findViewById(R.id.decSingleItem).setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return dataInsider.size();
        }

        static class vhInsider extends RecyclerView.ViewHolder {
            TextView title, className, time;

            public vhInsider(@NonNull View itemView) {
                super(itemView);
                this.time = itemView.findViewById(R.id.timeSingleItem);
                this.title = itemView.findViewById(R.id.titleSingleItem);
                this.className = itemView.findViewById(R.id.classSingleItem);
            }
        }
    }


}