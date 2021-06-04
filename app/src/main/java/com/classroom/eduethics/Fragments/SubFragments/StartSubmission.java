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
import com.classroom.eduethics.Utils.ExtraFunctions;
import com.classroom.eduethics.Utils.GlobalVariables;
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

import static com.classroom.eduethics.Utils.LocalConstants.FROM.FROM_ASSIGNMENT_FRAG;
import static com.classroom.eduethics.Utils.LocalConstants.FROM.FROM_EVALUATE_FRAG;
import static com.classroom.eduethics.Utils.LocalConstants.FROM.FROM_TEST_FRAG;

public class StartSubmission extends Fragment {

    Map<String, Object> data;
    Dialog dialog;


    ArrayList<String> attachments = new ArrayList<>();
    ArrayList<Object> attachmentsData = new ArrayList<>();

    ArrayList<String> attachmentsOnEdit = new ArrayList<>();
    ArrayList<Object> attachmentsDataOnEdit = new ArrayList<>();


    ArrayList<String> forDelete = new ArrayList<>();
    ArrayList<String> forAdd = new ArrayList<>();
    List<String> urls = new ArrayList<>();

    uploadImagesAdapter imagesadapter = new uploadImagesAdapter(attachments, this, LocalConstants.FROM.FROM_SUMBIT_TEST_ATT,null);
    uploadImagesAdapter imagesadapterAlready;

    RecyclerView uploadsRecyclerview, solutionRecyclerView;

    int from;

    List<String> attachmentsDataFix = new ArrayList<>();

    public StartSubmission(Map<String, Object> data, int from, boolean isDead) {
        this.from = from;
        this.data = data;
        this.isDead = isDead;
        imagesadapterAlready = new uploadImagesAdapter(attachmentsDataFix, this, FROM_EVALUATE_FRAG,null);
    }

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl(LocalConstants.STORAGE_REF);    //change the url according to your firebase app
    String collection = "";
    String documentName = "";
    TextView score, feedback;
    View root;


