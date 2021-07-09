package com.noah.timely.courses;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.noah.timely.R;
import com.noah.timely.util.Utility.Alert;
import com.noah.timely.core.SchoolDatabase;
import com.noah.timely.util.ThreadUtils;
import com.noah.timely.error.ErrorDialog;

import org.greenrobot.eventbus.EventBus;

import static com.noah.timely.util.Utility.playAlertTone;
import static com.noah.timely.courses.SemesterFragment.ARG_POSITION;

@SuppressWarnings("ConstantConditions")
public class AddCourseDialog extends DialogFragment implements View.OnClickListener {
    private EditText edt_courseName, edt_courseCode;
    private RadioGroup grp_semesterGroup;
    private int mCredits;
    private CheckBox cbx_clear;
    private SchoolDatabase database;

    public void show(Context context, int pagePosition) {
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_POSITION, pagePosition);
        setArguments(bundle);
        FragmentManager manager = ((FragmentActivity) context).getSupportFragmentManager();
        show(manager, AddCourseDialog.class.getName());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        database = new SchoolDatabase(getContext());
        return new ACDialog(getContext());
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }

    private boolean registerAndClose() {
        boolean registered = registerCourse();
        if (registered)
            dismiss();
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

    private boolean registerCourse() {
        String courseName = edt_courseName.getText().toString();
        String courseCode = edt_courseCode.getText().toString();
        int credit = mCredits;

        boolean errorOccurred = false;
        if (TextUtils.isEmpty(courseName)) {
            edt_courseName.setError("Field required");
            errorOccurred = true;
        }
        if (TextUtils.isEmpty(courseCode)) {
            edt_courseCode.setError("Field required");
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
        Context context = getContext();

        if (database.isCourseAbsent(model)) {
            ThreadUtils.runBackgroundTask(() -> {
                int[] data = database.addCourse(model, semester);
                int addPos = data[1];
                if (addPos != -1) {
                    model.setId(addPos);
                    model.setChronologicalOrder(data[0]);
                    playAlertTone(context.getApplicationContext(), Alert.COURSE);
                    EventBus.getDefault().post(new CUpdateMessage(model, CUpdateMessage.EventType.NEW,
                                                                  pagePosition1));
                } else {
                    Toast.makeText(context, "An Error occurred", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            ErrorDialog.Builder errorBuilder = new ErrorDialog.Builder();
            errorBuilder.setDialogMessage("Duplicate course found")
                        .setShowSuggestions(false);
            new ErrorDialog().showErrorMessage(context, errorBuilder.build());
        }
        return true;
    }

    @Override
    public void onDestroyView() {
        database.close();
        super.onDestroyView();
    }

    private class ACDialog extends Dialog {
        private final Integer[] credits = {
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                21, 22, 23, 24, 25, 26, 27, 28, 29, 30,
                31, 32, 33, 34, 35, 36, 37, 38, 39, 40};

        public ACDialog(@NonNull Context context) {
            super(context, R.style.Dialog);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_edges);
            setContentView(R.layout.dialog_add_course);
            setCanceledOnTouchOutside(false);
            findViewById(R.id.cancel).setOnClickListener(AddCourseDialog.this);
            edt_courseCode = findViewById(R.id.course_code);
            edt_courseName = findViewById(R.id.course_name);
            grp_semesterGroup = findViewById(R.id.semester_group);
            RadioButton rd_firstSemester = findViewById(R.id.first);
            RadioButton rd_secondSemester = findViewById(R.id.second);
            CheckBox cbx_multiple = findViewById(R.id.multiple);
            cbx_clear = findViewById(R.id.clear);

            if (getArguments().getInt(ARG_POSITION) == 0) {
                rd_firstSemester.setChecked(true);
            } else rd_secondSemester.setChecked(true);

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
                    Toast.makeText(getContext(), "An Error occurred", Toast.LENGTH_SHORT).show();

            });

            Spinner spin_credits = findViewById(R.id.credits);
            ArrayAdapter<Integer> creditAdapter
                    = new ArrayAdapter<>(getContext(),
                                         android.R.layout.simple_spinner_item,
                                         credits);
            creditAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
            spin_credits.setAdapter(creditAdapter);
            spin_credits.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position,
                                           long id) {
                    mCredits = credits[position];
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }
    }
}
