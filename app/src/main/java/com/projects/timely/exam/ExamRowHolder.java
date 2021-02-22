package com.projects.timely.exam;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.projects.timely.R;
import com.projects.timely.core.DataModel;
import com.projects.timely.core.RequestRunner;
import com.projects.timely.error.ErrorDialog;

import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.os.ConfigurationCompat;
import androidx.recyclerview.widget.RecyclerView;

import static com.projects.timely.core.Globals.isUserPreferred24Hours;

@SuppressWarnings("ConstantConditions")
public class ExamRowHolder extends RecyclerView.ViewHolder {
    public static final String DELETE_REQUEST = "Delete Exam";
    private static final int[] COLORS_2 = {
            android.R.color.holo_purple,
            R.color.pink,
            android.R.color.holo_green_light,
            android.R.color.holo_blue_dark,
            android.R.color.holo_orange_dark
    };
    private ExamTimetableFragment user;
    private ExamTimetableFragment.ExamRowAdapter examRowAdapter;
    private List<DataModel> eList;
    private CoordinatorLayout coordinator;
    private View leftIndicator, rightIndicator;
    private TextView tv_time, tv_courseName, tv_courseCode, tv_examDay;

    public ExamRowHolder(@NonNull View itemView) {
        super(itemView);
        leftIndicator = itemView.findViewById(R.id.left_indicator);
        rightIndicator = itemView.findViewById(R.id.right_indicator);
        tv_time = itemView.findViewById(R.id.time);
        tv_courseName = itemView.findViewById(R.id.course_name);
        tv_courseCode = itemView.findViewById(R.id.course_code);
        tv_examDay = itemView.findViewById(R.id.exam_day);
        itemView.findViewById(R.id.delete_exam)
                .setOnClickListener(v -> {
                    RequestRunner runner = RequestRunner.getInstance();
                    runner.with(user.getActivity(),
                                this,
                                examRowAdapter,
                                eList)
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
                        .setSuggestion2("After registration, use that course title")
                        .setSuggestion1("Register courses first");
                new ErrorDialog().showErrorMessage(user.getContext(), builder.build());
            }
        });

    }

    public ExamRowHolder with(ExamTimetableFragment user,
                              ExamTimetableFragment.ExamRowAdapter examRowAdapter,
                              List<DataModel> eList,
                              CoordinatorLayout coordinator) {
        this.user = user;
        this.examRowAdapter = examRowAdapter;
        this.eList = eList;
        this.coordinator = coordinator;
        return this;
    }

    void bindView() {
        ExamModel examModel = (ExamModel) eList.get(getAdapterPosition());
        Context context = user.getContext();

        int indicatorColor = ContextCompat.getColor(context, COLORS_2[examModel.getDayIndex()]);
        leftIndicator.setBackgroundColor(indicatorColor);
        rightIndicator.setBackgroundColor(indicatorColor);
        tv_courseCode.setText(examModel.getCourseCode());
        tv_courseName.setText(examModel.getCourseName());
        tv_examDay.setText(examModel.getDay());

        String start, end;
        if (isUserPreferred24Hours(context)) {
            start = examModel.getStart();
            end = examModel.getEnd();
        } else {
            start = convertTime(examModel.getStart(), context);
            end = convertTime(examModel.getEnd(), context);
        }
        tv_time.setText(String.format("%s - %s", start, end));
    }

    // Convert to 12 hours clock format
    private String convertTime(String time, Context context) {
        try {
            String[] st = time.split(":");
            int hh = Integer.parseInt(st[0]);
            int mm = Integer.parseInt(st[1]);

            Resources aResources = context.getResources();
            Configuration config = aResources.getConfiguration();
            Locale locale = ConfigurationCompat.getLocales(config).get(0);

            String formattedHrAM = String.format(locale, "%02d", (hh == 0 ? 12 : hh));
            String formattedHrPM = String.format(locale, "%02d", (hh % 12 == 0 ? 12 : hh % 12));
            String formattedMinAM = String.format(locale, "%02d", mm) + " AM";
            String formattedMinPM = String.format(locale, "%02d", mm) + " PM";

            boolean isAM = hh >= 0 && hh < 12;

            return isAM ? formattedHrAM + ":" + formattedMinAM
                        : formattedHrPM + ":" + formattedMinPM;
        } catch (NumberFormatException exc) {
            return null;
        }
    }
}
