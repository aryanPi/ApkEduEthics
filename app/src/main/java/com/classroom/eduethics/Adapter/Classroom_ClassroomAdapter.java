package com.classroom.eduethics.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.classroom.eduethics.Fragments.ClassroomStudentFragment;
import com.classroom.eduethics.Models.Classroom_ClassroomModel;
import com.classroom.eduethics.R;

import java.util.ArrayList;

public class Classroom_ClassroomAdapter extends RecyclerView.Adapter<Classroom_ClassroomAdapter.vh> {

    Fragment fragment;
    ArrayList<Classroom_ClassroomModel> data;

    public Classroom_ClassroomAdapter(ClassroomStudentFragment fragment, ArrayList<Classroom_ClassroomModel> data) {
        this.fragment = fragment;
        this.data = data;
    }

    @NonNull
    @Override
    public vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new vh(LayoutInflater.from(fragment.getContext()).inflate(R.layout.single_item_classroom_classroom, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull vh holder, int position) {
        holder.subjectName.setText(data.get(position).getSubject());
        holder.className.setText(data.get(position).getaClass());
        holder.indication.setBackgroundColor(ResourcesCompat.getColor(fragment.getResources(),R.color.customPrimary, null));

        if (data.get(position).isConfirm()){
            holder.itemView.setOnClickListener(v -> ((ClassroomStudentFragment)fragment).visitClassroom(position));
            holder.isConfirm.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class vh extends RecyclerView.ViewHolder {

        View indication;
        TextView subjectName, className, time,isConfirm;

        public vh(@NonNull View itemView) {
            super(itemView);
            this.isConfirm = itemView.findViewById(R.id.isConfirm);
            this.indication = itemView.findViewById(R.id.indicationOfClassroom_singleItem);
            this.subjectName = itemView.findViewById(R.id.subject_single_item);
            this.className = itemView.findViewById(R.id.class_single_item);
            this.time = itemView.findViewById(R.id.dateTimeStarted_single_item);

        }
    }

}
