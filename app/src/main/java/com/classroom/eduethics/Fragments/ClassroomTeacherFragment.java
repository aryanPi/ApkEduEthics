package com.classroom.eduethics.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.Adapter.ClassroomTeachersAdapter;
import com.classroom.eduethics.Adapter.ScheduleAdapterTeacher;
import com.classroom.eduethics.Fragments.SubFragments.ViewTimeTable;
import com.classroom.eduethics.Fragments.TeacherFragments.CreateClassroomFragment;
import com.classroom.eduethics.Fragments.TeacherFragments.WebSitePage;
import com.classroom.eduethics.Fragments.TeacherFragments.WebsiteView;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.ExtraFunctions;
import com.classroom.eduethics.Utils.GlobalVariables;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassroomTeacherFragment extends Fragment {

    Context context;


    RecyclerView classroomRecyclerView, scheduleRecycler;

    List<Map<String, Object>> classRoomsData;

    public ClassroomTeacherFragment(Context context) {
        this.context = context;
    }
    View root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_classroom_teacher, container, false);
        MainActivity.actionBarTitle.setText("Eduethics (Teacher)");
        MainActivity.TO_WHICH_FRAG = LocalConstants.TO_FRAG.TO_CLASSROOM;
        classroomRecyclerView = root.findViewById(R.id.recyclerViewClassroom);
        classroomRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        scheduleRecycler = root.findViewById(R.id.scheduleRecycler);
        scheduleRecycler.setLayoutManager(new LinearLayoutManager(getContext()));


        ((TextView) root.findViewById(R.id.date)).setText(ExtraFunctions.getReadableDateInString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.YEAR)));

        root.findViewById(R.id.viewTimeTable).setOnClickListener(v -> viewTimeTable());


        FirebaseFirestore.getInstance()
                .collection("teacher")
                .document(GlobalVariables.uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (getActivity() == null) return;
                        if(!documentSnapshot.getString("isSite").equals("no")){
                            ((TextView)root.findViewById(R.id.t)).setText("View Website");
                            root.findViewById(R.id.website).setOnClickListener(v -> getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame,new WebsiteView(documentSnapshot.getString("isSite"))).commit());
                        }else{
                            root.findViewById(R.id.website).setOnClickListener(v -> getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame,new WebSitePage()).commit());
                        }
                        classRoomsData = (List<Map<String, Object>>) documentSnapshot.get("classroom");
                        if (classRoomsData != null) {
                            ids = new ArrayList<>();
                            for (Map<String, Object> obj : classRoomsData) {
                                ids.add(obj.get("id").toString());
                            }
                            schedule(ids);
                            classroomRecyclerView.setAdapter(new ClassroomTeachersAdapter(ClassroomTeacherFragment.this, classRoomsData));
                        }

                    }
                });

        root.findViewById(R.id.createClassroom).setOnClickListener(v -> getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, new CreateClassroomFragment(context, -1)).commit());

        return root;
    }
    List<String> ids = new ArrayList<>();
    private void schedule(List<String> ids) {
        List<Map<String, String>> dataForSend = new ArrayList<>();
        for (String id : ids) {
            FirebaseFirestore.getInstance()
                    .collection("classroom")
                    .document(id)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {

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
                            if (ids.indexOf(id)==ids.size()-1){
                                if (dataForSend.size()!=0)
                                {
                                    TransitionManager.beginDelayedTransition(root.findViewById(R.id.MM));
                                    root.findViewById(R.id.noSch).setVisibility(View.GONE);
                                    scheduleRecycler.setVisibility(View.VISIBLE);
                                     scheduleRecycler.setAdapter(new ScheduleAdapterTeacher(dataForSend,ClassroomTeacherFragment.this));
                                }

                            }
                        }
                    });
        }
    }

    private void viewTimeTable() {
        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                R.anim.slide_in,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.slide_out  // popExit
        ).replace(R.id.fragmentFrame, new ViewTimeTable(classRoomsData)).commit();
    }

    public void click(int position) {
        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                R.anim.slide_in,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.slide_out  // popExit
        ).replace(R.id.fragmentFrame, new HomeClassroom(getContext(), classRoomsData.get(position).get("id").toString(), LocalConstants.TYPE.TEACHER)).commit();
    }


    public void joinClass(int position) {
        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                R.anim.slide_in,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.slide_out  // popExit
        ).replace(R.id.fragmentFrame, new HomeClassroom(context, ids.get(position), LocalConstants.TYPE.TEACHER)).commit();
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setExitTransition(inflater.inflateTransition(R.transition.fade));
    }
}