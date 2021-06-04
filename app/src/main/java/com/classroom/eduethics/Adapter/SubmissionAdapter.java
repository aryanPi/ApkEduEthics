package com.classroom.eduethics.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;


import com.classroom.eduethics.Fragments.SubFragments.ViewSubmissionTest;
import com.classroom.eduethics.R;

import java.util.List;
import java.util.Map;

public class SubmissionAdapter extends RecyclerView.Adapter<SubmissionAdapter.vh> {

    List<Map<String, Object>> data;
Fragment fragment;
    public SubmissionAdapter(Fragment fragment, List<Map<String, Object>> data) {
        this.fragment = fragment;
        this.data = data;
    }

    @NonNull
    @Override
    public vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new vh(LayoutInflater.from(parent.getContext()).inflate(R.layout.single_item_submission,parent,false));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onBindViewHolder(@NonNull vh holder, int position) {
        ((TextView)holder.itemView.findViewById(R.id.name)).setText(data.get(position).get("name").toString());
        if (data.get(position).get("feedback").toString().equals("-")){
            ((TextView)holder.itemView.findViewById(R.id.feedbackIndication)).setText("Feedback not given");
        }else{
            ((TextView)holder.itemView.findViewById(R.id.feedbackIndication)).setText("Feedback Submitted");
        }

        if (!data.get(position).get("score").toString().startsWith("-"))
            ((TextView)holder.itemView.findViewById(R.id.score)).setText("Score : "+data.get(position).get("score").toString());
        else{
            holder.itemView.findViewById(R.id.score).setVisibility(View.GONE);
        }

        holder.itemView.findViewById(R.id.evaluateBtn).setOnClickListener(v -> ((ViewSubmissionTest)fragment).evaluate(position));



    }

    class vh extends RecyclerView.ViewHolder {
        public vh(@NonNull View itemView) {
            super(itemView);
        }
    }

}
