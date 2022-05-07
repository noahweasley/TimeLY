package com.noah.timely.exam;

import static com.noah.timely.util.Converter.convertTime;
import static com.noah.timely.util.MiscUtil.DAYS_3;
import static com.noah.timely.util.MiscUtil.isUserPreferred24Hours;
import static com.noah.timely.util.MiscUtil.playAlertTone;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
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

import com.google.android.material.textfield.TextInputLayout;
import com.noah.timely.R;
import com.noah.timely.core.SchoolDatabase;
import com.noah.timely.error.ErrorDialog;
import com.noah.timely.util.Converter;
import com.noah.timely.util.MiscUtil;
import com.noah.timely.util.PatternUtils;
import com.noah.timely.util.ThreadUtils;
import com.noah.timely.util.adapters.SimpleOnItemSelectedListener;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_add_exam);
      setSupportActionBar(findViewById(R.id.toolbar));
      getSupportActionBar().setTitle(R.string.register_exams);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_keyboard_arrow_down_24);
      getSupportActionBar().setHomeActionContentDescription(R.string.pull_down);

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
      edt_startTime.setOnTouchListener(this::onTouch);

      ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this,
                                                              R.layout.simple_dropdown_item_1line,
                                                              database.getAllRegisteredCourses());
      courseAdapter.setDropDownViewResource(R.layout.simple_dropdown_item_1line);
      atv_courseName.setAdapter(courseAdapter);

      Spinner spin_days = findViewById(R.id.day_spin);
      ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this,
                                                           R.layout.simple_spinner_item,
                                                           DAYS_3);

      dayAdapter.setDropDownViewResource(R.layout.simple_dropdown_item_1line);
      spin_days.setAdapter(dayAdapter);
      spin_days.setOnItemSelectedListener(new SimpleOnItemSelectedListener() {
         @Override
         public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            examDay = DAYS_3[pos];
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
         int padding;
         int drawableWidth = editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();

         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            padding = editText.getPaddingEnd();
         else padding = editText.getPaddingRight();

         if (event.getX() >= (editText.getWidth() - drawableWidth - padding)) {
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

   @Override
   public boolean onSupportNavigateUp() {
      onBackPressed();
      return true;
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
      if (registered) onBackPressed();
      return registered;
   }

   private boolean registerExam() {
      String course = atv_courseName.getText().toString();
      String end = edt_endTime.getText().toString();
      String start = edt_startTime.getText().toString();
      String code = database.getCourseCodeFromName(course);

      String timeRegex24 = PatternUtils._24_HoursClock;
      String timeRegex12 = PatternUtils._12_HoursClock;

      boolean errorOccurred = false, use24 = isUserPreferred24Hours(this);

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

      if (errorOccurred) return false;

      start = use24 ? start : convertTime(start, Converter.UNIT_24);
      end = use24 ? end : convertTime(end, Converter.UNIT_24);

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
               playAlertTone(getApplicationContext(), MiscUtil.Alert.EXAM);
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
