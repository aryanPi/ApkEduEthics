package com.classroom.eduethics.Fragments.HomeFragemtns;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.Adapter.StudentsAdapter;
import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.Fragments.SubFragments.FeeDetailsTeacher;
import com.classroom.eduethics.Fragments.TeacherFragments.TeachersFeeStudentView;
import com.classroom.eduethics.Models.HomeClassroomWholeModel;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Services.FireMessage;
import com.classroom.eduethics.Utils.ExtraFunctions;
import com.classroom.eduethics.Utils.GlobalVariables;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class StudentFragment extends Fragment {

    StudentsAdapter reqAdapter;
    StudentsAdapter currentAdapter;
    View progress;

    View root;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setEnterTransition(inflater.inflateTransition(R.transition.slide_right));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_student, container, false);
    if(GlobalVariables.isStudent){
        ((TextView) root.findViewById(R.id.tit)).setText("View all the details of \nstudents in the classroom here");
    }
        runFirst();

        return root;
    }

    private void runFirst() {

        reqAdapter = new StudentsAdapter(this, HomeClassroom.model.getPendingStudents(), 2);
        currentAdapter = new StudentsAdapter(this, HomeClassroom.model.getCurrentStudents(), 1);

        RecyclerView recyclerViewStudents = root.findViewById(R.id.recyclerViewStudents);
        RecyclerView recyclerReqStudents = root.findViewById(R.id.recyclerReqStudents);

        progress = root.findViewById(R.id.progress);
        progress.setVisibility(View.GONE);
        root.findViewById(R.id.inviteStudent).setOnClickListener(v -> {
            Intent intent2 = new Intent();
            intent2.setAction(Intent.ACTION_SEND);
            intent2.setType("text/plain");
            intent2.putExtra(Intent.EXTRA_TEXT, "Your have been invited by " + GlobalVariables.name + " to join classroom " + HomeClassroom.model.getClassName() + " \n\nClassroom Id : " + HomeClassroom.model.getId() + "\nSubject : " + HomeClassroom.model.getSubject());
            startActivity(Intent.createChooser(intent2, "Share via"));
        });

        TextView totalEnrolledStudents = root.findViewById(R.id.countEnrolledStudent);
        TextView totalReqStudents = root.findViewById(R.id.countReqStudent);

        if (GlobalVariables.isStudent) {
            recyclerReqStudents.setVisibility(View.GONE);
            root.findViewById(R.id.temp2).setVisibility(View.GONE);
            totalReqStudents.setVisibility(View.GONE);
        } else {
            recyclerReqStudents.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerReqStudents.setAdapter(reqAdapter);
            totalReqStudents.setText("(" + HomeClassroom.model.getPendingStudents().size() + ")");
        }

        totalEnrolledStudents.setText("(" + HomeClassroom.model.getCurrentStudents().size() + ")");

        recyclerViewStudents.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewStudents.setAdapter(currentAdapter);

        MainActivity.TO_WHICH_FRAG = LocalConstants.TO_FRAG.TO_CLASSROOM;

    }


    public void action(int position, int actionType) {
        progress.setVisibility(View.VISIBLE);
        String id = HomeClassroom.model.getPendingStudents().get(position).get("id").toString();

        List<Map<String, Object>> list = HomeClassroom.model.getPendingStudents();
        List<Map<String, Object>> newListPending = new ArrayList<>();
        List<Map<String, Object>> newListCurrent = HomeClassroom.model.getCurrentStudents();

        for (Map<String, Object> stringObjectMap : list) {
            if (!stringObjectMap.get("id").toString().equals(id)) {
                newListPending.add(stringObjectMap);
            } else {
                newListCurrent.add(stringObjectMap);
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("pendingStudents", newListPending);
        if (actionType == 1) {
            ExtraFunctions.sendNoti(id, "Request Accpeted", HomeClassroom.model.getClassName() + " Classroom is up now.", getContext());
            map.put("currentStudents", newListCurrent);
        }

        FirebaseFirestore.getInstance()
                .collection("classroom")
                .document(HomeClassroom.classId)
                .update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FirebaseFirestore.getInstance()
                                .collection("classroom")
                                .document(HomeClassroom.classId)
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        FirebaseFirestore.getInstance()
                                                .collection("students")
                                                .document(id)
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                        List<String> lst = (List) documentSnapshot.get("classes");
                                                        List<String> finalLst = new ArrayList<>();
                                                        for (String s : lst) {
                                                            if (s.endsWith("_p")) {
                                                                if (s.equals(HomeClassroom.classId + "_p")) {
                                                                    finalLst.add(s.split("_")[0]);
                                                                } else {
                                                                    finalLst.add(s);
                                                                }
                                                            } else {
                                                                finalLst.add(s);
                                                            }
                                                        }

                                                        FirebaseFirestore.getInstance()
                                                                .collection("students")
                                                                .document(id)
                                                                .update("classes", finalLst)
                                                                .addOnSuccessListener(aVoid1 -> {
                                                                    HomeClassroom.model = documentSnapshot.toObject(HomeClassroomWholeModel.class);
                                                                    if (getActivity() == null)
                                                                        return;

                                                                    FirebaseFirestore.getInstance()
                                                                            .collection("classroom")
                                                                            .document(HomeClassroom.classId)
                                                                            .get()
                                                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                                @Override
                                                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                                    HomeClassroom.model = documentSnapshot.toObject(HomeClassroomWholeModel.class);
                                                                                    runFirst();
                                                                                }
                                                                            });

                                                                }).addOnFailureListener(e -> progress.setVisibility(View.GONE));
                                                    }
                                                }).addOnFailureListener(e -> progress.setVisibility(View.GONE));
                                    }
                                }).addOnFailureListener(e -> progress.setVisibility(View.GONE));
                    }
                }).addOnFailureListener(e -> progress.setVisibility(View.GONE));

        if (actionType == 1) {
            FirebaseFirestore.getInstance()
                    .collection("fees")
                    .document(HomeClassroom.classId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            List<Map<String, Object>> students = (List) documentSnapshot.get("students");
                            if (students == null) return;
                            Map<String, Object> singleData = new HashMap<>();
                            singleData.put("adv", (Integer.parseInt(documentSnapshot.getString("feePerM")) * -1) + "");
                            singleData.put("rec", "0");
                            singleData.put("id", id);
                            singleData.put("name", HomeClassroom.model.getPendingStudents().get(position).get("name").toString());
                            singleData.put("totalFee", documentSnapshot.getString("feePerM"));
                            students.add(singleData);
                            Map<String, Object> map = new HashMap<>();
                            map.put("students", students);
                            FirebaseFirestore.getInstance()
                                    .collection("fees")
                                    .document(HomeClassroom.classId)
                                    .update(map);

                            addRecord((Integer.parseInt(documentSnapshot.getString("feePerM")) * -1) + "", id);
                        }
                    });
        }

    }

    private void addRecord(String amt, String id) {
        Map<String, Object> map2 = new HashMap<>();
        List<Map<String, Object>> singleData2 = new ArrayList<>();
        Map<String, Object> studentData = new HashMap<>();
        studentData.put("amt", amt);
        studentData.put("approval", 3);
        studentData.put("dec", "System Added");
        studentData.put("from", "sys");
        studentData.put("mode", "System");
        studentData.put("date", Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "-" + Calendar.getInstance().get(Calendar.MONTH) + "-" + Calendar.getInstance().get(Calendar.YEAR));
        singleData2.add(studentData);
        map2.put("data", singleData2);
        FirebaseFirestore.getInstance()
                .collection("fees")
                .document(HomeClassroom.classId)
                .collection("record")
                .document(id)
                .set(map2);
    }

    public void deleteStudent(int position) {
        Dialog dialog;
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.confirmation_dialog);

        ((TextView) dialog.findViewById(R.id.confirmationTitle)).setText("Remove !");
        ((TextView) dialog.findViewById(R.id.confirmationText)).setText("Want to remove this Student from your classroom " + HomeClassroom.model.getClassName() + "?");
        ((TextView) dialog.findViewById(R.id.confirmationBtn)).setTextColor(ResourcesCompat.getColor(getResources(), android.R.color.holo_red_dark, null));
        ((TextView) dialog.findViewById(R.id.confirmationBtn)).setText("Remove");

        dialog.findViewById(R.id.confirmationBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setVisibility(View.VISIBLE);
                List<Map<String, Object>> list = HomeClassroom.model.getCurrentStudents();
                String id = HomeClassroom.model.getCurrentStudents().get(position).get("id").toString();
                List<Map<String, Object>> newCurrentList = new ArrayList<>();
                for (Map<String, Object> stringObjectMap : list) {
                    if (!stringObjectMap.get("id").toString().equals(id)) {
                        newCurrentList.add(stringObjectMap);
                    }
                }

                Map<String, Object> map = new HashMap<>();
                map.put("currentStudents", newCurrentList);

                FirebaseFirestore.getInstance()
                        .collection("classroom")
                        .document(HomeClassroom.classId)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                List<Map<String, Object>> data = (List) documentSnapshot.get("assignments");
                                List<Map<String, Object>> forUpload = new ArrayList<>();
                                for (Map<String, Object> datum : data) {
                                    if (((List<String>) datum.get("submitted")).contains(id)) {
                                        ((List<String>) datum.get("submitted")).remove(id);
                                    }
                                    forUpload.add(datum);
                                }
                                List<Map<String, Object>> data2 = (List) documentSnapshot.get("test");
                                List<Map<String, Object>> forUpload2 = new ArrayList<>();
                                for (Map<String, Object> datum : data2) {
                                    if (((List<String>) datum.get("submitted")).contains(id)) {
                                        ((List<String>) datum.get("submitted")).remove(id);
                                    }
                                    forUpload2.add(datum);
                                }

                                map.put("assignments", forUpload);
                                map.put("test", forUpload2);

                                FirebaseFirestore.getInstance()
                                        .collection("classroom")
                                        .document(HomeClassroom.classId)
                                        .update(map)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                FirebaseFirestore.getInstance()
                                                        .collection("classroom")
                                                        .document(HomeClassroom.classId)
                                                        .collection("assignmentSub")
                                                        .get()
                                                        .addOnSuccessListener(queryDocumentSnapshots -> {
                                                            if (queryDocumentSnapshots != null)
                                                                if (queryDocumentSnapshots.getDocuments() != null)
                                                                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                                                        FirebaseFirestore.getInstance()
                                                                                .collection("classroom")
                                                                                .document(HomeClassroom.classId)
                                                                                .collection("assignmentSub")
                                                                                .document(document.getId())
                                                                                .update(id, FieldValue.delete());

                                                                    }

                                                        }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        if (getActivity() == null)
                                                            return;
                                                        progress.setVisibility(View.GONE);
                                                    }
                                                });

                                                FirebaseFirestore.getInstance()
                                                        .collection("classroom")
                                                        .document(HomeClassroom.classId)
                                                        .collection("testSub")
                                                        .get()
                                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                                                    FirebaseFirestore.getInstance()
                                                                            .collection("classroom")
                                                                            .document(HomeClassroom.classId)
                                                                            .collection("testSub")
                                                                            .document(document.getId())
                                                                            .update(id, FieldValue.delete());

                                                                }
                                                            }
                                                        });
                                                FirebaseFirestore.getInstance().collection("students")
                                                        .document(id)
                                                        .get()
                                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                List<String> classes = (List) documentSnapshot.get("classes");
                                                                if (classes.contains(HomeClassroom.classId)) {
                                                                    classes.remove(HomeClassroom.classId);
                                                                }
                                                                Map<String, Object> map = new HashMap<>();
                                                                map.put("classes", classes);
                                                                FirebaseFirestore.getInstance().collection("students")
                                                                        .document(id)
                                                                        .update(map);
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        if (getActivity() == null)
                                                            return;
                                                        progress.setVisibility(View.GONE);
                                                    }
                                                });

                                                progress.setVisibility(View.GONE);
                                            }
                                            //HERE _________________________-------------->>>>
                                        });


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (getActivity() == null)
                            return;
                        progress.setVisibility(View.GONE);
                    }
                });
                getActivity().onBackPressed();
                dialog.dismiss();
            }
        });


        dialog.findViewById(R.id.cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }


    public void openFee(int position) {
        FirebaseFirestore.getInstance()
                .collection("fees")
                .document(HomeClassroom.classId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        List<Map<String, Object>> data = (List) documentSnapshot.get("students");
                        boolean isOpen = false;
                        for (Map<String, Object> objectMap : data) {
                            if (objectMap.get("id").toString().equals(HomeClassroom.model.getCurrentStudents().get(position).get("id").toString())) {
                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, new TeachersFeeStudentView(objectMap)).commit();
                                isOpen = true;
                                break;
                            }
                        }
                        if (!isOpen)
                            Toast.makeText(getContext(), "No Record Found", Toast.LENGTH_SHORT).show();
                    }
                });


    }
}