package com.noah.timely.exam;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.noah.timely.R;
import com.noah.timely.core.DataModel;
import com.noah.timely.core.RequestRunner;
import com.noah.timely.error.ErrorDialog;

import java.util.List;
import java.util.Locale;

import static com.noah.timely.util.Utility.isUserPreferred24Hours;

public class ExamRowHolder extends RecyclerView.ViewHolder {
    public static final String DELETE_REQUEST = "Delete Exam";
    private static final int[] COLORS_2 = {
            android.R.color.holo_purple,
            R.color.pink,
            android.R.color.holo_green_light,
            android.R.color.holo_blue_dark,
            android.R.color.holo_orange_dark
    };
    private static final int[] DRAWABLE = {
            R.drawable.rounded_cl_pu,
            R.drawable.rounded_cl_pi,
            R.drawable.rounded_cl_gl,
            R.drawable.rounded_cl_bd,
            R.drawable.rounded_cl_od
    };
    private ExamTimetableFragment user;
    private ExamTimetableFragment.ExamRowAdapter examRowAdapter;
    private List<DataModel> eList;
    private CoordinatorLayout coordinator;
    private final View leftIndicator, rightIndicator;
    private final TextView tv_time, tv_courseName, tv_courseCode, tv_examDay;
    private final View v_selectionOverlay;
    private boolean isChecked;
    private ExamModel exam;
    private final ImageButton btn_deleteExam;

    public ExamRowHolder(@NonNull View rootView) {
        super(rootView);
        leftIndicator = rootView.findViewById(R.id.left_indicator);
        rightIndicator = rootView.findViewById(R.id.right_indicator);
        tv_time = rootView.findViewById(R.id.time);
        tv_courseName = rootView.findViewById(R.id.course_name);
        tv_courseCode = rootView.findViewById(R.id.course_code);
        tv_examDay = rootView.findViewById(R.id.exam_day);
        v_selectionOverlay = rootView.findViewById(R.id.checked_overlay);
        btn_deleteExam = rootView.findViewById(R.id.delete_exam);

        btn_deleteExam.setOnClickListener(v -> {
            RequestRunner runner = RequestRunner.createInstance();
            RequestRunner.Builder builder = new RequestRunner.Builder();
            builder.setOwnerContext(user.getActivity())
                   .setModelList(eList)
                   .setAdapterPosition(getAbsoluteAdapterPosition());

            runner.setRequestParams(builder.getParams())
                  .runRequest(DELETE_REQUEST);

            Snackbar bar = Snackbar.make(coordinator, "Exam Deleted", Snackbar.LENGTH_LONG);
            bar.setActionTextColor(Color.YELLOW);
            bar.setAction("Undo", x -> runner.undoRequest());
            bar.show();
        });

        tv_courseCode.setOnClickListener(v -> {
            if (TextUtils.equals(tv_courseCode.getText(), "NIL")) {
                ErrorDialog.Builder builder = new ErrorDialog.Builder();
                builder.setDialogMessage("No matching course code found")
                       .setShowSuggestions(true)
                       .setSuggestionCount(2)
                       .setSuggestion1("Register courses first")
                       .setSuggestion2("After registration, use that course title");
                new ErrorDialog().showErrorMessage(user.getContext(), builder.build());
            }
        });

        // Multi - Select actions
        rootView.setOnLongClickListener(l -> {
            trySelectExam();
            examRowAdapter.setMultiSelectionEnabled(!examRowAdapter.isMultiSelectionEnabled()
                                                            || examRowAdapter.getCheckedCoursesCount() != 0);
            return true;
        });

        rootView.setOnClickListener(c -> {
            if (examRowAdapter.isMultiSelectionEnabled()) {
                trySelectExam();
                if (examRowAdapter.getCheckedCoursesCount() == 0) {
                    examRowAdapter.setMultiSelectionEnabled(false);
                }
            }
        });
    }

    // Disable click on views not allowed to fire View#onClick while in multi-selection mode
    private void tryDisableViews(boolean disable) {
        btn_deleteExam.setFocusable(!disable);
        btn_deleteExam.setEnabled(!disable);
    }

    private void trySelectExam() {
        isChecked = !isChecked;
        v_selectionOverlay.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        examRowAdapter.onChecked(getAbsoluteAdapterPosition(), isChecked, exam.getId());
    }

    public ExamRowHolder with(ExamTimetableFragment user,
                              ExamTimetableFragment.ExamRowAdapter examRowAdapter,
                              List<DataModel> eList,
                              CoordinatorLayout coordinator) {
        this.user = user;
        this.examRowAdapter = examRowAdapter;
        this.eList = eList;
        this.exam = (ExamModel) eList.get(getAbsoluteAdapterPosition());
        this.coordinator = coordinator;
        return this;
    }

    void bindView() {
        Context context = user.getContext();
        int rightColor = ContextCompat.getColor(context, COLORS_2[exam.getDayIndex()]);
        Drawable leftDrawable = ContextCompat.getDrawable(context, DRAWABLE[exam.getDayIndex()]);

        leftIndicator.setBackground(leftDrawable);
        rightIndicator.setBackgroundColor(rightColor);

        tv_courseCode.setText(exam.getCourseCode());
        tv_courseName.setText(exam.getCourseName());
        tv_examDay.setText(exam.getDay());

        String start, end;
        if (isUserPreferred24Hours(context)) {
            start = exam.getStart();
            end = exam.getEnd();
        } else {
            start = convertTime(exam.getStart());
            end = convertTime(exam.getEnd());
        }
        tv_time.setText(String.format("%s - %s", start, end));
        tryDisableViews(examRowAdapter.isMultiSelectionEnabled());
    }

    // Convert to 12 hours clock format
    private String convertTime(String time) {
        try {
            String[] st = time.split(":");
            int hh = Integer.parseInt(st[0]);
            int mm = Integer.parseInt(st[1]);

            String formattedHrAM = String.format(Locale.US, "%02d", (hh == 0 ? 12 : hh));
            String formattedHrPM = String.format(Locale.US, "%02d", (hh % 12 == 0 ? 12 : hh % 12));
            String formattedMinAM = String.format(Locale.US, "%02d", mm) + " AM";
            String formattedMinPM = String.format(Locale.US, "%02d", mm) + " PM";

            boolean isAM = hh >= 0 && hh < 12;

            return isAM ? formattedHrAM + ":" + formattedMinAM
                        : formattedHrPM + ":" + formattedMinPM;
        } catch (NumberFormatException exc) {
            return null;
        }
    }
}
