package com.classroom.eduethics.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.Adapter.Classroom_ClassroomAdapter;
import com.classroom.eduethics.Adapter.ScheduleAdapterTeacher;
import com.classroom.eduethics.Fragments.SubFragments.ViewTimeTable;
import com.classroom.eduethics.Models.Classroom_ClassroomModel;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Services.FireMessage;
import com.classroom.eduethics.Utils.ExtraFunctions;
import com.classroom.eduethics.Utils.GlobalVariables;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.classroom.eduethics.Utils.LocalConstants.TO_FRAG.TO_BACK_ENROLL;

public class ClassroomStudentFragment extends Fragment {

    RecyclerView classroomRecyclerView;
    ArrayList<Classroom_ClassroomModel> data = new ArrayList<>();
    ArrayList<Classroom_ClassroomModel> dataSchedule = new ArrayList<>();
    ConstraintLayout constraintLayout;

    Context context;

    boolean isEnrolled = false;

    private static final String TAG = "ClassroomFragment";

    public ClassroomStudentFragment(Context context) {
        this.context = context;
    }

    TextInputEditText enrollID;

    View root;

    View progress;
    List<Map<String, Object>> toTimeTableData = new ArrayList<>();

    RecyclerView scheduleRecycler;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_classroom, container, false);
        MainActivity.TO_WHICH_FRAG = LocalConstants.TO_FRAG.TO_CLASSROOM;
        MainActivity.actionBarTitle.setText("Eduethics Desk");


        ((TextView) root.findViewById(R.id.todayData)).setText(ExtraFunctions.getReadableDateInString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.YEAR)));


        scheduleRecycler = root.findViewById(R.id.scheduleRecycler);
        scheduleRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        progress = root.findViewById(R.id.progress);
        progress.setVisibility(View.GONE);

        classroomRecyclerView = root.findViewById(R.id.recyclerviewClassroom_Classroom);
        constraintLayout = root.findViewById(R.id.MAIN_CLASSROOM);

        root.findViewById(R.id.enrole_Classroom).setOnClickListener(v -> enrollClassroom());
        enrollID = root.findViewById(R.id.enrollID);

        root.findViewById(R.id.viewTimeTable).setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, new ViewTimeTable(toTimeTableData)).commit();
        });

        getClassRoom();

        return root;
    }

    private void enrollClassroom() {
        if (isEnrolled) {
            progress.setVisibility(View.VISIBLE);
            String id = enrollID.getText().toString();
            FirebaseFirestore.getInstance()
                    .collection("utilData")
                    .document("classroomIds")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            boolean isFind = false;
                            for (Map.Entry<String, Object> stringObjectEntry : documentSnapshot.getData().entrySet()) {
                                if (stringObjectEntry.getValue().toString().equals(id)) {
                                    isFind = true;
                                    FirebaseFirestore.getInstance().collection("students")
                                            .document(GlobalVariables.uid)
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot1) {
                                                    List<String> data = (List) documentSnapshot1.get("classes");
                                                    if (data == null)
                                                        data = new ArrayList<>();
                                                    Toast.makeText(getContext(), "" + GlobalVariables.uid, Toast.LENGTH_SHORT).show();
                                                    data.add(stringObjectEntry.getKey() + "_p");

                                                    Map<String, Object> map1 = new HashMap<>();
                                                    map1.put("classes", data);

                                                    FirebaseFirestore.getInstance()
                                                            .collection("classroom")
                                                            .document(stringObjectEntry.getKey())
                                                            .get()
                                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                    List<Map<String, Object>> lst = (List) documentSnapshot.get("pendingStudents");
                                                                    Map<String, Object> map = new HashMap<>();
                                                                    map.put("id", GlobalVariables.uid);
                                                                    map.put("image", "-"/*documentSnapshot1.getString("-")*/);
                                                                    Toast.makeText(getContext(), "" + documentSnapshot1.getString("name"), Toast.LENGTH_SHORT).show();
                                                                    map.put("name", documentSnapshot1.getString("name"));
                                                                    map.put("number", documentSnapshot1.getString("number"));
                                                                    lst.add(map);
                                                                    String teacherId = documentSnapshot.getString("teacherId");
                                                                    if (teacherId != null)
                                                                        FirebaseFirestore.getInstance()
                                                                                .collection("teacher")
                                                                                .document(teacherId)
                                                                                .get()
                                                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                                    @Override
                                                                                    public void onSuccess(DocumentSnapshot documentSnapshot2) {
                                                                                        try {
                                                                                            new FireMessage("New Student Request", GlobalVariables.name + " is requesting to join your " + documentSnapshot.getString("className") + " classroom.", getContext()).sendToToken(documentSnapshot2.getString("token"));
                                                                                        } catch (Exception e) {
                                                                                            e.printStackTrace();
                                                                                        }
                                                                                    }
                                                                                });

                                                                    FirebaseFirestore.getInstance()
                                                                            .collection("classroom")
                                                                            .document(stringObjectEntry.getKey())
                                                                            .update("pendingStudents", lst)
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    FirebaseFirestore.getInstance()
                                                                                            .collection("students")
                                                                                            .document(GlobalVariables.uid)
                                                                                            .update(map1)
                                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                @Override
                                                                                                public void onSuccess(Void aVoid) {

                                                                                                    getClassRoom();
                                                                                                    if (getActivity() == null)
                                                                                                        return;
                                                                                                    getActivity().onBackPressed();
                                                                                                    progress.setVisibility(View.GONE);
                                                                                                    isEnrolled = false;
                                                                                                }
                                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                        @Override
                                                                                        public void onFailure(@NonNull Exception e) {
                                                                                            progress.setVisibility(View.GONE);
                                                                                        }
                                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                                        @Override
                                                                                        public void onFailure(@NonNull Exception e) {
                                                                                            progress.setVisibility(View.GONE);
                                                                                        }
                                                                                    });
                                                                                }
                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            progress.setVisibility(View.GONE);
                                                                        }
                                                                    });
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            progress.setVisibility(View.GONE);
                                                        }
                                                    });
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progress.setVisibility(View.GONE);
                                        }
                                    });
                                    break;
                                }
                            }
                            if (!isFind) {
                                progress.setVisibility(View.GONE);
                                enrollID.setError("Please enter correct classroom id");
                            }
                        }
                    });
        } else {
            MainActivity.frag = this;
            isEnrolled = true;
            TransitionManager.beginDelayedTransition(constraintLayout);
            MainActivity.TO_WHICH_FRAG = TO_BACK_ENROLL;
            root.findViewById(R.id.noEnroll).setVisibility(View.GONE);
            root.findViewById(R.id.yesEnroll).setVisibility(View.VISIBLE);
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
            params.topToBottom = R.id.temp4;
            params.setMargins(24, 36, 24, 24);
            root.findViewById(R.id.inviteTeacher).setVisibility(View.VISIBLE);
            root.findViewById(R.id.enrole_Classroom).setLayoutParams(params);
        }

    }

    public void backEnroll() {
        isEnrolled = false;
        TransitionManager.beginDelayedTransition(constraintLayout);
        root.findViewById(R.id.noEnroll).setVisibility(View.VISIBLE);
        root.findViewById(R.id.yesEnroll).setVisibility(View.GONE);
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        params.setMargins(24, 0, 24, 18);
        root.findViewById(R.id.inviteTeacher).setVisibility(View.GONE);
        root.findViewById(R.id.enrole_Classroom).setLayoutParams(params);
        MainActivity.TO_WHICH_FRAG = LocalConstants.TO_FRAG.TO_CLASSROOM;
        MainActivity.frag = null;
        if (ids.size() != 0) root.findViewById(R.id.noSch).setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
    }

    private void getClassRoom() {
        progress.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance()
                .collection("students")
                .document(GlobalVariables.uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> doFetchAndForward((List) documentSnapshot.get("classes")))
                .addOnFailureListener(e -> {
                    if (getActivity() == null) return;
                    Toast.makeText(context, "Unable To Fetch Data !", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    progress.setVisibility(View.GONE);
                });
    }

    List<String> ids = new ArrayList<>();


    private void doFetchAndForward(List<String> runningClasses) {
        ids = new ArrayList<>();
        for (String runningClass : runningClasses)
            if (!runningClass.endsWith("_p")) ids.add(runningClass);


        toTimeTableData = new ArrayList<>();
        if (runningClasses == null) {
            progress.setVisibility(View.GONE);
            return;
        }
        ((TextView) root.findViewById(R.id.countClassroom)).setText("(" + runningClasses.size() + ")");

        data.clear();
        if (runningClasses.size() == 0) {
            progress.setVisibility(View.GONE);
        }

        for (String classId : runningClasses) {
            boolean isConfirm = true;
            if (classId.endsWith("_p")) {
                classId = classId.split("_")[0];
                isConfirm = false;
            }
            boolean finalIsConfirm = isConfirm;
            String finalClassId = classId;
            FirebaseFirestore.getInstance()
                    .collection("classroom")
                    .document(classId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Classroom_ClassroomModel model = new Classroom_ClassroomModel();
                        model.setDoc(finalClassId);
                        model.setaClass(documentSnapshot.getString("className"));
                        model.setSubject(documentSnapshot.getString("subject"));
                        model.setStudentsActive(documentSnapshot.getString("stats"));
                        model.setConfirm(finalIsConfirm);
                        Map<String, Object> si = new HashMap<>();
                        si.put("id", finalClassId);
                        si.put("name", documentSnapshot.getString("className"));
                        toTimeTableData.add(si);

                        data.add(model);
                        if (data.size() == runningClasses.size()) {
                            //data.addAll(data);
                            if (getActivity() == null) return;
                            classroomRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                            classroomRecyclerView.setAdapter(new Classroom_ClassroomAdapter(ClassroomStudentFragment.this, data));
                            progress.setVisibility(View.GONE);
                        }
                    })
                    .addOnFailureListener(e -> progress.setVisibility(View.GONE));
        }

        schedule(ids);
    }


    private void schedule(List<String> ids) {
        List<Map<String, String>> dataForSend = new ArrayList<>();
        for (String id : ids) {
            FirebaseFirestore.getInstance()
                    .collection("classroom")
                    .document(id)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {

                        List<Map<String, Object>> table = (List) documentSnapshot.get("timetable");
                        String dayToday = LocalConstants.DAYS[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1];
                        for (Map<String, Object> obj : table) {
                            if (obj.get("day").toString().equals(dayToday))
                                if ((boolean) obj.get("isSet")) {
                                    Map<String, String> temp1 = new HashMap<>();
                                    temp1.put("time", obj.get("fromTime").toString());
                                    temp1.put("class", documentSnapshot.getString("className"));
                                    temp1.put("subject", documentSnapshot.getString("subject"));
                                    temp1.put("id", id);
                                    dataForSend.add(temp1);
                                }
                        }
                        if (ids.indexOf(id) == ids.size() - 1) {
                            if (dataForSend.size() != 0) {
                                TransitionManager.beginDelayedTransition(root.findViewById(R.id.MAIN_CLASSROOM));
                                root.findViewById(R.id.noSch).setVisibility(View.GONE);
                                scheduleRecycler.setVisibility(View.VISIBLE);
                                scheduleRecycler.setAdapter(new ScheduleAdapterTeacher(dataForSend, ClassroomStudentFragment.this));
                            }

                        }
                    });
        }
    }


    public void visitClassroom(int position) {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, new HomeClassroom(context, data.get(position).getDoc(), LocalConstants.TYPE.STUDENT)).commit();
    }

    public void joinClassroom(int position) {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, new HomeClassroom(context, data.get(position).getDoc(), LocalConstants.TYPE.STUDENT)).commit();
    }

    public void joinClass(int position) {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, new HomeClassroom(context, ids.get(position), LocalConstants.TYPE.STUDENT)).commit();
    }
}