    boolean isDead;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_start_submission, container, false);

        imagesadapter.setProgress(root.findViewById(R.id.progress));
        imagesadapterAlready.setProgress(root.findViewById(R.id.progress));

        MainActivity.TO_WHICH_FRAG = from == FROM_ASSIGNMENT_FRAG ? LocalConstants.TO_FRAG.TO_ASSIGNMENT_FRAG : LocalConstants.TO_FRAG.TO_TEST_FRAG;


        ((TextView) root.findViewById(R.id.topic)).setText(data.get("topic").toString());
        ((TextView) root.findViewById(R.id.maxMarks)).setText(data.get("maxMarks").toString());
        ((TextView) root.findViewById(R.id.deadline)).setText(data.get("date").toString().trim() + " " + ExtraFunctions.getReadableTime(Integer.parseInt(data.get("time").toString().split(":")[0]), Integer.parseInt(data.get("time").toString().split(":")[1])));
        ((TextView) root.findViewById(R.id.des)).setText(data.get("description").toString());

        pd = root.findViewById(R.id.progress);
        RecyclerView recyclerviewAttachments = root.findViewById(R.id.recyclerviewAttachments);
        recyclerviewAttachments.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerviewAttachments.setAdapter(imagesadapterAlready);
        attachmentsDataFix.addAll((List) data.get("attachment"));
        if (((List) data.get("attachment")).size() == 0) {
            recyclerviewAttachments.setVisibility(View.GONE);
            root.findViewById(R.id.temp4).setVisibility(View.GONE);
        }
        imagesadapterAlready.notifyDataSetChanged();

        solutionRecyclerView = root.findViewById(R.id.solutionRecyclerView);
        score = root.findViewById(R.id.score);
        feedback = root.findViewById(R.id.feedback);

        uploadsRecyclerview = root.findViewById(R.id.uploadsRecyclerview);
        uploadsRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        uploadsRecyclerview.setAdapter(imagesadapter);

        if (from == FROM_TEST_FRAG) {
            collection = "testSub";
            documentName = "test";
        } else if (from == FROM_ASSIGNMENT_FRAG) {
            collection = "assignmentSub";
            documentName = "assignments";
        }

        FirebaseFirestore.getInstance()
                .collection("classroom")
                .document(HomeClassroom.classId)
                .collection(collection)
                .document(data.get("resultId").toString())
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (getActivity() == null) return;
                    Map<String, Object> data2 = (Map) documentSnapshot.get(GlobalVariables.uid);

                    int localFrom = LocalConstants.FROM.FROM_SUMBIT_TEST;
                    if (data2 != null) {
                        if ((data2.get("feedback").toString().equals("-") && data2.get("score").toString().split("/")[0].equals("-")) || isDead) {
                            root.findViewById(R.id.solitionConstraint).setVisibility(View.GONE);
                        } else {
                            localFrom = FROM_TEST_FRAG;
                            showSolutions(data2);
                        }

                        attachmentsOnEdit.addAll((List) data2.get("attachment"));
                        urls.addAll(attachmentsOnEdit);
                        for (int i = 0; i < attachmentsOnEdit.size(); i++) {
                            attachmentsData.add("-");
                        }
                        attachments.addAll(attachmentsOnEdit);
                        imagesadapter = new uploadImagesAdapter(attachments, StartSubmission.this, localFrom,root.findViewById(R.id.progress));
                        uploadsRecyclerview.setAdapter(imagesadapter);
                        if (attachments.size() == 0) {
                            uploadsRecyclerview.setVisibility(View.GONE);
                        }
                    } else if (isDead) {
                        root.findViewById(R.id.recyclerviewAttachments).setVisibility(View.GONE);
                        showSolutions(data2);
                    }
                }).addOnFailureListener(e -> {
            if (getActivity() == null) return;
            Toast.makeText(getContext(), "Failed !", Toast.LENGTH_SHORT).show();
        });

        if (!isDead) {
            root.findViewById(R.id.addAttachmentBtn).setOnClickListener(v -> addAtachment());
        } else {
            root.findViewById(R.id.addAttachmentBtn).setVisibility(View.INVISIBLE);
            root.findViewById(R.id.temp0).setVisibility(View.GONE);
            root.findViewById(R.id.addAttachmentBtn).setVisibility(View.GONE);
            root.findViewById(R.id.uploadsRecyclerview).setVisibility(View.GONE);
        }


        root.findViewById(R.id.submit).setOnClickListener(v -> submit());

        return root;
    }

    private void showSolutions(Map<String, Object> data2) {

        root.findViewById(R.id.solitionConstraint).setVisibility(View.VISIBLE);

        if (((List) data.get("solution")).size() == 0) {
            root.findViewById(R.id.empty).setVisibility(View.VISIBLE);
            ((TextView)root.findViewById(R.id.emptyMessage)).setText("No Solution has been added by teacher");

            solutionRecyclerView.setVisibility(View.GONE);
            root.findViewById(R.id.temp6).setVisibility(View.GONE);
        } else {
            solutionRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            solutionRecyclerView.setAdapter(imagesadapterAlready);
            solutionRecyclerView.setAdapter(new uploadImagesAdapter((List) data.get("solution"), StartSubmission.this, FROM_TEST_FRAG,root.findViewById(R.id.progress)));
            root.findViewById(R.id.submit).setVisibility(View.GONE);
        }

        if (data2 != null) {
            if (data2.get("feedback") != null) {
                feedback.setText(data2.get("feedback").toString());
                score.setText(data2.get("score").toString());
                root.findViewById(R.id.addAttachmentBtn).setVisibility(View.INVISIBLE);
            } else {
                feedback.setVisibility(View.GONE);
                score.setVisibility(View.GONE);
            }
        } else {
            root.findViewById(R.id.temp8).setVisibility(View.GONE);
            root.findViewById(R.id.score).setVisibility(View.GONE);
            root.findViewById(R.id.feedback).setVisibility(View.GONE);
            root.findViewById(R.id.temp7).setVisibility(View.GONE);
        }

    }

    private void addAtachment() {
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

    private void submit() {
        pd.setVisibility(View.VISIBLE);

        for (String s : attachmentsOnEdit)
            if (!attachments.contains(s)) {
                forDelete.add(s);
                urls.remove(s);
            }

        if (forDelete.size() != 0) for (int i = 0; i < forDelete.size(); i++) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(forDelete.get(i));
            int finalI = i;
            storageReference.delete().addOnSuccessListener(aVoid -> {
                if (finalI == forDelete.size() - 1) subFunctionUpload();
            }).addOnFailureListener(exception -> {
                if (finalI == forDelete.size() - 1) subFunctionUpload();
            });
        }
        else subFunctionUpload();

    }

    View pd;

    private void subFunctionUpload() {
        String child = "";
        if (from == FROM_ASSIGNMENT_FRAG) {
            child = "assignmentData";
        } else if (from == FROM_TEST_FRAG) {
            child = "testData";
        }
        if (forAdd.size() == 0) {
            uploadToClassroom();
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

                    UploadTask uploadTask = storageRef.child(child + "/" + HomeClassroom.classId + "/sub/" + randomKey + ".jpg").putBytes(data);
                    uploadTask.addOnFailureListener(exception -> Toast.makeText(getContext(), "failed", Toast.LENGTH_SHORT).show()).addOnSuccessListener(taskSnapshot -> {
                        urls.add(taskSnapshot.getMetadata().getPath());
                        uploadImageCounter++;
                        checkandpost();
                    });
                } else {
                    final String randomKey = UUID.randomUUID().toString();
                    String extension;
                    if (selectedDataPath.getPath().contains(".")) {
                        extension = selectedDataPath.getPath().substring(selectedDataPath.getPath().lastIndexOf("."));
                    } else {
                        extension = ".pdf";
                    }

                    StorageReference riversRef = storageRef.child((child + "/" + HomeClassroom.classId + "/sub/" + randomKey + extension));


                    riversRef.putFile(selectedDataPath)
                            .addOnSuccessListener(taskSnapshot -> {
                                pd.setVisibility(View.GONE);
                                String name = riversRef.getDownloadUrl().toString();
                                urls.add(taskSnapshot.getMetadata().getPath());
                                uploadImageCounter++;
                                checkandpost();
                            }).addOnProgressListener(snapshot -> {
                    }).addOnFailureListener(e -> Toast.makeText(getContext(), "Unable to post your Image !", Toast.LENGTH_SHORT).show());

                }
            }

        }

    }

    int uploadImageCounter = 0;

    private void checkandpost() {
        if (uploadImageCounter == forAdd.size()) {
            uploadToClassroom();
        }
    }

    private void uploadToClassroom() {
        Map<String, Object> mapF = new HashMap<>();
        mapF.put("feedback", "-");
        mapF.put("isSeen", false);
        mapF.put("name", "name1");
        mapF.put("score", "-/" + data.get("maxMarks").toString());
        mapF.put("attachment", urls);

        Map<String, Object> map = new HashMap<>();
        map.put(GlobalVariables.uid, mapF);


        FirebaseFirestore.getInstance()
                .collection("classroom")
                .document(HomeClassroom.classId)
                .collection(collection)
                .document(data.get("resultId").toString())
                .update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
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
                                                List<String> submitted = (List) stringObjectMap.get("submitted");
                                                submitted.add(GlobalVariables.uid);
                                                dataF.get(dataF.indexOf(stringObjectMap)).put("submitted", submitted);
                                                Map<String, Object> dataForUpload = new HashMap<>();
                                                dataForUpload.put(documentName, dataF);
                                                FirebaseFirestore.getInstance()
                                                        .collection("classroom")
                                                        .document(HomeClassroom.classId)
                                                        .update(dataForUpload)
                                                        .addOnSuccessListener(aVoid1 -> {
                                                            pd.setVisibility(View.GONE);
                                                            if (getActivity() == null) return;

                                                            getActivity().onBackPressed();
                                                        });
                                            }
                                        }

                                    }
                                });
                    }
                });

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
        imagesadapter.notifyItemRemoved(position);
    }

/*        FirebaseStorage.getInstance().getReference().child(attachmentsDataFix.get(position)).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //  profileImage.setImageURI(uri);
                startActivity(new Intent(getActivity(), ImagePage.class).putExtra("urlImage", uri).putExtra("type", attachmentsDataFix.get(position).substring(attachmentsDataFix.get(position).lastIndexOf(".") + 1)));
                //Log.d("Test"," Success!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //Log.d("Test"," Failed!");
            }
        });*/
    }
