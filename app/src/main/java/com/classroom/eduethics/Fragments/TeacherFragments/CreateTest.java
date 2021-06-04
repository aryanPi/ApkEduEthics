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
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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


public class CreateTest extends Fragment {

    Map<String, Object> data;
    boolean isEdit = false;

    public CreateTest() {

    }

    int position;

    public CreateTest(Map<String, Object> data, int position) {
        this.data = data;
        this.isEdit = true;
        this.position = position;
        MainActivity.TO_WHICH_FRAG = LocalConstants.TO_FRAG.TO_TEST_FRAG;
    }

    TextView dateText, timesText;
    int[] t = new int[2];
    int[] d = new int[3];

    boolean isDone = false;

    Calendar dateTime = Calendar.getInstance();

    ArrayList<String> attachments = new ArrayList<>();
    ArrayList<Object> attachmentsData = new ArrayList<>();

    ArrayList<String> attachmentsOnEdit = new ArrayList<>();

    ArrayList<String> forDelete = new ArrayList<>();
    ArrayList<String> forAdd = new ArrayList<>();


    uploadImagesAdapter imagesadapter = new uploadImagesAdapter(attachments, this, LocalConstants.FROM.FROM_CREATE_TEST,null);


    public RecyclerView uploadRecyclerView;


    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl(LocalConstants.STORAGE_REF);    //change the url according to your firebase app

//    Uri selectedDataPath;
    //   Bitmap bitmap = null;


    TextInputEditText topic, description, testDuration, marksPerCorrect, panaltyMarks, maxMarks;

