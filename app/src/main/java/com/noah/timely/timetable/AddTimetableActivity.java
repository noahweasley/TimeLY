package com.noah.timely.timetable;

import static com.noah.timely.timetable.DaysFragment.ARG_CHRONOLOGY;
import static com.noah.timely.timetable.DaysFragment.ARG_CLASS;
import static com.noah.timely.timetable.DaysFragment.ARG_DATA;
import static com.noah.timely.timetable.DaysFragment.ARG_DAY;
import static com.noah.timely.timetable.DaysFragment.ARG_PAGE_POSITION;
import static com.noah.timely.timetable.DaysFragment.ARG_POSITION;
import static com.noah.timely.timetable.DaysFragment.ARG_TIME;
import static com.noah.timely.timetable.DaysFragment.ARG_TO_EDIT;
import static com.noah.timely.util.MiscUtil.Alert.COURSE;
import static com.noah.timely.util.MiscUtil.DAYS;
import static com.noah.timely.util.MiscUtil.isUserPreferred24Hours;
import static com.noah.timely.util.MiscUtil.playAlertTone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
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

import com.noah.timely.R;
import com.noah.timely.core.SchoolDatabase;
import com.noah.timely.error.ErrorDialog;
import com.noah.timely.util.ThreadUtils;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.greenrobot.eventbus.EventBus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * A clone of {@link AddTimetableDialog} that would be used as an alternate to adding timetables
 */
