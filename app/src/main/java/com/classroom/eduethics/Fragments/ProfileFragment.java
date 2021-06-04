package com.classroom.eduethics.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.Activity.SpalashActivity;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.GlobalVariables;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class ProfileFragment extends Fragment {

    Context context;
    TextInputEditText nameProfile, parentMobileNo, classProfile, numberProfile;
    String name, number, parentNumber, aClass, profilePic;
    List<String> subjects;
    ConstraintLayout MAIN_PROFILE;


    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl(LocalConstants.STORAGE_REF);    //change the url according to your firebase app


    public ProfileFragment(Context context) {
        this.context = context;

    }

    View root;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences("USER_PREF",
                Context.MODE_PRIVATE);

        MainActivity.toolbarImageFilter.setVisibility(View.GONE);

        if (GlobalVariables.isStudent) {
            root = inflater.inflate(R.layout.fragment_profile_student, container, false);
            student();
        } else {
            root = inflater.inflate(R.layout.fragment_profile_teacher, container, false);
            root.findViewById(R.id.viewProfile).setVisibility(View.VISIBLE);
            root.findViewById(R.id.editProfile).setVisibility(View.GONE);
            teacher();
        }

        MainActivity.actionBarTitle.setText("Profile");

        root.findViewById(R.id.deleteAccBtn).setOnClickListener(v -> deleteAcc());

        return root;
    }

    private void teacher() {
        FirebaseFirestore.getInstance()
                .collection("teacher")
                .document(GlobalVariables.uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (getContext() == null) return;
                    name = documentSnapshot.getString("name");
                    number = documentSnapshot.getString("phNo");
                    profilePic = documentSnapshot.getString("profilePic");
                    ((TextView) root.findViewById(R.id.joinDate)).setText(documentSnapshot.getString("joinDate"));
                    ((TextView) root.findViewById(R.id.name)).setText(documentSnapshot.getString("name"));
                    ((TextView) root.findViewById(R.id.instituteId)).setText("Institute id\n" + number);
                    if (!profilePic.equals("-")) {
                        FirebaseStorage.getInstance().getReference().child(profilePic).getDownloadUrl().addOnSuccessListener(uri -> {
                            if (getContext() != null)
                                Glide.with(getContext()).load(uri).centerCrop().into((ImageView) root.findViewById(R.id.profilePic));
                        }).addOnFailureListener(exception -> Toast.makeText(context, "Network Issue !", Toast.LENGTH_SHORT).show());
                    }

                    MainActivity.toolbarImageFilter.setImageDrawable(getActivity().getDrawable(R.drawable.ic_edit_pen));
                    MainActivity.toolbarImageFilter.setVisibility(View.VISIBLE);
                    MainActivity.toolbarImageFilter.setOnClickListener(v -> editTeacher());
                    root.findViewById(R.id.progress).setVisibility(View.GONE);

                });
    }

    private void editTeacher() {
        TransitionManager.beginDelayedTransition(root.findViewById(R.id.MAIN_PROFILE_TEACHER));
        root.findViewById(R.id.viewProfile).setVisibility(View.GONE);
        root.findViewById(R.id.editProfile).setVisibility(View.VISIBLE);

        root.findViewById(R.id.profilePicEditAble).setOnClickListener(v -> upload());
        root.findViewById(R.id.edit).setOnClickListener(v -> upload());

        if (!profilePic.equals("-")) {
            FirebaseStorage.getInstance()
                    .getReference()
                    .child(profilePic)
                    .getDownloadUrl()
                    .addOnSuccessListener(uri -> Glide.with(getContext()).load(uri).centerCrop().into((ImageView) root.findViewById(R.id.profilePicEditAble)))
                    .addOnFailureListener(exception -> Toast.makeText(context, "Network Issue !", Toast.LENGTH_SHORT).show());
        }

        ((TextInputEditText) root.findViewById(R.id.nameProfile)).setText(name);
        ((TextInputEditText) root.findViewById(R.id.number)).setText(number);
        ((TextInputEditText) root.findViewById(R.id.number)).addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        root.findViewById(R.id.saveAndUpdateBtnprofile).setOnClickListener(v -> saveTeacher());
    }


    Uri uriImage = null;

    private void saveTeacher() {
        root.findViewById(R.id.progress).setVisibility(View.VISIBLE);

        if (uriImage != null) {
            final String randomKey = UUID.randomUUID().toString();
            String extension;
            if (uriImage.getPath().contains(".")) {
                extension = uriImage.getPath().substring(uriImage.getPath().lastIndexOf("."));
            } else {
                extension = ".jpeg";
            }

            StorageReference riversRef = storageRef.child(("profilePic/" + randomKey + extension));

            riversRef.putFile(uriImage)
                    .addOnSuccessListener(taskSnapshot -> {
                        image = taskSnapshot.getMetadata().getPath();
                        uploadToFirebaseTeacher();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Unable to post your Image !", Toast.LENGTH_SHORT).show();
                        root.findViewById(R.id.progress).setVisibility(View.GONE);
                    });

        } else {
            uploadToFirebaseTeacher();

        }

    }

    String image = "-";

    private void uploadToFirebaseTeacher() {
        Map<String, Object> map = new HashMap<>();
        map.put("phNo", ((TextInputEditText) root.findViewById(R.id.number)).getText().toString());
        map.put("name", ((TextInputEditText) root.findViewById(R.id.nameProfile)).getText().toString());
        map.put("profilePic", image);
        FirebaseFirestore.getInstance().collection("teacher").document(GlobalVariables.uid)
                .update(map).addOnCompleteListener(task -> {
            root.findViewById(R.id.progress).setVisibility(View.GONE);
            TransitionManager.beginDelayedTransition(root.findViewById(R.id.MAIN_PROFILE_TEACHER));
            root.findViewById(R.id.viewProfile).setVisibility(View.VISIBLE);
            root.findViewById(R.id.editProfile).setVisibility(View.GONE);
            teacher();
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Failed To Upload Data !", Toast.LENGTH_SHORT).show();
            root.findViewById(R.id.progress).setVisibility(View.GONE);
        });
    }

    private void upload() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, LocalConstants.PERMISSION_CODE.SELECT_PICTURE);
        //mGetContent.launch("image/*");
    }

    private void student() {

        MAIN_PROFILE = root.findViewById(R.id.MAIN_PROFILE);

        nameProfile = root.findViewById(R.id.nameProfile);
        parentMobileNo = root.findViewById(R.id.parentMobileNo);
        classProfile = root.findViewById(R.id.classProfile);
        numberProfile = root.findViewById(R.id.numberProfile);

        root.findViewById(R.id.saveAndUpdateBtnprofile).setOnClickListener(v -> save());

        FirebaseFirestore.getInstance()
                .collection("students")
                .document(GlobalVariables.uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    name = documentSnapshot.getString("name");
                    number = documentSnapshot.getString("number");
                    parentNumber = documentSnapshot.getString("parentNo");
                    aClass = documentSnapshot.getString("myClass");
                    doWork();
                });

    }

    private void doWork() {
        nameProfile.setText(name);
        numberProfile.setText(number);
        parentMobileNo.setText(parentNumber);
        classProfile.setText(aClass);
    }


    private void deleteAcc() {
        String id = GlobalVariables.uid;
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.confirmation_dialog);

        ((TextView)dialog.findViewById(R.id.confirmationBtn)).setText("Delete");
        ((TextView)dialog.findViewById(R.id.confirmationTitle)).setText("Delete Account !");
        ((TextView)dialog.findViewById(R.id.confirmationText)).setText("Want to Delete your Account?");

        dialog.findViewById(R.id.cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.confirmationBtn).setOnClickListener(v -> {
            root.findViewById(R.id.progress).setVisibility(View.VISIBLE);
            dialog.dismiss();
            if (GlobalVariables.isStudent) {
                FirebaseFirestore.getInstance()
                        .collection("students")
                        .document(id)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                List<String> classes = (List) documentSnapshot.get("classes");
                                if (classes.size() != 0) {
                                    for (String className : classes) {
                                        FirebaseFirestore.getInstance()
                                                .collection("classroom")
                                                .document(className)
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        List<Map<String, Object>> currentStudents = (List) documentSnapshot.get("currentStudents");
                                                        if (currentStudents != null && currentStudents.size() != 0) {
                                                            for (int i = 0; i < currentStudents.size(); i++) {
                                                                if (currentStudents.get(i).get("id").toString().equals(id)) {
                                                                    currentStudents.remove(i);
                                                                    i--;
                                                                }
                                                            }

                                                        }
                                                        List<Map<String, Object>> assignments = (List) documentSnapshot.get("assignments");
                                                        List<Map<String, Object>> assignmentsFinal = new ArrayList<>();
                                                        if (assignments != null && assignments.size() != 0) {
                                                            for (int i = 0; i < assignments.size(); i++) {
                                                                if (((List<String>) assignments.get(i).get("submitted")).contains(id)) {
                                                                    ((List<String>) assignments.get(i).get("submitted")).remove(id);
                                                                }
                                                                assignmentsFinal.add(assignments.get(i));
                                                            }

                                                        }

                                                        List<Map<String, Object>> testCurrent = (List) documentSnapshot.get("test");

                                                        List<Map<String, Object>> testFinal = new ArrayList<>();
                                                        if (testCurrent != null && testCurrent.size() != 0) {
                                                            for (Map<String, Object> test : testCurrent) {
                                                                if (((List<String>) test.get("submitted")).contains(id)) {
                                                                    ((List<String>) test.get("submitted")).remove(id);
                                                                }
                                                                testFinal.add(test);
                                                            }
                                                        }

                                                        List<Map<String, Object>> pendingStudentsCurrentFinal = (List) documentSnapshot.get("pendingStudents");
                                                        if (pendingStudentsCurrentFinal != null && pendingStudentsCurrentFinal.size() != 0) {
                                                            for (int position = 0; position < pendingStudentsCurrentFinal.size(); position++) {
                                                                if (((String) pendingStudentsCurrentFinal.get(position).get("id")).equals(id)) {
                                                                    pendingStudentsCurrentFinal.remove(position);
                                                                    position--;
                                                                }
                                                            }
                                                        }

                                                        Map<String, Object> map = new HashMap<>();
                                                        Log.d("TAG", "onSuccess: " + testCurrent + " : " + testFinal);
                                                        if (testFinal.size() != testCurrent.size()) {
                                                            map.put("test", testFinal);
                                                        }
                                                        if (assignments.size() != assignmentsFinal.size()) {
                                                            map.put("assignments", assignmentsFinal);
                                                        }
                                                        map.put("pendingStudents", pendingStudentsCurrentFinal);
                                                        map.put("currentStudents", currentStudents);

                                                        FirebaseFirestore.getInstance()
                                                                .collection("classroom")
                                                                .document(className)
                                                                .update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toast.makeText(getContext(), "Done 1", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });

                                                        FirebaseFirestore.getInstance()
                                                                .collection("classroom")
                                                                .document(className)
                                                                .collection("assignmentSub")
                                                                .get()
                                                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                                                    if (queryDocumentSnapshots != null) {
                                                                        if (queryDocumentSnapshots.getDocuments() != null)
                                                                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                                                                FirebaseFirestore.getInstance()
                                                                                        .collection("classroom")
                                                                                        .document(className)
                                                                                        .collection("assignmentSub")
                                                                                        .document(document.getId())
                                                                                        .update(id, FieldValue.delete());
                                                                            }
                                                                    }

                                                                });

                                                        FirebaseFirestore.getInstance()
                                                                .collection("classroom")
                                                                .document(className)
                                                                .collection("testSub")
                                                                .get()
                                                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                                                    if (queryDocumentSnapshots != null) {
                                                                        if (queryDocumentSnapshots.getDocuments() != null)
                                                                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                                                                FirebaseFirestore.getInstance()
                                                                                        .collection("classroom")
                                                                                        .document(className)
                                                                                        .collection("testSub")
                                                                                        .document(document.getId())
                                                                                        .update(id, FieldValue.delete());
                                                                            }
                                                                    }

                                                                });

                                                    }
                                                });
                                    }
                                }

                                if (FirebaseAuth.getInstance().getCurrentUser() != null)
                                    FirebaseAuth.getInstance().getCurrentUser().delete().addOnSuccessListener(aVoid -> {
                                        FirebaseFirestore.getInstance()
                                                .collection("students")
                                                .document(id)
                                                .delete();
                                        logout();
                                    }).addOnFailureListener(e -> {
                                        FirebaseFirestore.getInstance()
                                                .collection("students")
                                                .document(id)
                                                .delete();
                                        logout();
                                    });
                                else
                                    FirebaseFirestore.getInstance()
                                            .collection("students")
                                            .document(id)
                                            .delete().addOnSuccessListener(aVoid -> logout()).addOnFailureListener(e -> Toast.makeText(context, "Network Issue!", Toast.LENGTH_SHORT).show());
                            }
                        });
            }else{
                root.findViewById(R.id.progress).setVisibility(View.VISIBLE);

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl("gs://eduethicsapp.appspot.com");    //change the url according to your firebase app
                FirebaseFirestore.getInstance()
                        .collection("teacher")
                        .document(GlobalVariables.uid)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                List<Map<String, Object>> classes = (List) documentSnapshot.get("classroom");
                                List<String> classrooms = new ArrayList<>();
                                for (Map<String, Object> object : classes) {
                                    classrooms.add(object.get("id").toString());
                                }
                                for (String ids : classrooms) {
                                    FirebaseFirestore.getInstance()
                                            .collection("classroom")
                                            .document(ids).get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    List<Map<String, Object>> studentsCur = (List) documentSnapshot.get("currentStudents");
                                                    List<Map<String, Object>> studentsPen = (List) documentSnapshot.get("pendingStudents");

                                                    for (Map<String, Object> o : studentsCur) {
                                                        FirebaseFirestore.getInstance()
                                                                .collection("students")
                                                                .document(o.get("id").toString())
                                                                .get()
                                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                        List<String> cls = (List) documentSnapshot.get("classes");
                                                                        for (int i = 0; i < cls.size(); i++) {
                                                                            if (cls.get(i).equals(ids)) {
                                                                                cls.remove(i);
                                                                                break;
                                                                            }
                                                                        }

                                                                        Map<String, Object> map = new HashMap<>();
                                                                        map.put("classes", cls);
                                                                        FirebaseFirestore.getInstance().collection("students").document(o.get("id").toString()).update(map);
                                                                    }
                                                                });
                                                    }

                                                    for (Map<String, Object> o : studentsPen) {
                                                        FirebaseFirestore.getInstance()
                                                                .collection("students")
                                                                .document(o.get("id").toString())
                                                                .get()
                                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                        List<String> cls = (List) documentSnapshot.get("classes");
                                                                        for (int i = 0; i < cls.size(); i++) {
                                                                            if (cls.get(i).equals(ids + "_p")) {
                                                                                cls.remove(i);
                                                                                break;
                                                                            }
                                                                        }

                                                                        Map<String, Object> map = new HashMap<>();
                                                                        map.put("classes", cls);
                                                                        FirebaseFirestore.getInstance().collection("students").document(o.get("id").toString()).update(map);
                                                                    }
                                                                });
                                                    }


                                                    FirebaseFirestore.getInstance()
                                                            .collection("classroom")
                                                            .document(ids).delete();
                                                    loopingFinishCheck();
                                                }
                                            });

                                    FirebaseFirestore.getInstance()
                                            .collection("fees")
                                            .document(ids).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            loopingFinishCheck();
                                        }
                                    });

                                    FirebaseFirestore.getInstance()
                                            .collection("fees")
                                            .document(ids)
                                            .collection("record")
                                            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                                document.getReference().delete();
                                            }

                                        }
                                    });

                                    FirebaseFirestore.getInstance()
                                            .collection("timetable")
                                            .document(ids).delete().addOnSuccessListener(aVoid -> loopingFinishCheck());
                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put(ids, FieldValue.delete());

                                    FirebaseFirestore.getInstance().collection("utilData").document("classroomIds").update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            loopingFinishCheck();
                                        }
                                    });

                                    storageRef.child("assignmentData").child(ids).delete();
                                    storageRef.child("testData").child(ids).delete();
                                    storageRef.child("chatData").child(ids).delete();
                                    storageRef.child("noticeData").child(ids).delete();
                                    storageRef.child("studyMaterial").child(ids).delete();
                                }
                                FirebaseFirestore.getInstance().collection("teacher").document(GlobalVariables.uid).delete().addOnSuccessListener(aVoid -> loopingFinishCheck());


                            }
                        });

            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();


    }




    int loopingCount = 0;

    private void loopingFinishCheck() {
        loopingCount++;
        if (loopingCount == 4) {
            if (FirebaseAuth.getInstance().getCurrentUser() != null)
                FirebaseAuth.getInstance().getCurrentUser().delete().addOnFailureListener(e -> logout()).addOnSuccessListener(aVoid -> logout());
            else {
                logout();
            }
        }
    }


    private void logout() {

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            getActivity().runOnUiThread(() -> {
                SharedPreferences prefs = getActivity().getApplicationContext().getSharedPreferences("USER_PREF",
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                GlobalVariables.isStudent = null;
                GlobalVariables.name = "";
                GlobalVariables.uid = "";
                editor.putString("type", "none");
                editor.clear();
                editor.apply();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getContext(), SpalashActivity.class));
            });
        }, 2000);
    }

    private void save() {
        if (!(nameProfile.getText().toString().equals(name) &&
                classProfile.getText().toString().equals(aClass) &&
                parentMobileNo.getText().toString().equals(parentNumber))) {
            Toast.makeText(context, "samenot", Toast.LENGTH_SHORT).show();
            Map<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("number", number);
            map.put("parentNo", parentMobileNo.getText().toString());
            map.put("class", aClass);
            map.put("subjects", subjects);
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(GlobalVariables.uid)
                    .update(map)
                    .addOnSuccessListener(aVoid -> Snackbar.make(MAIN_PROFILE, "Saved !", 2000))
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, e.getMessage() + "", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(context, "same", Toast.LENGTH_SHORT).show();
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LocalConstants.PERMISSION_CODE.SELECT_PICTURE) {
            if (data == null) return;
            Uri selectedImageUri = data.getData();
            if (null != selectedImageUri) uriImage = selectedImageUri;
        }

    }

    @Override
    public void onDestroyView() {
        MainActivity.toolbarImageFilter.setVisibility(View.GONE);
        super.onDestroyView();
    }
}