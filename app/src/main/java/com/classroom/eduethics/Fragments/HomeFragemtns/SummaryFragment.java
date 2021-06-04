package com.classroom.eduethics.Fragments.HomeFragemtns;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.Activity.VideoConfrence;
import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.Fragments.SubFragments.AttendanceStudent;
import com.classroom.eduethics.Fragments.SubFragments.AttendenceTeacher;
import com.classroom.eduethics.Fragments.SubFragments.FeeDetailsTeacher;
import com.classroom.eduethics.Fragments.TeacherFragments.AddFee;
import com.classroom.eduethics.Fragments.TeacherFragments.TeachersFeeStudentView;
import com.classroom.eduethics.Fragments.TeacherFragments.TimeTable;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.ExtraFunctions;
import com.classroom.eduethics.Utils.GlobalVariables;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SummaryFragment extends Fragment {


    TextView whichSub, classRoomIdTV, feeText, aboutClassStu, aboutInsStu;
    Context context;

    TableLayout timeTable_Table;
    EditText aboutClass, aboutInt;

    ConstraintLayout constraintLayoutIns;
    ConstraintLayout constraintLayoutClass;
    ConstraintLayout constraintLayoutTimeTable;

    ConstraintLayout MAIN_SUMMARY;

    public SummaryFragment(Context context) {
        this.context = context;
    }

    View root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_summary_teacher, container, false);


        root.findViewById(R.id.inviteStudentUP).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent();
                intent2.setAction(Intent.ACTION_SEND);
                intent2.setType("text/plain");
                intent2.putExtra(Intent.EXTRA_TEXT, "Your have been invited by " + GlobalVariables.name + " to join classroom " + HomeClassroom.model.getClassName() + " \n\nClassroom Id : " + HomeClassroom.model.getId() + "\nSubject : " + HomeClassroom.model.getSubject());
                startActivity(Intent.createChooser(intent2, "Share via"));
            }
        });

        root.findViewById(R.id.viewAtendenceBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalVariables.isStudent) {
                    getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                            R.anim.slide_in,  // enter
                            R.anim.fade_out,  // exit
                            R.anim.fade_in,   // popEnter
                            R.anim.slide_out  // popExit
                    ).replace(R.id.fragmentFrame, new AttendanceStudent()).commit();

                } else {
                    getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                            R.anim.slide_in,  // enter
                            R.anim.fade_out,  // exit
                            R.anim.fade_in,   // popEnter
                            R.anim.slide_out  // popExit
                    ).replace(R.id.fragmentFrame, new AttendenceTeacher()).commit();
                }
            }
        });


        MainActivity.TO_WHICH_FRAG = LocalConstants.TO_FRAG.TO_CLASSROOM;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER);
        }

        if (HomeClassroom.from == LocalConstants.TYPE.TEACHER) teacher();
        else student();

        forAll();

        return root;
    }

    private void forAll() {

        root.findViewById(R.id.temp20).setOnClickListener(v -> showAboutClass());
        root.findViewById(R.id.temp19).setOnClickListener(v -> showAboutIns());
        root.findViewById(R.id.temp18).setOnClickListener(v -> showTimeTable());
        root.findViewById(R.id.temp1).setOnClickListener(v -> showFeeCons());
        root.findViewById(R.id.viewFee).setOnClickListener(v -> viewFee());


        whichSub = root.findViewById(R.id.whichSub);
        classRoomIdTV = root.findViewById(R.id.classRoomId);
        constraintLayoutIns = root.findViewById(R.id.aboutInstructorConstraint);
        constraintLayoutClass = root.findViewById(R.id.aboutClassroomConstraint);
        constraintLayoutTimeTable = root.findViewById(R.id.timeTableConstraint);
        MAIN_SUMMARY = root.findViewById(R.id.MAIN_SUMMARY);
        feeText = root.findViewById(R.id.feeText);

        ((Button) root.findViewById(R.id.viewFee)).setText(HomeClassroom.model.getFee().equals("0") ? "Add Fee" : "View");
        feeText.setText(HomeClassroom.model.getFee().equals("0") ? "Fee not Added" : getString(R.string.Rs) + HomeClassroom.model.getFee());

        whichSub.setText(HomeClassroom.model.getSubject());
        classRoomIdTV.setText("Classroom Id : " + HomeClassroom.model.getId());
        timeTable_Table = root.findViewById(R.id.tableTimetable);


        List<Map<String, Object>> timeTable = HomeClassroom.model.getTimetable();
        TableRow tableRow = new TableRow(context);
        tableRow.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tableRow.addView(getTV("Day", getResources().getColor(R.color.customPrimary), 18));
        tableRow.addView(getTV("From", getResources().getColor(R.color.customPrimary), 18));
        tableRow.addView(getTV("To", getResources().getColor(R.color.customPrimary), 18));
        timeTable_Table.addView(tableRow);

        for (Map<String, Object> map : timeTable) {
            if ((Boolean) map.get("isSet")) {
                TableRow tableRowT = new TableRow(context);
                tableRowT.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                tableRowT.addView(getTV(map.get("day").toString(), Color.BLACK, 14));
                tableRowT.addView(getTV(ExtraFunctions.getReadableTime(Integer.parseInt(map.get("fromTime").toString().split(":")[0]), Integer.parseInt(map.get("fromTime").toString().split(":")[1])), ResourcesCompat.getColor(getResources(), R.color.darkgrey, null), 14));
                tableRowT.addView(getTV(ExtraFunctions.getReadableTime(Integer.parseInt(map.get("toTime").toString().split(":")[0]), Integer.parseInt(map.get("toTime").toString().split(":")[1])), ResourcesCompat.getColor(getResources(), R.color.darkgrey, null), 14));
                timeTable_Table.addView(tableRowT);
            }
        }

        showAboutIns();
        showAboutClass();
        showTimeTable();
        showFeeCons();

    }

    private void student() {
        root.findViewById(R.id.inviteStudentUP).setVisibility(View.GONE);
        root.findViewById(R.id.viewTimeTable).setVisibility(View.GONE);
        root.findViewById(R.id.saveAboutInst).setVisibility(View.GONE);
        root.findViewById(R.id.temp22).setVisibility(View.GONE);
        root.findViewById(R.id.aboutInstructorText).setVisibility(View.GONE);
        root.findViewById(R.id.temp21).setVisibility(View.GONE);
        root.findViewById(R.id.saveAboutClass).setVisibility(View.GONE);
        root.findViewById(R.id.aboutClassroomText).setVisibility(View.GONE);

        root.findViewById(R.id.viewFee).setVisibility(((Button) root.findViewById(R.id.viewFee)).getText().toString().equals("Add Fee") ? View.GONE : View.VISIBLE);

        root.findViewById(R.id.aboutClassroomTextStudent).setVisibility(View.VISIBLE);
        root.findViewById(R.id.aboutInstructorTextStudent).setVisibility(View.VISIBLE);

        aboutInsStu = root.findViewById(R.id.aboutInstructorTextStudent);
        aboutClassStu = root.findViewById(R.id.aboutClassroomTextStudent);

        ((TextView) root.findViewById(R.id.goLive)).setText("Join class");


        root.findViewById(R.id.goLive).setOnClickListener(v -> {
            root.findViewById(R.id.progress).setVisibility(View.VISIBLE);
            FirebaseFirestore.getInstance()
                    .collection("classroom")
                    .document(HomeClassroom.classId)
                    .collection("liveClasses")
                    .document("status")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                if ((boolean) documentSnapshot.get("isActive")) {
                                    attendenceS(documentSnapshot.getString("roomId"), documentSnapshot.get("time") + "");
                                } else {
                                    root.findViewById(R.id.progress).setVisibility(View.GONE);
                                    Snackbar.make(root.findViewById(R.id.MAIN), "No Live Class !", 1500).show();
                                }
                            } else {
                                root.findViewById(R.id.progress).setVisibility(View.GONE);
                                Snackbar.make(root.findViewById(R.id.MAIN), "No Live Class !", 1500).show();
                            }
                        }
                    });
        });


    }

    private void attendenceS(String roomId, String time) {
        Calendar calendar = Calendar.getInstance();
        String date = calendar.get(Calendar.DAY_OF_MONTH) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.YEAR);

        FirebaseFirestore.getInstance()
                .collection("attendence")
                .document(HomeClassroom.classId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Map<String, Object> map = new HashMap<>();
                        List<String> dates = documentSnapshot.get("dates") != null ? (List) documentSnapshot.get("dates") : new ArrayList<>();
                        dates.add(date);
                        map.put("dates", dates);
                        FirebaseFirestore.getInstance()
                                .collection("attendence")
                                .document(HomeClassroom.classId)
                                .set(map);
                    }
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
                        if (documentSnapshot.exists()) if (documentSnapshot.get("data") != null)
                            att = (List) documentSnapshot.get("data");
                        att.add(date + "-p");
                        Map<String, Object> map = new HashMap<>();
                        map.put("data", att);
                        FirebaseFirestore.getInstance()
                                .collection("students")
                                .document(GlobalVariables.uid)
                                .collection("attendance")
                                .document(HomeClassroom.classId)
                                .set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "sucess 1", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "failed 1", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Toast.makeText(context, "sucess 2", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "failed 2", Toast.LENGTH_SHORT).show();
            }
        });

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
                        if (documentSnapshot.getData() != null)
                            preData = documentSnapshot.getData();

                        List<Map<String, Object>> ownData = new ArrayList<>();
                        if (preData.size() != 0)
                            if (preData.get(GlobalVariables.uid) != null)
                                ownData = (List) preData.get(GlobalVariables.uid);
                        String entryTime = calendar.getTimeInMillis() + "";
                        Map<String, Object> newData = new HashMap<>();
                        newData.put("entryTime", entryTime);
                        newData.put("exitTime", "-");
                        ownData.add(newData);

                        preData.put(GlobalVariables.uid, ownData);

                        FirebaseFirestore.getInstance()
                                .collection("attendence")
                                .document(HomeClassroom.classId)
                                .collection(date)
                                .document(time)
                                .set(preData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        startActivity(new Intent(getContext(), VideoConfrence.class).putExtra("roomId", roomId).putExtra("classId", HomeClassroom.classId).putExtra("date", date).putExtra("time", time).putExtra("entryTime", entryTime));
                                    }
                                });
                    }
                });
    }

    private boolean checkAndPermission() {
        boolean isAllSet = true;
        for (String permission : LocalConstants.PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(getContext(), permission)
                    == PackageManager.PERMISSION_DENIED) {
                isAllSet = false;
            }
        }
        if (!isAllSet) {
            ActivityCompat.requestPermissions(getActivity(), LocalConstants.PERMISSIONS, LocalConstants.REQ_CODE.ALL);
        }
        return isAllSet;
    }

    private void teacher() {
        aboutClass = root.findViewById(R.id.aboutClassroomText);
        aboutInt = root.findViewById(R.id.aboutInstructorText);

        aboutInt.setText(HomeClassroom.model.getAboutInstructor());
        aboutClass.setText(HomeClassroom.model.getAboutClass());

        root.findViewById(R.id.saveAboutClass).setOnClickListener(v -> saveAboutClass());
        root.findViewById(R.id.saveAboutInst).setOnClickListener(v -> saveAboutInst());
        root.findViewById(R.id.viewTimeTable).setOnClickListener(v -> getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                R.anim.slide_in,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.slide_out  // popExit
        ).replace(R.id.fragmentFrame, new TimeTable(context)).commit());

        root.findViewById(R.id.goLive).setOnClickListener(v -> {
            if (checkAndPermission()) {
                root.findViewById(R.id.progress).setVisibility(View.VISIBLE);
                String id = ((int) (Math.random() * 100000000)) + "";
                Map<String, Object> map = new HashMap<>();
                map.put("isActive", true);
                map.put("activeStudents", 0);
                map.put("roomId", id);
                map.put("time", Calendar.getInstance().getTimeInMillis());


                FirebaseFirestore.getInstance()
                        .collection("classroom")
                        .document(HomeClassroom.classId)
                        .collection("liveClasses")
                        .document("status")
                        .set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (getActivity() == null) return;
                        root.findViewById(R.id.progress).setVisibility(View.GONE);
                        startActivity(new Intent(getContext(), VideoConfrence.class).putExtra("roomId", id).putExtra("classId", HomeClassroom.classId));
                    }
                });
            }
        });
    }


    private void viewFee() {

        if (((Button) root.findViewById(R.id.viewFee)).getText().toString().equals("Add Fee")) {
            getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                    R.anim.slide_in,  // enter
                    R.anim.fade_out,  // exit
                    R.anim.fade_in,   // popEnter
                    R.anim.slide_out  // popExit
            ).replace(R.id.fragmentFrame, new AddFee(context)).commit();
        } else if (!GlobalVariables.isStudent)
            getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                    R.anim.slide_in,  // enter
                    R.anim.fade_out,  // exit
                    R.anim.fade_in,   // popEnter
                    R.anim.slide_out  // popExit
            ).replace(R.id.fragmentFrame, new FeeDetailsTeacher(context)).commit();
        else
            getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                    R.anim.slide_in,  // enter
                    R.anim.fade_out,  // exit
                    R.anim.fade_in,   // popEnter
                    R.anim.slide_out  // popExit
            ).replace(R.id.fragmentFrame, new TeachersFeeStudentView(null)).commit();
    }

    private void saveAboutInst() {
        Map<String, Object> map = new HashMap<>();
        map.put("aboutInstructor", ((EditText) root.findViewById(R.id.aboutInstructorText)).getText().toString());
        FirebaseFirestore.getInstance()
                .collection("classroom")
                .document(HomeClassroom.classId)
                .update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Added About Instructor !", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void saveAboutClass() {
        Map<String, Object> map = new HashMap<>();
        map.put("aboutClass", ((EditText) root.findViewById(R.id.aboutClassroomText)).getText().toString());
        FirebaseFirestore.getInstance()
                .collection("classroom")
                .document(HomeClassroom.classId)
                .update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "Added About Classroom !", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private TextView getTV(String s, int color, float size) {
        TextView textView = new TextView(context);
        textView.setText(s);
        TableRow.LayoutParams param = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        param.weight = 1;
        param.setMargins(4, 2, 4, 2);
        textView.setLayoutParams(param);
        textView.setBackgroundColor(Color.TRANSPARENT);
        textView.setTextSize(size);
        textView.setTextColor(color);
        textView.setPadding(8, 6, 8, 6);
        textView.setGravity(Gravity.CENTER);
        return textView;
    }


    private void showAboutIns() {
        TransitionManager.beginDelayedTransition(MAIN_SUMMARY);
        if (HomeClassroom.from == LocalConstants.TYPE.TEACHER) {
            aboutInt.setVisibility(aboutInt.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            root.findViewById(R.id.temp22).setVisibility(aboutInt.getVisibility());
            root.findViewById(R.id.saveAboutInst).setVisibility(aboutInt.getVisibility());
        } else {
            aboutInsStu.setVisibility(aboutInsStu.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        }

    }

    private void showAboutClass() {
        TransitionManager.beginDelayedTransition(MAIN_SUMMARY);
        if (HomeClassroom.from == LocalConstants.TYPE.TEACHER) {
            aboutClass.setVisibility(aboutClass.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            root.findViewById(R.id.temp21).setVisibility(aboutClass.getVisibility());
            root.findViewById(R.id.saveAboutClass).setVisibility(aboutClass.getVisibility());
        } else {
            aboutClassStu.setVisibility(aboutClassStu.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        }
    }

    private void showTimeTable() {
        TransitionManager.beginDelayedTransition(MAIN_SUMMARY);
        timeTable_Table.setVisibility(timeTable_Table.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        if (HomeClassroom.from == LocalConstants.TYPE.TEACHER)
            root.findViewById(R.id.viewTimeTable).setVisibility(timeTable_Table.getVisibility());
    }


    public void showFeeCons() {
        TransitionManager.beginDelayedTransition(MAIN_SUMMARY);
        feeText.setVisibility(feeText.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        root.findViewById(R.id.viewFee).setVisibility(feeText.getVisibility());
    }

}