public class AddTimetableActivity extends AppCompatActivity {
   private static final int UNIT_12 = 12;
   private static final int UNIT_24 = 24;
   private static String selectedDay;
   private AutoCompleteTextView atv_courseName;
   private EditText edt_startTime, edt_endTime;
   private CheckBox cbx_clear;
   private SchoolDatabase database;

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_add_timetable);

      setSupportActionBar(findViewById(R.id.toolbar));
      getSupportActionBar().setTitle("Add Timetable");
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      database = new SchoolDatabase(this);

      cbx_clear = findViewById(R.id.clear);
      CheckBox cbx_multiple = findViewById(R.id.multiple);

      atv_courseName = findViewById(R.id.course_name);
      edt_startTime = findViewById(R.id.start_time);
      edt_endTime = findViewById(R.id.end_time);

      edt_endTime.setOnTouchListener(this::onTouch);
      edt_startTime.setOnTouchListener(this::onTouch);

      ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this,
              R.layout.simple_dropdown_item_1line,
              database.getAllRegisteredCourses());
      courseAdapter.setDropDownViewResource(R.layout.simple_dropdown_item_1line);
      atv_courseName.setAdapter(courseAdapter);

      Spinner spin_days = findViewById(R.id.day_spin);

      ArrayAdapter<String> daysAdapter = new ArrayAdapter<>(this,
              R.layout.simple_spinner_item,
              DAYS);

      daysAdapter.setDropDownViewResource(R.layout.simple_dropdown_item_1line);
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

      Intent intent = getIntent();
      if (intent.getBooleanExtra(ARG_TO_EDIT, false)) {

         TimetableModel data = (TimetableModel) intent.getSerializableExtra(ARG_DATA);
         getIntent().putExtra(ARG_CHRONOLOGY, data.getChronologicalOrder());

         if (!isUserPreferred24Hours(this)) {
            data.setStartTime(convert(data.getStartTime(), UNIT_12));
            data.setEndTime(convert(data.getEndTime(), UNIT_12));
         }

         edt_endTime.setText(data.getEndTime());
         edt_startTime.setText(data.getStartTime());
         atv_courseName.setText(data.getFullCourseName());
         spin_days.setSelection(data.getDayIndex());

      } else {
         spin_days.setSelection(getTabPosition());
      }

      findViewById(R.id.register).setOnClickListener(v -> {

         boolean success = cbx_multiple.isChecked() ? registerAndClear() : registerAndClose();
         int m;

         if (getIntent().getBooleanExtra(ARG_TO_EDIT, false)) m = R.string.update_pending;
         else m = R.string.registration_pending;

         if (success) {

            Toast message = Toast.makeText(this, m, Toast.LENGTH_SHORT);
            if (!cbx_multiple.isChecked()) message.setGravity(Gravity.CENTER, 0, 0);
            message.show();

         } else Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show();

      });
   }

   private boolean onTouch(View view, MotionEvent event) {
      EditText editText = (EditText) view;
      TimePickerDialog.OnTimeSetListener tsl = (TimePickerDialog timePicker, int hourOfDay, int minute,
                                                int second) -> {
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

            TimePickerDialog dpd = TimePickerDialog.newInstance(tsl,
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    isUserPreferred24Hours(this));
            dpd.setVersion(TimePickerDialog.Version.VERSION_2);
            dpd.show(getSupportFragmentManager(), "TimePickerDialog");
            return true;
         }
      }
      return false;
   }

   private int getTabPosition() {
      return getIntent().getIntExtra(ARG_PAGE_POSITION, 0);
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
      if (isRegistered) onBackPressed();
      return isRegistered;
   }

   private boolean registerAndClear() {
      boolean isRegistered = registerTimetable();
      if (isRegistered && cbx_clear.isChecked()) {
         atv_courseName.setText(null);
         edt_startTime.setText(null);
         edt_endTime.setText(null);
      }
      return isRegistered;
   }

   private int getPagePosition() {
      int pagePosition;
      switch (selectedDay) {
         case "Monday":
            pagePosition = 0;
            break;
         case "Tuesday":
            pagePosition = 1;
            break;
         case "Wednesday":
            pagePosition = 2;
            break;
         case "Thursday":
            pagePosition = 3;
            break;
         case "Friday":
            pagePosition = 4;
            break;
         case "Saturday":
            pagePosition = 5;
            break;
         default:
            pagePosition = -1;
      }
      return pagePosition;
   }

   private boolean registerTimetable() {
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

      if (errorOccurred) {
         return false;
      }

      start = use24 ? start : convert(start, UNIT_24);
      end = use24 ? end : convert(end, UNIT_24);

      int pagePosition = getPagePosition();
      TimetableModel formerTimetable = (TimetableModel) getIntent().getSerializableExtra(ARG_DATA);

      TimetableModel timetable = new TimetableModel(course, start, end, code, false, selectedDay);

      if (getIntent().getBooleanExtra(ARG_TO_EDIT, false)) {
         boolean updated = database.updateTimetableData(timetable, timetable.getDay());

         if (updated) {
            cancelTimetableNotifier(this, formerTimetable); // cancel former alarm first

            timetable.setId(formerTimetable.getId());
            timetable.setChronologicalOrder(formerTimetable.getChronologicalOrder());
            EventBus.getDefault()
                    .post(new TUpdateMessage(timetable, pagePosition, TUpdateMessage.EventType.UPDATE_CURRENT));

            scheduleTimetableAlarm(this, timetable, pagePosition);

         } else Toast.makeText(this, "An Error Occurred", Toast.LENGTH_LONG).show();

      } else {
         if (database.isTimeTableAbsent(timetable.getDay(), timetable)) {
            ThreadUtils.runBackgroundTask(() -> {

               Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

               int[] insertData = database.addTimeTableData(timetable, timetable.getDay());
               if (insertData[1] != -1) {
                  // set the id to be used with view holder's item Id
                  timetable.setChronologicalOrder(insertData[0]);
                  timetable.setId(insertData[1]);
                  // after adding to database, update the UI and schedule notification
                  EventBus.getDefault().post(new TUpdateMessage(timetable, pagePosition,
                          TUpdateMessage.EventType.NEW));

                  scheduleTimetableAlarm(this, timetable, pagePosition);

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

   // cancel timetable
   private void cancelTimetableNotifier(Context context, TimetableModel timetable) {
      String[] t = timetable.getStartTime().split("[: ]");

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

      AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
      Intent timetableIntent = new Intent(context, TimetableNotifier.class);
      timetableIntent.addCategory("com.noah.timely.timetable")
              .setAction("com.noah.timely.timetable.addAction")
              .setDataAndType(Uri.parse("content://com.noah.timely.add." + timeInMillis),
                      "com.noah.timely.dataType");

      PendingIntent pi = PendingIntent.getBroadcast(context, 555, timetableIntent,
              PendingIntent.FLAG_CANCEL_CURRENT);
      pi.cancel();
      manager.cancel(pi);
   }

   // schedule timetable
   private void scheduleTimetableAlarm(Context context, TimetableModel timetable, int pagePosition) {
      int position = timetable.getChronologicalOrder();
      String time = timetable.getStartTime();
      String course = timetable.getFullCourseName() + " (" + timetable.getCourseCode() + ")";
      String[] t = timetable.getStartTime().split("[: ]");

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

      AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

      Intent timetableIntent = new Intent(context, TimetableNotifier.class);
      timetableIntent.putExtra(ARG_TIME, time)
              .putExtra(ARG_CLASS, course)
              .putExtra(ARG_DAY, timetable.getCalendarDay())
              .putExtra(ARG_POSITION, position)
              .putExtra(ARG_PAGE_POSITION, pagePosition)
              .addCategory("com.noah.timely.timetable")
              .setAction("com.noah.timely.timetable.addAction")
              .setDataAndType(Uri.parse("content://com.noah.timely.add." + timeInMillis),
                      "com.noah.timely.dataType");

      PendingIntent pi = PendingIntent.getBroadcast(context, 555, timetableIntent,
              PendingIntent.FLAG_UPDATE_CURRENT);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC, timeInMillis, pi);
         } else {
            manager.setExact(AlarmManager.RTC, timeInMillis, pi);
         }

      } else manager.set(AlarmManager.RTC, timeInMillis, pi);
      // notify user
      playAlertTone(context, COURSE);
   }

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
