package com.classroom.eduethics.Fragments.HomeFragemtns;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.classroom.eduethics.Activity.ImagePage;
import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.Adapter.ChatAdapter;
import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.Models.ChatModel;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.GlobalVariables;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class ChatFragment extends Fragment {

    RecyclerView chatRecyclerView;
    List<ChatModel> data = new ArrayList<>();
    ChatAdapter adapter;
    EditText messageEditText;

    TextView nameImage;
    Bitmap bitmapAtachment;
    Uri uriAttachment;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl(LocalConstants.STORAGE_REF);    //change the url according to your firebase app

    View progress;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("chats/" + HomeClassroom.classId);
    View root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_chat, container, false);




        progress = root.findViewById(R.id.progress);
        nameImage = root.findViewById(R.id.nameImageChatFrag);
        root.findViewById(R.id.deleteBtnImageChatFrag).setOnClickListener(v -> {
            nameImage.setText("");
            root.findViewById(R.id.uploadItemChatFrag).setVisibility(View.GONE);
        });

        root.findViewById(R.id.attachFile).setOnClickListener(v -> upload());

        MainActivity.TO_WHICH_FRAG = LocalConstants.TO_FRAG.TO_CLASSROOM;


        messageEditText = root.findViewById(R.id.messageEditText);

        root.findViewById(R.id.send).setOnClickListener(v -> send());

        adapter = new ChatAdapter(this, data, GlobalVariables.uid);
        chatRecyclerView = root.findViewById(R.id.chatRecyclerView);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(adapter);


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                if (map != null)
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        if (getActivity() == null)
                            return;

                        ChatModel chatModel = new ChatModel();
                        chatModel.setName(((Map) entry.getValue()).get("name").toString());
                        chatModel.setMessage(((Map) entry.getValue()).get("message").toString());
                        chatModel.setId(((Map) entry.getValue()).get("id").toString());
                        chatModel.setAttachment(((Map) entry.getValue()).get("attachment") != null ? ((Map) entry.getValue()).get("attachment").toString() : "-");
                        chatModel.setTime(entry.getKey());


                        boolean have = false;
                        for (ChatModel datum : data)
                            if (datum.getTime().equals(chatModel.getTime())) {
                                have = true;
                                break;
                            }

                        if (!have) {
                            data.add(chatModel);
                            Collections.sort(data);
                            adapter.notifyDataSetChanged();
                        }


                    }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return root;
    }

    Dialog dialog;

    private void upload() {
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.upload_dialog);
        dialog.findViewById(R.id.camera).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED)
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, LocalConstants.REQ_CODE.CAMERA_REQUEST);
            else {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, LocalConstants.PERMISSION_CODE.CAMERA_REQUEST);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.pdf).setOnClickListener(v -> {
            Intent chooseFile;
            Intent intent;
            chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("application/pdf");
            intent = Intent.createChooser(chooseFile, "Choose a file");
            startActivityForResult(intent, LocalConstants.PERMISSION_CODE.PICKFILE_RESULT_CODE);
            dialog.dismiss();
        });

        dialog.findViewById(R.id.image).setOnClickListener(v -> {
            Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(i, "Select Picture"), LocalConstants.PERMISSION_CODE.SELECT_PICTURE);
            dialog.dismiss();
        });

        dialog.findViewById(R.id.upload).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void send() {
        if (getActivity() == null)
            return;


        if (!messageEditText.getText().toString().equals("") || root.findViewById(R.id.uploadItemChatFrag).getVisibility() == View.VISIBLE) {
            progress.setVisibility(View.VISIBLE);
            Map<String, Object> map = new HashMap<>();
            map.put("message", messageEditText.getText().toString());
            map.put("name", GlobalVariables.name);
            map.put("id", GlobalVariables.uid);

            if (root.findViewById(R.id.uploadItemChatFrag).getVisibility() == View.VISIBLE)
                uploadToStorage(nameImage.getText().toString(), map);
            else forward("-", map);


        }
    }

    private void uploadToStorage(String s, Map<String, Object> map) {

        if (s.startsWith("314")) {
            uriAttachment = null;
        } else {
            bitmapAtachment = null;
        }


        if (uriAttachment == null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmapAtachment.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            final String randomKey = UUID.randomUUID().toString();

            UploadTask uploadTask = storageRef.child("chatData/" + HomeClassroom.classId + "/" + randomKey + ".jpg").putBytes(data);
            uploadTask.addOnFailureListener(exception -> Toast.makeText(getContext(), "failed", Toast.LENGTH_SHORT).show()).addOnSuccessListener(taskSnapshot -> {
                forward(taskSnapshot.getMetadata().getPath(), map);
            });
        } else {
            final String randomKey = UUID.randomUUID().toString();
            String extension;
            if (uriAttachment.getPath().contains(".")) {
                extension = uriAttachment.getPath().substring(uriAttachment.getPath().lastIndexOf("."));
            } else {
                extension = ".pdf";
            }

            StorageReference riversRef = storageRef.child(("chatData/" + HomeClassroom.classId + "/" + randomKey + extension));


            riversRef.putFile(uriAttachment)
                    .addOnSuccessListener(taskSnapshot -> {
                        forward(taskSnapshot.getMetadata().getPath(), map);
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Unable to post your Image !", Toast.LENGTH_SHORT).show());

        }
    }

    private void forward(String path, Map<String, Object> map) {

        map.put("attachment", path);

        messageEditText.setText("");
        Map<String, Object> mapF = new HashMap<>();

        String time = Calendar.getInstance().getTimeInMillis()+"";

        mapF.put(time, map);

        myRef.updateChildren(mapF).addOnSuccessListener(aVoid -> {
            if (getActivity() == null)
                return;
            root.findViewById(R.id.uploadItemChatFrag).setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (getActivity() == null)
            return;

        switch (requestCode) {
            case LocalConstants.PERMISSION_CODE.PICKFILE_RESULT_CODE:
                if (data != null)
                    if (data.getData() != null) {

                        uriAttachment = data.getData();
                        nameImage.setText(data.getData().getPath());
                        Glide.with(getContext()).load(R.drawable.ic_pdf).into((ImageView) root.findViewById(R.id.imageTypeChatFrag));
                        root.findViewById(R.id.uploadItemChatFrag).setVisibility(View.VISIBLE);
                    }
                break;
            case LocalConstants.PERMISSION_CODE.SELECT_PICTURE:
                if (data != null) {
                    Uri selectedImageUri = data.getData();
                    if (null != selectedImageUri) {
                        uriAttachment = selectedImageUri;
                        root.findViewById(R.id.uploadItemChatFrag).setVisibility(View.VISIBLE);
                        nameImage.setText(selectedImageUri.getPath());
                        Glide.with(getContext()).load(R.drawable.ic_image).into((ImageView) root.findViewById(R.id.imageTypeChatFrag));
                    }

                }
                break;
            case LocalConstants.PERMISSION_CODE.CAMERA_REQUEST:
                if (data != null)
                    if (data.getExtras().get("data") != null) {
                        bitmapAtachment = (Bitmap) data.getExtras().get("data");
                        nameImage.setText("314" + UUID.randomUUID().toString());
                        Glide.with(getContext()).load(R.drawable.ic_camera).into((ImageView) root.findViewById(R.id.imageTypeChatFrag));
                        root.findViewById(R.id.uploadItemChatFrag).setVisibility(View.VISIBLE);
                    }
                break;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocalConstants.REQ_CODE.CAMERA_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, LocalConstants.PERMISSION_CODE.CAMERA_REQUEST);
            } else {
                Toast.makeText(getActivity(), "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void downloadAndShow(int position) {
        root.findViewById(R.id.progress).setVisibility(View.VISIBLE);
        FirebaseStorage.getInstance().getReference().child(data.get(position).getAttachment())
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    if (getActivity() == null)
                        return;
                    root.findViewById(R.id.progress).setVisibility(View.GONE);
                    if (data.get(position).getAttachment().endsWith("pdf")) {
                        Activity activity = getActivity();
                        Intent intent = new Intent(activity, ImagePage.class);
                        intent.putExtra("urlImage", uri).putExtra("type", "pdf");
                        startActivity(intent);
                    } else if (data.get(position).getAttachment().endsWith("png") || data.get(position).getAttachment().endsWith("jpg") || data.get(position).getAttachment().endsWith("jpeg")) {
                        Activity activity = getActivity();
                        Intent intent = new Intent(activity, ImagePage.class);
                        intent.putExtra("urlImage", uri).putExtra("type", "image");
                        startActivity(intent);
                    }
                }).addOnFailureListener(e -> {
            if (getActivity() == null)
                return;

            root.findViewById(R.id.progress).setVisibility(View.GONE);
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}