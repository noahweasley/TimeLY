package com.projects.timely.exam;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.projects.timely.R;
import com.projects.timely.core.SchoolDatabase;
import com.projects.timely.util.ThreadUtils;
import com.projects.timely.error.ErrorDialog;

import org.greenrobot.eventbus.EventBus;

import static com.projects.timely.core.AppUtils.Alert;
import static com.projects.timely.core.AppUtils.DAYS_3;
import static com.projects.timely.core.AppUtils.playAlertTone;

@SuppressWarnings("ConstantConditions")
public class AddExamDialog extends DialogFragment implements View.OnClickListener {
    static final String ARG_PAGE_POSITION = "Tab position";
    private AutoCompleteTextView atv_courseName;
    private EditText edt_startTime, edt_endTime;
    private CheckBox cbx_clear;
    private String examDay;

    /**
     * Make this dialog visible to the user
     *
     * @param context the context in which the dialog is going to use to be added to the activity's
     *                fragment manager
     * @param pagePos the tab position at which this dialog displays content to user
     */
    public void show(Context context, int pagePos) {
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_PAGE_POSITION, pagePos);
        setArguments(bundle);
        FragmentManager manager = ((FragmentActivity) context).getSupportFragmentManager();
        show(manager, AddExamDialog.class.getName());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AETDialog(getContext());
    }

    private boolean registerAndClear() {
        boolean registered = registerExam();
        if (registered && cbx_clear.isChecked()) {
            edt_endTime.setText(null);
            edt_startTime.setText(null);
            atv_courseName.setText(null);
        }
        return registered;
    }

    private boolean registerAndClose() {
        boolean registered = registerExam();
        if (registered)
            dismiss();
        return registered;
    }

    @SuppressWarnings("unused")
    private boolean registerExam() {
        SchoolDatabase database = new SchoolDatabase(getContext());
        String course = atv_courseName.getText().toString();
        String end = edt_endTime.getText().toString();
        String start = edt_startTime.getText().toString();
        String code = database.getCourseCodeFromName(course);

        String timeRegex  /* 24 Hours format */
                = "^(?:(0[0-9]|1[0-9]|2[0-3]):(0[0-9]|1[0-9]|2[0-9]|3[0-9]|4[0-9]|5[0-9]))$";

        boolean errorOccurred = false;
        if (!start.matches(timeRegex)) {
            edt_startTime.setError("Format: hh:ss");
            errorOccurred = true;
        }

        if (!end.matches(timeRegex)) {
            edt_endTime.setError("Format: hh:ss");
            errorOccurred = true;
        }

        if (TextUtils.isEmpty(course)) {
            atv_courseName.setError("Required");
            errorOccurred = true;
        }
        if (errorOccurred) return false;

        int pagePosition = getArguments().getInt(ARG_PAGE_POSITION);

        @SuppressLint("DefaultLocale")
        String examWeek = String.format("%s_%d", "WEEK", pagePosition + 1);
        ExamModel exam = new ExamModel(code, course, start, end);
        exam.setWeek(examWeek);
        exam.setDay(examDay);

        Context context = getContext();
        if (database.isExamAbsent(examWeek, exam)) {
            ThreadUtils.runBackgroundTask(() -> {
                int[] data = database.addExam(exam, examWeek);
                if (data[1] != -1) {
                    exam.setId(data[1]);
                    exam.setChronologicalOrder(data[0]);
                    EventBus.getDefault().post(new UpdateMessage(exam,
                                                                 UpdateMessage.EventType.NEW,
                                                                 pagePosition));
                    playAlertTone(context.getApplicationContext(), Alert.EXAM);
                } else {
                    Toast.makeText(context, "An Error occurred", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            ErrorDialog.Builder builder = new ErrorDialog.Builder();
            builder.setShowSuggestions(false)
                    .setDialogMessage("Duplicate Exam Found");
            new ErrorDialog().showErrorMessage(getContext(), builder.build());
        }
        database.close();
        return true;
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }

    private class AETDialog extends Dialog {

        public AETDialog(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_edges);
            setContentView(R.layout.dialog_add_exam);
            setCanceledOnTouchOutside(false);
            findViewById(R.id.cancel).setOnClickListener(AddExamDialog.this);
            CheckBox cbx_multiple = findViewById(R.id.multiple);
            cbx_clear = findViewById(R.id.clear);
            findViewById(R.id.register).setOnClickListener(v -> {
                boolean success = cbx_multiple.isChecked() ? registerAndClear()
                                                           : registerAndClose();
                if (success) {
                    Toast message = Toast.makeText(getContext(), R.string.registration_pending,
                                                   Toast.LENGTH_SHORT);
                    if (!cbx_multiple.isChecked())
                        message.setGravity(Gravity.CENTER, 0, 0);
                    message.show();
                } else
                    Toast.makeText(getContext(), "Error occurred", Toast.LENGTH_SHORT).show();
            });

            atv_courseName = findViewById(R.id.course_name);
            edt_startTime = findViewById(R.id.start_time);
            edt_endTime = findViewById(R.id.end_time);

            SchoolDatabase database = new SchoolDatabase(getContext());
            ArrayAdapter<String> courseAdapter
                    = new ArrayAdapter<>(getContext(),
                                         android.R.layout.simple_dropdown_item_1line,
                                         database.getAllRegisteredCourses());
            atv_courseName.setAdapter(courseAdapter);

            Spinner spin_days = findViewById(R.id.day_spin);
            ArrayAdapter<String> dayAdapter
                    = new ArrayAdapter<>(getContext(),
                                         android.R.layout.simple_spinner_item,
                                         DAYS_3);
            dayAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
            spin_days.setAdapter(dayAdapter);
            spin_days.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                           long id) {
                    examDay = DAYS_3[pos];
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            database.close();
        }
    }

}
