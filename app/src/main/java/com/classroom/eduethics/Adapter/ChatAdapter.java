package com.classroom.eduethics.Adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.classroom.eduethics.Fragments.HomeFragemtns.ChatFragment;
import com.classroom.eduethics.Models.ChatModel;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.ExtraFunctions;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.vh> {

    Fragment fragment;
    List<ChatModel> data;
    String selfId;

    public ChatAdapter(Fragment fragment, List<ChatModel> data, String selfId) {
        this.fragment = fragment;
        this.data = data;
        this.selfId = selfId;
    }

    @NonNull
    @Override
    public vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new vh(LayoutInflater.from(parent.getContext()).inflate(R.layout.single_chat_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull vh holder, int position) {
        if (selfId.equals(data.get(position).getId())){
            holder.MAIN_CHAT_SINGLE.setGravity(Gravity.END);
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            layoutParams.startToEnd = R.id.guideline30P;
            holder.MAIN_CHAT_SINGLE.setLayoutParams(layoutParams);
        }


        holder.name.setText(data.get(position).getName());
        holder.message.setText(data.get(position).getMessage());
        holder.time.setText(ExtraFunctions.getTimeFromLong(data.get(position).getTime()));
        holder.time.setVisibility(View.GONE);

        if (position > 0) {
            if (data.get(position).getId().equals(data.get(position - 1).getId())) {
                holder.name.setVisibility(View.GONE);
            }
        }
        if (!data.get(position).getAttachment().equals("-")) {
            holder.uploadItem.setVisibility(View.VISIBLE);
            holder.uploadItem.setOnClickListener(v -> ((ChatFragment)fragment).downloadAndShow(position));
            ((TextView) holder.itemView.findViewById(R.id.nameImage)).setText(data.get(position).getAttachment());
            ImageView imageView = holder.itemView.findViewById(R.id.imageType);
            if (data.get(position).getAttachment().endsWith("pdf")) {
                Glide.with(fragment.getContext()).load(R.drawable.ic_pdf).into(imageView);
            } else if (data.get(position).getAttachment().endsWith("png") || data.get(position).getAttachment().endsWith("jpeg") || data.get(position).getAttachment().endsWith("jpg")) {
                Glide.with(fragment.getContext()).load(R.drawable.ic_image).into(imageView);
            } else {
                Glide.with(fragment.getContext()).load(R.drawable.ic_attachment_pin).into(imageView);
            }
        holder.itemView.findViewById(R.id.messageLinear).setVisibility(View.GONE);
        }else{
            holder.uploadItem.setVisibility(View.GONE);
        }
        if (data.get(position).getMessage().equals("")) holder.message.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(v -> holder.time.setVisibility(holder.time.getVisibility() == View.GONE ? View.VISIBLE : View.GONE));

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class vh extends RecyclerView.ViewHolder {
        TextView name, message, time;
        LinearLayout MAIN_CHAT_SINGLE;
        ConstraintLayout uploadItem;

        public vh(@NonNull View itemView) {
            super(itemView);
            this.uploadItem = itemView.findViewById(R.id.uploadItem);
            this.MAIN_CHAT_SINGLE = itemView.findViewById(R.id.MAIN_CHAT_SINGLE);
            this.name = itemView.findViewById(R.id.name);
            this.message = itemView.findViewById(R.id.message);
            this.time = itemView.findViewById(R.id.time);
        }
    }
}
