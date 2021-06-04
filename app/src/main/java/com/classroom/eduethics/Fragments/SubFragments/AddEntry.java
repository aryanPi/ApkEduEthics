package com.classroom.eduethics.Fragments.SubFragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.ExtraFunctions;
import com.classroom.eduethics.Utils.GlobalVariables;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddEntry extends Fragment {

    View root;


    TextView dateText;
    int[] d = new int[3];

    String id, name;

    int type;
    boolean isEdit = false;
    Map<String, Object> data;

    public void isEditState(Map<String, Object> data) {
        this.data = data;
        this.isEdit = true;
    }

    public AddEntry(int type, String name, String id) {
        this.id = id;
        this.type = type;
        this.name = name;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_add_entry, container, false);

        MainActivity.TO_WHICH_FRAG = LocalConstants.TO_FRAG.TO_HOME_CLASSROOM;
        dateText = root.findViewById(R.id.dateText);

        Calendar dateTime = Calendar.getInstance();
        d[0] = dateTime.get(Calendar.DAY_OF_MONTH);
        d[1] = dateTime.get(Calendar.MONTH) + 1;
        d[2] = dateTime.get(Calendar.YEAR);

        ((TextView) root.findViewById(R.id.nameStudent)).setText(name);

        if (type == -1) {
            root.findViewById(R.id.modeTypeRadioGroup).setVisibility(View.GONE);
            ((TextInputLayout) root.findViewById(R.id.temp)).setHint("Amount Due");
        }

        if (GlobalVariables.isStudent)
            ((TextInputLayout) root.findViewById(R.id.temp)).setHint("Amount Paid");

        if (isEdit) {
            if (data.get("mode").toString().equals("Cash Mode")) {
                ((RadioButton) root.findViewById(R.id.cash)).setChecked(true);
            } else {
                ((RadioButton) root.findViewById(R.id.otherOnline)).setChecked(true);
            }
            ((TextInputEditText) root.findViewById(R.id.amountRec)).setText(Integer.parseInt(data.get("amt").toString()) < 0 ? Integer.parseInt(data.get("amt").toString()) * -1 + "" : data.get("amt").toString());
            ((TextInputEditText) root.findViewById(R.id.description)).setText(data.get("dec").toString());
            d[0] = Integer.parseInt(data.get("date").toString().split("-")[0]);
            d[1] = Integer.parseInt(data.get("date").toString().split("-")[1]);
            d[2] = Integer.parseInt(data.get("date").toString().split("-")[2]);
        }

        root.findViewById(R.id.saveCreate).setOnClickListener(v -> saveCreate());

        root.findViewById(R.id.date).setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                dateText.setText(ExtraFunctions.getReadableDate(dayOfMonth, month + 1, year));
                d[0] = dayOfMonth;
                d[1] = month + 1;
                d[2] = year;
            },
                    Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH),
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            dialog.getDatePicker().setMaxDate(new Date().getTime());
            dialog.show();
        });

        dateText.setText(ExtraFunctions.getReadableDate(d[0], d[1], d[2]));

        return root;
    }

    private void saveCreate() {
        if (((TextInputEditText) root.findViewById(R.id.amountRec)).getText().toString().equals("")) {
            ((TextInputEditText) root.findViewById(R.id.amountRec)).setError("Required !");
        } else if (Integer.parseInt(((TextInputEditText) root.findViewById(R.id.amountRec)).getText().toString()) < 0) {
            ((TextInputEditText) root.findViewById(R.id.amountRec)).setError("Should be in positive !");
        } else {
            root.findViewById(R.id.progress).setVisibility(View.VISIBLE);
            String amount = ((TextInputEditText) root.findViewById(R.id.amountRec)).getText().toString();
            String dec = ((TextInputEditText) root.findViewById(R.id.description)).getText().toString();
            String mode = type == 1 ? ((RadioGroup) root.findViewById(R.id.modeTypeRadioGroup)).getCheckedRadioButtonId() == R.id.cash ? "Cash Mode" : "Other Online Mode" : "";


            String date = d[0] + "-" + d[1] + "-" + d[2];
            String from = GlobalVariables.isStudent ? "s" : "t";
            long approval = GlobalVariables.isStudent ? -1 : 1;


            FirebaseFirestore.getInstance()
                    .collection("fees")
                    .document(HomeClassroom.classId)
                    .collection("record")
                    .document(id)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                            List<Map<String, Object>> dataRec = new ArrayList<>();
                            if ((List) documentSnapshot.get("data") != null) {
                                dataRec = (List) documentSnapshot.get("data");
                            }
                            if (isEdit) {
                                for (Map<String, Object> stringObjectMap : dataRec) {
                                    if (stringObjectMap.get("amt").toString().equals(data.get("amt").toString())
                                            &&
                                            stringObjectMap.get("approval").toString().equals(data.get("approval").toString())
                                            &&
                                            stringObjectMap.get("date").toString().equals(data.get("date").toString())
                                            &&
                                            stringObjectMap.get("dec").toString().equals(data.get("dec").toString())
                                            &&
                                            stringObjectMap.get("from").toString().equals(data.get("from").toString())
                                            &&
                                            stringObjectMap.get("mode").toString().equals(data.get("mode").toString())) {
                                        dataRec.remove(stringObjectMap);
                                        break;
                                    }
                                }
                            }

                            Map<String, Object> forUp = new HashMap<>();
                            if (type == -1)
                                forUp.put("amt", (Integer.parseInt(amount) * -1) + "");
                            else forUp.put("amt", amount);

                            forUp.put("dec", dec);
                            forUp.put("date", date);
                            forUp.put("mode", mode);
                            forUp.put("from", from);
                            forUp.put("approval", approval);

                            dataRec.add(forUp);


                            Map<String, Object> f = new HashMap<>();
                            f.put("data", dataRec);
                            f.put("system",documentSnapshot.get("system"));

                            FirebaseFirestore.getInstance()
                                    .collection("fees")
                                    .document(HomeClassroom.classId)
                                    .collection("record")
                                    .document(id)
                                    .set(f)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            editStudentFee(id, root, getActivity());
                                        }
                                    });
                        }
                    });
        }


    }


    public static void editStudentFee(String idT, View rootT, Activity activity) {

        final int[] advT = {0};
        final int[] recT = {0};

        FirebaseFirestore.getInstance()
                .collection("fees")
                .document(HomeClassroom.classId)
                .collection("record")
                .document(idT)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        List<Map<String, Object>> lst = (List) documentSnapshot.get("data");
                        for (Map<String, Object> objectMap : lst) {
                            if ((long) objectMap.get("approval") == 1) {
                                if (!objectMap.get("amt").toString().contains("-")) {
                                    recT[0] += Integer.parseInt(objectMap.get("amt").toString());
                                }
                                advT[0] += Integer.parseInt(objectMap.get("amt").toString());
                            }
                        }

                        FirebaseFirestore.getInstance()
                                .collection("fees")
                                .document(HomeClassroom.classId)
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        List<Map<String, Object>> dataLocal = (List) documentSnapshot.get("students");
                                        for (Map<String, Object> singleData : dataLocal) {
                                            if (singleData.get("id").toString().equals(idT)) {
                                                dataLocal.get(dataLocal.indexOf(singleData)).put("adv", (advT[0] - Integer.parseInt(singleData.get("totalFee").toString())) + "");
                                                dataLocal.get(dataLocal.indexOf(singleData)).put("rec", recT[0] + "");
                                                break;
                                            }
                                        }
                                        FirebaseFirestore.getInstance().collection("teacher")
                                                .document(documentSnapshot.getString("teacherId"))
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                    }
                                                });

                                        Map<String, Object> forUp = new HashMap<>();
                                        forUp.put("students", dataLocal);
                                        FirebaseFirestore.getInstance()
                                                .collection("fees")
                                                .document(HomeClassroom.classId)
                                                .update(forUp).addOnSuccessListener(aVoid -> {


                                            rootT.findViewById(R.id.progress).setVisibility(View.GONE);
                                            activity.onBackPressed();
                                        }).addOnFailureListener(e -> rootT.findViewById(R.id.progress).setVisibility(View.GONE));
                                    }
                                });
                    }
                });
    }
}