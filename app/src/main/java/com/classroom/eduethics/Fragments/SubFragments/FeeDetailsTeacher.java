package com.classroom.eduethics.Fragments.SubFragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.Adapter.FeeAdapter;
import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.Fragments.TeacherFragments.TeachersFeeStudentView;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FeeDetailsTeacher extends Fragment {


    Context context;
    RecyclerView recyclerviewFeeDetails;

    List<Map<String,Object>> data = new ArrayList<>();




    public FeeDetailsTeacher(Context context){
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root= inflater.inflate(R.layout.fragment_fee_details, container, false);
        MainActivity.TO_WHICH_FRAG = LocalConstants.TO_FRAG.TO_HOME_CLASSROOM;


        recyclerviewFeeDetails = root.findViewById(R.id.recyclerviewFeeDetails);
        recyclerviewFeeDetails.setLayoutManager(new LinearLayoutManager(context));

        root.findViewById(R.id.collectFee).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        FirebaseFirestore.getInstance()
                .collection("fees")
                .document(HomeClassroom.classId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        data = (List)documentSnapshot.get("students");
                        ((TextView)root.findViewById(R.id.fees)).setText(getString(R.string.Rs)+documentSnapshot.getString("feePerM")+"/Month");

                        Toast.makeText(context, ""+documentSnapshot.getString("startDate"), Toast.LENGTH_SHORT).show();
                        ((TextView)root.findViewById(R.id.startDateText)).setText(documentSnapshot.getString("startDate").equals("-")?"-":documentSnapshot.getString("startDate").split("-")[0]+" "+LocalConstants.MONTHS[(Integer.parseInt(documentSnapshot.getString("startDate").split("-")[1]))]+" "+documentSnapshot.getString("startDate").split("-")[2]);
                        ((TextView)root.findViewById(R.id.endDateText)).setText(documentSnapshot.getString("startDate").equals("-")?"-":
                                documentSnapshot.getString("startDate").split("-")[0]+" "+LocalConstants.MONTHS[(Integer.parseInt(documentSnapshot.getString("startDate").split("-")[1])+1)]+" "+documentSnapshot.getString("startDate").split("-")[2]);

                        ((TextView)root.findViewById(R.id.className)).setText(HomeClassroom.model.getSubject());

                        recyclerviewFeeDetails.setAdapter(new FeeAdapter(data, FeeDetailsTeacher.this,((TextView)root.findViewById(R.id.totalRec)),((TextView)root.findViewById(R.id.totalDue)),((TextView)root.findViewById(R.id.totalAdv))));
                    }
                });

        return root;
    }

    public void click(int position){
        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                R.anim.slide_in,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.slide_out  // popExit
        ).replace(R.id.fragmentFrame,new TeachersFeeStudentView(data.get(position))).commit();
    }

}