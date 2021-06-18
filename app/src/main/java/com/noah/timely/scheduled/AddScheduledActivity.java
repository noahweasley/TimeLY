package com.noah.timely.scheduled;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.noah.timely.R;
import com.noah.timely.core.SchoolDatabase;
import com.noah.timely.error.ErrorDialog;
import com.noah.timely.timetable.TimetableModel;
import com.noah.timely.util.ThreadUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.noah.timely.core.AppUtils.Alert.SCHEDULED_TIMETABLE;
import static com.noah.timely.core.AppUtils.DAYS;
import static com.noah.timely.core.AppUtils.playAlertTone;
import static com.noah.timely.scheduled.ScheduledTimetableFragment.ARG_DATA;
import static com.noah.timely.scheduled.ScheduledTimetableFragment.ARG_TO_EDIT;

/**
 * A clone of {@link AddScheduledDialog} that would be used as an alternate to adding scheduled timetables
 */
public class AddScheduledActivity extends AppCompatActivity {
    public static final String ARG_TIME = "Scheduled time";
    public static final String ARG_COURSE = "Course code";
    public static final String ARG_DAY = "Schedule Repeat day";
    private static String selectedDay = DAYS[0];
    private AutoCompleteTextView atv_courseName;
    private EditText edt_startTime, edt_endTime, edt_lecturerName;
    private RadioGroup imp_group;
    private CheckBox cbx_clear;
    private SchoolDatabase database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_scheduled);

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Add Scheduled Class");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database = new SchoolDatabase(this);
        cbx_clear = findViewById(R.id.clear);

        CheckBox cbx_multiple = findViewById(R.id.multiple);
        findViewById(R.id.register).setOnClickListener(v -> {
            boolean success = cbx_multiple.isChecked() ? registerAndClear()
                                                       : registerAndClose();
            int m;
            if (getIntent().getBooleanExtra(ARG_TO_EDIT, false))
                m = R.string.update_pending;
            else m = R.string.registration_pending;

            if (success) {
                Toast message = Toast.makeText(this, m, Toast.LENGTH_SHORT);

                if (!cbx_multiple.isChecked()) message.setGravity(Gravity.CENTER, 0, 0);
                message.show();

            } else Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show();
        });

        atv_courseName = findViewById(R.id.course_name);
        edt_startTime = findViewById(R.id.start_time);
        edt_endTime = findViewById(R.id.end_time);
        edt_lecturerName = findViewById(R.id.lecturer_name);
        imp_group = findViewById(R.id.importance_group);

        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this,
                                                                android.R.layout.simple_dropdown_item_1line,
                                                                database.getAllRegisteredCourses());

        atv_courseName.setAdapter(courseAdapter);

        Spinner spin_days = findViewById(R.id.day_spin);
        ArrayAdapter<String> daysAdapter = new ArrayAdapter<>(this,
                                                              android.R.layout.simple_spinner_item,
                                                              DAYS);

        daysAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        spin_days.setAdapter(daysAdapter);
        spin_days.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                selectedDay = DAYS[pos];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // unused, but had to implement.
            }
        });

        if (getIntent().getBooleanExtra(ARG_TO_EDIT, false)) {
            TimetableModel data = (TimetableModel) getIntent().getSerializableExtra(ARG_DATA);
            edt_endTime.setText(data.getEndTime());
            edt_startTime.setText(data.getStartTime());
            atv_courseName.setText(data.getFullCourseName());
            edt_lecturerName.setText(data.getLecturerName());
            spin_days.setSelection(data.getDayIndex());
        }
    }

    @Override
    protected void onDestroy() {
        database.close();
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private boolean registerAndClose() {
        boolean isRegistered = registerTimetable();
        if (isRegistered)
            onBackPressed();
        return isRegistered;
    }

    private boolean registerTimetable() {
        String course = atv_courseName.getText().toString();
        String end = edt_endTime.getText().toString();
        String start = edt_startTime.getText().toString();
        String lecturerName = edt_lecturerName.getText().toString();
        String code = database.getCourseCodeFromName(course);

        String importance;
        int checkedRadioButtonId = imp_group.getCheckedRadioButtonId();
        if (checkedRadioButtonId == R.id.less_important) {
            importance = "Less Important";
        } else if (checkedRadioButtonId == R.id.very_important) {
            importance = "Very Important";
        } else {
            importance = "Not Important";
        }

        String timeRegex  /* 24 Hours format */
                = "^(?:(0[0-9]|1[0-9]|2[0-3]):(0[0-9]|1[0-9]|2[0-9]|3[0-9]|4[0-9]|5[0-9]))$";

        boolean errorOccurred = false;
        if (!start.matches(timeRegex)) {
            edt_startTime.setError("Format: HH:SS");
            errorOccurred = true;
        }

        if (!end.matches(timeRegex)) {
            edt_endTime.setError("Format: HH:SS");
            errorOccurred = true;
        }

        if (TextUtils.isEmpty(course)) {
            atv_courseName.setError("Required");
            errorOccurred = true;
        }

        if (TextUtils.isEmpty(lecturerName)) {
            edt_lecturerName.setError("Required");
            errorOccurred = true;
        }

        if (errorOccurred) {
            database.close();
            return false;
        }

        TimetableModel newTimetable = new TimetableModel(lecturerName, course, start, end, code, importance,
                                                         selectedDay);

        if (getIntent().getBooleanExtra(ARG_TO_EDIT, false)) {

            TimetableModel formerTimetable = (TimetableModel) getIntent().getSerializableExtra(ARG_DATA);

            boolean updated = database.updateTimetableData(newTimetable, SchoolDatabase.SCHEDULED_TIMETABLE);

            if (updated) {
                newTimetable.setId(formerTimetable.getId());
                newTimetable.setChronologicalOrder(formerTimetable.getChronologicalOrder());
                // after updating database, update the UI and re-schedule notification
                cancelTimetableNotifier(this, formerTimetable);

                EventBus.getDefault()
                        .post(new UpdateMessage(newTimetable, UpdateMessage.EventType.UPDATE_CURRENT));

                scheduleTimetableAlarm(this, newTimetable);

            } else Toast.makeText(this, "An Error Occurred", Toast.LENGTH_LONG).show();

        } else {
            if (database.isTimeTableAbsent(SchoolDatabase.SCHEDULED_TIMETABLE, newTimetable)) {
                ThreadUtils.runBackgroundTask(() -> {

                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

                    int[] insertData = database.addTimeTableData(newTimetable, SchoolDatabase.SCHEDULED_TIMETABLE);

                    if (insertData[1] != -1) {
                        // set the id to be used with view holder's item Id
                        newTimetable.setChronologicalOrder(insertData[0]);
                        newTimetable.setId(insertData[1]);
                        // after adding to database, update the UI and schedule notification
                        EventBus.getDefault()
                                .post(new UpdateMessage(newTimetable, UpdateMessage.EventType.NEW));
                        scheduleTimetableAlarm(this, newTimetable);
                    } else {
                        Toast.makeText(this, "An Error Occurred", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                // Error message
                ErrorDialog.Builder builder = new ErrorDialog.Builder();
                builder.setDialogMessage("Duplicate start time present")
                       .setShowSuggestions(false);
                new ErrorDialog().showErrorMessage(this, builder.build());
            }
        }

        return true;
    }

    private boolean registerAndClear() {
        boolean isRegistered = registerTimetable();
        if (isRegistered && cbx_clear.isChecked()) {
            atv_courseName.setText(null);
            edt_startTime.setText(null);
            edt_endTime.setText(null);
            edt_lecturerName.setText(null);
        }
        return isRegistered;
    }

    private void cancelTimetableNotifier(Context context, TimetableModel timetable) {
        String[] t = timetable.getStartTime().split(":");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, timetable.getCalendarDay());
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(t[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(t[1]));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.setTimeInMillis(calendar.getTimeInMillis() - TimeUnit.MINUTES.toMillis(10));

        long NOW = System.currentTimeMillis();
        long CURRENT = calendar.getTimeInMillis();
        long timeInMillis = CURRENT < NOW ? CURRENT + TimeUnit.DAYS.toMillis(7) : CURRENT;

        Log.d(getClass().getSimpleName(), "Cancelling alarm: " + new Date(timeInMillis));

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent timetableIntent = new Intent(context, ScheduledTaskNotifier.class);
        timetableIntent.addCategory("com.noah.timely.scheduled")
                       .setAction("com.noah.timely.scheduled.addAction")
                       .setDataAndType(
                               Uri.parse("content://com.noah.timely.scheduled.add." + timeInMillis),
                               "com.noah.timely.scheduled.dataType");

        PendingIntent pi = PendingIntent.getBroadcast(context, 1156, timetableIntent,
                                                      PendingIntent.FLAG_CANCEL_CURRENT);
        pi.cancel();
        manager.cancel(pi);
    }

    private void scheduleTimetableAlarm(Context context, TimetableModel timetable) {
        String[] sTime = timetable.getStartTime().split(":");
        String course = timetable.getFullCourseName() + " (" + timetable.getCourseCode() + ")";
        int day = timetable.getCalendarDay();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(sTime[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(sTime[1]));
        calendar.set(Calendar.DAY_OF_WEEK, day);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.setTimeInMillis(calendar.getTimeInMillis() - TimeUnit.MINUTES.toMillis(10));

        long NOW = System.currentTimeMillis();
        long CURRENT = calendar.getTimeInMillis();
        long triggerTime = CURRENT < NOW ? CURRENT + TimeUnit.DAYS.toMillis(7) : CURRENT;

        Log.d(getClass().getSimpleName(), "Scheduling notification for: " + new Date(triggerTime));

        AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        Intent scheduleIntent = new Intent(context, ScheduledTaskNotifier.class)
                .putExtra(ARG_TIME, timetable.getStartTime())
                .putExtra(ARG_COURSE, course)
                .putExtra(ARG_DAY, day)
                .addCategory("com.noah.timely.scheduled")
                .setAction("com.noah.timely.scheduled.addAction")
                .setDataAndType(
                        Uri.parse("content://com.noah.timely.scheduled.add." + triggerTime),
                        "com.noah.timely.scheduled.dataType");

        PendingIntent pi = PendingIntent.getBroadcast(context, 1156, scheduleIntent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            manager.setExact(AlarmManager.RTC, triggerTime, pi);
        manager.set(AlarmManager.RTC, triggerTime, pi);
        playAlertTone(context.getApplicationContext(), SCHEDULED_TIMETABLE);
    }

}