package com.classroom.eduethics.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.classroom.eduethics.Fragments.ClassroomStudentFragment;
import com.classroom.eduethics.Fragments.ClassroomTeacherFragment;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.ExtraFunctions;
import com.classroom.eduethics.Utils.GlobalVariables;

import java.util.List;
import java.util.Map;

public class ScheduleAdapterTeacher extends RecyclerView.Adapter<ScheduleAdapterTeacher.vh> {

    List<Map<String, String>> data;
    Fragment fragment;

    public ScheduleAdapterTeacher(List<Map<String, String>> data, Fragment fragment) {
        this.data = data;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new vh(LayoutInflater.from(parent.getContext()).inflate(R.layout.single_schecule_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull vh holder, int position) {
        ((TextView) holder.itemView.findViewById(R.id.classSingleItem)).setText(data.get(position).get("class"));
        ((TextView) holder.itemView.findViewById(R.id.time)).setText(ExtraFunctions.getReadableTime(Integer.parseInt(data.get(position).get("time").split(":")[0]), Integer.parseInt(data.get(position).get("time").split(":")[1])));
        ((TextView) holder.itemView.findViewById(R.id.subjectSingleItem)).setText(data.get(position).get("subject"));
        ((TextView) holder.itemView.findViewById(R.id.classSingleItem)).setText(data.get(position).get("class"));
        if (GlobalVariables.isStudent)
            holder.joinBtn.setText("Join class");
        holder.joinBtn.setOnClickListener(v -> {
            if (GlobalVariables.isStudent) {
                ((ClassroomStudentFragment) fragment).joinClass(position);
            } else {
                ((ClassroomTeacherFragment) fragment).joinClass(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class vh extends RecyclerView.ViewHolder {
        TextView joinBtn;

        public vh(@NonNull View itemView) {
            super(itemView);
            this.joinBtn = itemView.findViewById(R.id.goLiveNow);
        }
    }
}
