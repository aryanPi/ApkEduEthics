package com.classroom.eduethics.Fragments.SubFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.Adapter.SubmissionAdapter;
import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.classroom.eduethics.Utils.LocalConstants.FROM.FROM_ASSIGNMENT_FRAG;
import static com.classroom.eduethics.Utils.LocalConstants.FROM.FROM_TEST_FRAG;


public class ViewSubmissionTest extends Fragment {

    String docId;
    List<Map<String, Object>> finalData = new ArrayList<>();
    List<String> keys = new ArrayList<>();
    int from;
    Map<String, Object> about;

    public ViewSubmissionTest(String docId, int from, Map<String, Object> about) {
        this.docId = docId;
        this.from = from;
        this.about = about;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_view_submission_test, container, false);

        MainActivity.TO_WHICH_FRAG = from == FROM_ASSIGNMENT_FRAG ? LocalConstants.TO_FRAG.TO_ASSIGNMENT_FRAG : LocalConstants.TO_FRAG.TO_TEST_FRAG;


        RecyclerView recyclerViewSubmission = root.findViewById(R.id.recyclerViewSubmission);
        recyclerViewSubmission.setLayoutManager(new LinearLayoutManager(getContext()));
        String collection = "";

        if (from == FROM_TEST_FRAG) {
            collection = "testSub";
            ((TextView) root.findViewById(R.id.duration)).setText("Duration : "+about.get("testDuration").toString()+" minutes");
        } else if (from == FROM_ASSIGNMENT_FRAG) collection = "assignmentSub";

        ((TextView) root.findViewById(R.id.topic)).setText(about.get("topic").toString());
        ((TextView) root.findViewById(R.id.date)).setText("Due Date : "+about.get("date").toString());


        FirebaseFirestore.getInstance()
                .collection("classroom")
                .document(HomeClassroom.classId)
                .collection(collection)
                .document(docId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (getActivity() == null) return;
                        Map<String, Object> data = documentSnapshot.getData();
                        if (data != null || data.size() != 0)
                            for (Map.Entry<String, Object> stringObjectEntry : data.entrySet()) {
                                finalData.add((Map) stringObjectEntry.getValue());
                                keys.add(stringObjectEntry.getKey());
                            }
                        if (finalData.size() == 0) {
                            root.findViewById(R.id.empty).setVisibility(View.VISIBLE);
                            ((TextView) root.findViewById(R.id.emptyMessage)).setText("No Submission Yet !");
                        }
                        ((TextView)root.findViewById(R.id.submitBy)).setText("Submitted by "+finalData.size()+"/"+HomeClassroom.model.getCurrentStudents().size()+" Students");

                        recyclerViewSubmission.setAdapter(new SubmissionAdapter(ViewSubmissionTest.this, finalData));
                    }
                })
        ;

        return root;
    }

    public void evaluate(int position) {
        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                R.anim.slide_in,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.slide_out  // popExit
        ).replace(R.id.fragmentFrame, new EvaluateFragment(docId, keys.get(position), finalData.get(position), from)).commit();
    }
}