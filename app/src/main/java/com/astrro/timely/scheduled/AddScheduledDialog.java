package com.astrro.timely.scheduled;

import static android.content.Context.ALARM_SERVICE;
import static com.astrro.timely.scheduled.ScheduledTimetableFragment.ARG_DATA;
import static com.astrro.timely.scheduled.ScheduledTimetableFragment.ARG_TO_EDIT;
import static com.astrro.timely.util.Converter.UNIT_12;
import static com.astrro.timely.util.Converter.convertTime;
import static com.astrro.timely.util.MiscUtil.DAYS;
import static com.astrro.timely.util.MiscUtil.isUserPreferred24Hours;

import android.app.AlarmManager;
import android.app.Dialog;
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
import android.view.ViewParent;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.astrro.timely.R;
import com.astrro.timely.alarms.AlarmHelper;
import com.astrro.timely.core.SchoolDatabase;
import com.astrro.timely.error.ErrorDialog;
import com.astrro.timely.timetable.TimetableModel;
import com.astrro.timely.util.Converter;
import com.astrro.timely.util.PatternUtils;
import com.astrro.timely.util.ThreadUtils;
import com.astrro.timely.util.adapters.SimpleOnItemSelectedListener;
import com.astrro.timely.util.sound.AlertType;
import com.astrro.timely.util.sound.SoundUtils;
import com.google.android.material.textfield.TextInputLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AddScheduledDialog extends DialogFragment implements View.OnClickListener {
   public static final String ARG_TIME = "Scheduled time";
   public static final String ARG_COURSE = "Course code";
   public static final String ARG_DAY = "Schedule Repeat day";
   private static String selectedDay = DAYS[0];
   private AutoCompleteTextView atv_courseName;
   private EditText edt_startTime, edt_endTime, edt_lecturerName;
   private RadioGroup imp_group;
   private CheckBox cbx_clear;
   private FragmentManager manager;

   public void show(Context context) {
      manager = ((FragmentActivity) context).getSupportFragmentManager();
      show(manager, AddScheduledDialog.class.getName());
   }

   public void show(Context context, boolean toEdit, TimetableModel timetable) {
      Bundle bundle = new Bundle();
      bundle.putBoolean(ARG_TO_EDIT, toEdit);
      bundle.putSerializable(ARG_DATA, timetable);
      setArguments(bundle);
      FragmentManager manager = ((FragmentActivity) context).getSupportFragmentManager();
      show(manager, AddScheduledDialog.class.getName());
   }

   @NonNull
   @Override
   public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
      return new ASDialog(getContext());
   }

   @Override
   public void onClick(View v) {
      dismiss();
   }

   private boolean registerAndClose() {
      boolean isRegistered = registerTimetable();
      if (isRegistered) dismiss();
      return isRegistered;
   }

   private boolean registerTimetable() {
      SchoolDatabase database = new SchoolDatabase(getContext());
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

      String timeRegex24 = PatternUtils._24_HoursClock;
      String timeRegex12 = PatternUtils._12_HoursClock;

      boolean errorOccurred = false, use24 = isUserPreferred24Hours(getContext());

      if (use24 && !start.matches(timeRegex24)) {
         ViewParent container = edt_startTime.getParent();
         TextInputLayout edt_startTimeParent = ((TextInputLayout) container.getParent());
         edt_startTimeParent.setError("Format: HH:SS");
         errorOccurred = true;
      } else {
         if (!use24 && !start.matches(timeRegex12)) {
            ViewParent container = edt_startTime.getParent();
            TextInputLayout edt_startTimeParent = ((TextInputLayout) container.getParent());
            edt_startTimeParent.setError("12 hours mode");
            errorOccurred = true;
         }
      }

      if (use24 && !end.matches(timeRegex24)) {
         ViewParent container = edt_endTime.getParent();
         TextInputLayout edt_endTimeParent = ((TextInputLayout) container.getParent());
         edt_endTimeParent.setError("Format: HH:SS");
         errorOccurred = true;
      } else {
         if (!use24 && !end.matches(timeRegex12)) {
            ViewParent container = edt_endTime.getParent();
            TextInputLayout edt_endTimeParent = ((TextInputLayout) container.getParent());
            edt_endTimeParent.setError("12 hours mode");
            errorOccurred = true;
         }
      }

      if (TextUtils.isEmpty(course)) {
         ViewParent container = atv_courseName.getParent();
         TextInputLayout atv_courseNameParent = ((TextInputLayout) container.getParent());
         atv_courseNameParent.setError("Field Required");
         errorOccurred = true;
      }

      if (TextUtils.isEmpty(lecturerName)) {
         ViewParent container = edt_lecturerName.getParent();
         TextInputLayout edt_lecturerNameParent = ((TextInputLayout) container.getParent());
         edt_lecturerNameParent.setError("Field Required");
         errorOccurred = true;
      }

      if (errorOccurred) {
         return false;
      }

      start = use24 ? start : convertTime(start, Converter.UNIT_24);
      end = use24 ? end : convertTime(end, Converter.UNIT_24);

      TimetableModel newTimetable = new TimetableModel(lecturerName, course, start, end, code, importance, selectedDay);

      Context context = getContext();
      if (getArguments() != null && getArguments().getBoolean(ARG_TO_EDIT)) {
         TimetableModel formerTimetable = (TimetableModel) getArguments().getSerializable(ARG_DATA);
         boolean updated = database.updateTimetableData(newTimetable, SchoolDatabase.SCHEDULED_TIMETABLE);

         if (updated) {
            newTimetable.setId(formerTimetable.getId());
            newTimetable.setChronologicalOrder(formerTimetable.getChronologicalOrder());
            // after updating database, update the UI and re-schedule notification
            cancelTimetableNotifier(context, formerTimetable);
            EventBus.getDefault()
                    .post(new SUpdateMessage(newTimetable, SUpdateMessage.EventType.UPDATE_CURRENT));
            scheduleTimetableAlarm(context, newTimetable);
         } else Toast.makeText(context, "An Error Occurred", Toast.LENGTH_SHORT).show();

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
                          .post(new SUpdateMessage(newTimetable, SUpdateMessage.EventType.NEW));
                  scheduleTimetableAlarm(context, newTimetable);
               } else {
                  Toast.makeText(context, "An Error Occurred", Toast.LENGTH_SHORT).show();
               }
            });
         } else {
            // Error message
            ErrorDialog.Builder builder = new ErrorDialog.Builder();
            builder.setDialogMessage("Duplicate start time present")
                   .setShowSuggestions(false);
            new ErrorDialog().showErrorMessage(context, builder.build());
         }
      }
      database.close();
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
      Intent timetableIntent = new Intent(context, ScheduledTaskNotifier.class);
      timetableIntent.addCategory("com.astrro.timely.scheduled")
                     .setAction("com.astrro.timely.scheduled.addAction")
                     .setDataAndType(Uri.parse("content://com.astrro.timely.scheduled.add." + timeInMillis),
                                     "com.astrro.timely.scheduled.dataType");

      PendingIntent pi = PendingIntent.getBroadcast(context, 1156, timetableIntent,
                                                    PendingIntent.FLAG_CANCEL_CURRENT);
      pi.cancel();
      manager.cancel(pi);
   }

   private void scheduleTimetableAlarm(Context context, TimetableModel timetable) {
      AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !manager.canScheduleExactAlarms()) {
         // schedule exact alarm access denied on Android 12 and above
         AlarmHelper.showInContextUI(getContext());
         return;
      }

      String[] sTime = timetable.getStartTime().split("[: ]");
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


      Intent scheduleIntent = new Intent(context, ScheduledTaskNotifier.class)
              .putExtra(ARG_TIME, timetable.getStartTime())
              .putExtra(ARG_COURSE, course)
              .putExtra(ARG_DAY, day)
              .addCategory("com.astrro.timely.scheduled")
              .setAction("com.astrro.timely.scheduled.addAction")
              .setDataAndType(Uri.parse("content://com.astrro.timely.scheduled.add." + triggerTime),
                              "com.astrro.timely.scheduled.dataType");

      PendingIntent pi = PendingIntent.getBroadcast(context, 1156, scheduleIntent, 0);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC, triggerTime, pi);
         } else {
            manager.setExact(AlarmManager.RTC, triggerTime, pi);
         }
      } else {
         manager.set(AlarmManager.RTC, triggerTime, pi);
      }
      SoundUtils.playAlertTone(context.getApplicationContext(), AlertType.SCHEDULED_TIMETABLE);
   }

   private class ASDialog extends Dialog {

      public ASDialog(Context context) {
         super(context, R.style.Dialog);
      }

      @Override
      protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         getWindow().requestFeature(Window.FEATURE_NO_TITLE);
         getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_edges);
         setContentView(R.layout.dialog_add_scheduled);
         setCanceledOnTouchOutside(false);
         cbx_clear = findViewById(R.id.clear);
         CheckBox cbx_multiple = findViewById(R.id.multiple);

         findViewById(R.id.cancel).setOnClickListener(AddScheduledDialog.this);
         findViewById(R.id.register).setOnClickListener(v -> {
            boolean success = cbx_multiple.isChecked() ? registerAndClear()
                                                       : registerAndClose();
            int m;
            if (getArguments() != null && getArguments().getBoolean(ARG_TO_EDIT))
               m = R.string.update_pending;
            else m = R.string.registration_pending;

            if (success) {
               Toast message = Toast.makeText(getContext(), m, Toast.LENGTH_SHORT);
               if (!cbx_multiple.isChecked()) message.setGravity(Gravity.CENTER, 0, 0);
               message.show();
            } else Toast.makeText(getContext(), "Error occurred", Toast.LENGTH_SHORT).show();
         });

         atv_courseName = findViewById(R.id.course_name);
         edt_startTime = findViewById(R.id.start_time);
         edt_endTime = findViewById(R.id.end_time);
         edt_lecturerName = findViewById(R.id.lecturer_name);
         imp_group = findViewById(R.id.importance_group);

         edt_endTime.setOnTouchListener(this::onTouch);
         edt_startTime.setOnTouchListener(this::onTouch);

         SchoolDatabase database = new SchoolDatabase(getContext());
         ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(getContext(),
                                                                 R.layout.simple_dropdown_item_1line,
                                                                 database.getAllRegisteredCourses());

         courseAdapter.setDropDownViewResource(R.layout.simple_dropdown_item_1line);
         atv_courseName.setAdapter(courseAdapter);

         Spinner spin_days = findViewById(R.id.day_spin);
         ArrayAdapter<String> daysAdapter = new ArrayAdapter<>(getContext(),
                                                               R.layout.simple_spinner_item,
                                                               DAYS);
         daysAdapter.setDropDownViewResource(R.layout.simple_dropdown_item_1line);
         spin_days.setAdapter(daysAdapter);
         spin_days.setOnItemSelectedListener(new SimpleOnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
               selectedDay = DAYS[pos];
            }

         });

         if (getArguments() != null && getArguments().getBoolean(ARG_TO_EDIT)) {
            TimetableModel data = (TimetableModel) getArguments().getSerializable(ARG_DATA);

            if (!isUserPreferred24Hours(getContext())) {
               data.setStartTime(convertTime(data.getStartTime(), UNIT_12));
               data.setEndTime(convertTime(data.getEndTime(), UNIT_12));
            }

            edt_endTime.setText(data.getEndTime());
            edt_startTime.setText(data.getStartTime());

            atv_courseName.setText(data.getFullCourseName());
            edt_lecturerName.setText(data.getLecturerName());
            spin_days.setSelection(data.getDayIndex());
            switch (data.getImportance()) {
               case "Not Important":
                  imp_group.check(R.id.not_important);
                  break;
               case "Less Important":
                  imp_group.check(R.id.less_important);
                  break;
               default:
                  imp_group.check(R.id.very_important);
            }
         }
         database.close();
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

            String parsedTime = isUserPreferred24Hours(getContext()) ? timeFormat24.format(calendar.getTime())
                                                                     : timeFormat12.format(calendar.getTime());

            editText.setText(parsedTime);
         };

         final int DRAWABLE_RIGHT = 2;

         if (event.getAction() == MotionEvent.ACTION_UP) {
            int padding;
            int drawableWidth = editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
               padding = editText.getPaddingEnd();
            else padding = editText.getPaddingRight();

            if (event.getX() >= (editText.getWidth() - drawableWidth - padding)) {
               Calendar calendar = Calendar.getInstance();

               TimePickerDialog dpd = TimePickerDialog.newInstance(tsl,
                                                                   calendar.get(Calendar.HOUR_OF_DAY),
                                                                   calendar.get(Calendar.MINUTE),
                                                                   isUserPreferred24Hours(getContext()));
               dpd.setVersion(TimePickerDialog.Version.VERSION_2);
               dpd.show(manager, "TimePickerDialog");
               return true;
            }
         }
         return false;
      }
   }
}