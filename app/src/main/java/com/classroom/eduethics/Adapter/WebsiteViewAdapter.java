package com.classroom.eduethics.Adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.classroom.eduethics.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WebsiteViewAdapter extends RecyclerView.Adapter<WebsiteViewAdapter.vh> {

    Fragment fragment;
    List<String> names,numbers;

    public WebsiteViewAdapter(Fragment fragment, List<String> names, List<String> numbers) {
        this.fragment = fragment;
        this.names = names;
        this.numbers = numbers;
    }

    @NonNull
    @NotNull
    @Override
    public vh onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new vh(LayoutInflater.from(parent.getContext()).inflate(R.layout.single_enquiry,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull WebsiteViewAdapter.vh holder, int position) {
        holder.name.setText(names.get(position));
        holder.num.setText(numbers.get(position));
        holder.whatsapp.setOnClickListener(v -> fragment.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=" + numbers.get(position)))));
        holder.call.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:"+numbers.get(position)));//change the number
            fragment.startActivity(callIntent);
        });
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    class vh extends RecyclerView.ViewHolder {
        TextView name,num;
        View whatsapp,call;

        public vh(@NonNull @NotNull View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.name);
            this.num = itemView.findViewById(R.id.number);
            this.whatsapp = itemView.findViewById(R.id.whatsappClick);
            this.call = itemView.findViewById(R.id.callClick);
        }
    }

}
