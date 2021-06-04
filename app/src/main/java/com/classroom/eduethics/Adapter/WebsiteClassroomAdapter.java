package com.classroom.eduethics.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.classroom.eduethics.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class WebsiteClassroomAdapter extends RecyclerView.Adapter<WebsiteClassroomAdapter.vh> {

    List<Map<String, Object>> data;
    List<String> selected, selectedSub;

    public WebsiteClassroomAdapter(List<Map<String, Object>> data, List<String> selected, List<String> selectedSub) {
        this.data = data;
        this.selected = selected;
        this.selectedSub = selectedSub;
    }


    @NonNull
    @NotNull
    @Override
    public vh onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new vh(LayoutInflater.from(parent.getContext()).inflate(R.layout.website_classroom_single_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull WebsiteClassroomAdapter.vh holder, int position) {

        holder.subjectName.setText(data.get(position).get("subject").toString());
        holder.className.setText(data.get(position).get("name").toString());


        holder.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selected.contains(data.get(position).get("name").toString())) {
                    selected.remove(data.get(position).get("name").toString());
                    selectedSub.remove(data.get(position).get("subject").toString());
                    holder.addBtn.setText("Add");
                } else {
                    selected.add(data.get(position).get("name").toString());
                    selectedSub.add(data.get(position).get("subject").toString());
                    holder.addBtn.setText("Remove");
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class vh extends RecyclerView.ViewHolder {
        TextView className, subjectName, addBtn;

        public vh(@NonNull @NotNull View itemView) {
            super(itemView);
            this.className = itemView.findViewById(R.id.classNameWebsite);
            this.subjectName = itemView.findViewById(R.id.subjectNameWebsite);
            this.addBtn = itemView.findViewById(R.id.addWebsiteClassroom);
        }
    }
}
