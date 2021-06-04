package com.classroom.eduethics.Fragments.TeacherFragments;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.classroom.eduethics.Adapter.WebsiteViewAdapter;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.GlobalVariables;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WebsiteView extends Fragment {

    String site;

    public WebsiteView(String site) {
        this.site = site;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_website_view, container, false);
        RecyclerView enquireyRecycler = root.findViewById(R.id.enquireyRecycler);
        String t = "Hey!\nCheck out my online store built using Edu Ethics app:\n\nhttps://"+site+"\n\nWatch my Demo Videos and enroll into my ongoing classrooms.\n\nThank you,\n"+ GlobalVariables.name;

        root.findViewById(R.id.shareWhatsapp).setOnClickListener(v -> {
            Intent sendIntent = new Intent().setAction(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, t).setType("text/plain").setPackage("com.whatsapp");
            //startActivity(Intent.createChooser(sendIntent, "Share on WhatsApp"));
            startActivity(sendIntent);
        });

        root.findViewById(R.id.shareonly).setOnClickListener(v -> startActivity(Intent.createChooser(new Intent().setAction(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, t ), "Share via")));

        FirebaseFirestore.getInstance()
                .collection("webD")
                .document(site)
                .collection("data")
                .document("data")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        if (documentSnapshot.getData() != null && documentSnapshot.getData().size() == 0) {
                            root.findViewById(R.id.empty).setVisibility(View.VISIBLE);
                            ((TextView) root.findViewById(R.id.emptyMessage)).setText("No Enquiries");
                        } else {
                            List<String> names = new ArrayList<>();
                            List<String> numbers = new ArrayList<>();
                            for (Map.Entry<String, Object> mapData : documentSnapshot.getData().entrySet()) {
                                names.add(mapData.getKey());
                                numbers.add(mapData.getValue().toString());
                            }
                            enquireyRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
                            enquireyRecycler.setAdapter(new WebsiteViewAdapter(WebsiteView.this, names, numbers));
                        }
                    } else {
                        root.findViewById(R.id.empty).setVisibility(View.VISIBLE);
                        ((TextView) root.findViewById(R.id.emptyMessage)).setText("No Enquiries");
                    }
                });

        return root;
    }
}