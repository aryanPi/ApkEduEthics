package com.classroom.eduethics.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.ExtraFunctions;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Map;

public class NotieAdapter extends RecyclerView.Adapter<NotieAdapter.vh> {

    List<Map<String, Object>> data;
    Context context;

    public NotieAdapter(Context context) {
        this.data = HomeClassroom.model.getNotice();
        this.context = context;

    }

    @NonNull
    @Override
    public vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new vh(LayoutInflater.from(context).inflate(R.layout.notice_single_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull vh holder, int position) {

        holder.timeTV.setText(data.get(position).get("date").toString() + " (" + ExtraFunctions.getReadableTime(Integer.parseInt(data.get(position).get("time").toString().split(":")[0]), Integer.parseInt(data.get(position).get("time").toString().split(":")[1]))+")");
        holder.noticeTV.setText(data.get(position).get("notice").toString());

        if (data.get(position).get("attachment").toString().equals("-")) {
            holder.noticeImage.setVisibility(View.GONE);
        } else {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl(LocalConstants.STORAGE_REF);    //change the url according to your firebase app
            StorageReference pathReference = storageRef.child(data.get(position).get("attachment").toString());

            pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // Got the download URL for 'users/me/profile.png'
                    Glide.with(context).load(uri).centerCrop().into(holder.noticeImage);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(context, "Error While Getting Image", Toast.LENGTH_SHORT).show();
                }
            });
        }

        //      holder.noticeTV.setText(notice.get(position));
//        holder.timeTV.setText(time.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class vh extends RecyclerView.ViewHolder {
        TextView timeTV, noticeTV;
        ImageView noticeImage;

        public vh(@NonNull View itemView) {
            super(itemView);
            this.noticeImage = itemView.findViewById(R.id.imageNotice);
            this.timeTV = itemView.findViewById(R.id.timeNoticeText);
            this.noticeTV = itemView.findViewById(R.id.noticeText);
        }

    }

}
