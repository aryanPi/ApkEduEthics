package com.classroom.eduethics.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.classroom.eduethics.Fragments.SubFragments.AddSolution;
import com.classroom.eduethics.Fragments.SubFragments.EvaluateFragment;
import com.classroom.eduethics.Fragments.SubFragments.StartSubmission;
import com.classroom.eduethics.Fragments.TeacherFragments.CreateAssignment;
import com.classroom.eduethics.Fragments.TeacherFragments.CreateTest;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.ExtraFunctions;

import java.util.List;

import static com.classroom.eduethics.Utils.LocalConstants.FROM.FROM_ADD_SOLUTION;
import static com.classroom.eduethics.Utils.LocalConstants.FROM.FROM_ASSIGNMENT_FRAG;
import static com.classroom.eduethics.Utils.LocalConstants.FROM.FROM_CREATE_ASSIGNMENT;
import static com.classroom.eduethics.Utils.LocalConstants.FROM.FROM_CREATE_TEST;
import static com.classroom.eduethics.Utils.LocalConstants.FROM.FROM_EVALUATE_FRAG;
import static com.classroom.eduethics.Utils.LocalConstants.FROM.FROM_SUMBIT_TEST;
import static com.classroom.eduethics.Utils.LocalConstants.FROM.FROM_SUMBIT_TEST_ATT;
import static com.classroom.eduethics.Utils.LocalConstants.FROM.FROM_TEST_FRAG;

public class uploadImagesAdapter extends RecyclerView.Adapter<uploadImagesAdapter.vh> {

    List<String> data;
    Fragment fragment;
    public int from;
    View progress;


    public void setProgress(View progress){
        this.progress = progress;
    }

    public uploadImagesAdapter(List<String> data, Fragment fragment, int from,View progress) {
        this.fragment = fragment;
        this.data = data;
        this.from = from;
        this.progress = progress;
    }

    @NonNull
    @Override
    public vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new vh(LayoutInflater.from(parent.getContext()).inflate(R.layout.upload_images_single_item, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull vh holder, int position) {

        /*if (((CreateTest) fragment).uploadRecyclerView.getBackground() == null)
            ((CreateTest) fragment).uploadRecyclerView.setBackground(ResourcesCompat.getDrawable(fragment.getResources(),R.drawable.background_popup_custom,null));*/

        holder.itemView.setOnClickListener(v -> {
                ExtraFunctions.downloadImageAndShow(progress,data.get(position),fragment.getActivity());
        });

        if (from == FROM_ASSIGNMENT_FRAG || from == FROM_TEST_FRAG || from == FROM_SUMBIT_TEST_ATT) {
            Glide.with(fragment.getContext()).load(R.drawable.ic_download).into(holder.deleteImage);
        } else {
            holder.deleteImage.setOnClickListener(v -> {
                if (from == FROM_CREATE_ASSIGNMENT) {
                    ((CreateAssignment) fragment).deleteImage(position);
                } else if (from == FROM_CREATE_TEST) {
                    ((CreateTest) fragment).deleteImage(position);
                } else if (from == FROM_SUMBIT_TEST) {
                    ((StartSubmission) fragment).deleteImage(position);
                } else if (from == FROM_ADD_SOLUTION) {
                    ((AddSolution) fragment).deleteImage(position);
                }
            });

        }


        ((TextView) holder.itemView.findViewById(R.id.nameImage)).setText(data.get(position).startsWith("314") ? data.get(position).substring(3) : data.get(position));
        if (data.get(position).endsWith("pdf")) {
            Glide.with(fragment.getContext()).load(R.drawable.ic_pdf).centerCrop().fitCenter().into((ImageView) holder.itemView.findViewById(R.id.imageType));
        } else if (data.get(position).endsWith("jpg")) {
            Glide.with(fragment.getContext()).load(R.drawable.ic_image).centerCrop().fitCenter().into((ImageView) holder.itemView.findViewById(R.id.imageType));
        } else {
            Glide.with(fragment.getContext()).load(R.drawable.ic_attachment_pin).centerCrop().fitCenter().into((ImageView) holder.itemView.findViewById(R.id.imageType));
        }

    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    class vh extends RecyclerView.ViewHolder {
        ImageView deleteImage;

        public vh(@NonNull View itemView) {
            super(itemView);
            this.deleteImage = itemView.findViewById(R.id.deleteBtnImage);
        }
    }
}
