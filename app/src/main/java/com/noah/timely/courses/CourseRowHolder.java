package com.noah.timely.courses;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.noah.timely.R;
import com.noah.timely.core.DataModel;
import com.noah.timely.core.RequestRunner;
import com.noah.timely.courses.SemesterFragment.CourseAdapter;

import java.util.List;

public class CourseRowHolder extends RecyclerView.ViewHolder {
    public static final String DELETE_REQUEST = "Delete course";
    private static final int[] DRAWABLE = {
            R.drawable.rounded_cl_bb,
            R.drawable.rounded_cl_ol,
            R.drawable.rounded_cl_pi,
            R.drawable.rounded_cl_gd,
            R.drawable.rounded_cl_pu,
            R.drawable.rounded_cl_gl,
            R.drawable.rounded_cl_bd,
            R.drawable.rounded_cl_od,
            R.drawable.rounded_cl_rl,
            };
    private SemesterFragment user;
    private CourseAdapter courseAdapter;
    private List<DataModel> cList;
    private CourseModel cModel;
    private CoordinatorLayout coordinator;
    private final View lIndicator;
    private final ImageButton btn_deleteCourse;
    private final TextView tv_courseCode, tv_courseName, tv_credits;
    private final View v_selectionOverlay;
    private boolean isChecked;
    private CourseModel course;
    private int pagePosition;

    public CourseRowHolder(@NonNull View rootView) {
        super(rootView);
        lIndicator = rootView.findViewById(R.id.left_indicator);
        btn_deleteCourse = rootView.findViewById(R.id.delete_course);
        tv_courseName = rootView.findViewById(R.id.course_name);
        tv_courseCode = rootView.findViewById(R.id.course_code);
        tv_credits = rootView.findViewById(R.id.credits);
        v_selectionOverlay = rootView.findViewById(R.id.checked_overlay);

        btn_deleteCourse.setOnClickListener(v -> {
            RequestRunner runner = RequestRunner.createInstance();
            RequestRunner.Builder builder = new RequestRunner.Builder();
            builder.setOwnerContext(user.getActivity())
                   .setPagePosition(pagePosition)
                   .setAdapterPosition(getAbsoluteAdapterPosition())
                   .setModelList(cList);

            runner.setRequestParams(builder.getParams())
                  .runRequest(DELETE_REQUEST);

            Snackbar bar = Snackbar.make(coordinator, "Course Deleted", Snackbar.LENGTH_LONG);
            bar.setActionTextColor(Color.YELLOW);
            bar.setAction("Undo", x -> runner.undoRequest());
            bar.show();
        });

        // Multi - Select actions
        rootView.setOnLongClickListener(l -> {
            trySelectCourse();
            courseAdapter.setMultiSelectionEnabled(!courseAdapter.isMultiSelectionEnabled()
                                                           || courseAdapter.getCheckedCoursesCount() != 0);
            return true;
        });

        rootView.setOnClickListener(c -> {
            if (courseAdapter.isMultiSelectionEnabled()) {
                trySelectCourse();
                if (courseAdapter.getCheckedCoursesCount() == 0) {
                    courseAdapter.setMultiSelectionEnabled(false);
                }
            }
        });
    }

    private void trySelectCourse() {
        isChecked = !isChecked;
        v_selectionOverlay.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        courseAdapter.onChecked(getAbsoluteAdapterPosition(), isChecked, course.getId());
    }

    public CourseRowHolder with(SemesterFragment user,
                                CourseAdapter courseAdapter,
                                List<DataModel> cList,
                                int pagePosition,
                                CoordinatorLayout coordinator) {
        this.user = user;
        this.courseAdapter = courseAdapter;
        this.cList = cList;
        this.course = (CourseModel) cList.get(getAbsoluteAdapterPosition());
        this.cModel = (CourseModel) cList.get(getAbsoluteAdapterPosition());
        this.coordinator = coordinator;
        this.pagePosition = pagePosition;
        return this;
    }

    // Disable click on views not allowed to fire View#onClick while in multi-selection mode
    private void tryDisableViews(boolean disable) {
        btn_deleteCourse.setFocusable(!disable);
        btn_deleteCourse.setEnabled(!disable);
    }

    public void bindView() {
        Context context = user.getContext();
        int rowDrawable = DRAWABLE[getAbsoluteAdapterPosition() % DRAWABLE.length];
        lIndicator.setBackground(ContextCompat.getDrawable(context, rowDrawable));
        tv_courseCode.setText(cModel.getCourseCode());
        tv_courseName.setText(cModel.getCourseName());
        String credit = cModel.getCredits() + " credits";
        tv_credits.setText(credit);
        TooltipCompat.setTooltipText(btn_deleteCourse, "Delete");
        isChecked = courseAdapter.isChecked(getAbsoluteAdapterPosition());
        v_selectionOverlay.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        tryDisableViews(courseAdapter.isMultiSelectionEnabled());
    }
}