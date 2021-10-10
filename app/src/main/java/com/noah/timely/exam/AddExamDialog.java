package com.noah.timely.exam;

import static com.noah.timely.util.Converter.convertTime;
import static com.noah.timely.util.MiscUtil.Alert;
import static com.noah.timely.util.MiscUtil.DAYS_3;
import static com.noah.timely.util.MiscUtil.isUserPreferred24Hours;
import static com.noah.timely.util.MiscUtil.playAlertTone;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
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
import com.noah.timely.util.adapters.SimpleOnItemSelectedListener;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

@SuppressWarnings("ConstantConditions")
public class AddExamDialog extends DialogFragment implements View.OnClickListener {
   public static final int UNIT_12 = 12;
   public static final int UNIT_24 = 24;
   static final String ARG_PAGE_POSITION = "Tab position";
   private AutoCompleteTextView atv_courseName;
   private EditText edt_startTime, edt_endTime;
   private CheckBox cbx_clear;
   private String examDay;
   private SchoolDatabase database;
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
      show(manager, AddExamDialog.class.getName());
   }

   @NonNull
   @Override
   public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
      database = new SchoolDatabase(getContext());
      return new AETDialog(getContext());
   }

   @Override
   public void onDestroyView() {
      database.close();
      super.onDestroyView();
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

   private boolean registerExam() {
      String course = atv_courseName.getText().toString();
      String end = edt_endTime.getText().toString();
      String start = edt_startTime.getText().toString();
      String code = database.getCourseCodeFromName(course);

      String timeRegex24 = PatternUtils._24_HoursClock;
      String timeRegex12 = PatternUtils._12_HoursClock;

      boolean errorOccurred = false, use24 = isUserPreferred24Hours(getContext());

      if (use24 && !start.matches(timeRegex24)) {
         edt_startTime.setError("Format: hh:ss");
         errorOccurred = true;
      } else {
         if (!use24 && !start.matches(timeRegex12)) {
            edt_startTime.setError("12 hours mode");
            errorOccurred = true;
         }
      }

      if (use24 && !end.matches(timeRegex24)) {
         edt_endTime.setError("Format: hh:ss");
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

      start = use24 ? start : convertTime(start, Converter.UNIT_24);
      end = use24 ? end : convertTime(end, Converter.UNIT_24);

      int pagePosition = getArguments().getInt(ARG_PAGE_POSITION);

      String examWeek = String.format(Locale.US, "%s_%d", "WEEK", pagePosition + 1);
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
               EventBus.getDefault().post(new EUpdateMessage(exam, EUpdateMessage.EventType.NEW, pagePosition));
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
      return true;
   }


   @Override
   public void onClick(View v) {
      dismiss();
   }

   private class AETDialog extends Dialog {

      public AETDialog(@NonNull Context context) {
         super(context, R.style.Dialog);
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

         edt_endTime.setOnTouchListener(this::onTouch);
         edt_startTime.setOnTouchListener(this::onTouch);

         SchoolDatabase database = new SchoolDatabase(getContext());
         ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(getContext(),
                                                                 R.layout.simple_dropdown_item_1line,
                                                                 database.getAllRegisteredCourses());
         courseAdapter.setDropDownViewResource(R.layout.simple_dropdown_item_1line);
         atv_courseName.setAdapter(courseAdapter);

         Spinner spin_days = findViewById(R.id.day_spin);
         ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(getContext(),
                                                              R.layout.simple_spinner_item,
                                                              DAYS_3);
         dayAdapter.setDropDownViewResource(R.layout.simple_dropdown_item_1line);
         spin_days.setAdapter(dayAdapter);
         spin_days.setOnItemSelectedListener(new SimpleOnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                       long id) {
               examDay = DAYS_3[pos];
            }

         });

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
