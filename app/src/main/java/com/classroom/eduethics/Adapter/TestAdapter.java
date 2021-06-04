package com.classroom.eduethics.Adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;


import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.Fragments.HomeFragemtns.TestFragment;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.ExtraFunctions;
import com.classroom.eduethics.Utils.GlobalVariables;
import com.classroom.eduethics.Utils.LocalConstants;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class TestAdapter extends RecyclerView.Adapter<TestAdapter.vh> {

    List<Map<String, Object>> tests;
    Fragment fragment;
    int submitted = 0;

    public TestAdapter(Fragment fragment) {
        this.fragment = fragment;
        this.tests = HomeClassroom.model.getTest();
    }


    @NonNull
    @Override
    public vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new vh(LayoutInflater.from(fragment.getContext()).inflate(R.layout.test_single_item, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull vh holder, int position) {


        if (HomeClassroom.from == LocalConstants.TYPE.STUDENT) {
            holder.itemView.findViewById(R.id.teacherSection).setVisibility(View.GONE);
        }

        Map<String, Object> data = tests.get(position);
        ((TextView) holder.itemView.findViewById(R.id.title)).setText(data.get("topic").toString());
        ((TextView) holder.itemView.findViewById(R.id.time)).setText(data.get("date").toString() + "\n(" + ExtraFunctions.getReadableTime(Integer.parseInt(data.get("time").toString().split(":")[0]), Integer.parseInt(data.get("time").toString().split(":")[1])) + ")");
        ((TextView) holder.itemView.findViewById(R.id.maxMarks)).setText("Max Marks : " + data.get("maxMarks").toString());


        holder.itemView.findViewById(R.id.solution).setOnClickListener(v -> ((TestFragment) fragment).solution(position));

        if (HomeClassroom.from == LocalConstants.TYPE.TEACHER) {

            holder.submittedStudents.setText(((List) data.get("submitted")).size() + "/" + HomeClassroom.model.getCurrentStudents().size() + " Submitted");
            holder.status.setVisibility(View.GONE);
            holder.itemView.findViewById(R.id.edit).setOnClickListener(v -> ((TestFragment) fragment).edit(position));
            holder.itemView.findViewById(R.id.viewSubmission).setOnClickListener(v -> ((TestFragment) fragment).view(position));

        } else {

            Calendar calendar = Calendar.getInstance();

            if (calendar.get(Calendar.DAY_OF_MONTH) < Integer.parseInt(((String) data.get("date")).split("-")[0]) && calendar.get(Calendar.MONTH) < Integer.parseInt(((String) data.get("date")).split("-")[1]) && calendar.get(Calendar.YEAR) < Integer.parseInt(((String) data.get("date")).split("-")[2])) {
                holder.status.setText(((List) data.get("submitted")).contains(GlobalVariables.uid) ? "Completed" : "Not Started");
                tests.get(position).put("isDead",false);
                Toast.makeText(fragment.getContext(), "ok 3", Toast.LENGTH_SHORT).show();
            } else if (!((calendar.get(Calendar.HOUR_OF_DAY) * 60) + calendar.get(Calendar.MINUTE) < ((Integer.parseInt(((String) data.get("time")).split(":")[0]) * 60) + Integer.parseInt(((String) data.get("time")).split(":")[1]) + Integer.parseInt(((String) data.get("testDuration")))))) {
                holder.status.setText(((List) data.get("submitted")).contains(GlobalVariables.uid) ? "Completed" : "Not Started");
                tests.get(position).put("isDead",false);
            } else {
                holder.status.setText(((List) data.get("submitted")).contains(GlobalVariables.uid) ? "Completed" : "Test Closed");
                tests.get(position).put("isDead",true);
            }


            if (((List) data.get("submitted")).contains(GlobalVariables.uid)) {
                submitted++;
            }
            if (position == tests.size() - 1) {
                ((TestFragment) fragment).submittedText.setText(submitted + "/" + tests.size() + " Submitted");
                ((TestFragment) fragment).pendingText.setText((tests.size() - submitted) + " Pending");
            }
            holder.itemView.setOnClickListener(v -> ((TestFragment) fragment).open(position,(boolean)tests.get(position).get("isDead")));
        }
        ((TextView) holder.itemView.findViewById(R.id.duration)).setText("Duration : " + data.get("testDuration").toString()+" min(s)");

    }

    @Override
    public int getItemCount() {
        return tests.size();
    }

    class vh extends RecyclerView.ViewHolder {
        TextView status, submittedStudents;

        public vh(@NonNull View itemView) {
            super(itemView);
            submittedStudents = itemView.findViewById(R.id.submittedStudents);
            this.status = itemView.findViewById(R.id.status);
        }
    }
}
