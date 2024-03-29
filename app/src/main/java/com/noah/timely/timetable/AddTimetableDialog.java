package com.noah.timely.timetable;

import static com.noah.timely.timetable.DaysFragment.ARG_CHRONOLOGY;
import static com.noah.timely.timetable.DaysFragment.ARG_CLASS;
import static com.noah.timely.timetable.DaysFragment.ARG_DATA;
import static com.noah.timely.timetable.DaysFragment.ARG_DAY;
import static com.noah.timely.timetable.DaysFragment.ARG_PAGE_POSITION;
import static com.noah.timely.timetable.DaysFragment.ARG_POSITION;
import static com.noah.timely.timetable.DaysFragment.ARG_TIME;
import static com.noah.timely.timetable.DaysFragment.ARG_TO_EDIT;
import static com.noah.timely.util.Converter.convertTime;
import static com.noah.timely.util.MiscUtil.Alert.COURSE;
import static com.noah.timely.util.MiscUtil.DAYS;
import static com.noah.timely.util.MiscUtil.isUserPreferred24Hours;
import static com.noah.timely.util.MiscUtil.playAlertTone;

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

import com.noah.timely.R;
import com.noah.timely.core.SchoolDatabase;
import com.noah.timely.error.ErrorDialog;
import com.noah.timely.util.Converter;
import com.noah.timely.util.PatternUtils;
import com.noah.timely.util.ThreadUtils;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AddTimetableDialog extends DialogFragment implements View.OnClickListener {
   private static String selectedDay;
   private AutoCompleteTextView atv_courseName;
   private EditText edt_startTime, edt_endTime;
   private CheckBox cbx_clear;
   private FragmentManager manager;

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
      manager = ((FragmentActivity) context).getSupportFragmentManager();
      show(manager, AddTimetableDialog.class.getName());
   }

   /**
    * Make this dialog visible to the user
    *
    * @param context   the context in which the dialog is going to use to be added to the
    *                  activity's
    *                  fragment manager
    * @param toEdit    flag to be checked if it is an edit operation
    * @param timetable the former timetable data
    */
   public void show(Context context, boolean toEdit, TimetableModel timetable) {
      Bundle bundle = new Bundle();
      int pagePos = timetable.getDayIndex();
      bundle.putInt(ARG_PAGE_POSITION, pagePos);
      bundle.putBoolean(ARG_TO_EDIT, toEdit);
      bundle.putSerializable(ARG_DATA, timetable);
      setArguments(bundle);
      manager = ((FragmentActivity) context).getSupportFragmentManager();
      show(manager, AddTimetableDialog.class.getName());
   }

   @NonNull
   @Override
   public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
      return new ATDialog(getContext());
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

   private boolean registerAndClear() {
      boolean isRegistered = registerTimetable();
      if (isRegistered && cbx_clear.isChecked()) {
         atv_courseName.setText(null);
         edt_startTime.setText(null);
         edt_endTime.setText(null);
      }
      return isRegistered;
   }

   private boolean registerTimetable() {
      SchoolDatabase database = new SchoolDatabase(getContext());
      String course = atv_courseName.getText().toString();
      String end = edt_endTime.getText().toString();
      String start = edt_startTime.getText().toString();
      String code = database.getCourseCodeFromName(course);

      String timeRegex24 = PatternUtils._24_HoursClock;
      String timeRegex12 = PatternUtils._12_HoursClock;

      boolean errorOccurred = false, use24 = isUserPreferred24Hours(getContext());

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

      start = use24 ? start : convertTime(start, Converter.UNIT_24);
      end = use24 ? end : convertTime(end, Converter.UNIT_24);

      int pagePosition = getPagePosition();
      TimetableModel formerTimetable = (TimetableModel) getArguments().getSerializable(ARG_DATA);
      TimetableModel timetable = new TimetableModel(course, start, end, code, false);
      timetable.setDay(selectedDay);

      if (getArguments().getBoolean(ARG_TO_EDIT)) {
         Context context = getContext();
         boolean updated = database.updateTimetableData(timetable, timetable.getDay());

         if (updated) {
            cancelTimetableNotifier(context, formerTimetable); // cancel former alarm first
            timetable.setId(formerTimetable.getId());
            timetable.setChronologicalOrder(formerTimetable.getChronologicalOrder());
            EventBus.getDefault().post(new TUpdateMessage(timetable, pagePosition,
                                                          TUpdateMessage.EventType.UPDATE_CURRENT));
            scheduleTimetableAlarm(getContext(), timetable, pagePosition);
         } else Toast.makeText(getContext(), "An Error Occurred", Toast.LENGTH_LONG).show();

      } else {
         Context context = getContext();
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
                  scheduleTimetableAlarm(context, timetable, pagePosition);
               } else {
                  Toast.makeText(context, "An Error Occurred", Toast.LENGTH_LONG).show();
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

      PendingIntent pi = PendingIntent.getBroadcast(context, 555, timetableIntent, PendingIntent.FLAG_CANCEL_CURRENT);

      pi.cancel();
      manager.cancel(pi);
   }

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

      PendingIntent pi = PendingIntent.getBroadcast(context, 555, timetableIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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

   private int getTabPosition() {
      return getArguments().getInt(ARG_PAGE_POSITION);
   }

   private class ATDialog extends Dialog {

      public ATDialog(@NonNull Context context) {
         super(context, R.style.Dialog);
      }

      @Override
      protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         getWindow().requestFeature(Window.FEATURE_NO_TITLE);
         getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_edges);
         setContentView(R.layout.dialog_add_timetable);
         setCanceledOnTouchOutside(false);
         cbx_clear = findViewById(R.id.clear);
         CheckBox cbx_multiple = findViewById(R.id.multiple);
         findViewById(R.id.cancel).setOnClickListener(AddTimetableDialog.this);
         findViewById(R.id.register).setOnClickListener(v -> {
            boolean success = cbx_multiple.isChecked() ? registerAndClear()
                                                       : registerAndClose();
            int m;
            if (getArguments().getBoolean(ARG_TO_EDIT)) m = R.string.update_pending;
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

         edt_startTime.setOnTouchListener(this::onTouch);
         edt_endTime.setOnTouchListener(this::onTouch);

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

         if (getArguments().getBoolean(ARG_TO_EDIT)) {
            TimetableModel data = (TimetableModel) getArguments().getSerializable(ARG_DATA);
            getArguments().putInt(ARG_CHRONOLOGY, data.getChronologicalOrder());

            if (!isUserPreferred24Hours(getContext())) {
               data.setStartTime(convertTime(data.getStartTime(), Converter.UNIT_12));
               data.setEndTime(convertTime(data.getEndTime(), Converter.UNIT_12));
            }

            edt_endTime.setText(data.getEndTime());
            edt_startTime.setText(data.getStartTime());

            atv_courseName.setText(data.getFullCourseName());
            spin_days.setSelection(data.getDayIndex());
         } else {
            spin_days.setSelection(getTabPosition());
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