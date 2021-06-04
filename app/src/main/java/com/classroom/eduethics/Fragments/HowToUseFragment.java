package com.classroom.eduethics.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.R;


public class HowToUseFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root =  inflater.inflate(R.layout.fragment_how_to_use, container, false);
        MainActivity.actionBarTitle.setText("How To Use");
        return root;
    }
}