package com.classroom.eduethics.Fragments.TeacherFragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.Fragments.SubFragments.AddEntry;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.GlobalVariables;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeachersFeeStudentView extends Fragment {

    View root;

    Map<String, Object> data;

    public TeachersFeeStudentView(Map<String, Object> data) {
        this.data = data;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_teachers_fee_student_view, container, false);
        MainActivity.TO_WHICH_FRAG = LocalConstants.TO_FRAG.TO_HOME_CLASSROOM;

        if (GlobalVariables.isStudent) {
            student();
        } else {
            teacher();
        }

        return root;
    }

    private void student() {
        root.findViewById(R.id.teacherView).setVisibility(View.GONE);
        root.findViewById(R.id.studentView).setVisibility(View.VISIBLE);
        ((TextView) root.findViewById(R.id.addRec)).setText("Add Amount Paid");
        ((TextView) root.findViewById(R.id.addDue)).setText("Pay Online");
        ((TextView) root.findViewById(R.id.addDue)).setTextColor(ResourcesCompat.getColor(getResources(), R.color.mainCustomPrimary, null));

        root.findViewById(R.id.addDue).setOnClickListener(v -> payOnline());

        root.findViewById(R.id.addRec).setOnClickListener(v -> amountPaidS(null));


        RecyclerView recyclerFeeStudent = root.findViewById(R.id.recyclerFeeStudent);
        recyclerFeeStudent.setLayoutManager(new LinearLayoutManager(getContext()));
        FirebaseFirestore.getInstance()
                .collection("fees")
                .document(HomeClassroom.classId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        FirebaseFirestore.getInstance()
                                .collection("teacher")
                                .document(documentSnapshot.getString("teacherId")).get()
                                .addOnSuccessListener(documentSnapshot1 -> ((TextView) root.findViewById(R.id.teacherS)).setText(documentSnapshot1.get("name").toString()));


                        List<Map<String, Object>> dataRec = (List) documentSnapshot.get("students");
                        for (Map<String, Object> objectMap : dataRec) {
                            if (objectMap.get("id").toString().equals(GlobalVariables.uid)) {
                                totalFee = objectMap.get("totalFee").toString();

                                FirebaseFirestore.getInstance()
                                        .collection("fees")
                                        .document(HomeClassroom.classId)
                                        .collection("record")
                                        .document(GlobalVariables.uid)
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if (documentSnapshot != null) {
                                                    if (documentSnapshot.get("data") != null)
                                                        if (((List) documentSnapshot.get("data")).size() != 0) {
                                                            Toast.makeText(getContext(), "gg", Toast.LENGTH_SHORT).show();
                                                            recyclerFeeStudent.setAdapter(new feeAdapter((List) documentSnapshot.get("data"), TeachersFeeStudentView.this, (TextView) root.findViewById(R.id.totalRec), (TextView) root.findViewById(R.id.totalDue), totalFee));
                                                        } else
                                                            Toast.makeText(getContext(), "emp 1", Toast.LENGTH_SHORT).show();
                                                    else
                                                        Toast.makeText(getContext(), "emoty 2", Toast.LENGTH_SHORT).show();
                                                    ((TextView) root.findViewById(R.id.studentFeeS)).setText(getString(R.string.Rs) + totalFee);
                                                    ((TextView) root.findViewById(R.id.subjectClassS)).setText(HomeClassroom.model.getSubject() + " (" + HomeClassroom.model.getClassName() + ")");
                                                }
                                            }
                                        });
                                break;
                            }
                        }
                    }
                });

    }

    private void payOnline() {
        Toast.makeText(getContext(), "Pay Online Functions", Toast.LENGTH_SHORT).show();
    }

    private void amountPaidS(Map<String, Object> isEdit) {
        AddEntry addEntry = new AddEntry(1, HomeClassroom.model.getSubject() + " (" + HomeClassroom.model.getClassName() + ")", GlobalVariables.uid);
        if (isEdit != null) addEntry.isEditState(isEdit);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, addEntry).commit();
    }

    private void teacher() {
        root.findViewById(R.id.addRec).setOnClickListener(v -> addRec());
        root.findViewById(R.id.addDue).setOnClickListener(v -> addDue());


        root.findViewById(R.id.temp1).setOnClickListener(v -> upload());

        RecyclerView recyclerFeeStudent = root.findViewById(R.id.recyclerFeeStudent);
        recyclerFeeStudent.setLayoutManager(new LinearLayoutManager(getContext()));
        FirebaseFirestore.getInstance()
                .collection("fees")
                .document(HomeClassroom.classId)
                .collection("record")
                .document(data.get("id").toString())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot != null) {
                            if (documentSnapshot.get("data") != null)
                                if (((List) documentSnapshot.get("data")).size() != 0) {
                                    Toast.makeText(getContext(), "ff", Toast.LENGTH_SHORT).show();
                                    recyclerFeeStudent.setAdapter(new feeAdapter((List) documentSnapshot.get("data"), TeachersFeeStudentView.this, (TextView) root.findViewById(R.id.totalRec), (TextView) root.findViewById(R.id.totalDue), data.get("totalFee").toString()));
                                }

                            ((TextView) root.findViewById(R.id.studentFee)).setText(getString(R.string.Rs) + data.get("totalFee").toString());
                        }
                    }
                });
    }

    String totalFee = "";

    private void addDue() {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, new AddEntry(-1, data.get("name").toString(), data.get("id").toString())).commit();
    }

    private void addRec() {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, new AddEntry(1, data.get("name").toString(), data.get("id").toString())).commit();
    }

    static class feeAdapter extends RecyclerView.Adapter<feeAdapter.vh> {

        List<Map<String, Object>> dataLocal;
        Fragment fragment;
        TextView recTotal, totalAdv;

        String totalFee;
        int totalREC = 0, totalADV = 0;


        public feeAdapter(List<Map<String, Object>> dataLocal, Fragment fragment, TextView recTotal, TextView totalAdv, String totalFee) {
            this.dataLocal = dataLocal;
            this.fragment = fragment;
            this.recTotal = recTotal;
            this.totalAdv = totalAdv;
            this.totalFee = totalFee;
        }

        @NonNull
        @Override
        public vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new vh(LayoutInflater.from(parent.getContext()).inflate(R.layout.single_item_single_fee_view, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull vh holder, int position) {

            ((TextView) holder.itemView.findViewById(R.id.amount)).setText(fragment.getString(R.string.Rs) + (dataLocal.get(position).get("amt").toString().contains("-") ? dataLocal.get(position).get("amt").toString().substring(1) : dataLocal.get(position).get("amt").toString()));
            ((TextView) holder.itemView.findViewById(R.id.decSingleItem)).setText(dataLocal.get(position).get("dec").toString());
            ((TextView) holder.itemView.findViewById(R.id.dateSingleItem)).setText(dataLocal.get(position).get("date").toString().split("-")[0] + "\n" + (LocalConstants.MONTHS[Integer.parseInt(dataLocal.get(position).get("date").toString().split("-")[1]) - 1]).substring(0, 3));

            if ((long) dataLocal.get(position).get("approval") == 3) {
                ((TextView) holder.itemView.findViewById(R.id.fromStatus)).setText("System Added");
                ((TextView) holder.itemView.findViewById(R.id.modeOfPayment)).setText("");
                ((TextView) holder.itemView.findViewById(R.id.statusPayment)).setText("Due");
                ((TextView) holder.itemView.findViewById(R.id.statusPayment)).setText((long) dataLocal.get(position).get("approval") == 3 ? "Due" : "Paid");
            } else {
                ((TextView) holder.itemView.findViewById(R.id.fromStatus)).setText(dataLocal.get(position).get("from").toString().equals("t") ? GlobalVariables.isStudent ? "Teacher Added" : "You Added" : GlobalVariables.isStudent ? "You Added" : "Student Added");
                ((TextView) holder.itemView.findViewById(R.id.modeOfPayment)).setText(dataLocal.get(position).get("mode").toString());
                long approval = ((long) dataLocal.get(position).get("approval"));

                if (dataLocal.get(position).get("amt").toString().contains("-")) {
                    ((TextView) holder.itemView.findViewById(R.id.statusPayment)).setText(approval == 1 ? "Due" : approval == -2 ? "Rejected" : "Pending");
                } else {
                    ((TextView) holder.itemView.findViewById(R.id.statusPayment)).setText(approval == 1 ? "Received" : approval == -2 ? "Rejected" : "Pending");
                    if (approval == 1)
                        totalREC += Integer.parseInt(dataLocal.get(position).get("amt").toString());
                }
                if (approval == 1)
                    totalADV += Integer.parseInt(dataLocal.get(position).get("amt").toString());


                if (((TextView) holder.itemView.findViewById(R.id.statusPayment)).getText().toString().equals("Rejected")) {
                    ((TextView) holder.itemView.findViewById(R.id.amount)).setTextColor(ResourcesCompat.getColor(fragment.getResources(), R.color.secondary_text, null));
                    ((TextView) holder.itemView.findViewById(R.id.statusPayment)).setTextColor(ResourcesCompat.getColor(fragment.getResources(), android.R.color.holo_red_dark, null));
                }


                holder.itemView.setOnClickListener(v -> ((TeachersFeeStudentView) fragment).itemclick(dataLocal.get(position)));
            }

            ((TextView) holder.itemView.findViewById(R.id.amount)).setTextColor(ResourcesCompat.getColor(fragment.getResources(), Integer.parseInt(dataLocal.get(position).get("amt").toString()) < 0 ? android.R.color.holo_red_light : android.R.color.holo_green_light, null));

            if (position == dataLocal.size() - 1) {
                totalAdv.setText(fragment.getString(R.string.Rs) + (((totalADV - Integer.parseInt(totalFee))) < 0 ? (totalADV - Integer.parseInt(totalFee)) * -1 : (totalADV - Integer.parseInt(totalFee))));
                recTotal.setText(fragment.getString(R.string.Rs) + totalREC);
                totalAdv.setTextColor(ResourcesCompat.getColor(fragment.getResources(), (totalADV - Integer.parseInt(totalFee)) < 0 ? android.R.color.holo_red_light : android.R.color.holo_green_light, null));
            }

        }

        @Override
        public int getItemCount() {
            return dataLocal.size();
        }

        static class vh extends RecyclerView.ViewHolder {

            public vh(@NonNull View itemView) {
                super(itemView);

            }
        }

    }

    int type = 0;


    private void itemclick(Map<String, Object> dataLocal) {
        Dialog dialog;
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_fee_details);
        long approval = ((long) dataLocal.get("approval"));

        if (approval == 1) {
            dialog.findViewById(R.id.onPendingLayout).setVisibility(View.GONE);
            if (GlobalVariables.isStudent) {
                dialog.findViewById(R.id.editTransactionBtn).setVisibility(View.GONE);
            } else {
                dialog.findViewById(R.id.editTransactionBtn).setVisibility(View.VISIBLE);
                dialog.findViewById(R.id.editTransactionBtn).setOnClickListener(v -> {
                    AddEntry addEntry = new AddEntry(type, data.get("name").toString(), data.get("id").toString());
                    addEntry.isEditState(dataLocal);
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFrame, addEntry).commit();
                    dialog.dismiss();
                });
            }

        } else if (approval == -1) {
            if (GlobalVariables.isStudent) {
                dialog.findViewById(R.id.editTransactionBtn).setVisibility(View.VISIBLE);
                dialog.findViewById(R.id.onPendingLayout).setVisibility(View.GONE);
                dialog.findViewById(R.id.editTransactionBtn).setOnClickListener(v -> amountPaidS(dataLocal));
            } else {
                dialog.findViewById(R.id.editTransactionBtn).setVisibility(View.GONE);
                dialog.findViewById(R.id.onPendingLayout).setVisibility(View.VISIBLE);
                dialog.findViewById(R.id.approvedBtn).setOnClickListener(v -> {
                    approve(1, dataLocal);
                    dialog.dismiss();
                });
                dialog.findViewById(R.id.notRecBtn).setOnClickListener(v -> {
                    approve(-2, dataLocal);
                    dialog.dismiss();
                });
            }

        } else if (approval == -2) {
            dialog.findViewById(R.id.onPendingLayout).setVisibility(View.GONE);
            dialog.findViewById(R.id.editTransactionBtn).setVisibility(View.GONE);
        }

        TextView headlingDialogList = dialog.findViewById(R.id.headlingDialogList);
        if (dataLocal.get("from").toString().equals("t")) {
            if (GlobalVariables.isStudent) {
                headlingDialogList.setText("Teacher Added");
            } else {
                headlingDialogList.setText("You Added");
            }
        } else {
            if (GlobalVariables.isStudent) {
                headlingDialogList.setText("You Added");
            } else {
                headlingDialogList.setText("Student Added");
            }
        }

        if (!headlingDialogList.getText().toString().equals("You Added")) {
            dialog.findViewById(R.id.editTransactionBtn).setVisibility(View.GONE);
        }

        if (dataLocal.get("amt").toString().contains("-")) {
            ((TextView) dialog.findViewById(R.id.amountText)).setText(getString(R.string.Rs) + dataLocal.get("amt").toString().substring(1));
            ((TextView) dialog.findViewById(R.id.transactionStatus)).setText(approval == 1 ? "Due" : approval == -2 ? "Rejected" : "Pending");
            if (approval == 1) type = -1;
        } else {
            ((TextView) dialog.findViewById(R.id.amountText)).setText(getString(R.string.Rs) + dataLocal.get("amt").toString());
            ((TextView) dialog.findViewById(R.id.transactionStatus)).setText(approval == 1 ? "Received" : approval == -2 ? "Rejected" : "Pending");
            if (approval == 1) type = 1;
        }

        if (approval == -2) {
            ((TextView) dialog.findViewById(R.id.amountText)).setTextColor(ResourcesCompat.getColor(getResources(), R.color.secondary_text, null));
            ((TextView) dialog.findViewById(R.id.transactionStatus)).setTextColor(ResourcesCompat.getColor(getResources(), android.R.color.holo_red_dark, null));
        }


        if (dataLocal.get("mode").toString().equals("-")) {
            dialog.findViewById(R.id.modeLayout).setVisibility(View.GONE);
        } else {
            ((TextView) dialog.findViewById(R.id.modeText)).setText(dataLocal.get("mode").toString());
        }

        if (dataLocal.get("dec").toString().trim().equals("")) {
            dialog.findViewById(R.id.descriptionLayout).setVisibility(View.GONE);
        } else {
            ((TextView) dialog.findViewById(R.id.descriptionText)).setText(dataLocal.get("dec").toString());
        }
        ((TextView) dialog.findViewById(R.id.dateText)).setText(dataLocal.get("date").toString());
        ((TextView) dialog.findViewById(R.id.amountText)).setTextColor(ResourcesCompat.getColor(getResources(), Integer.parseInt(dataLocal.get("amt").toString()) < 0 ? android.R.color.holo_red_light : android.R.color.holo_green_light, null));

        //  dialog.findViewById(R.id.cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void approve(int state, Map<String, Object> dataLocal) {
        root.findViewById(R.id.progress).setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance()
                .collection("fees")
                .document(HomeClassroom.classId)
                .collection("record")
                .document(data.get("id").toString())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        List<Map<String, Object>> dataRec = new ArrayList<>();
                        if ((List) documentSnapshot.get("data") != null) {
                            dataRec = (List) documentSnapshot.get("data");
                        }

                        for (Map<String, Object> stringObjectMap : dataRec) {
                            if (stringObjectMap.get("amt").toString().equals(dataLocal.get("amt").toString())
                                    &&
                                    stringObjectMap.get("approval").toString().equals(dataLocal.get("approval").toString())
                                    &&
                                    stringObjectMap.get("date").toString().equals(dataLocal.get("date").toString())
                                    &&
                                    stringObjectMap.get("dec").toString().equals(dataLocal.get("dec").toString())
                                    &&
                                    stringObjectMap.get("from").toString().equals(dataLocal.get("from").toString())
                                    &&
                                    stringObjectMap.get("mode").toString().equals(dataLocal.get("mode").toString())) {
                                dataRec.get(dataRec.indexOf(stringObjectMap)).put("approval", state);

                                Map<String, Object> f = new HashMap<>();


                                f.put("data", dataRec);
                                f.put("system", documentSnapshot.get("system"));
                                FirebaseFirestore.getInstance()
                                        .collection("fees")
                                        .document(HomeClassroom.classId)
                                        .collection("record")
                                        .document(data.get("id").toString())
                                        .set(f)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                AddEntry.editStudentFee(data.get("id").toString(), root, getActivity());
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        root.findViewById(R.id.progress).setVisibility(View.VISIBLE);
                                        Toast.makeText(getContext(), "Network Issue!", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                break;
                            }
                        }


                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                root.findViewById(R.id.progress).setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network Issue!", Toast.LENGTH_SHORT).show();

            }
        });
    }


    private void upload() {
        Dialog dialog;

        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.edit_student_fee_dialog);


        EditText edittext = dialog.findViewById(R.id.studentFeeDialogText);

        edittext.postDelayed(() -> {
            edittext.requestFocus();
            ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(edittext, InputMethodManager.SHOW_IMPLICIT);
        }, 200);


        dialog.findViewById(R.id.post).setOnClickListener(v -> {
            root.findViewById(R.id.progress).setVisibility(View.VISIBLE);
            editStudentFee(edittext.getText().toString());
            dialog.dismiss();
        });
        dialog.findViewById(R.id.cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void editStudentFee(String newFee) {
        FirebaseFirestore.getInstance()
                .collection("fees")
                .document(HomeClassroom.classId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        List<Map<String, Object>> dataLocal = (List) documentSnapshot.get("students");
                        for (Map<String, Object> singleData : dataLocal) {
                            if (singleData.get("id").toString().equals(data.get("id").toString())) {
                                dataLocal.get(dataLocal.indexOf(singleData)).put("totalFee", newFee);
                                break;
                            }
                        }
                        Map<String, Object> forUp = new HashMap<>();
                        forUp.put("students", dataLocal);
                        FirebaseFirestore.getInstance()
                                .collection("fees")
                                .document(HomeClassroom.classId)
                                .update(forUp).addOnSuccessListener(aVoid -> {
                            AddEntry.editStudentFee(data.get("id").toString(), root, getActivity());
                        }).addOnFailureListener(e -> root.findViewById(R.id.progress).setVisibility(View.GONE));

                    }
                });
    }

}

