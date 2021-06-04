package com.classroom.eduethics.Fragments.SubFragments;

import android.Manifest;
import android.app.Activity;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.classroom.eduethics.Activity.ImagePage;
import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.Adapter.uploadImagesAdapter;
import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddSolution extends Fragment {

    String storageName;

    public AddSolution(String documentName, Map<String, Object> data, boolean isEdit) {
        this.data = data;
        this.documentName = documentName;
        this.isEdit = isEdit;
        if (documentName.equals("test")) {
            storageName = "testData";
        } else {
            storageName = "assignmentData";
        }
    }

    ArrayList<String> attachments = new ArrayList<>();
    ArrayList<Object> attachmentsData = new ArrayList<>();
    ArrayList<String> attachmentsOnEdit = new ArrayList<>();
    ArrayList<String> forDelete = new ArrayList<>();
    ArrayList<String> forAdd = new ArrayList<>();

    uploadImagesAdapter imagesadapter = new uploadImagesAdapter(attachments, this, LocalConstants.FROM.FROM_ADD_SOLUTION,null);

    public RecyclerView uploadRecyclerView;

    List<String> urls = new ArrayList<>();


    String documentName;
    Map<String, Object> data;

    boolean isEdit;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl(LocalConstants.STORAGE_REF);    //change the url according to your firebase app
    private int uploadImageCounter = 0;

    View root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_add_solution, container, false);
        imagesadapter.setProgress(root.findViewById(R.id.progress));
        pd = new ProgressDialog(getContext());
        if (isEdit) {
            attachmentsOnEdit.addAll((List) data.get("solution"));
            urls.addAll(attachmentsOnEdit);
            for (int i = 0; i < attachmentsOnEdit.size(); i++) {
                attachmentsData.add("-");
            }
            attachments.addAll(attachmentsOnEdit);
            imagesadapter.notifyDataSetChanged();
        }

        uploadRecyclerView = root.findViewById(R.id.recyclerviewAttachments);
        uploadRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        uploadRecyclerView.setAdapter(imagesadapter);
        MainActivity.TO_WHICH_FRAG = LocalConstants.TO_FRAG.TO_HOME_CLASSROOM;

        root.findViewById(R.id.save).setOnClickListener(v -> uploadFinal());

        root.findViewById(R.id.addAttachment).setOnClickListener(v -> upload());
        return root;
    }

    private void uploadFinal() {
        pd.setTitle("Uploading Image...");
        pd.show();

        if (isEdit) {
            for (String s : attachmentsOnEdit) {
                if (!attachments.contains(s)) {
                    forDelete.add(s);
                    urls.remove(s);
                }
            }

            if (forDelete.size() != 0) {
                for (int i = 0; i < forDelete.size(); i++) {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(forDelete.get(i));
                    int finalI = i;
                    storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            if (finalI == forDelete.size() - 1) {
                                subFunctionUpload();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {

                        }
                    });
                }

            } else {
                subFunctionUpload();
            }

        } else {
            subFunctionUpload();
        }
    }
    ProgressDialog pd;

    private void subFunctionUpload() {
        if (forAdd.size() == 0) {
            uploadToClassroom(pd);
            return;
        }
        for (int i = 0; i < attachments.size(); i++) {
            Bitmap bitmap = null;
            Uri selectedDataPath = null;
            if (forAdd.contains(attachments.get(i))) {
                if (attachments.get(i).startsWith("314")) bitmap = (Bitmap) attachmentsData.get(i);
                else selectedDataPath = (Uri) attachmentsData.get(i);

                if (selectedDataPath == null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] data = baos.toByteArray();

                    final String randomKey = UUID.randomUUID().toString();

                    UploadTask uploadTask = storageRef.child(storageName + "/" + HomeClassroom.classId + "/solution/" + randomKey + ".jpg").putBytes(data);
                    uploadTask.addOnFailureListener(exception -> Toast.makeText(getContext(), "failed", Toast.LENGTH_SHORT).show()).addOnSuccessListener(taskSnapshot -> {
                        urls.add(taskSnapshot.getMetadata().getPath());
                        uploadImageCounter++;
                        checkandpost(pd);
                    });
                } else {
                    final String randomKey = UUID.randomUUID().toString();
                    String extension;
                    if (selectedDataPath.getPath().contains(".")) {
                        extension = selectedDataPath.getPath().substring(selectedDataPath.getPath().lastIndexOf("."));
                    } else {
                        extension = ".pdf";
                    }

                    StorageReference riversRef = storageRef.child((storageName + "/" + HomeClassroom.classId + "/solution/" + randomKey + extension));


                    riversRef.putFile(selectedDataPath)
                            .addOnSuccessListener(taskSnapshot -> {
                                urls.add(taskSnapshot.getMetadata().getPath());
                                uploadImageCounter++;
                                checkandpost(pd);
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Unable to post your Image !", Toast.LENGTH_SHORT).show());

                }
            }

        }
    }

    private void checkandpost(ProgressDialog pd) {
        if (uploadImageCounter == forAdd.size()) {
            uploadToClassroom(pd);
        }
    }


    private void uploadToClassroom(ProgressDialog pd) {
        FirebaseFirestore.getInstance()
                .collection("classroom")
                .document(HomeClassroom.classId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        List<Map<String, Object>> dataF = (List) documentSnapshot.get(documentName);

                        for (Map<String, Object> stringObjectMap : dataF) {
                            if (stringObjectMap.get("resultId").toString().equals(data.get("resultId").toString())) {
                                List<String> submitted = (List) stringObjectMap.get("solution");

                                for (Object attachmentsDatum : attachmentsData)
                                    if (!attachmentsDatum.toString().equals("-"))
                                        submitted.add(urls.get(attachmentsData.indexOf(attachmentsDatum)));

                                for (String s : forDelete) {
                                    submitted.remove(s);
                                }

                                dataF.get(dataF.indexOf(stringObjectMap)).put("solution", submitted);
                                Map<String, Object> dataForUpload = new HashMap<>();
                                dataForUpload.put(documentName, dataF);
                                FirebaseFirestore.getInstance()
                                        .collection("classroom")
                                        .document(HomeClassroom.classId)
                                        .update(dataForUpload)
                                        .addOnSuccessListener(aVoid1 -> {
                                            if (getActivity()==null)return;
                                            pd.dismiss();
                                            getActivity().onBackPressed();
                                        });
                            }
                        }

                    }
                });
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


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;
        switch (requestCode) {
            case LocalConstants.PERMISSION_CODE.PICKFILE_RESULT_CODE:
                attachmentsData.add(data.getData());
                attachments.add(data.getData().getPath());
                forAdd.add(data.getData().getPath());
                imagesadapter.notifyItemInserted(attachments.size() - 1);
                ((TextView) dialog.findViewById(R.id.pdf)).setBackground(getActivity().getDrawable(R.drawable.second_btn));
                break;
            case LocalConstants.PERMISSION_CODE.SELECT_PICTURE:
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    ((TextView) dialog.findViewById(R.id.image)).setBackground(getActivity().getDrawable(R.drawable.second_btn));
                    attachmentsData.add(selectedImageUri);
                    attachments.add(selectedImageUri.getPath());
                    forAdd.add(selectedImageUri.getPath());
                    imagesadapter.notifyItemInserted(attachments.size() - 1);
                }
                break;
            case LocalConstants.PERMISSION_CODE.CAMERA_REQUEST:
                ((TextView) dialog.findViewById(R.id.camera)).setBackground(getActivity().getDrawable(R.drawable.second_btn));
                attachmentsData.add((Bitmap) data.getExtras().get("data"));
                attachments.add("314" + UUID.randomUUID().toString());
                forAdd.add("314" + UUID.randomUUID().toString());
                imagesadapter.notifyItemInserted(attachments.size() - 1);
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

    public void deleteImage(int position) {
        attachmentsData.remove(position);
        attachments.remove(position);
        if (attachmentsData.size() == 0) uploadRecyclerView.setBackground(null);
        imagesadapter = new uploadImagesAdapter(attachments, this, LocalConstants.FROM.FROM_ADD_SOLUTION,root.findViewById(R.id.progress));
        uploadRecyclerView.setAdapter(imagesadapter);

    }



}