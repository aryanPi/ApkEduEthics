package com.classroom.eduethics.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;


import com.classroom.eduethics.Fragments.ClassroomTeacherFragment;
import com.classroom.eduethics.R;

import java.util.List;
import java.util.Map;

public class ClassroomTeachersAdapter extends RecyclerView.Adapter<ClassroomTeachersAdapter.vh> {

    List<Map<String, Object>> classrooms;
    Fragment fragment;

    public ClassroomTeachersAdapter(Fragment fragment, List<Map<String, Object>> classrooms) {
        this.classrooms = classrooms;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new vh(LayoutInflater.from(fragment.getContext()).inflate(R.layout.classroom_teacher_single_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull vh holder, int position) {
        Map<String, Object> clasroom = classrooms.get(position);
        holder.subject.setText(clasroom.get("subject").toString());
        holder.classTV.setText(clasroom.get("name").toString());
        StringBuilder d = new StringBuilder();
        holder.totalStudents.setText(classrooms.get(position).get("totalStudents").toString()+" Student(s)");

        holder.itemView.setOnClickListener(v -> ((ClassroomTeacherFragment)fragment).click(position));
    }

    @Override
    public int getItemCount() {
        return classrooms.size();
    }

    static class vh extends RecyclerView.ViewHolder {
        TextView subject, classTV, totalStudents;

        public vh(@NonNull View itemView) {
            super(itemView);
            this.classTV = itemView.findViewById(R.id.classSingleItem);
            this.subject = itemView.findViewById(R.id.subjectSingleItem);
            this.totalStudents = itemView.findViewById(R.id.totalStudents);
        }
    }
}



