package com.classroom.eduethics.Adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.Fragments.HomeFragemtns.StudentFragment;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.GlobalVariables;
import com.classroom.eduethics.Utils.LocalConstants;

import java.util.List;
import java.util.Map;

public class StudentsAdapter extends RecyclerView.Adapter<StudentsAdapter.vh> {

    Fragment fragment;
    List<Map<String, Object>> data;
    int of;

    public StudentsAdapter(Fragment fragment, List<Map<String, Object>> data, int of) {
        this.fragment = fragment;
        this.data = data;
        this.of = of;
    }

    @NonNull
    @Override
    public vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new vh(LayoutInflater.from(parent.getContext()).inflate(R.layout.single_student_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull vh holder, int position) {
        Map<String, Object> single = data.get(position);
        holder.name.setText(single.get("name").toString());
        if (of==1 && !GlobalVariables.isStudent){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((StudentFragment)fragment).openFee(position);
                }
            });
        }

        if (single.get("image").toString().equals("-")) {
            holder.initialName.setText(single.get("name").toString().substring(0, 1));
        } else {
            Glide.with(fragment.getContext()).load(Uri.parse(single.get("image").toString())).centerCrop().circleCrop().into(holder.image);
            holder.initialName.setVisibility(View.GONE);
        }

        if (HomeClassroom.from== LocalConstants.TYPE.TEACHER){
            holder.number.setText(single.get("number").toString());
            if (of == 1) {
                holder.call.setOnClickListener(v -> fragment.startActivity(new Intent(Intent.ACTION_CALL).setData(Uri.parse("tel:" + single.get("number").toString()))));
                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((StudentFragment) fragment).deleteStudent(position);
                    }
                });
            } else {
                holder.call.setImageDrawable(ResourcesCompat.getDrawable(fragment.getResources(), R.drawable.ic_check, null));
                holder.call.setOnClickListener(v -> ((StudentFragment) fragment).action(position, 1));
                holder.delete.setOnClickListener(v -> ((StudentFragment) fragment).action(position, -1));
            }

        }else{
            holder.delete.setVisibility(View.INVISIBLE);
            holder.call.setVisibility(View.INVISIBLE);
            holder.number.setVisibility(View.GONE);
        }


    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class vh extends RecyclerView.ViewHolder {

        ImageView image, delete, call;
        TextView number, name, initialName;

        public vh(@NonNull View itemView) {
            super(itemView);
            this.image = itemView.findViewById(R.id.image);
            this.number = itemView.findViewById(R.id.number);
            this.call = itemView.findViewById(R.id.call);
            this.delete = itemView.findViewById(R.id.delete);
            this.name = itemView.findViewById(R.id.name);
            this.initialName = itemView.findViewById(R.id.nameInitial);
        }
    }

}
