package com.classroom.eduethics.Fragments.HomeFragemtns;

import android.content.Context;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.Adapter.AssignmentAdapter;
import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.Fragments.SubFragments.AddSolution;
import com.classroom.eduethics.Fragments.SubFragments.StartSubmission;
import com.classroom.eduethics.Fragments.SubFragments.ViewSubmissionTest;
import com.classroom.eduethics.Fragments.TeacherFragments.CreateAssignment;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.LocalConstants;


public class AssignmentFragment extends Fragment {

    RecyclerView recyclerAssignment;
    Context context;

    public TextView submittedText, pendingText;

    public AssignmentFragment(Context context) {
        this.context = context;
        MainActivity.TO_WHICH_FRAG = LocalConstants.TO_FRAG.TO_HOME_CLASSROOM;
    }

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
        root = inflater.inflate(R.layout.fragment_assignment, container, false);

        if (HomeClassroom.from == LocalConstants.TYPE.STUDENT) {
            ((TextView) root.findViewById(R.id.tit)).setText("Assignments scheculed by teacher \nappear here");

            root.findViewById(R.id.createAssignment).setVisibility(View.GONE);
        } else {
            teacher();
        }

        pendingText = root.findViewById(R.id.pendingText);
        submittedText = root.findViewById(R.id.submittedText);

        recyclerAssignment = root.findViewById(R.id.recyclerAssignment);
        recyclerAssignment.setLayoutManager(new LinearLayoutManager(context));
        recyclerAssignment.setAdapter(new AssignmentAdapter(this));

        if (HomeClassroom.model.getAssignments().size() == 0) {
            root.findViewById(R.id.empty).setVisibility(View.VISIBLE);
            ((TextView) root.findViewById(R.id.emptyMessage)).setText("No Assignment Found!");
        }

        root.findViewById(R.id.createAssignment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                ).replace(R.id.fragmentFrame, new CreateAssignment()).commit();
            }
        });

        return root;
    }


    private void teacher() {
        root.findViewById(R.id.pendingText).setVisibility(View.GONE);
        root.findViewById(R.id.submittedText).setVisibility(View.GONE);
        root.findViewById(R.id.createAssignment).setOnClickListener(v -> getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                R.anim.slide_in,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.slide_out  // popExit
        ).replace(R.id.fragmentFrame, new CreateAssignment()).commit());
    }

    public void view(int position) {
        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                R.anim.slide_in,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.slide_out  // popExit
        ).replace(R.id.fragmentFrame, new ViewSubmissionTest(HomeClassroom.model.getAssignments().get(position).get("resultId").toString(), LocalConstants.FROM.FROM_ASSIGNMENT_FRAG, HomeClassroom.model.getAssignments().get(position))).commit();
    }

    public void edit(int position) {
        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                R.anim.slide_in,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.slide_out  // popExit
        ).replace(R.id.fragmentFrame, new CreateAssignment(HomeClassroom.model.getAssignments().get(position), position)).commit();
    }

    public void open(int position, boolean isDead) {
        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                R.anim.slide_in,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.slide_out  // popExit
        ).replace(R.id.fragmentFrame, new StartSubmission(HomeClassroom.model.getAssignments().get(position), LocalConstants.FROM.FROM_ASSIGNMENT_FRAG, isDead)).commit();
    }

    public void downloadImageAndShow(int position) {

    }

    public void solution(int position) {
        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                R.anim.slide_in,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.slide_out  // popExit
        ).replace(R.id.fragmentFrame, new AddSolution("assignments", HomeClassroom.model.getAssignments().get(position), true)).commit();

    }
}