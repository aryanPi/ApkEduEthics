package com.classroom.eduethics.Fragments.TeacherFragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.Models.FeeDetailsCollectionModel;
import com.classroom.eduethics.Models.HomeClassroomWholeModel;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.ExtraFunctions;
import com.classroom.eduethics.Utils.GlobalVariables;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateClassroomFragment extends Fragment {

    Context context;
    String phNo, uid, name;
    int from;

    public CreateClassroomFragment(Context context, int from) {
        MainActivity.TO_WHICH_FRAG = LocalConstants.TO_FRAG.TO_CLASSROOM;
        this.context = context;
        this.from = from;
    }

    View root;

    RadioGroup classroomtypeRadio;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_create_classroom, container, false);

        classroomtypeRadio = root.findViewById(R.id.classroomtypeRadio);
        root.findViewById(R.id.nextC).setOnClickListener(v -> workFirst());
        return root;
    }

    private void post2(HomeClassroomWholeModel model, Map<String, String> classroom, List<Map<String, String>> classrrooms, Map<String, Object> details) {
/*
        TransitionManager.beginDelayedTransition(root.findViewById(R.id.MAIN_CREATECLASSROOM));
        root.findViewById(R.id.progress).setVisibility(View.VISIBLE);

        Map<String, Object> classroom = new HashMap<>();

        classroom.put("name", ((TextInputEditText) root.findViewById(R.id.classroomName)).getText().toString());
        classroom.put("subject", ((TextInputEditText) root.findViewById(R.id.subjectName)).getText().toString());
        String type = ((RadioButton) root.findViewById(classroomtypeRadio.getCheckedRadioButtonId())).getText().toString();
        classroom.put("type", type);

        HomeClassroomWholeModel model = getClassroom();
*/
        FirebaseFirestore.getInstance()
                .collection("classroom")
                .add(model)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        FirebaseFirestore.getInstance()
                                .collection("teacher")
                                .document(GlobalVariables.uid)
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        String id = documentReference.getId();
                                        classroom.put("id", id);
                                        List<Map<String, String>> d = ((List) documentSnapshot.get("classroom"));
                                        d.add(classroom);
                                        Map<String, Object> finalData = new HashMap<>();
                                        finalData.put("classroom", d);
                                        FirebaseFirestore.getInstance()
                                                .collection("teacher")
                                                .document(GlobalVariables.uid)
                                                .set(finalData, SetOptions.merge())
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        onSucessDone(id, model);
                                                    }
                                                });
                                    }
                                });
                    }
                });


    }

    private void workFirst() {
        TransitionManager.beginDelayedTransition(root.findViewById(R.id.MAIN_CREATECLASSROOM));
        root.findViewById(R.id.progress).setVisibility(View.VISIBLE);

        Map<String, Object> details = new HashMap<>();
        List<Map<String, String>> classrrooms = new ArrayList<>();
        Map<String, String> classroom = new HashMap<>();

        classroom.put("name", ((TextInputEditText) root.findViewById(R.id.classroomName)).getText().toString());
        classroom.put("subject", ((TextInputEditText) root.findViewById(R.id.subjectName)).getText().toString());
        String type = ((RadioButton) root.findViewById(classroomtypeRadio.getCheckedRadioButtonId())).getText().toString();
        classroom.put("type", type);
        classroom.put("totalStudents", "0");
        classroom.put("fee","0");

        HomeClassroomWholeModel model = getClassroom();

        if (from == 1) {
            post(model, classroom, classrrooms, details);
        } else {
            post2(model, classroom, classrrooms, details);
        }

    }

    private void post(HomeClassroomWholeModel model, Map<String, String> classroom, List<Map<String, String>> classrrooms, Map<String, Object> details) {

        FirebaseFirestore.getInstance()
                .collection("classroom")
                .add(model)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String id = documentReference.getId();
                        classroom.put("id", id);
                        classrrooms.add(classroom);
                        Toast.makeText(context, model.getId(), Toast.LENGTH_SHORT).show();

                        details.put("name", name);
                        details.put("Ins", "None");
                        details.put("about", "None");
                        details.put("phNo", phNo);
                        details.put("uid", uid);
                        details.put("isSite", "no");
                        details.put("classroom", classrrooms);
                        details.put("joinDate", ExtraFunctions.getReadableDateInString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.YEAR)));
                        details.put("profilePic", "-");


                        FirebaseFirestore.getInstance()
                                .collection("teacher")
                                .document(uid)
                                .set(details)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        onSucessDone(id, model);
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed !", Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private void onSucessDone(String id, HomeClassroomWholeModel model) {

        Map<String, Object> map = new HashMap<>();
        map.put(id, model.getId() + "");

        FirebaseFirestore.getInstance()
                .collection("utilData")
                .document("classroomIds")
                .update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        if (from == 1) {
                            SharedPreferences prefs = getActivity().getApplicationContext().getSharedPreferences("USER_PREF",
                                    Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("uid", uid);
                            editor.putString("name", name);
                            editor.putString("type", "teacher");
                            editor.apply();
                        }
                        if (getActivity()==null)return;
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        getActivity().finish();
                    }
                });
        /*FirebaseFirestore.getInstance()
                .collection("fees")
                .document(id)
                .set(new FeeDetailsCollectionModel("0", new ArrayList<>(), "0", "0","-","0",uid))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {


                    }
                });*/

    }

    private HomeClassroomWholeModel getClassroom() {
        HomeClassroomWholeModel model = new HomeClassroomWholeModel();
        model.setAboutClass(((TextInputEditText) root.findViewById(R.id.classroomName)).getText().toString() + " Classroom");
        model.setAboutInstructor("Mr./Ms." + name + " Instructor is conducting this Class");
        model.setAssignments(new ArrayList<>());
        model.setClassName(((TextInputEditText) root.findViewById(R.id.classroomName)).getText().toString());
        model.setNotice(new ArrayList<>());
        model.setStudyMaterial(new ArrayList<>());
        model.setSubject(((TextInputEditText) root.findViewById(R.id.subjectName)).getText().toString());
        model.setTest(new ArrayList<>());
        model.setCurrentStudents(new ArrayList<>());
        model.setPendingStudents(new ArrayList<>());
        model.setId(((int) (Math.random() * 1000000)) + "");
        model.setTimetable(getEmptyTimeTable());
        model.setFee("0");
        model.setTeacherId(uid);
        return model;
    }

    private List<Map<String, Object>> getEmptyTimeTable() {


        List<Map<String, Object>> timeTable = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("day", LocalConstants.DAYS[i]);
            map.put("isSet", false);
            map.put("toTime", "-");
            map.put("fromTime", "-");
            timeTable.add(map);
        }
        return timeTable;
    }

    public void setDetails(String uid, String phNo, String name) {
        this.phNo = phNo;
        this.uid = uid;
        this.name = name;
    }
}