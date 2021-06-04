package com.classroom.eduethics.Fragments.TeacherFragments;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.classroom.eduethics.Activity.ImagePage;
import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.Adapter.uploadImagesAdapter;
import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.Fragments.SubFragments.AddSolution;
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


public class CreateAssignment extends Fragment {

    TextInputEditText topic, description, maxMarks;
    TextView dateText, timeText;
    int[] d = new int[3];
    int[] t = new int[2];
    Dialog dialog;


    ArrayList<String> attachments = new ArrayList<>();
    ArrayList<Object> attachmentsData = new ArrayList<>();

    ArrayList<String> attachmentsOnEdit = new ArrayList<>();

    ArrayList<String> forDelete = new ArrayList<>();
    ArrayList<String> forAdd = new ArrayList<>();

    boolean isEdit = false;

    Map<String, Object> data;
    int position;

    public CreateAssignment() {
    }

    public CreateAssignment(Map<String, Object> data, int position) {
        this.data = data;
        this.isEdit = true;
        this.position = position;
    }

    uploadImagesAdapter imagesadapter = new uploadImagesAdapter(attachments, this, LocalConstants.FROM.FROM_CREATE_ASSIGNMENT,null);

    List<String> urls = new ArrayList<>();

    int uploadImageCounter = 0;

    RecyclerView uploadRecyclerView;

    View pd;

