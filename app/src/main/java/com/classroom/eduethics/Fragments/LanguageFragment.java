package com.classroom.eduethics.Fragments;

import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.R;

import java.util.ArrayList;
import java.util.List;


public class LanguageFragment extends Fragment {


    List<View> views = new ArrayList<>();

    int pre =0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_language, container, false);
        MainActivity.actionBarTitle.setText("Language");

        views.add(root.findViewById(R.id.en));
        views.add(root.findViewById(R.id.hi));
        views.add(root.findViewById(R.id.pu));


        root.findViewById(R.id.selectLang).setOnClickListener(v -> selectlang());

        root.findViewById(R.id.en).setOnClickListener(v -> thiClick(0));
        root.findViewById(R.id.hi).setOnClickListener(v -> thiClick(1));
        root.findViewById(R.id.pu).setOnClickListener(v -> thiClick(2));

        thiClick(0);
        thiClick(0);

        return root;
    }

    private void selectlang() {

    }

    private void thiClick(int i) {
        if (pre!=i){
            ((TransitionDrawable) views.get(pre).getBackground()).startTransition(100);
            ((TransitionDrawable) views.get(i).getBackground()).reverseTransition(100);
        }
        pre = i;
    }
}