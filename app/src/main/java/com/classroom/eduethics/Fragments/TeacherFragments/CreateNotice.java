package com.classroom.eduethics.Fragments.TeacherFragments;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.ExtraFunctions;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CreateNotice extends Fragment {

    boolean isAttached = false;
    Bitmap bitmap;
    Uri selectedDataPath;

    TextInputEditText description;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl(LocalConstants.STORAGE_REF);    //change the url according to your firebase app
    View root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_create_notice, container, false);
        MainActivity.TO_WHICH_FRAG = LocalConstants.TO_FRAG.TO_HOME_CLASSROOM;
        description = root.findViewById(R.id.description);
        pd = root.findViewById(R.id.progress);

        root.findViewById(R.id.addAttachment).setOnClickListener(v -> upload());

        root.findViewById(R.id.saveCreateNotice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (description.getText().toString().equals("")) {
                    description.setError("Required !");
                } else {
                    pd.setVisibility(View.VISIBLE);
                    if (isAttached) {
                        uploadToFirebase();
                    } else {
                        uploadToClassroom(null);
                    }
                }
            }
        });

        return root;
    }

    private void upload() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.upload_dialog);
        dialog.findViewById(R.id.camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_DENIED)
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, LocalConstants.REQ_CODE.CAMERA_REQUEST);
                else {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, LocalConstants.PERMISSION_CODE.CAMERA_REQUEST);
                }
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.pdf).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseFile;
                Intent intent;
                chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("application/pdf");
                intent = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(intent, LocalConstants.PERMISSION_CODE.PICKFILE_RESULT_CODE);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i, "Select Picture"), LocalConstants.PERMISSION_CODE.SELECT_PICTURE);
                dialog.dismiss();
            }
        });

        ((TextView) dialog.findViewById(R.id.upload)).setText("Select");
        dialog.findViewById(R.id.upload).setOnClickListener(v -> dialog.dismiss());

        dialog.findViewById(R.id.cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    View pd;

    private void uploadToFirebase() {

        if (selectedDataPath == null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            final String randomKey = UUID.randomUUID().toString();

            UploadTask uploadTask = storageRef.child("testData/" + HomeClassroom.classId + "/" + randomKey + ".jpg").putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    if (getActivity() == null) return;
                    Toast.makeText(getContext(), "failed", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    uploadToClassroom(taskSnapshot);
                }
            });
        } else {
            final String randomKey = UUID.randomUUID().toString();
            String extension = "";

            if (selectedDataPath.getPath().contains(".")) {
                extension = selectedDataPath.getPath().substring(selectedDataPath.getPath().lastIndexOf("."));
            } else {
                extension = ".jpg";
            }


            StorageReference riversRef = storageRef.child(("noticeData/" + HomeClassroom.classId + "/" + randomKey + extension));

            riversRef.putFile(selectedDataPath)
                    .addOnSuccessListener(taskSnapshot -> {
                        uploadToClassroom(taskSnapshot);
                    }).addOnFailureListener(e -> {
                if (getActivity() == null) return;
                Toast.makeText(getContext(), "Unable to post your Image !", Toast.LENGTH_SHORT).show();
            });

        }
    }

    private void uploadToClassroom(UploadTask.TaskSnapshot taskSnapshot) {
        Map<String, Object> map = new HashMap<>();
        map.put("notice", description.getText().toString());
        map.put("date", Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "/" + Calendar.getInstance().get(Calendar.MONTH) + "/" + Calendar.getInstance().get(Calendar.YEAR));
        map.put("time", Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":" + Calendar.getInstance().get(Calendar.MINUTE));

        if (taskSnapshot != null) {
            String downloadUrl = taskSnapshot.getMetadata().getPath();
            map.put("attachment", downloadUrl);
        } else {
            map.put("attachment", "-");
        }


        FirebaseFirestore.getInstance()
                .collection("classroom")
                .document(HomeClassroom.classId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Map<String, Object> d = documentSnapshot.getData();
                        ((List) d.get("notice")).add(map);
                        FirebaseFirestore.getInstance()
                                .collection("classroom")
                                .document(HomeClassroom.classId)
                                .set(d)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        if (getActivity() == null) return;
                                        Toast.makeText(getContext(), "Uploaded Notice", Toast.LENGTH_SHORT).show();


                                        List<String> ids = new ArrayList<>();
                                        for (Map<String, Object> currentStudent : HomeClassroom.model.getCurrentStudents())
                                            ids.add(currentStudent.get("id").toString());
                                        ExtraFunctions.sendNoti(ids,"Notice Information",description.getText().toString(),getContext());

                                        pd.setVisibility(View.GONE);
                                        getActivity().onBackPressed();
                                    }
                                })
                                .addOnFailureListener(e -> pd.setVisibility(View.GONE));
                    }
                }).addOnFailureListener(e -> pd.setVisibility(View.GONE));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;
        switch (requestCode) {
            case LocalConstants.PERMISSION_CODE.PICKFILE_RESULT_CODE:
                selectedDataPath = data.getData();
                isAttached = true;
                ((TextView) root.findViewById(R.id.attachedFileName)).setText("*File Selected");
                break;
            case LocalConstants.PERMISSION_CODE.SELECT_PICTURE:
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    selectedDataPath = selectedImageUri;
                    isAttached = true;
                    ((TextView) root.findViewById(R.id.attachedFileName)).setText("*Image Selected");
                }
                break;
            case LocalConstants.PERMISSION_CODE.CAMERA_REQUEST:
                bitmap = (Bitmap) data.getExtras().get("data");
                isAttached = true;
                ((TextView) root.findViewById(R.id.attachedFileName)).setText("*Camera Image Selected");
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

}