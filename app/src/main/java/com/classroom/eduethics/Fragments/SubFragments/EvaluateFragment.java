package com.classroom.eduethics.Fragments.SubFragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.classroom.eduethics.Activity.ImagePage;
import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.Adapter.uploadImagesAdapter;
import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.classroom.eduethics.Utils.LocalConstants.FROM.FROM_ASSIGNMENT_FRAG;
import static com.classroom.eduethics.Utils.LocalConstants.FROM.FROM_EVALUATE_FRAG;
import static com.classroom.eduethics.Utils.LocalConstants.FROM.FROM_TEST_FRAG;

public class EvaluateFragment extends Fragment {

    String docId, studentId;
    Map<String, Object> data;

    ArrayList<String> submissionList = new ArrayList<>();

    uploadImagesAdapter adapter;

    int from;

    public EvaluateFragment(String docId, String studentId, Map<String, Object> data, int from) {
        this.studentId = studentId;
        this.docId = docId;
        this.data = data;
        this.from = from;
        adapter = new uploadImagesAdapter(submissionList, this, FROM_EVALUATE_FRAG,null);
    }
    View root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_evaluate, container, false);

        adapter.setProgress(root.findViewById(R.id.progress));

        RecyclerView submissionRecyclerView = root.findViewById(R.id.submissionRecyclerView);
        submissionRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        MainActivity.TO_WHICH_FRAG = from == FROM_ASSIGNMENT_FRAG ? LocalConstants.TO_FRAG.TO_ASSIGNMENT_FRAG : LocalConstants.TO_FRAG.TO_TEST_FRAG;
        submissionRecyclerView.setAdapter(adapter);
        MainActivity.TO_WHICH_FRAG = LocalConstants.TO_FRAG.TO_HOME_CLASSROOM;
        ((EditText) root.findViewById(R.id.score)).setText(data.get("score").toString().split("/")[0].equals("-")?"":data.get("score").toString().split("/")[0]);
        ((EditText) root.findViewById(R.id.totalScore)).setText(data.get("score").toString().split("/")[1]);

        submissionList.addAll((List) data.get("attachment"));
        adapter.notifyDataSetChanged();

        if (!data.get("feedback").toString().equals("-")) {
            ((EditText) root.findViewById(R.id.feedbackText)).setText(data.get("feedback").toString());
        }


        root.findViewById(R.id.submit).setOnClickListener(v -> {
            boolean allDone = true;
            if (((EditText) root.findViewById(R.id.score)).getText().toString().equals("")){
                ((EditText) root.findViewById(R.id.score)).setError("Required!");
                allDone=false;
            }else if (Integer.parseInt(((EditText) root.findViewById(R.id.score)).getText().toString())>Integer.parseInt(((EditText) root.findViewById(R.id.totalScore)).getText().toString())){
                Toast.makeText(getContext(), "Give right Score", Toast.LENGTH_SHORT).show();
                allDone = false;
            }


            if (allDone){
                Map<String, Object> map = new HashMap<>();
                data.put("score", ((EditText) root.findViewById(R.id.score)).getText().toString() + "/" + ((EditText) root.findViewById(R.id.totalScore)).getText().toString());
                if (((EditText) root.findViewById(R.id.feedbackText)).getText().toString().equals("")) {
                    data.put("feedback", "-");
                } else {
                    data.put("feedback", ((EditText) root.findViewById(R.id.feedbackText)).getText().toString());
                }
                data.put("isSeen", true);
                map.put(studentId, data);
                String collection = "";
                if (from == FROM_ASSIGNMENT_FRAG) {
                    collection = "assignmentSub";
                } else if (from == FROM_TEST_FRAG) {
                    collection = "testSub";
                }
                FirebaseFirestore.getInstance()
                        .collection("classroom")
                        .document(HomeClassroom.classId)
                        .collection(collection)
                        .document(docId)
                        .update(map)
                        .addOnSuccessListener(aVoid -> {
                            if (getActivity() == null) return;
                            getActivity().onBackPressed();
                        });
            }

        });

        return root;
    }


}