    Calendar dateTime = Calendar.getInstance();

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl(LocalConstants.STORAGE_REF);    //change the url according to your firebase app
    View root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_create_assignment, container, false);

        imagesadapter.setProgress(root.findViewById(R.id.progress));

        MainActivity.TO_WHICH_FRAG = LocalConstants.TO_FRAG.TO_ASSIGNMENT_FRAG;

        pd = root.findViewById(R.id.progress);

        uploadRecyclerView = root.findViewById(R.id.uploadRecyclerView);
        uploadRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        uploadRecyclerView.setAdapter(imagesadapter);

        d[0] = dateTime.get(Calendar.DAY_OF_MONTH);
        d[1] = dateTime.get(Calendar.MONTH);
        d[2] = dateTime.get(Calendar.YEAR);

        dateText = root.findViewById(R.id.dateText);
        timeText = root.findViewById(R.id.timeText);

        topic = root.findViewById(R.id.topic);
        description = root.findViewById(R.id.description);
        maxMarks = root.findViewById(R.id.maxMarks);

        dateText.setOnClickListener(v -> ExtraFunctions.datePicker(getContext(), dateText, d));
        timeText.setOnClickListener(v -> ExtraFunctions.timePicker(getContext(), timeText, t));

        root.findViewById(R.id.addAttachment).setOnClickListener(v -> upload());
        Toast.makeText(getContext(), "here", Toast.LENGTH_SHORT).show();

        if (isEdit) {
            d[0] = Integer.parseInt(data.get("date").toString().split("/")[0]);
            d[1] = Integer.parseInt(data.get("date").toString().split("/")[1]);
            d[2] = Integer.parseInt(data.get("date").toString().split("/")[2]);

            t[0] = Integer.parseInt(data.get("time").toString().split(":")[0]);
            t[1] = Integer.parseInt(data.get("time").toString().split(":")[1]);

            topic.setText(data.get("topic").toString());
            description.setText(data.get("description").toString());
            maxMarks.setText(data.get("maxMarks").toString());
            attachmentsOnEdit.addAll((List) data.get("attachment"));
            urls.addAll(attachmentsOnEdit);
            for (int i = 0; i < attachmentsOnEdit.size(); i++) {
                attachmentsData.add("-");
            }

            attachments.addAll(attachmentsOnEdit);
        } else {
            d[0] = dateTime.get(Calendar.DAY_OF_MONTH);
            d[1] = dateTime.get(Calendar.MONTH) + 1;
            d[2] = dateTime.get(Calendar.YEAR);
            t[0] = dateTime.get(Calendar.HOUR_OF_DAY);
            t[1] = dateTime.get(Calendar.MINUTE);
        }

        timeText.setText(ExtraFunctions.getReadableTime(t[0], t[1]));
        dateText.setText(ExtraFunctions.getReadableDate(d[0], d[1], d[2]));

        root.findViewById(R.id.saveCreateAssignment).setOnClickListener(v -> {
            boolean allDone = true;
            if (topic.getText().toString().equals("")) {
                topic.setError("Required !");
                allDone = false;
            }
            if (description.getText().toString().equals("")) {
                description.setError("Required !");
                allDone = false;
            }
            if (maxMarks.getText().toString().equals("")) {
                maxMarks.setError("Required !");
                allDone = false;
            }
            if (allDone) {
                if ((d[0] == Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) && (d[1] - 1 == Calendar.getInstance().get(Calendar.MONTH)) && (d[2] == Calendar.getInstance().get(Calendar.YEAR))) {
                    if (((Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60) + Calendar.getInstance().get(Calendar.MINUTE)) > (t[0] * 60 + t[1])) {
                        root.findViewById(R.id.time).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.error_time, null));
                        Toast.makeText(getContext(), "Please Set Valid Time for deadline !", Toast.LENGTH_SHORT).show();
                        allDone = false;
                    } else {
                        root.findViewById(R.id.time).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.background_blocks_12dp, null));
                    }
                }
            }


            if (allDone) {
                pd.setVisibility(View.VISIBLE);
                uploadToFirebase();
            }
        });

        return root;
    }


    private void uploadToFirebase() {


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
                            pd.setVisibility(View.GONE);
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

    private void subFunctionUpload() {
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
                Toast.makeText(getContext(), "Tost 1", Toast.LENGTH_SHORT).show();
                if (selectedDataPath == null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] data = baos.toByteArray();

                    final String randomKey = UUID.randomUUID().toString();

                    UploadTask uploadTask = storageRef.child("assignmentData/" + HomeClassroom.classId + "/" + randomKey + ".jpg").putBytes(data);
                    uploadTask
                            .addOnFailureListener(exception -> Toast.makeText(getContext(), "failed", Toast.LENGTH_SHORT).show())
                            .addOnSuccessListener(taskSnapshot -> {
                                urls.add(taskSnapshot.getMetadata().getPath());
                                uploadImageCounter++;
                                Toast.makeText(getContext(), "Tost", Toast.LENGTH_SHORT).show();
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

                    StorageReference riversRef = storageRef.child(("assignmentData/" + HomeClassroom.classId + "/" + randomKey + extension));

                    riversRef.putFile(selectedDataPath)
                            .addOnSuccessListener(taskSnapshot -> {
                                pd.setVisibility(View.GONE);
                                String name = riversRef.getDownloadUrl().toString();
                                urls.add(taskSnapshot.getMetadata().getPath());
                                uploadImageCounter++;
                                checkandpost();
                            }).addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Unable to post your Image !", Toast.LENGTH_SHORT).show();
                        Log.d("TAG", e.getLocalizedMessage() + " " + e.getMessage() + "subFunctionUpload: ");
                        e.printStackTrace();
                    });

                }
            }

        }
    }

    private void checkandpost() {
        if (uploadImageCounter == forAdd.size()) {
            uploadToClassroom();
        }
    }


    private void uploadToClassroom() {
        Map<String, Object> map = new HashMap<>();
        String randomId = isEdit ? data.get("resultId").toString() : UUID.randomUUID().toString();
        map.put("topic", topic.getText().toString());
        map.put("description", description.getText().toString());
        map.put("date", d[0] + "/" + d[1] + "/" + d[2]);
        map.put("time", t[0] + ":" + t[1]);
        map.put("attachment", urls);
        map.put("submitted", new ArrayList<>());
        map.put("maxMarks", maxMarks.getText().toString());
        map.put("resultId", randomId);
        map.put("solution", new ArrayList<>());

        Toast.makeText(getContext(), "" + urls.size(), Toast.LENGTH_SHORT).show();

        FirebaseFirestore.getInstance()
                .collection("classroom")
                .document(HomeClassroom.classId)
                .collection("assignmentSub")
                .document(randomId)
                .set(new HashMap<>())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Map<String, Object> fData = new HashMap<>();
                        if (isEdit)
                            HomeClassroom.model.getAssignments().remove(position);
                        List<Map<String, Object>> lst = HomeClassroom.model.getAssignments();
                        lst.add(map);
                        fData.put("assignments", lst);
                        FirebaseFirestore.getInstance()
                                .collection("classroom")
                                .document(HomeClassroom.classId)
                                .update(fData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        if (getActivity() == null) return;
                                        Toast.makeText(getContext(), "Complete", Toast.LENGTH_SHORT).show();
                                        pd.setVisibility(View.GONE);
                                        List<String> ids = new ArrayList<>();
                                        for (Map<String, Object> currentStudent : HomeClassroom.model.getCurrentStudents())
                                            ids.add(currentStudent.get("id").toString());
                                        ExtraFunctions.sendNoti(ids,"New Assignment",topic.getText().toString()+" Assignment posted by teacher.",getContext());
                                        addSolutionDialog();

                                    }
                                });
                    }
                });


        FirebaseFirestore.getInstance()
                .collection("timetable")
                .document(HomeClassroom.classId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Map<String, Object> forUpload = new HashMap<>();
                        Map<String, Object> data = new HashMap<>();
                        data.put("time", Calendar.getInstance().getTimeInMillis() + "");
                        data.put("subject", HomeClassroom.model.getSubject());
                        data.put("type", "assignment");
                        data.put("title", "Assignment " + topic.getText().toString());
                        data.put("dec", "Last Submission date for this assignment is " + d[0] + " " + LocalConstants.MONTHS[d[1] - 1]);
                        List<Map<String, Object>> preData = new ArrayList<>();

                        if (documentSnapshot.get(d[0] + "" + d[1] + "" + d[2]) != null)
                            preData = (List) documentSnapshot.get(dateText.getText().toString());

                        preData.add(data);
                        forUpload.put(d[0] + "-" + d[1] + "-" + d[2], preData);
                        FirebaseFirestore.getInstance()
                                .collection("timetable")
                                .document(HomeClassroom.classId)
                                .update(forUpload);
                    }
                });


    }

    private void addSolutionDialog() {
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.add_solution_dialog);

        dialog.findViewById(R.id.add).setOnClickListener(v -> {
            if (getActivity() != null)
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, new AddSolution("assignments", data, false)).commit();
            dialog.dismiss();
        });

        dialog.findViewById(R.id.cancel).setOnClickListener(v -> {
            if (getActivity() != null)
                getActivity().onBackPressed();
            dialog.dismiss();
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    private void upload() {
        dialog = new Dialog(getContext());
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
            }
        });

        dialog.findViewById(R.id.image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(i, "Select Picture"), LocalConstants.PERMISSION_CODE.SELECT_PICTURE);
            }
        });

        ((TextView) dialog.findViewById(R.id.upload)).setText("Select");
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
        imagesadapter.notifyItemRemoved(position);
    }



}