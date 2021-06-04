package com.classroom.eduethics.Fragments.TeacherFragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.Adapter.TimeTableAdapter;
import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.ExtraFunctions;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TimeTable extends Fragment {

    Context context;


    RecyclerView recyclerViewTimeTable;

    List<Map<String, Object>> data;

    public TimeTable(Context context) {
        this.context = context;
    }

    boolean isEdit = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_time_tabke, container, false);

        MainActivity.TO_WHICH_FRAG = LocalConstants.TO_FRAG.TO_HOME_CLASSROOM;

        recyclerViewTimeTable = root.findViewById(R.id.recyclerFeeStudent);
        recyclerViewTimeTable.setLayoutManager(new LinearLayoutManager(context));

        FirebaseFirestore.getInstance()
                .collection("classroom")
                .document(HomeClassroom.classId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (getActivity() == null) return;
                        data = (List) documentSnapshot.get("timetable");
                        recyclerViewTimeTable.setAdapter(new TimeTableAdapter(data, TimeTable.this));
                    }
                });
        return root;
    }

    public void datePicked(int position, int hourOfDayFrom, int minFrom, int hourOfDayTo, int minTo, boolean b) {
        data.get(position).put("fromTime", hourOfDayFrom + ":" + minFrom);
        data.get(position).put("toTime", hourOfDayTo + ":" + minTo);
        data.get(position).put("isSet", b);
        isEdit = true;
        FirebaseFirestore.getInstance()
                .collection("classroom")
                .document(HomeClassroom.classId)
                .update("timetable", data);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isEdit){
            List<String> ids = new ArrayList<>();
            for (Map<String, Object> currentStudent : HomeClassroom.model.getCurrentStudents()) {
                ids.add(currentStudent.get("id").toString());
            }
            ExtraFunctions.sendNoti(ids,HomeClassroom.model.getClassName()+" Classroom Update","Time table scheduled of "+HomeClassroom.model.getClassName()+" Classroom by teacher.",getContext());
        }
    }
}