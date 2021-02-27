package com.projects.timely.courses;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.projects.timely.R;
import com.projects.timely.core.DataModel;
import com.projects.timely.core.RequestRunner;
import com.projects.timely.courses.SemesterFragment.CourseAdapter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

@SuppressWarnings("all")
public class CourseRowHolder extends RecyclerView.ViewHolder {
    public static final String DELETE_REQUEST = "Delete course";
    private static final int[] COLORS = {
            android.R.color.holo_blue_bright,
            android.R.color.holo_orange_light,
            R.color.pink,
            android.R.color.holo_green_dark,
            android.R.color.holo_purple,
            android.R.color.holo_green_light,
            android.R.color.holo_blue_dark,
            android.R.color.holo_orange_dark,
            android.R.color.holo_red_light
    };
    private SemesterFragment user;
    private CourseAdapter courseAdapter;
    private List<DataModel> cList;
    private CourseModel cModel;
    private CoordinatorLayout coordinator;
    private int position;
    private View lIndicator;
    private ImageButton btn_deleteCourse;
    private TextView tv_courseCode, tv_courseName, tv_credits;

    public CourseRowHolder(@NonNull View itemView) {
        super(itemView);
        lIndicator = itemView.findViewById(R.id.left_indicator);
        btn_deleteCourse = itemView.findViewById(R.id.delete_course);
        tv_courseName = itemView.findViewById(R.id.course_name);
        tv_courseCode = itemView.findViewById(R.id.course_code);
        tv_credits = itemView.findViewById(R.id.credits);

        btn_deleteCourse.setOnClickListener(v -> {
            RequestRunner runner = RequestRunner.getInstance();
            runner.with(user.getActivity(),
                        this,
                        courseAdapter,
                        cList)
                    .runRequest(DELETE_REQUEST);
            Snackbar bar = Snackbar.make(coordinator, "Course Deleted", Snackbar.LENGTH_LONG);
            bar.setActionTextColor(Color.YELLOW);
            bar.setAction("Undo", x -> runner.undoRequest());
            bar.show();
        });

    }

    public CourseRowHolder with(SemesterFragment user,
                                CourseAdapter courseAdapter,
                                List<DataModel> cList,
                                CoordinatorLayout coordinator,
                                int position) {
        this.user = user;
        this.courseAdapter = courseAdapter;
        this.cList = cList;
        this.cModel = (CourseModel) cList.get(position);
        this.coordinator = coordinator;
        this.position = position;
        return this;
    }

    public void bindView() {
        Context context = user.getContext();
        int rowColor = COLORS[ getAbsoluteAdapterPosition() % COLORS.length];
        lIndicator.setBackgroundColor(ContextCompat.getColor(context, rowColor));
        tv_courseCode.setText(cModel.getCourseCode());
        tv_courseName.setText(cModel.getCourseName());
        String credit = String.valueOf(cModel.getCredits() + " credits");
        tv_credits.setText(credit);
        TooltipCompat.setTooltipText(btn_deleteCourse, "Delete");
    }
}
