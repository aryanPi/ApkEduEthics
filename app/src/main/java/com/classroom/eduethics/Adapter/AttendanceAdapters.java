package com.classroom.eduethics.Adapter;

import android.graphics.Color;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.ExtraFunctions;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class AttendanceAdapters extends RecyclerView.Adapter<AttendanceAdapters.vh> {


    //(time)              UID-s   List    entry,exit
    Map<String, List<Map<String, List<Map<String, String>>>>> data2;


    Fragment fragment;
    //time->sts
    List<Map<String, List<Map<String, String>>>> data = new ArrayList<>();
    List<String> times = new ArrayList<>();

    public AttendanceAdapters(Map<String, Map<String, List<Map<String, String>>>> innderData, Fragment fragment) {
        for (Map.Entry<String, Map<String, List<Map<String, String>>>> obj : innderData.entrySet()) {
            times.add(obj.getKey());
            data.add(obj.getValue());
        }
        this.fragment = fragment;
    }


    @NonNull
    @Override
    public vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new vh(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_innter_att_teacher, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull vh holder, int position) {
        holder.attTime.setText("Time : " + ExtraFunctions.getTimeFromLong(times.get(position)));
        holder.recyclerViewAttTeacherItem.setLayoutManager(new LinearLayoutManager(fragment.getContext()));
        FirebaseFirestore.getInstance().collection("classroom").document(HomeClassroom.classId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        holder.totalStudents.setText("Total : " + ((List) documentSnapshot.get("currentStudents")).size() + " Students");

                        holder.presentStudents.setText("Present : " + data.get(position).size() + " Student(s)");
                        holder.absentStudents.setText("Absent : " + (((List) documentSnapshot.get("currentStudents")).size() - data.get(position).size()) + " Student(s)");
                        List<Map<String, Object>> days = (List) documentSnapshot.get("timetable");
                        Calendar calendar = Calendar.getInstance();
                        String day = LocalConstants.DAYS[calendar.get(Calendar.DAY_OF_WEEK) - 1];
                        for (Map<String, Object> obj : days) {
                            if (obj.get("day").toString().equals(day)) {
                                if ((boolean) obj.get("isSet")) {
                                    String fT = obj.get("fromTime").toString();
                                    String tT = obj.get("toTime").toString();
                                    Calendar calendar1 = Calendar.getInstance();
                                    calendar1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(fT.split(":")[0]));
                                    calendar1.set(Calendar.MINUTE, Integer.parseInt(fT.split(":")[1]));

                                    Calendar calendar2 = Calendar.getInstance();
                                    calendar2.set(Calendar.HOUR_OF_DAY, Integer.parseInt(tT.split(":")[0]));
                                    calendar2.set(Calendar.MINUTE, Integer.parseInt(tT.split(":")[1]));

                                    String diff = ExtraFunctions.getDurationFromLong(calendar1.getTimeInMillis() + "", calendar2.getTimeInMillis() + "", true);
                                    holder.recyclerViewAttTeacherItem.setAdapter(new innerAdapter(data.get(position), fragment, diff));
                                    break;
                                }
                                holder.recyclerViewAttTeacherItem.setAdapter(new innerAdapter(data.get(position), fragment, "-"));
                                break;
                            } else if (days.indexOf(day) == days.size() - 1) {
                                holder.recyclerViewAttTeacherItem.setAdapter(new innerAdapter(data.get(position), fragment, "-"));
                            }
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class vh extends RecyclerView.ViewHolder {
        RecyclerView recyclerViewAttTeacherItem;
        TextView totalStudents, presentStudents, attTime, absentStudents;

        public vh(@NonNull View itemView) {
            super(itemView);
            this.absentStudents = itemView.findViewById(R.id.absentStudents);
            this.attTime = itemView.findViewById(R.id.attTime);
            this.totalStudents = itemView.findViewById(R.id.totalStudents);
            this.presentStudents = itemView.findViewById(R.id.presentStudents);
            this.recyclerViewAttTeacherItem = itemView.findViewById(R.id.recyclerViewAttTeacherItem);
        }
    }


    static class innerAdapter extends RecyclerView.Adapter<innerAdapter.vh> {

        List<String> namesId = new ArrayList<>();
        List<List<Map<String, String>>> data = new ArrayList<>();

        Fragment fragment;
        String diff;

        public innerAdapter(Map<String, List<Map<String, String>>> data, Fragment fragment, String diff) {
            if (data == null) this.data = new ArrayList<>();
            else
                for (Map.Entry<String, List<Map<String, String>>> obj : data.entrySet()) {
                    namesId.add(obj.getKey());
                    this.data.add(obj.getValue());
                }
            this.fragment = fragment;
            this.diff = diff;
        }

        @NonNull
        @Override
        public innerAdapter.vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new innerAdapter.vh(LayoutInflater.from(parent.getContext()).inflate(R.layout.single_item_attendance, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull innerAdapter.vh holder, int position) {
            holder.name.setText(namesId.get(position));

            holder.name.setOnClickListener(v -> holder.attStudentDetails.setVisibility(holder.attStudentDetails.getVisibility() == View.GONE ? View.VISIBLE : View.GONE));

            FirebaseFirestore.getInstance().collection("students").document(namesId.get(position)).get().addOnSuccessListener(documentSnapshot -> holder.name.setText(documentSnapshot.getString("name")));

            holder.attStudentDetails.setLayoutManager(new LinearLayoutManager(fragment.getContext()));
            holder.attStudentDetails.setAdapter(new innderSecondAdapter(data.get(position), holder.dur, diff, holder));
            holder.dur.setOnClickListener(v -> holder.name.callOnClick());

        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public static class vh extends RecyclerView.ViewHolder {
            TextView name, dur;
            RecyclerView attStudentDetails;

            public vh(@NonNull View itemView) {
                super(itemView);
                this.dur = itemView.findViewById(R.id.durSingle);
                this.name = itemView.findViewById(R.id.name);
                this.attStudentDetails = itemView.findViewById(R.id.attStudentDetails);
            }
        }
    }


    static class innderSecondAdapter extends RecyclerView.Adapter<innderSecondAdapter.vh> {


        List<Map<String, String>> data;
        TextView dur;
        List<String> durations = new ArrayList<>();
        String diff;
        innerAdapter.vh ho;

        public innderSecondAdapter(List<Map<String, String>> data, TextView dur, String diff, innerAdapter.vh h) {
            this.data = data;
            this.dur = dur;
            this.diff = diff;
            this.ho = h;
        }


        @NonNull
        @Override
        public innderSecondAdapter.vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new vh(LayoutInflater.from(parent.getContext()).inflate(R.layout.single_attendance_details, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull innderSecondAdapter.vh holder, int position) {
            ((TextView) holder.itemView.findViewById(R.id.fromTime)).setText(ExtraFunctions.getTimeFromLong(data.get(position).get("entryTime")) + " - ");
            ((TextView) holder.itemView.findViewById(R.id.toTime)).setText(data.get(position).get("exitTime").equals("-")?"No Entry":ExtraFunctions.getTimeFromLong(data.get(position).get("exitTime")));

            durations.add(ExtraFunctions.getDurationFromLong(data.get(position).get("entryTime"), data.get(position).get("exitTime"), true));

            ((TextView) holder.itemView.findViewById(R.id.durationTime)).setText(ExtraFunctions.getDurationFromLong(data.get(position).get("entryTime"), data.get(position).get("exitTime"), false));

            if (position == data.size() - 1) {
                long h = 0;
                long m = 0;
                long s = 0;
                for (String duration : durations) {
                    if (!duration.equals("No Entry")){
                        h += Long.parseLong(duration.split(":")[0]);
                        m += Long.parseLong(duration.split(":")[1]);
                        s += Long.parseLong(duration.split(":")[2]);
                    }
                }
                if (!diff.equals("-"))
                    if ((h * 60 * 60 + m * 60 + s) < (Integer.parseInt(diff.split(":")[0]) * 60 * 60 + Integer.parseInt(diff.split(":")[1]) * 60 + Integer.parseInt(diff.split(":")[2])))
                        dur.setTextColor(Color.parseColor("#ffcc0000"));
                    else dur.setTextColor(Color.parseColor("#ff669900"));
                else dur.setTextColor(Color.parseColor("#ff669900"));
                dur.setText((h == 0 ? "" : h + " hr ") + (m == 0 ? "" : m + " min ") + (s == 0 ? "" : s + " sec"));
                if (dur.getText().toString().equals("")){
                    dur.setText("No Entry But Present");
                }
                ho.attStudentDetails.setVisibility(View.GONE);
            }

        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public static class vh extends RecyclerView.ViewHolder {
            public vh(@NonNull View itemView) {
                super(itemView);
            }
        }
    }

}
