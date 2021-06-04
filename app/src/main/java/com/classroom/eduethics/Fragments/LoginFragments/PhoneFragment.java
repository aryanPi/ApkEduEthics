package com.classroom.eduethics.Fragments.LoginFragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.chaos.view.PinView;
import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.Fragments.TeacherFragments.CreateClassroomFragment;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Services.OTP_Receiver;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class PhoneFragment extends Fragment {

    private String verificationId;
    EditText mobileNoMainPage;

    int codeSentStatus = 0;

    ConstraintLayout teacher, student;
    TextView sendOtpBtn;
    Context context;

    String uid;


    PinView verificationCode;

    public PhoneFragment(Context context) {
        this.context = context;
    }

    View root;
    int TorS = LocalConstants.TYPE.STUDENT;


    private void recaptura() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_phone, container, false);

        initFirebaseAlso();

        return root;
    }

    private void initFirebaseAlso() {
        FirebaseApp.initializeApp(context);

        mobileNoMainPage = root.findViewById(R.id.mobileNoMainPage);
        verificationCode = root.findViewById(R.id.otp_view);


        //root.findViewById(R.id.progress).setVisibility(View.GONE);

        teacher = root.findViewById(R.id.teacher);
        student = root.findViewById(R.id.student);

        oneTime();
        oneTime();


        teacher.setOnClickListener(v -> {
            if (TorS != LocalConstants.TYPE.TEACHER) {
                ((TransitionDrawable) student.getBackground()).reverseTransition(200);
                ((TransitionDrawable) teacher.getBackground()).startTransition(200);
                TorS = LocalConstants.TYPE.TEACHER;
                TransitionManager.beginDelayedTransition(root.findViewById(R.id.MAIN_PHONE));
                root.findViewById(R.id.ofStudent).setVisibility(View.GONE);
            }
        });
        student.setOnClickListener(v -> {
            if (TorS != LocalConstants.TYPE.STUDENT) {
                ((TransitionDrawable) student.getBackground()).startTransition(200);
                ((TransitionDrawable) teacher.getBackground()).reverseTransition(200);
                TorS = LocalConstants.TYPE.STUDENT;
                TransitionManager.beginDelayedTransition(root.findViewById(R.id.MAIN_PHONE));
                root.findViewById(R.id.ofStudent).setVisibility(View.VISIBLE);
            }
        });

        new OTP_Receiver().setEditText(verificationCode);

        verificationCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 6) sendOtpBtn.callOnClick();
            }
        });

        sendOtpBtn = root.findViewById(R.id.nextA);
        sendOtpBtn.setOnClickListener(v -> {

            if (codeSentStatus == 1) {
                TransitionManager.beginDelayedTransition(root.findViewById(R.id.MAIN_PHONE));
                root.findViewById(R.id.progress).setVisibility(View.VISIBLE);

                String code = verificationCode.getText().toString().trim();
                if (code.isEmpty() || code.length() < 6) {
                    verificationCode.setError("Enter code...");
                    verificationCode.requestFocus();
                    return;
                }
                verifyCode(code);
            } else if (codeSentStatus == 0) {
                TransitionManager.beginDelayedTransition(root.findViewById(R.id.MAIN_PHONE));
                root.findViewById(R.id.progress).setVisibility(View.VISIBLE);
                requestSMSPermission();

                codeSentStatus = 1;
                sendVerificationCode("+91" + mobileNoMainPage.getText().toString());
            } else if (codeSentStatus == 2) {
                if (TorS == LocalConstants.TYPE.STUDENT) {
                    studentDone();
                } else {
                    if (((TextInputEditText) root.findViewById(R.id.nametxt)).getText().toString().equals("")) {
                        ((TextInputEditText) root.findViewById(R.id.nametxt)).setError("Name Required!");
                    } else {
                        CreateClassroomFragment fragment = new CreateClassroomFragment(getContext(), 1);
                        fragment.setDetails(uid, "+91" + mobileNoMainPage.getText().toString(), ((TextInputEditText) root.findViewById(R.id.nametxt)).getText().toString());
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrameLogin, fragment).commit();
                    }

                }
            }


        });


    }

    private void studentDone() {
        boolean allDone = true;

        if (((TextInputEditText) root.findViewById(R.id.className)).getText().toString().equals("")) {
            ((TextInputEditText) root.findViewById(R.id.className)).setError("Required!");
            allDone = false;
        }
        if (((TextInputEditText) root.findViewById(R.id.parentNo)).getText().toString().equals("")) {
            ((TextInputEditText) root.findViewById(R.id.parentNo)).setError("Required!");
            allDone = false;
        }
        if (((TextInputEditText) root.findViewById(R.id.nametxt)).getText().toString().equals("")) {
            ((TextInputEditText) root.findViewById(R.id.nametxt)).setError("Name Required!");
            allDone = false;
        }

        if (allDone) {
            TransitionManager.beginDelayedTransition(root.findViewById(R.id.MAIN_PHONE));
            root.findViewById(R.id.progress).setVisibility(View.VISIBLE);

            Map<String, Object> details = new HashMap<>();
            details.put("name", ((TextInputEditText) root.findViewById(R.id.nametxt)).getText().toString());
            details.put("about", "None");
            details.put("number", mobileNoMainPage.getText().toString().startsWith("+91") ? mobileNoMainPage.getText().toString() : "+91" + mobileNoMainPage.getText().toString());
            details.put("parentNo", ((TextInputEditText) root.findViewById(R.id.parentNo)).getText().toString());
            details.put("classes", new ArrayList<>());
            details.put("myClass", ((TextInputEditText) root.findViewById(R.id.className)).getText().toString());
            details.put("token", "");


            FirebaseFirestore.getInstance()
                    .collection("students")
                    .document(uid)
                    .set(details)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            SharedPreferences prefs = getActivity().getApplicationContext().getSharedPreferences("USER_PREF",
                                    Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("uid", uid);
                            editor.putString("type", "student");
                            editor.putString("name", ((TextInputEditText) root.findViewById(R.id.nametxt)).getText().toString());
                            editor.apply();

                            Intent intent = new Intent(getContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed !", Toast.LENGTH_SHORT).show());

        }

    }

    int count = 0;

    private void findDoRun(String type, String id, String name) {
        count++;
        if (type.equals("-")) {
            if (count == 2) {
                root.findViewById(R.id.progress).setVisibility(View.GONE);
                TransitionManager.beginDelayedTransition(root.findViewById(R.id.MAIN_PHONE));
                verificationCode.setVisibility(View.GONE);
                root.findViewById(R.id.titletxt).setVisibility(View.VISIBLE);
                root.findViewById(R.id.i2).setVisibility(View.VISIBLE);
                root.findViewById(R.id.im).setVisibility(View.GONE);

                root.findViewById(R.id.phonenumbertextll).setVisibility(View.GONE);
                mobileNoMainPage.setEnabled(false);
                root.findViewById(R.id.beforeOtpVerify).setVisibility(View.VISIBLE);
                root.findViewById(R.id.ofStudent).setVisibility(View.VISIBLE);
                ((TextView) root.findViewById(R.id.codeText)).setTextColor(ResourcesCompat.getColor(getResources(), R.color.hintColor, null));
                mobileNoMainPage.setTextColor(ResourcesCompat.getColor(getResources(), R.color.hintColor, null));
                codeSentStatus = 2;
                sendOtpBtn.setText("Continue");
            }
        } else {
            if (getActivity() != null) {
                SharedPreferences prefs = getActivity().getApplicationContext().getSharedPreferences("USER_PREF",
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("type", type);
                editor.putString("uid", id);
                editor.putString("name", name);
                editor.apply();

                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();

            }
        }
        Toast.makeText(context, "here", Toast.LENGTH_SHORT).show();

    }


    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);

    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        doTask(task);
                    } else {
                        root.findViewById(R.id.progress).setVisibility(View.GONE);
                        task.getException().printStackTrace();
                        verificationCode.setItemBackgroundColor(Color.parseColor("#74FF0000"));
                        Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
                        Log.d("TAG", "signInWithCredential: " + task.getException().getMessage());
                        Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void doTask(Task<AuthResult> task) {
        FirebaseFirestore.getInstance()
                .collection("teacher")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            if (document.getString("phNo") != null)
                                if (document.getString("phNo").equals("+91" + mobileNoMainPage.getText().toString())) {
                                    findDoRun("teacher", document.getId(), document.getString("name"));
                                    return;
                                }
                        }
                        findDoRun("-", "", "");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Log.d("TAG", "onFailure: " + e.getMessage());
                e.printStackTrace();
                Toast.makeText(context, "failed teacher", Toast.LENGTH_SHORT).show();
            }
        });

        FirebaseFirestore.getInstance()
                .collection("students")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            if (document.getString("number") != null)
                                if (document.getString("number").equals("+91" + mobileNoMainPage.getText().toString())) {
                                    findDoRun("student", document.getId(), document.getString("name"));
                                    return;
                                }
                        }
                        findDoRun("-", "", "");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Log.d("TAG", "onFailure: " + e.getMessage());
                e.printStackTrace();
                Toast.makeText(context, "failed students", Toast.LENGTH_SHORT).show();
            }
        });
        if (task == null) {
            uid = UUID.randomUUID().toString();
        } else {
            uid = task.getResult().getUser().getUid();
        }

        Snackbar.make(root.findViewById(R.id.MAIN_PHONE), "AUTHENTICATION COMPLETE", 2000).show();
    }

    private void sendVerificationCode(String number) {

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                        .setPhoneNumber(number)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(getActivity())                 // Activity (for callback binding)
                        .setCallbacks(mCallBack)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);


    }


    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {


        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
            TransitionManager.beginDelayedTransition(root.findViewById(R.id.MAIN_PHONE));
            sendOtpBtn.setText("Verify");
            root.findViewById(R.id.progress).setVisibility(View.GONE);
            root.findViewById(R.id.titletxt).setVisibility(View.GONE);
            root.findViewById(R.id.i2).setVisibility(View.GONE);

            verificationCode.setVisibility(View.VISIBLE);

        }


        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                verificationCode.setText(code);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            e.printStackTrace();
            Log.d("TAG", "onVerificationFailed: " + e.getLocalizedMessage());
            Log.d("TAG", "onVerificationFailed: " + e.getMessage());
            Log.d("TAG", "onVerificationFailed: " + e.getCause());
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }


        @Override
        public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
            super.onCodeAutoRetrievalTimeOut(s);
            Toast.makeText(context, "timeout", Toast.LENGTH_SHORT).show();
        }
    };

    private void oneTime() {
        ((TransitionDrawable) student.getBackground()).startTransition(1);
        ((TransitionDrawable) teacher.getBackground()).reverseTransition(1);
    }


    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void requestSMSPermission() {
        String permission = Manifest.permission.RECEIVE_SMS;

        int grant = ContextCompat.checkSelfPermission(getActivity(), permission);
        if (grant != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[1];
            permission_list[0] = permission;

            ActivityCompat.requestPermissions(getActivity(), permission_list, 1);
        }
    }


}
