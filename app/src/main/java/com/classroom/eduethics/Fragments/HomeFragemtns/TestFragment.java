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
import com.classroom.eduethics.Adapter.TestAdapter;
import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.Fragments.SubFragments.AddSolution;
import com.classroom.eduethics.Fragments.SubFragments.StartSubmission;
import com.classroom.eduethics.Fragments.SubFragments.ViewSubmissionTest;
import com.classroom.eduethics.Fragments.TeacherFragments.CreateTest;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.LocalConstants;


public class TestFragment extends Fragment {

    RecyclerView recyclerTest;
    Context context;
    View root;

    public TextView submittedText, pendingText;

    public TestFragment(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setEnterTransition(inflater.inflateTransition(R.transition.slide_right));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_test_student, container, false);

        MainActivity.TO_WHICH_FRAG = LocalConstants.TO_FRAG.TO_CLASSROOM;

        if (HomeClassroom.from == LocalConstants.TYPE.STUDENT)
        {
            ((TextView) root.findViewById(R.id.tit)).setText("Tests scheculed by teacher \nappear here");

            root.findViewById(R.id.createTest).setVisibility(View.GONE);
        }
        else
            root.findViewById(R.id.createTest).setOnClickListener(v -> getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                    R.anim.slide_in,  // enter
                    R.anim.fade_out,  // exit
                    R.anim.fade_in,   // popEnter
                    R.anim.slide_out  // popExit
            ).replace(R.id.fragmentFrame, new CreateTest()).commit());

        pendingText = root.findViewById(R.id.pendingText);
        submittedText = root.findViewById(R.id.submittedText);

        if (HomeClassroom.from == LocalConstants.TYPE.TEACHER) {
            root.findViewById(R.id.pendingText).setVisibility(View.GONE);
            root.findViewById(R.id.submittedText).setVisibility(View.GONE);
        }

        if (HomeClassroom.model.getTest().size()==0){
            root.findViewById(R.id.empty).setVisibility(View.VISIBLE);
            ((TextView)root.findViewById(R.id.emptyMessage)).setText("No Test Found!");
        }


        recyclerTest = root.findViewById(R.id.recyclerTest);
        recyclerTest.setLayoutManager(new LinearLayoutManager(context));
        recyclerTest.setAdapter(new TestAdapter(this));


        return root;
    }


    public void view(int position) {
        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                R.anim.slide_in,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.slide_out  // popExit
        ).replace(R.id.fragmentFrame, new ViewSubmissionTest(HomeClassroom.model.getTest().get(position).get("resultId").toString(), LocalConstants.FROM.FROM_TEST_FRAG,HomeClassroom.model.getTest().get(position))).commit();
    }

    public void edit(int position) {
        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                R.anim.slide_in,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.slide_out  // popExit
        ).replace(R.id.fragmentFrame, new CreateTest(HomeClassroom.model.getTest().get(position), position)).commit();
    }

    public void open(int position,boolean isDead) {
        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                R.anim.slide_in,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.slide_out  // popExit
        ).replace(R.id.fragmentFrame, new StartSubmission(HomeClassroom.model.getTest().get(position), LocalConstants.FROM.FROM_TEST_FRAG,isDead)).commit();
    }

    public void downloadImageAndShow(int position) {

    }

    public void solution(int position) {
        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                R.anim.slide_in,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.slide_out  // popExit
        ).replace(R.id.fragmentFrame, new AddSolution("test", HomeClassroom.model.getTest().get(position), true)).commit();
    }
}