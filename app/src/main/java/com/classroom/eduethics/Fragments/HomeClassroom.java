package com.classroom.eduethics.Fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;


import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.Adapter.FragmentAdapter;
import com.classroom.eduethics.Fragments.HomeFragemtns.AssignmentFragment;
import com.classroom.eduethics.Fragments.HomeFragemtns.ChatFragment;
import com.classroom.eduethics.Fragments.HomeFragemtns.NoticeBoardFragment;
import com.classroom.eduethics.Fragments.HomeFragemtns.StudentFragment;
import com.classroom.eduethics.Fragments.HomeFragemtns.StudyMaterialFragment;
import com.classroom.eduethics.Fragments.HomeFragemtns.SummaryFragment;
import com.classroom.eduethics.Fragments.HomeFragemtns.TestFragment;
import com.classroom.eduethics.Models.HomeClassroomWholeModel;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class HomeClassroom extends Fragment {

    Context context;
    View root;

    TabLayout tabLayout;
    ViewPager2 pager2;
    FragmentAdapter adapter;

    public static HomeClassroomWholeModel model;
    public static String classId;
    public static int from;
    public static int toWhichFrag = 0;

    public HomeClassroom(Context context, String classID, int from) {
        this.context = context;
        classId = classID;
        HomeClassroom.from = from;
        MainActivity.TO_WHICH_FRAG = LocalConstants.TO_FRAG.TO_CLASSROOM;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_home_classroom, container, false);
        tabLayout = root.findViewById(R.id.tab_layout);
        pager2 = root.findViewById(R.id.pageViewerHomeClassroom);

        runTask();

        MainActivity.actionBarTitle.setText("Classroom");




        return root;
    }



    public void runTask() {
        root.findViewById(R.id.progress).setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance()
                .collection("classroom")
                .document(classId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        model = documentSnapshot.toObject(HomeClassroomWholeModel.class);
                        forward();
                    }
                });
    }

    private void forward() {

        if (getActivity()==null){
            return;
        }

        FragmentManager fm = getActivity().getSupportFragmentManager();
        ArrayList<Fragment> fragments = new ArrayList<>();

        fragments.add(new SummaryFragment(context));
        fragments.add(new StudentFragment());
        fragments.add(new ChatFragment());
        fragments.add(new AssignmentFragment(context));
        fragments.add(new TestFragment(context));
        fragments.add(new NoticeBoardFragment(context));
        fragments.add(new StudyMaterialFragment(context));

        adapter = new FragmentAdapter(fm, getLifecycle(), fragments);
        pager2.setAdapter(adapter);




        tabLayout.addTab(tabLayout.newTab().setText("Summary"));
        tabLayout.addTab(tabLayout.newTab().setText("Students"));
        tabLayout.addTab(tabLayout.newTab().setText("Chat"));
        tabLayout.addTab(tabLayout.newTab().setText("Assignments"));
        tabLayout.addTab(tabLayout.newTab().setText("Test"));
        tabLayout.addTab(tabLayout.newTab().setText("Notice Board"));
        tabLayout.addTab(tabLayout.newTab().setText("Study Material"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);





        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        pager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
                if (position==4){

                }
            }
        });


        pager2.setCurrentItem(toWhichFrag);
        root.findViewById(R.id.progress).setVisibility(View.GONE);
    }



}