package com.classroom.eduethics.Fragments.SubFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.GlobalVariables;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationFrag extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notification, container, false);
        RecyclerView recyclerNotification = root.findViewById(R.id.recyclerNotification);
        recyclerNotification.setLayoutManager(new LinearLayoutManager(getContext()));


        FirebaseFirestore.getInstance()
                .collection(GlobalVariables.isStudent ? "students" : "teacher")
                .document(GlobalVariables.uid)
                .collection("noti")
                .document("data")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        Map<String, Object> data = new HashMap<>();
                        List<Map<String, String>> forSendForward = new ArrayList<>();
                        if (documentSnapshot.getData()!=null){
                            data = documentSnapshot.getData();
                            for (Map.Entry<String, Object> entry : data.entrySet()) {
                                Map<String, String> map = new HashMap<>();
                                map.put("title", ((Map) entry.getValue()).get("title").toString());

                                String s2 = DateFormat.format("dd:MM", new java.sql.Date(Long.parseLong(entry.getKey()))).toString();

                                s2 = s2.split(":")[0]+" "+ LocalConstants.MONTHS[Integer.parseInt(s2.split(":")[1])-1].substring(0,3);

                                map.put("body", ((Map) entry.getValue()).get("body").toString());
                                map.put("date", s2);
                                forSendForward.add(map);

                            }
                        }

                        if (forSendForward.size() != 0) {
                            recyclerNotification.setAdapter(new adapterInner(forSendForward));
                        }else{
                            root.findViewById(R.id.empty).setVisibility(View.VISIBLE);
                            ((TextView)root.findViewById(R.id.emptyMessage)).setText("No Notication");
                        }
                    }
                });
        return root;
    }

    public static class adapterInner extends RecyclerView.Adapter<adapterInner.vh> {

        List<Map<String, String>> data;

        public adapterInner(List<Map<String, String>> data) {
            this.data = data;
        }

        @NonNull
        @NotNull
        @Override
        public vh onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            return new vh(LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_layout_single,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull NotificationFrag.adapterInner.vh holder, int position) {
            ((TextView)holder.itemView.findViewById(R.id.titleNotification)).setText(data.get(position).get("title"));
            ((TextView)holder.itemView.findViewById(R.id.bodyNotficaiton)).setText(data.get(position).get("body"));
            ((TextView)holder.itemView.findViewById(R.id.dateNotification)).setText(data.get(position).get("date"));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public static class vh extends RecyclerView.ViewHolder {
            public vh(@NonNull @NotNull View itemView) {
                super(itemView);
            }
        }
    }

}