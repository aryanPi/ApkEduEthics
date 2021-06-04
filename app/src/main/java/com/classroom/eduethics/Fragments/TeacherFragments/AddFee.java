package com.classroom.eduethics.Fragments.TeacherFragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.classroom.eduethics.Activity.MainActivity;
import com.classroom.eduethics.Fragments.HomeClassroom;
import com.classroom.eduethics.Models.FeeDetailsCollectionModel;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.ExtraFunctions;
import com.classroom.eduethics.Utils.GlobalVariables;
import com.classroom.eduethics.Utils.LocalConstants;
import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AddFee extends Fragment {


    View root;
    TextView fromDate, fromMonth, fromYear, toDate, toMonth, toYear;
    TextInputLayout feeLayout;
    TextInputEditText feeText;
    TextView dateReminder1, dateReminder2;
    Calendar calendar = Calendar.getInstance();
    View selectedMonthView = null;
    int selectedMonthFrom = calendar.get(Calendar.MONTH) + 1;
    int selectedMonthTo = calendar.get(Calendar.MONTH) + 2;
    int selectedYearTo = Calendar.getInstance().get(Calendar.YEAR);
    String selectedDate = "1";
    TextView[] dates = null;


    RadioGroup radioGroup;
    RadioButton month, course;

    Context context;


    public AddFee(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_add_fee, container, false);
        MainActivity.TO_WHICH_FRAG = LocalConstants.TO_FRAG.TO_HOME_CLASSROOM;
        fromDate = root.findViewById(R.id.fromDate);
        fromMonth = root.findViewById(R.id.fromMonth);
        fromYear = root.findViewById(R.id.fromYear);
        toDate = root.findViewById(R.id.toDate);
        toMonth = root.findViewById(R.id.toMonth);
        toYear = root.findViewById(R.id.toYear);
        dateReminder1 = root.findViewById(R.id.dateRem1);
        dateReminder2 = root.findViewById(R.id.dateRem2);
        fromDate.setOnClickListener(v -> selectDate());
        toDate.setOnClickListener(v -> selectDate());
        fromMonth.setOnClickListener(v -> selectMonth(1, calendar.get(Calendar.MONTH) ));
        toMonth.setOnClickListener(v -> selectMonth(2, selectedMonthFrom));
        fromYear.setOnClickListener(v -> selectYear(1));
        toYear.setOnClickListener(v -> selectYear(2));
        fromMonth.setText(LocalConstants.MONTHS[selectedMonthFrom].substring(0, 3));
        toMonth.setText(LocalConstants.MONTHS[selectedMonthTo]);

        feeLayout = root.findViewById(R.id.tempf3);
        feeText = root.findViewById(R.id.feeText);

        month = root.findViewById(R.id.monthlyFee);
        course = root.findViewById(R.id.courseFee);
        radioGroup = root.findViewById(R.id.feeTypeRadioGroup);

        course.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                root.findViewById(R.id.tt3).setVisibility(View.GONE);
                dateReminder2.setVisibility(View.GONE);
                dateReminder1.setVisibility(View.GONE);
                feeLayout.setHint("Course Fee");
            }
        });
        month.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                root.findViewById(R.id.tt3).setVisibility(View.VISIBLE);
                dateReminder2.setVisibility(View.VISIBLE);
                dateReminder1.setVisibility(View.VISIBLE);
                feeLayout.setHint("Classroom Fee (per month)");
            }
        });

        month.setChecked(true);


        root.findViewById(R.id.createFee).setOnClickListener(v -> createFee());

        return root;
    }

    private void createFee() {
        boolean allDone = true;
        if (fromYear.getText().toString().equals(toYear.getText().toString())) {
            int fm = -1, tm = -1;
            for (int i = 0; i < LocalConstants.MONTHS.length; i++) {
                if (LocalConstants.MONTHS[i].substring(0, 3).equals(fromMonth.getText().toString())) {
                    fm = i;
                    break;
                }
            }

            if (feeText.getText().toString().equals("")) {
                feeText.setError("Required!");
                allDone = false;
            }
            if (allDone) {
                root.findViewById(R.id.progress).setVisibility(View.VISIBLE);


                List<Map<String, Object>> students = (List) HomeClassroom.model.getCurrentStudents();
                List<String> ids = new ArrayList<>();
                List<String> names = new ArrayList<>();
                for (Map<String, Object> ob : students) {
                    ids.add(ob.get("id").toString());
                    names.add(ob.get("name").toString());
                }

                List<Map<String,Object>> studentsList = new ArrayList<>();
                for (String id : ids) {
                    Map<String, Object> s = new HashMap<>();
                    s.put("adv", (Integer.parseInt(feeText.getText().toString()) * -1) + "");
                    s.put("id", id);
                    s.put("name", names.get(ids.indexOf(id)));
                    s.put("rec", "0");
                    s.put("totalFee", feeText.getText().toString());
                    studentsList.add(s);
                }

                FirebaseFirestore.getInstance()
                        .collection("fees")
                        .document(HomeClassroom.classId)
                        .set(new FeeDetailsCollectionModel("0", studentsList, (HomeClassroom.model.getCurrentStudents().size() * Integer.parseInt(feeText.getText().toString())) + "", "0", fromDate.getText().toString() + "-" + fm + "-" + fromYear.getText().toString(), feeText.getText().toString(), GlobalVariables.uid,""))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                FirebaseFirestore.getInstance().collection("classroom")
                                        .document(HomeClassroom.classId)
                                        .update("fee", feeText.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        getActivity().onBackPressed();
                                        root.findViewById(R.id.progress).setVisibility(View.VISIBLE);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        root.findViewById(R.id.progress).setVisibility(View.GONE);
                                    }
                                });

                                for (String id : ids) {

                                    Map<String,Object> map = new HashMap<>();
                                    List<Map<String,Object>> singleData = new ArrayList<>();
                                    Map<String,Object> studentData = new HashMap<>();
                                    studentData.put("amt",(Integer.parseInt(feeText.getText().toString())*-1)+"");
                                    studentData.put("approval",3);
                                    studentData.put("dec","System Added");
                                    studentData.put("from","sys");
                                    studentData.put("mode","System");
                                    studentData.put("date",calendar.get(Calendar.DAY_OF_MONTH)+"-"+calendar.get(Calendar.MONTH)+"-"+calendar.get(Calendar.YEAR));
                                    singleData.add(studentData);
                                    map.put("system",singleData);
                                    FirebaseFirestore.getInstance()
                                            .collection("fees")
                                            .document(HomeClassroom.classId)
                                            .collection("record")
                                            .document(id)
                                            .set(map);
                                }
                                if (ids.size()>0){
                                    ExtraFunctions.sendNoti(ids,"Fee Added by Teacher",HomeClassroom.model.getClassName()+" Fee structure is added.",getContext());
                                }


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        root.findViewById(R.id.progress).setVisibility(View.GONE);
                    }
                });

            }
        }
    }


    private void selectMonth(int type, int min) {
        Dialog dialog;
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_select_date);

        dialog.findViewById(R.id.dateTable).setVisibility(View.GONE);

        ListView listView = dialog.findViewById(R.id.monthsList);
        listView.setVisibility(View.VISIBLE);

        List<String> lst = new ArrayList<>();
        if (type == 1) {
            lst.add(LocalConstants.MONTHS[min]);
            lst.add(LocalConstants.MONTHS[min + 1]);

        } else if (Integer.parseInt(toYear.getText().toString()) == calendar.get(Calendar.YEAR) + 1) {
            lst.addAll(Arrays.asList(LocalConstants.MONTHS).subList(0, 11));
        } else
            lst.addAll(Arrays.asList(LocalConstants.MONTHS).subList(selectedMonthFrom + 1, selectedMonthFrom + 1 > 10 ? LocalConstants.MONTHS.length - 1 : 12));


        listView.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, lst));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3) {
                if (selectedMonthView != null)
                    selectedMonthView.setBackground(null);
                v.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.selected_month, null));
                selectedMonthView = v;
                if (type == 1) {
                    selectedMonthFrom = position + min;
                } else
                    selectedMonthTo = position + (Integer.parseInt(toYear.getText().toString()) == calendar.get(Calendar.YEAR) + 1 ? 0 : selectedMonthFrom + 1);

            }
        });

        dialog.findViewById(R.id.select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == 2) {
                    toMonth.setText(LocalConstants.MONTHS[selectedMonthTo].substring(0, 3));
                } else {
                    fromMonth.setText(LocalConstants.MONTHS[selectedMonthFrom].substring(0, 3));
                    if (selectedMonthTo <= selectedMonthFrom && Integer.parseInt(toYear.getText().toString()) == calendar.get(Calendar.YEAR)) {
                        selectedMonthTo = selectedMonthFrom + 1;
                        toMonth.setText(LocalConstants.MONTHS[selectedMonthTo].substring(0, 3));
                    }
                }
                nextRem();
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void nextRem() {
        dateReminder1.setText(selectedDate + " " + LocalConstants.MONTHS[selectedMonthFrom + 1] + " " + fromYear.getText().toString());
        dateReminder2.setText(selectedDate + " " + LocalConstants.MONTHS[selectedMonthFrom + 2] + " " + fromYear.getText().toString());
    }

    private void selectYear(int type) {
        Dialog dialog;
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_select_date);

        dialog.findViewById(R.id.dateTable).setVisibility(View.GONE);

        ListView listView = dialog.findViewById(R.id.monthsList);
        listView.setVisibility(View.VISIBLE);

        List<String> lst = new ArrayList<>();
        lst.add(calendar.get(Calendar.YEAR) + "");
        if (type == 2)
            lst.add(calendar.get(Calendar.YEAR) + 1 + "");

        listView.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, lst));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3) {
                if (selectedMonthView != null)
                    selectedMonthView.setBackground(null);
                v.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.selected_month, null));
                selectedMonthView = v;
                selectedYearTo = position;
            }
        });

        dialog.findViewById(R.id.select).setOnClickListener(v -> {
            if (type == 2) {
                if (selectedYearTo == 0 && selectedMonthTo < calendar.get(Calendar.MONTH)) {
                    Toast.makeText(context, "YES", Toast.LENGTH_SHORT).show();
                    toMonth.setText(LocalConstants.MONTHS[selectedMonthFrom + 1]);
                } else {
                    Toast.makeText(context, selectedMonthTo + " " + calendar.get(Calendar.MONTH), Toast.LENGTH_SHORT).show();

                }
                toYear.setText((calendar.get(Calendar.YEAR) + selectedYearTo) + "");
            } else fromYear.setText((calendar.get(Calendar.YEAR)) + "");
            nextRem();
            dialog.dismiss();
        });
        dialog.findViewById(R.id.cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void selectDate() {
        dates = new TextView[28];
        Dialog dialog;
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_select_date);

        TableLayout tableLayout = dialog.findViewById(R.id.dateTable);

        int c = 1;
        for (int k = 1; k <= 4; k++) {
            TableRow tableRowHead = new TableRow(context);
            tableRowHead.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            for (int i = 0; i < 7; i++) {
                TextView textView = new TextView(context);
                TableRow.LayoutParams params = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.weight = 1;
                params.setMargins(8, 8, 8, 8);
                textView.setLayoutParams(params);
                textView.setText((c++) + "");
                textView.setTextColor(getResources().getColor(R.color.darkgrey));
                textView.setBackgroundColor(Color.TRANSPARENT);
                textView.setTextSize(18);
                textView.setOnClickListener(v -> {
                    for (TextView date : dates)
                        date.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.circler_unselected_date, null));
                    v.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.circle_selected_date, null));
                    selectedDate = ((TextView) v).getText().toString();
                });
                if (selectedDate.equals(textView.getText().toString()))
                    textView.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.circle_selected_date, null));
                else
                    textView.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.circler_unselected_date, null));
                textView.setPadding(6, 6, 6, 6);
                textView.setGravity(Gravity.CENTER);
                dates[c - 2] = textView;
                tableRowHead.addView(textView);
            }
            tableLayout.addView(tableRowHead);
        }


        dialog.findViewById(R.id.select).setOnClickListener(v -> {
            fromDate.setText(selectedDate);
            toDate.setText(selectedDate);
            dates = null;
            nextRem();
            dialog.dismiss();

        });
        dialog.findViewById(R.id.cancel).setOnClickListener(v -> {
            dialog.dismiss();
            dates = null;
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

}