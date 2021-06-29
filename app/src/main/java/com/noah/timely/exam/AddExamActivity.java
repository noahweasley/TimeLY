package com.noah.timely.exam;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.noah.timely.R;
import com.noah.timely.util.Utility;
import com.noah.timely.core.SchoolDatabase;
import com.noah.timely.error.ErrorDialog;
import com.noah.timely.util.ThreadUtils;

import org.greenrobot.eventbus.EventBus;

import static com.noah.timely.util.Utility.DAYS_3;
import static com.noah.timely.util.Utility.playAlertTone;

/**
 * A clone of {@link AddExamDialog} that would be used as an alternate to adding exams
 */
public class AddExamActivity extends AppCompatActivity {
    static final String ARG_PAGE_POSITION = "Tab position";
    private SchoolDatabase database;
    private AutoCompleteTextView atv_courseName;
    private EditText edt_startTime, edt_endTime;
    private CheckBox cbx_clear;
    private String examDay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_exam);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Register Exams");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database = new SchoolDatabase(this);

        CheckBox cbx_multiple = findViewById(R.id.multiple);
        cbx_clear = findViewById(R.id.clear);


        findViewById(R.id.register).setOnClickListener(v -> {

            boolean success = cbx_multiple.isChecked() ? registerAndClear() : registerAndClose();

            if (success) {
                Toast message
                        = Toast.makeText(this, R.string.registration_pending, Toast.LENGTH_SHORT);

                if (!cbx_multiple.isChecked())
                    message.setGravity(Gravity.CENTER, 0, 0);

                message.show();
            } else
                Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show();
        });

        atv_courseName = findViewById(R.id.course_name);
        edt_startTime = findViewById(R.id.start_time);
        edt_endTime = findViewById(R.id.end_time);

        ArrayAdapter<String> courseAdapter
                = new ArrayAdapter<>(this,
                                     android.R.layout.simple_dropdown_item_1line,
                                     database.getAllRegisteredCourses());
        atv_courseName.setAdapter(courseAdapter);

        Spinner spin_days = findViewById(R.id.day_spin);
        ArrayAdapter<String> dayAdapter
                = new ArrayAdapter<>(this,
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
    }

    @Override
    protected void onDestroy() {
        database.close();
        super.onDestroy();
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private boolean registerAndClose() {
        boolean registered = registerExam();
        if (registered)
            onBackPressed();
        return registered;
    }

    private boolean registerExam() {
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

        int pagePosition = getIntent().getIntExtra(ARG_PAGE_POSITION, 0);

        @SuppressLint("DefaultLocale")
        String examWeek = String.format("%s_%d", "WEEK", pagePosition + 1);
        ExamModel exam = new ExamModel(code, course, start, end);
        exam.setWeek(examWeek);
        exam.setDay(examDay);

        if (database.isExamAbsent(examWeek, exam)) {
            ThreadUtils.runBackgroundTask(() -> {
                int[] data = database.addExam(exam, examWeek);
                if (data[1] != -1) {
                    exam.setId(data[1]);
                    exam.setChronologicalOrder(data[0]);
                    EventBus.getDefault().post(new UpdateMessage(exam,
                                                                 UpdateMessage.EventType.NEW,
                                                                 pagePosition));
                    playAlertTone(getApplicationContext(), Utility.Alert.EXAM);
                } else {
                    Toast.makeText(this, "An Error occurred", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            ErrorDialog.Builder builder = new ErrorDialog.Builder();
            builder.setShowSuggestions(false)
                   .setDialogMessage("Duplicate Exam Found");
            new ErrorDialog().showErrorMessage(this, builder.build());
        }
        return true;
    }
}
