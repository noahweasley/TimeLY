package com.projects.timely.timetable;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.projects.timely.R;
import com.projects.timely.core.DataModel;
import com.projects.timely.core.RequestRunner;
import com.projects.timely.error.ErrorDialog;
import com.projects.timely.scheduled.AddScheduledDialog;
import com.projects.timely.scheduled.ScheduledTimetableFragment;
import com.projects.timely.VerticalTextView;

import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.os.ConfigurationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import static com.projects.timely.core.Globals.isUserPreferred24Hours;

@SuppressWarnings("ConstantConditions")
public class TimeTableRowHolder extends RecyclerView.ViewHolder {

    private static final int[] COLORS_1 = {
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
    private static final int[] COLORS_2 = {
            android.R.color.holo_purple,
            R.color.pink,
            android.R.color.holo_green_light,
            android.R.color.holo_blue_dark,
            android.R.color.holo_orange_dark,
            android.R.color.holo_red_light
    };
    private ImageView img_schImp;
    private View lIndicator, rIndicator;
    private TextView tv_time, tv_course, tv_lecturer, atv_FCN;
    private VerticalTextView tv_day;
    private Fragment user;
    private TimetableModel tModel;
    private List<DataModel> tList;
    private RecyclerView.Adapter<?> rowAdapter;
    private CoordinatorLayout coordinator;
    private String timetable;

    public TimeTableRowHolder(@NonNull View itemView) {
        super(itemView);
        lIndicator = itemView.findViewById(R.id.indicator);
        rIndicator = itemView.findViewById(R.id.indicator2);
        tv_time = itemView.findViewById(R.id.time);
        tv_course = itemView.findViewById(R.id.subject);
        tv_lecturer = itemView.findViewById(R.id.name);
        atv_FCN = itemView.findViewById(R.id.full_course_name);
        img_schImp = itemView.findViewById(R.id.schedule_importance);
        tv_day = itemView.findViewById(R.id.vertical_text);

        ImageButton btn_delete = itemView.findViewById(R.id.deleteButton);
        ImageButton btn_edit = itemView.findViewById(R.id.editButton);

        btn_delete.setOnClickListener(v -> {
            String deleteRequest
                    = user instanceof ScheduledTimetableFragment
                      ? ScheduledTimetableFragment.DELETE_REQUEST : DaysFragment.DELETE_REQUEST;

            RequestRunner runner = RequestRunner.getInstance();
            runner.with(user.getActivity(), this, rowAdapter, tList)
                    .setTimetableData(timetable)
                    .runRequest(deleteRequest);
            Snackbar.make(coordinator, "Timetable Deleted", Snackbar.LENGTH_LONG)
                    .setActionTextColor(Color.YELLOW)
                    .setAction("undo", (view) -> runner.undoRequest())
                    .show();
        });

        btn_edit.setOnClickListener(v -> {
            tModel.setChronologicalOrder(getAdapterPosition());
            if (user instanceof DaysFragment) {
                tModel.setDay(timetable);
                new AddTimetableDialog().show(user.getContext(), true, tModel);
            } else {
                new AddScheduledDialog().show(user.getContext(), true, tModel);
            }
        });

        tv_course.setOnClickListener(v -> {
            if (TextUtils.equals(tv_course.getText(), "NIL")) {
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

    public TimeTableRowHolder with(Fragment user,
                                   RecyclerView.Adapter<?> rowAdapter,
                                   List<DataModel> tList,
                                   CoordinatorLayout coordinator,
                                   int position) {
        this.user = user;
        this.tModel = (TimetableModel) tList.get(position);
        this.tList = tList;
        this.rowAdapter = rowAdapter;
        this.coordinator = coordinator;
        return this;
    }

    public void bindView() {
        Context context = user.getContext();
        // Set the importance drawable according to scheduled timetable importance.
        // getImportance() will be null if it is called by TableFragment
        if (tModel.getImportance() != null) {
            switch (tModel.getImportance()) {
                case "Not Important":
                    img_schImp.setBackgroundResource(R.drawable.schedule_not_important);
                    break;
                case "Very Important":
                    img_schImp.setBackgroundResource(R.drawable.schedule_very_important);
                    break;
                case "Less Important":
                    img_schImp.setBackgroundResource(R.drawable.schedule_less_important);
            }
            TooltipCompat.setTooltipText(img_schImp, tModel.getImportance());
        }
        if (user instanceof DaysFragment) {
            int rowColor = COLORS_1[getAdapterPosition() % COLORS_1.length];
            lIndicator.setBackgroundColor(ContextCompat.getColor(context, rowColor));
        } else {
            // indicator2 and verticalTextView will return null for users other than
            // the ScheduledTimetableFragment, so use them only for that particular fragment
            // to prevent application crash.
            int dayColor = COLORS_2[tModel.getDayIndex()];
            lIndicator.setBackgroundColor(ContextCompat.getColor(context, dayColor));
            rIndicator.setBackgroundColor(ContextCompat.getColor(context, dayColor));
            tv_day.setText(tModel.getDay());
            tv_lecturer.setText(truncateName(tModel.getLecturerName()));
        }

        tv_course.setText(tModel.getCourseCode());
        atv_FCN.setText(tModel.getFullCourseName());

        String start, end;
        if (isUserPreferred24Hours(context)) {
            start = tModel.getStartTime();
            end = tModel.getEndTime();
        } else {
            start = convertTime(tModel.getStartTime(), context);
            end = convertTime(tModel.getEndTime(), context);
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

    public TimeTableRowHolder setTimetableDay(String timetable) {
        this.timetable = timetable;
        return this;
    }

    // Determines if there was an added title in the lecturer's name
    private boolean startsWithAny(String[] titles, String s) {
        for (String title : titles)
            if (s.startsWith(title))
                return true;
        return false;
    }

    // Truncate lecturer's name to enable user view more of the name because
    // if the name is too long, the system will add ellipses at the end of the name
    // thereby removing some important parts of the name.
    private String truncateName(String fullName) {
        String[] nameTokens = fullName.split(" ");

        String[] titles = {"Barr", "Barrister", "Doc", "Doctor", "Dr", "Engineer", "Engr", "Mr",
                "Mister", "Mrs", "Ms", "Prof", "Professor"};

        StringBuilder nameBuilder = new StringBuilder();
        int iMax = nameTokens.length - 1;
        int nameLimit = user.getContext().getResources().getInteger(R.integer.name_limit);

        if (nameTokens.length > 2 &&
                fullName.length() > nameLimit && startsWithAny(titles, fullName)) {
            // Append the title if there is one
            switch (nameTokens[0]) {
                case "Barrister":
                    nameBuilder.append(titles[0] /* Barr */).append(" ");
                    break;
                case "Doctor":
                case "Doc":
                    nameBuilder.append(titles[4] /* Dr */).append(" ");
                    break;
                case "Engineer":
                    nameBuilder.append(titles[6] /* Engr */).append(" ");
                    break;
                case "Mister":
                    nameBuilder.append(titles[7] /* Mr */).append(" ");
                    break;
                case "Mrs":
                    nameBuilder.append(titles[10] /* Mr */).append(" ");
                    break;
                case "Professor":
                    nameBuilder.append(titles[11] /* Prof */).append(" ");
                    break;
                default:
                    nameBuilder.append(nameTokens[0]).append(" ");
            }
            // Shorten the names to their first characters in uppercase
            for (int i = 1; i <= iMax; i++) {
                if (i == iMax) {
                    nameBuilder.append(" ");
                    break;
                }
                nameBuilder.append(Character.toUpperCase(nameTokens[i].charAt(0))).append(".");
                nameBuilder.append(" ");
            }

            int bMaxLen = nameBuilder.length();
            return nameBuilder.replace(bMaxLen - 1, bMaxLen, nameTokens[iMax]).toString();
        }
        return fullName;
    }
}
