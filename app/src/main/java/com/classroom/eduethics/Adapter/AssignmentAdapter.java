package com.classroom.eduethics.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;


import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.Fragments.HomeFragemtns.AssignmentFragment;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.ExtraFunctions;
import com.classroom.eduethics.Utils.GlobalVariables;
import com.classroom.eduethics.Utils.LocalConstants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.vh> {

    List<Map<String, Object>> assignments;
    Fragment fragment;
    int submitted = 0;

    public AssignmentAdapter(Fragment fragment) {
        this.fragment = fragment;
        this.assignments = HomeClassroom.model.getAssignments();
    }

    @NonNull
    @Override
    public vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new vh(LayoutInflater.from(fragment.getContext()).inflate(R.layout.assignment_single_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull vh holder, int position) {
        Map<String, Object> data = assignments.get(position);

        holder.title.setText(data.get("topic").toString());
        holder.time.setText(ExtraFunctions.getReadableTime(Integer.parseInt(data.get("time").toString().split(":")[0]), Integer.parseInt(data.get("time").toString().split(":")[1])));
        holder.maxNo.setText("Max Marks : " + data.get("maxMarks").toString());
        holder.date.setText(data.get("date").toString());


        if (HomeClassroom.from == LocalConstants.TYPE.TEACHER) {
            holder.submittedStudents.setText(((List) data.get("submitted")).size() + "/" + HomeClassroom.model.getCurrentStudents().size() + " Submitted");
            holder.status.setVisibility(View.GONE);
            holder.itemView.findViewById(R.id.edit).setOnClickListener(v -> ((AssignmentFragment) fragment).edit(position));
            holder.itemView.findViewById(R.id.viewSubmission).setOnClickListener(v -> ((AssignmentFragment) fragment).view(position));
            holder.itemView.findViewById(R.id.solution).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((AssignmentFragment) fragment).solution(position);
                }
            });
        } else {

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(((String) data.get("date")).split("-")[2]));
            calendar.set(Calendar.MONTH, Integer.parseInt(((String) data.get("date")).split("-")[1]));
            calendar.set(Calendar.YEAR, Integer.parseInt(((String) data.get("date")).split("-")[0]));
            calendar.set(Calendar.HOUR, Integer.parseInt(((String) data.get("time")).split(":")[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(((String) data.get("time")).split(":")[1]));

            if (Calendar.getInstance().getTimeInMillis() < calendar.getTimeInMillis()) {
                holder.status.setText(((List) data.get("submitted")).contains(GlobalVariables.uid) ? "Completed" : "Not Started");
                assignments.get(position).put("isDead", false);
            }else{
                holder.status.setText(((List) data.get("submitted")).contains(GlobalVariables.uid) ? "Completed" : "Test Closed");
                assignments.get(position).put("isDead", true);
            }

            holder.itemView.findViewById(R.id.solution).setVisibility(View.GONE);
            holder.itemView.findViewById(R.id.edit).setVisibility(View.GONE);
            holder.itemView.findViewById(R.id.viewSubmission).setVisibility(View.GONE);

            if (((List) data.get("submitted")).contains(GlobalVariables.uid)) {
                submitted++;
            }
            if (position == data.size() - 1) {
                ((AssignmentFragment) fragment).submittedText.setText(submitted + "/" + data.size() + "Submitted");
                ((AssignmentFragment) fragment).pendingText.setText((submitted - data.size()) + " Pending");
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((AssignmentFragment) fragment).open(position, (boolean) assignments.get(position).get("isDead"));
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return assignments.size();
    }

    class vh extends RecyclerView.ViewHolder {
        TextView title, date, time, status, maxNo, submittedStudents;

        public vh(@NonNull View itemView) {
            super(itemView);
            this.submittedStudents = itemView.findViewById(R.id.submittedStudents);
            this.date = itemView.findViewById(R.id.date);
            this.time = itemView.findViewById(R.id.time);
            this.status = itemView.findViewById(R.id.status);
            this.maxNo = itemView.findViewById(R.id.maxMarks);
            this.title = itemView.findViewById(R.id.title);

        }
    }
}
