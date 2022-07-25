package com.astrro.timely.courses;

import static com.astrro.timely.courses.SemesterFragment.ARG_POSITION;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.astrro.timely.R;
import com.astrro.timely.core.SchoolDatabase;
import com.astrro.timely.error.ErrorDialog;
import com.astrro.timely.util.ThreadUtils;
import com.astrro.timely.util.adapters.SimpleOnItemSelectedListener;
import com.astrro.timely.util.sound.AlertType;
import com.astrro.timely.util.sound.SoundUtils;
import com.google.android.material.textfield.TextInputLayout;

import org.greenrobot.eventbus.EventBus;

/**
 * A clone of {@link AddCourseDialog} that would be used as an alternate to adding courses
 */
public class AddCourseActivity extends AppCompatActivity {
   private final Integer[] CREDITS = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                                       21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40 };
   private SchoolDatabase database;
   private EditText edt_courseName, edt_courseCode;
   private RadioGroup grp_semesterGroup;
   private int mCredits;
   private CheckBox cbx_clear;

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_add_course);
      setSupportActionBar(findViewById(R.id.toolbar));
      getSupportActionBar().setTitle(R.string.register_courses);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_keyboard_arrow_down_24);
      getSupportActionBar().setHomeActionContentDescription(R.string.pull_down);

      database = new SchoolDatabase(this);

      edt_courseCode = findViewById(R.id.course_code);
      edt_courseName = findViewById(R.id.course_name);
      grp_semesterGroup = findViewById(R.id.semester_group);
      RadioButton rd_firstSemester = findViewById(R.id.first);
      RadioButton rd_secondSemester = findViewById(R.id.second);
      CheckBox cbx_multiple = findViewById(R.id.multiple);
      cbx_clear = findViewById(R.id.clear);

      if (getIntent().getIntExtra(ARG_POSITION, 0) == 0) {
         rd_firstSemester.setChecked(true);
      } else rd_secondSemester.setChecked(true);

      findViewById(R.id.register).setOnClickListener(v -> {
         boolean success = cbx_multiple.isChecked() ? registerAndClear() : registerAndClose();
         if (success) {
            Toast message = Toast.makeText(this, R.string.registration_pending, Toast.LENGTH_SHORT);
            if (!cbx_multiple.isChecked())
               message.setGravity(Gravity.CENTER, 0, 0);
            message.show();
         } else
            Toast.makeText(this, "An Error occurred", Toast.LENGTH_SHORT).show();

      });

      Spinner spin_credits = findViewById(R.id.credits);
      ArrayAdapter<Integer> creditAdapter = new ArrayAdapter<>(this, R.layout.simple_dropdown_item_1line, CREDITS);
      creditAdapter.setDropDownViewResource(R.layout.simple_dropdown_item_1line);
      spin_credits.setAdapter(creditAdapter);

      spin_credits.setOnItemSelectedListener(new SimpleOnItemSelectedListener() {
         @Override
         public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mCredits = CREDITS[position];
         }

      });
   }

   private boolean registerCourse() {
      String courseName = edt_courseName.getText().toString();
      String courseCode = edt_courseCode.getText().toString();
      int credit = mCredits;

      boolean errorOccurred = false;

      if (TextUtils.isEmpty(courseName)) {
         ViewParent container = edt_courseName.getParent();
         TextInputLayout edt_courseNameParent = ((TextInputLayout) container.getParent());
         edt_courseNameParent.setError("Field required");
         errorOccurred = true;
      }

      if (TextUtils.isEmpty(courseCode)) {
         ViewParent container = edt_courseCode.getParent();
         TextInputLayout edt_courseCodeParent = ((TextInputLayout) container.getParent());
         edt_courseCodeParent.setError("Field required");
         errorOccurred = true;
      }

      if (errorOccurred) return false;

      String semester;
      if (grp_semesterGroup.getCheckedRadioButtonId() == R.id.second) {
         semester = SchoolDatabase.SECOND_SEMESTER;
      } else {
         semester = SchoolDatabase.FIRST_SEMESTER;
      }

      int pagePosition;
      if (semester.equals(SchoolDatabase.SECOND_SEMESTER)) {
         pagePosition = 1;
      } else {
         pagePosition = 0;
      }

      CourseModel model = new CourseModel(semester, credit, courseCode, courseName);
      int pagePosition1 = pagePosition;

      if (database.isCourseAbsent(model)) {
         ThreadUtils.runBackgroundTask(() -> {
            int[] data = database.addCourse(model, semester);
            int addPos = data[1];

            if (addPos != -1) {
               model.setId(addPos);
               model.setChronologicalOrder(data[0]);
               SoundUtils.playAlertTone(getApplicationContext(), AlertType.COURSE);
               EventBus.getDefault().post(new CUpdateMessage(model, CUpdateMessage.EventType.NEW, pagePosition1));
            } else {
               Toast.makeText(this, "An Error occurred", Toast.LENGTH_LONG).show();
            }
         });

      } else {
         ErrorDialog.Builder errorBuilder = new ErrorDialog.Builder();
         errorBuilder.setDialogMessage("Duplicate course found");
         errorBuilder.setShowSuggestions(false);
         new ErrorDialog().showErrorMessage(this, errorBuilder.build());
      }

      return true;
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
      boolean registered = registerCourse();
      if (registered)
         onBackPressed();
      return registered;
   }

   private boolean registerAndClear() {
      boolean registered = registerCourse();
      if (cbx_clear.isChecked() && registered) {
         edt_courseName.setText(null);
         edt_courseCode.setText(null);
      }
      return registered;
   }
}
