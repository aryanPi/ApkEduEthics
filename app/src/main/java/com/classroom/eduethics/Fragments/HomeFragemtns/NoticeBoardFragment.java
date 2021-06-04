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
import com.classroom.eduethics.Adapter.NotieAdapter;
import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.Fragments.TeacherFragments.CreateNotice;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.LocalConstants;


public class NoticeBoardFragment extends Fragment {


    Context context;
    public  NoticeBoardFragment(Context context){
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
        View root = inflater.inflate(R.layout.fragment_notice_board, container, false);

        MainActivity.TO_WHICH_FRAG =  LocalConstants.TO_FRAG.TO_CLASSROOM ;

        if (HomeClassroom.from== LocalConstants.TYPE.STUDENT)
        {
            ((TextView) root.findViewById(R.id.tit)).setText("Important announcements posted\nby the teacher can be \nfound here");
            root.findViewById(R.id.createNotice).setVisibility(View.GONE);
        }


        if (HomeClassroom.model.getNotice().size()==0){
            root.findViewById(R.id.empty).setVisibility(View.VISIBLE);
            ((TextView)root.findViewById(R.id.emptyMessage)).setText("No Notice Found!");
        }

        RecyclerView recyclerviewNotice = root.findViewById(R.id.recyclerviewNotice);
        ((TextView)root.findViewById(R.id.countText)).setText(HomeClassroom.model.getNotice().size()+" Announcements Posted");
        recyclerviewNotice.setLayoutManager(new LinearLayoutManager(context));
        recyclerviewNotice.setAdapter(new NotieAdapter(context));

        root.findViewById(R.id.createNotice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(
                        R.anim.slide_in,  // enter
                        R.anim.fade_out,  // exit
                        R.anim.fade_in,   // popEnter
                        R.anim.slide_out  // popExit
                ).replace(R.id.fragmentFrame,new CreateNotice()).commit();
            }
        });



        return root;
    }
}