package com.classroom.eduethics.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.classroom.eduethics.Activity.CapActivity;
import com.classroom.eduethics.R;
import com.google.zxing.integration.android.IntentIntegrator;


public class WebLogin extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root= inflater.inflate(R.layout.fragment_web_login, container, false);


        root.findViewById(R.id.scan).setOnClickListener(v -> {
            IntentIntegrator scanIntegrator = new IntentIntegrator(getActivity());
            scanIntegrator.setPrompt("Scan QR From Website");
            scanIntegrator.setBeepEnabled(true);

            scanIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            scanIntegrator.setCaptureActivity(CapActivity.class);
            scanIntegrator.setOrientationLocked(true);
            scanIntegrator.setBarcodeImageEnabled(true);
            scanIntegrator.initiateScan();

        });

        return root;
    }

}