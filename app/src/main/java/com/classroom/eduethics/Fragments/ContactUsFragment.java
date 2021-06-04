package com.classroom.eduethics.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class ContactUsFragment extends Fragment {

    Context context;

    public ContactUsFragment(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_contact_us, container, false);

        MainActivity.actionBarTitle.setText("Contact Us");


        FirebaseFirestore.getInstance().collection("utilData").document("appData").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                ((TextView)root.findViewById(R.id.num1)).setText(documentSnapshot.getString("contactNo"));
                ((TextView)root.findViewById(R.id.num2)).setText(documentSnapshot.getString("contactNo"));
                ((TextView)root.findViewById(R.id.email)).setText(documentSnapshot.getString("contactEmail"));
            }
        });

        return root;
    }
}