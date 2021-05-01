package com.projects.timely.alarms;

import android.view.View;
import android.widget.Button;

import com.projects.timely.R;
import com.projects.timely.core.SchoolDatabase;

import androidx.recyclerview.widget.RecyclerView;

import static com.projects.timely.core.AppUtils.DAYS_2;

@SuppressWarnings("unused")
class ButtonListHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private int btnPos;
    private int alarmPosition;
    private SchoolDatabase database;
    private Button button;
    private boolean disabled;


    ButtonListHolder(View itemView) {
        super(itemView);
        button = itemView.findViewById(R.id.btn_list);
        button.setOnClickListener(this);
    }

    public ButtonListHolder with(int btnPos,
                                 int adapterPosition,
                                 SchoolDatabase database) {
        this.btnPos = btnPos;
        this.alarmPosition = adapterPosition;
        this.database = database;
        return this;
    }

    void bindView() {
        button.setText(String.valueOf(DAYS_2[btnPos]));
        // get the selected day from database
        Boolean[] selectedDays = database.getSelectedDays(alarmPosition);
        Boolean isSelected = selectedDays[btnPos];
        disabled = !isSelected;
        button.setBackgroundResource(isSelected ? R.drawable.enabled_round_button
                                                : R.drawable.disabled_round_button);
    }

    @Override
    public void onClick(View v) {
        disabled = !disabled;
        button.setBackgroundResource(disabled ? R.drawable.disabled_round_button
                                              : R.drawable.enabled_round_button);
            Boolean[] selectedDays = database.getSelectedDays(alarmPosition);
            selectedDays[btnPos] = !disabled;
            boolean updated = database.updateSelectedDays(alarmPosition, selectedDays);
    }
}