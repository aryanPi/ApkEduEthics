package com.classroom.eduethics.Adapter;

import android.app.TimePickerDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;


import com.classroom.eduethics.Fragments.TeacherFragments.TimeTable;
import com.classroom.eduethics.R;
import com.classroom.eduethics.Utils.ExtraFunctions;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class TimeTableAdapter extends RecyclerView.Adapter<TimeTableAdapter.vh> {

    List<Map<String, Object>> data;
    Fragment fragment;

    Calendar dateTime = Calendar.getInstance();
    int[] t = new int[4];

    public TimeTableAdapter(List<Map<String, Object>> data, Fragment fragment) {
        this.data = data;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public vh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new vh(LayoutInflater.from(parent.getContext()).inflate(R.layout.single_item_timeable, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull vh holder, int position) {

        t[0] = dateTime.get(Calendar.HOUR_OF_DAY);
        t[1] = dateTime.get(Calendar.MINUTE);
        t[2] = t[0];
        t[3] = t[1];

        holder.fromTime.setText(ExtraFunctions.getReadableTime(t[0], t[1]));
        holder.toTime.setText(ExtraFunctions.getReadableTime(t[2], t[3]));

        Map<String, Object> single = data.get(position);
        if (!(Boolean) single.get("isSet")) {
            holder.time.setVisibility(View.GONE);
            holder.daySwitch.setChecked(false);
        } else {
            holder.daySwitch.setChecked(true);
            holder.toTime.setText(ExtraFunctions.getReadableTime(Integer.parseInt(single.get("toTime").toString().split(":")[0]),Integer.parseInt(single.get("toTime").toString().split(":")[1])));
            holder.fromTime.setText(ExtraFunctions.getReadableTime(Integer.parseInt(single.get("fromTime").toString().split(":")[0]),Integer.parseInt(single.get("fromTime").toString().split(":")[1])));
        }
        holder.daySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                holder.time.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                ((TimeTable) fragment).datePicked(position, t[0], t[1], t[2], t[3],holder.daySwitch.isChecked());
            }
        });


        holder.toTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(fragment.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        holder.toTime.setText(ExtraFunctions.getReadableTime(hourOfDay, minute));
                        t[2] = hourOfDay;
                        t[3] = minute;
                        ((TimeTable) fragment).datePicked(position, t[0], t[1], t[2], t[3],holder.daySwitch.isChecked());
                    }
                }, dateTime.get(Calendar.HOUR_OF_DAY), dateTime.get(Calendar.MINUTE), false).show();

            }
        });

        holder.fromTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(fragment.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        holder.fromTime.setText(ExtraFunctions.getReadableTime(hourOfDay, minute));
                        t[0] = hourOfDay;
                        t[1] = minute;
                        ((TimeTable) fragment).datePicked(position, t[0], t[1], t[2], t[3],holder.daySwitch.isChecked());
                    }
                }, dateTime.get(Calendar.HOUR_OF_DAY), dateTime.get(Calendar.MINUTE), false).show();

            }
        });

        holder.dayName.setText(single.get("day").toString());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class vh extends RecyclerView.ViewHolder {
        TextView dayName, fromTime, toTime;
        SwitchCompat daySwitch;
        ConstraintLayout time;

        public vh(@NonNull View itemView) {
            super(itemView);
            this.time = itemView.findViewById(R.id.time);
            this.dayName = itemView.findViewById(R.id.dayName);
            this.fromTime = itemView.findViewById(R.id.fromTime);
            this.toTime = itemView.findViewById(R.id.toTime);
            this.daySwitch = itemView.findViewById(R.id.daySwitch);
        }
    }

}