    View root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_create_test, container, false);

    imagesadapter.setProgress(root.findViewById(R.id.progress));
        uploadRecyclerView = root.findViewById(R.id.uploadRecyclerView);
        uploadRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        uploadRecyclerView.setAdapter(imagesadapter);
        pd = root.findViewById(R.id.progress);
        dateText = root.findViewById(R.id.dateText);
        timesText = root.findViewById(R.id.timeText);

        topic = root.findViewById(R.id.topic);
        description = root.findViewById(R.id.description);
        testDuration = root.findViewById(R.id.testDuration);
        marksPerCorrect = root.findViewById(R.id.marksPerCorrect);
        panaltyMarks = root.findViewById(R.id.panaltyMarks);
        maxMarks = root.findViewById(R.id.maxMarks);


        if (isEdit) {
            d[0] = Integer.parseInt(data.get("date").toString().split("/")[0]);
            d[1] = Integer.parseInt(data.get("date").toString().split("/")[1]);
            d[2] = Integer.parseInt(data.get("date").toString().split("/")[2]);

            t[0] = Integer.parseInt(data.get("time").toString().split(":")[0]);
            t[1] = Integer.parseInt(data.get("time").toString().split(":")[1]);

            topic.setText(data.get("topic").toString());
            description.setText(data.get("description").toString());
            testDuration.setText(data.get("testDuration").toString());
            marksPerCorrect.setText(data.get("marksPerCorrect").toString());
            panaltyMarks.setText(data.get("panaltyMarks").toString());
            maxMarks.setText(data.get("maxMarks").toString());

            attachmentsOnEdit.addAll((List) data.get("attachment"));
            urls.addAll(attachmentsOnEdit);
            for (int i = 0; i < attachmentsOnEdit.size(); i++) {
                attachmentsData.add("-");
            }

            attachments.addAll(attachmentsOnEdit);
        } else {
            d[0] = dateTime.get(Calendar.DAY_OF_MONTH);
            d[1] = dateTime.get(Calendar.MONTH)+1;
            d[2] = dateTime.get(Calendar.YEAR);
            t[0] = dateTime.get(Calendar.HOUR_OF_DAY);
            t[1] = dateTime.get(Calendar.MINUTE);
        }


        timesText.setText(ExtraFunctions.getReadableTime(t[0], t[1]));
        dateText.setText(ExtraFunctions.getReadableDate(d[0], d[1], d[2]));

        root.findViewById(R.id.date).setOnClickListener(v -> ExtraFunctions.datePicker(getContext(), dateText, d));
        root.findViewById(R.id.addAttachment).setOnClickListener(v -> upload());
        root.findViewById(R.id.time).setOnClickListener(v -> ExtraFunctions.timePicker(getContext(), timesText, t));

        root.findViewById(R.id.continue_test).setOnClickListener(v -> {

            if (isDone) {
                uploadToFirebase();
            } else {
                boolean allDone = true;
                if (topic.getText().toString().equals("")) {
                    topic.setError("Required !");
                    allDone = false;
                }
                if (description.getText().toString().equals("")) {
                    description.setError("Required !");
                    allDone = false;
                }
                if (testDuration.getText().toString().equals("")) {
                    testDuration.setError("Required !");
                    allDone = false;
                }
                if (marksPerCorrect.getText().toString().equals("")) {
                    marksPerCorrect.setError("Required !");
                    allDone = false;
                }
                if (panaltyMarks.getText().toString().equals("")) {
                    panaltyMarks.setError("Required !");
                    allDone = false;
                }
                if (maxMarks.getText().toString().equals("")) {
                    maxMarks.setError("Required !");
                    allDone = false;
                }
                if (allDone) {
                    if (Integer.parseInt(maxMarks.getText().toString()) < Integer.parseInt(marksPerCorrect.getText().toString())) {
                        marksPerCorrect.setError("Greater than Max. Marks");
                        allDone = false;
                    }

                    if (Integer.parseInt(maxMarks.getText().toString()) < Integer.parseInt(panaltyMarks.getText().toString())) {
                        panaltyMarks.setError("Greater than Max. Marks");
                        allDone = false;
                    }
                    if ((d[0] == Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) && (d[1]-1 == Calendar.getInstance().get(Calendar.MONTH)) && (d[2] == Calendar.getInstance().get(Calendar.YEAR)))
                    {
                        if (((Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60) + Calendar.getInstance().get(Calendar.MINUTE)) > (t[0] * 60 + t[1])) {
                            ((LinearLayout) root.findViewById(R.id.time)).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.error_time, null));
                            Toast.makeText(getContext(), "Please Set Valid Time for deadline !", Toast.LENGTH_SHORT).show();
                            allDone = false;
                        }else{
                            ((LinearLayout) root.findViewById(R.id.time)).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.background_blocks_12dp, null));
                        }
                    }else{
                        Toast.makeText(getContext(), "not equal"+(d[0] == Calendar.getInstance().get(Calendar.DAY_OF_MONTH))+" : "+(d[1]-1 == Calendar.getInstance().get(Calendar.MONTH))+" : "+(d[2] == Calendar.getInstance().get(Calendar.YEAR)), Toast.LENGTH_SHORT).show();
                    }
                }

                if (allDone) {
                    ((TextView) v).setText("Add Question Paper");
                    TransitionManager.beginDelayedTransition(root.findViewById(R.id.MAIN_CREATETEST));
                    root.findViewById(R.id.part1Group).setVisibility(View.GONE);
                    root.findViewById(R.id.part2group).setVisibility(View.VISIBLE);
                    isDone = true;
                }
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


    private void uploadToFirebase() {
        pd.setVisibility(View.VISIBLE);

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

    int uploadImageCounter = 0;
    View pd;

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

                if (selectedDataPath == null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] data = baos.toByteArray();

                    final String randomKey = UUID.randomUUID().toString();

                    UploadTask uploadTask = storageRef.child("testData/" + HomeClassroom.classId + "/" + randomKey + ".jpg").putBytes(data);
                    uploadTask.addOnFailureListener(exception -> {
                        if (getActivity() == null) return;
                        Toast.makeText(getContext(), "failed", Toast.LENGTH_SHORT).show();
                    }).addOnSuccessListener(taskSnapshot -> {
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

                    StorageReference riversRef = storageRef.child(("testData/" + HomeClassroom.classId + "/" + randomKey + extension));


                    riversRef.putFile(selectedDataPath)
                            .addOnSuccessListener(taskSnapshot -> {
                                urls.add(taskSnapshot.getMetadata().getPath());
                                uploadImageCounter++;
                                checkandpost();
                            })
                            .addOnFailureListener(e -> {
                                if (getActivity() == null) return;
                                Toast.makeText(getContext(), "Unable to post your Image !", Toast.LENGTH_SHORT).show();
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

    List<String> urls = new ArrayList<>();

    private void uploadToClassroom() {

        Map<String, Object> map = new HashMap<>();


        map.put("topic", ((RadioButton)root.findViewById(((RadioGroup)root.findViewById(R.id.testTypeRadioGroup)).getCheckedRadioButtonId())).getText().toString()+" : "+topic.getText().toString());
        map.put("description", description.getText().toString());
        map.put("marksPerCorrect", marksPerCorrect.getText().toString());
        map.put("panaltyMarks", panaltyMarks.getText().toString());
        map.put("testDuration", testDuration.getText().toString());
        map.put("date", d[0] + "/" + d[1] + "/" + d[2]);
        map.put("time", t[0] + ":" + t[1]);
        map.put("attachment", urls);
        map.put("submitted", new ArrayList<>());
        map.put("solution", new ArrayList<>());
        map.put("maxMarks", maxMarks.getText().toString());
        String randomId = isEdit ? data.get("resultId").toString() : UUID.randomUUID().toString();
        map.put("resultId", randomId);
        if (data == null)
            data = new HashMap<>();
        data.put("resultId", randomId);

        FirebaseFirestore.getInstance()
                .collection("classroom")
                .document(HomeClassroom.classId)
                .collection("testSub")
                .document(randomId)
                .set(new HashMap<>())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Map<String, Object> fData = new HashMap<>();
                        if (isEdit)
                            HomeClassroom.model.getTest().remove(position);
                        List<Map<String, Object>> lst = HomeClassroom.model.getTest();
                        lst.add(map);
                        if (getActivity() != null)
                            Toast.makeText(getContext(), "" + ((List) lst.get(lst.size() - 1).get("attachment")).size(), Toast.LENGTH_SHORT).show();
                        fData.put("test", lst);
                        FirebaseFirestore.getInstance()
                                .collection("classroom")
                                .document(HomeClassroom.classId)
                                .update(fData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        if (getActivity() == null) return;
                                        Toast.makeText(getContext(), "Test Created", Toast.LENGTH_SHORT).show();


                                        List<String> ids = new ArrayList<>();
                                        for (Map<String, Object> currentStudent : HomeClassroom.model.getCurrentStudents())
                                            ids.add(currentStudent.get("id").toString());
                                        ExtraFunctions.sendNoti(ids,"New Test",topic.getText().toString()+" Test posted by teacher.",getContext());


                                        pd.setVisibility(View.GONE);
                                        addSolutionDialog();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.setVisibility(View.GONE);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.setVisibility(View.GONE);
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
                        data.put("type", "test");
                        data.put("title", "Test "+topic.getText().toString());
                        data.put("dec", "Last Submission date of this test is "+d[0]+" "+LocalConstants.MONTHS[d[1]]);
                        List<Map<String, Object>> preData = new ArrayList<>();

                        if (documentSnapshot.get(d[0] + "" + d[1] + "" + d[2]) != null) {
                            preData = (List) documentSnapshot.get(dateText.getText().toString());
                        }

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
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, new AddSolution("test", data, false)).commit();
            dialog.dismiss();
        });

        dialog.findViewById(R.id.cancel).setOnClickListener(v -> {
            dialog.dismiss();
            getActivity().onBackPressed();
        });

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
        imagesadapter = new uploadImagesAdapter(attachments, this, LocalConstants.FROM.FROM_CREATE_TEST,root.findViewById(R.id.progress));
        uploadRecyclerView.setAdapter(imagesadapter);

    }

    public void itemClick(int position) {

    }


}