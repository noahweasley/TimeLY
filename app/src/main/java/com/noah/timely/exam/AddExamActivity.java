package com.noah.timely.exam;

import static com.noah.timely.util.Utility.DAYS_3;
import static com.noah.timely.util.Utility.isUserPreferred24Hours;
import static com.noah.timely.util.Utility.playAlertTone;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
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
import androidx.fragment.app.FragmentManager;

import com.noah.timely.R;
import com.noah.timely.core.SchoolDatabase;
import com.noah.timely.error.ErrorDialog;
import com.noah.timely.util.ThreadUtils;
import com.noah.timely.util.Utility;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.greenrobot.eventbus.EventBus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
    public static final int UNIT_12 = 12;
    public static final int UNIT_24 = 24;

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
                Toast message = Toast.makeText(this, R.string.registration_pending, Toast.LENGTH_SHORT);

                if (!cbx_multiple.isChecked())
                    message.setGravity(Gravity.CENTER, 0, 0);

                message.show();
            } else
                Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show();
        });

        atv_courseName = findViewById(R.id.course_name);
        edt_startTime = findViewById(R.id.start_time);
        edt_endTime = findViewById(R.id.end_time);

        edt_endTime.setOnTouchListener(this::onTouch);
        edt_endTime.setOnTouchListener(this::onTouch);

        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this,
                                                                android.R.layout.simple_dropdown_item_1line,
                                                                database.getAllRegisteredCourses());
        atv_courseName.setAdapter(courseAdapter);

        Spinner spin_days = findViewById(R.id.day_spin);
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this,
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

    private boolean onTouch(View view, MotionEvent event) {
        EditText editText = (EditText) view;
        TimePickerDialog.OnTimeSetListener tsl = (TimePickerDialog timePicker, int hourOfDay, int minute, int second) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            SimpleDateFormat timeFormat24 = new SimpleDateFormat("HH:mm", Locale.US);
            SimpleDateFormat timeFormat12 = new SimpleDateFormat("hh:mm aa", Locale.US);

            String parsedTime = isUserPreferred24Hours(this) ? timeFormat24.format(calendar.getTime())
                                                             : timeFormat12.format(calendar.getTime());

            editText.setText(parsedTime);
        };

        final int DRAWABLE_RIGHT = 2;

        if (event.getAction() == MotionEvent.ACTION_UP) {
            int drawableWidth = editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();
            if (event.getX() >= (editText.getWidth() - drawableWidth)) {
                Calendar calendar = Calendar.getInstance();

                FragmentManager manager = getSupportFragmentManager();
                TimePickerDialog dpd = TimePickerDialog.newInstance(tsl,
                                                                    calendar.get(Calendar.HOUR_OF_DAY),
                                                                    calendar.get(Calendar.MINUTE),
                                                                    isUserPreferred24Hours(this));
                dpd.setVersion(TimePickerDialog.Version.VERSION_2);
                dpd.show(manager, "TimePickerDialog");
                return true;
            }
        }
        return false;
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

        String timeRegex24 = "^(?:(0[0-9]|1[0-9]|2[0-3]):(0[0-9]|1[0-9]|2[0-9]|3[0-9]|4[0-9]|5[0-9]))$";
        String timeRegex12 = "^((1[012]|0[1-9]):[0-5][0-9](\\\\s)?(?i) (am|pm))$";

        boolean errorOccurred = false, use24 = isUserPreferred24Hours(this);

        if (use24 && !start.matches(timeRegex24)) {
            edt_startTime.setError("Format: HH:SS");
            errorOccurred = true;
        } else {
            if (!use24 && !start.matches(timeRegex12)) {
                edt_startTime.setError("12 hours mode");
                errorOccurred = true;
            }
        }

        if (use24 && !end.matches(timeRegex24)) {
            edt_endTime.setError("Format: HH:SS");
            errorOccurred = true;
        } else {
            if (!use24 && !end.matches(timeRegex12)) {
                edt_endTime.setError("12 hours mode");
                errorOccurred = true;
            }
        }

        if (TextUtils.isEmpty(course)) {
            atv_courseName.setError("Required");
            errorOccurred = true;
        }

        if (errorOccurred) return false;

        start = use24 ? start : convert(start, UNIT_24);
        end = use24 ? end : convert(end, UNIT_24);

        int pagePosition = getIntent().getIntExtra(ARG_PAGE_POSITION, 0);

        String examWeek = String.format(Locale.US, "%s_%d", "WEEK", pagePosition + 1);
        ExamModel exam = new ExamModel(code, course, start, end);
        exam.setWeek(examWeek);
        exam.setDay(examDay);

        if (database.isExamAbsent(examWeek, exam)) {
            ThreadUtils.runBackgroundTask(() -> {
                int[] data = database.addExam(exam, examWeek);
                if (data[1] != -1) {
                    exam.setId(data[1]);
                    exam.setChronologicalOrder(data[0]);
                    EventBus.getDefault().post(new EUpdateMessage(exam, EUpdateMessage.EventType.NEW, pagePosition));
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

    @SuppressWarnings("all")
    private String convert(String time, int unit) {
        SimpleDateFormat timeFormat24 = new SimpleDateFormat("HH:mm", Locale.US);
        SimpleDateFormat timeFormat12 = new SimpleDateFormat("hh:mm aa", Locale.US);

        Date date;
        try {
            date = unit == UNIT_24 ? timeFormat12.parse(time) : timeFormat24.parse(time);
        } catch (ParseException e) {
            return null;
        }
        return unit == UNIT_24 ? timeFormat24.format(date.getTime()) : timeFormat12.format(date.getTime());
    }
}
