package com.classroom.eduethics.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;


import com.classroom.eduethics.Fragments.SubFragments.FeeDetailsTeacher;
import com.classroom.eduethics.R;

import java.util.List;
import java.util.Map;

public class FeeAdapter extends RecyclerView.Adapter<FeeAdapter.vh> {

    List<Map<String, Object>> data;
    Fragment fragment;
    TextView totalRec, totalDue, totalAdv;

    int tRec = 0, tAdv = 0, tDue = 0;

    public FeeAdapter(List<Map<String, Object>> data, Fragment fragment, TextView totalRec, TextView totalDue, TextView totalAdv) {
        this.data = data;
        this.fragment = fragment;
        this.totalAdv = totalAdv;
        this.totalDue = totalDue;
        this.totalRec = totalRec;
    }

    @NonNull
    @Override
    public vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new vh(LayoutInflater.from(parent.getContext()).inflate(R.layout.fee_details_single_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull vh holder, int position) {

        holder.adv.setText(fragment.getString(R.string.Rs) + (Integer.parseInt(data.get(position).get("adv").toString())<0?Integer.parseInt(data.get(position).get("adv").toString())*-1:Integer.parseInt(data.get(position).get("adv").toString())));
        holder.adv.setTextColor(ResourcesCompat.getColor(fragment.getResources(), Integer.parseInt(data.get(position).get("adv").toString()) < 0 ? android.R.color.holo_red_dark : android.R.color.holo_green_dark, null));

        holder.name.setText(data.get(position).get("name").toString());
        holder.rec.setText(fragment.getString(R.string.Rs) + data.get(position).get("rec").toString());

        holder.rec.setTextColor(ResourcesCompat.getColor(fragment.getResources(), Integer.parseInt(data.get(position).get("rec").toString()) < 0 ? android.R.color.holo_red_dark : android.R.color.holo_green_dark, null));
        holder.itemView.setOnClickListener(v -> ((FeeDetailsTeacher) fragment).click(position));

        tRec += Integer.parseInt(data.get(position).get("rec").toString());
        if (Integer.parseInt(data.get(position).get("adv").toString()) < 0) {
            tDue += Integer.parseInt(data.get(position).get("adv").toString());
        } else {
            tAdv += Integer.parseInt(data.get(position).get("adv").toString());
        }
        if (position == data.size() - 1) {
            totalRec.setText(fragment.getString(R.string.Rs) + (tRec<0?tRec*-1:tRec));
            totalDue.setText(fragment.getString(R.string.Rs) + (tDue<0?tDue*-1:tDue));
            totalAdv.setText(fragment.getString(R.string.Rs) + (tAdv<0?tAdv*-1:tAdv));

            totalAdv.setTextColor(ResourcesCompat.getColor(fragment.getResources(),tAdv<0? android.R.color.holo_red_dark: android.R.color.holo_green_light,null));
            totalRec.setTextColor(ResourcesCompat.getColor(fragment.getResources(),tRec<0? android.R.color.holo_red_dark: android.R.color.holo_green_light,null));
            totalDue.setTextColor(ResourcesCompat.getColor(fragment.getResources(),tDue<0? android.R.color.holo_red_dark: android.R.color.holo_green_light,null));


        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class vh extends RecyclerView.ViewHolder {

        TextView adv, rec, name;

        public vh(@NonNull View itemView) {
            super(itemView);
            this.adv = itemView.findViewById(R.id.adv);
            this.rec = itemView.findViewById(R.id.rec);
            this.name = itemView.findViewById(R.id.name);
        }

    }

}
