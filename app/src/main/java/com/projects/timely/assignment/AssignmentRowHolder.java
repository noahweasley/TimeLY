package com.projects.timely.assignment;

import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.projects.timely.R;
import com.projects.timely.assignment.AssignmentFragment.AssignmentRowAdapter;
import com.projects.timely.core.DataModel;
import com.projects.timely.core.RequestRunner;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import static com.projects.timely.assignment.AddAssignmentActivity.EDIT_POS;
import static com.projects.timely.assignment.AssignmentFragment.COURSE_CODE;
import static com.projects.timely.assignment.AssignmentFragment.DATE;
import static com.projects.timely.assignment.AssignmentFragment.DELETE_REQUEST;
import static com.projects.timely.assignment.AssignmentFragment.DESCRIPTION;
import static com.projects.timely.assignment.AssignmentFragment.LECTURER_NAME;
import static com.projects.timely.assignment.AssignmentFragment.TITLE;

public
class AssignmentRowHolder extends RecyclerView.ViewHolder {

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

    private AssignmentModel assignment;
    private FragmentActivity mActivity;
    private CoordinatorLayout coordinator;
    private AssignmentRowAdapter assignmentRowAdapter;
    private List<DataModel> aList;
    private View header;
    private TextView title, describe_text, date, lecturerName, course;
    private ImageView img_stats;

    public AssignmentRowHolder(@NonNull View rootView) {
        super(rootView);
        header = rootView.findViewById(R.id.header);
        title = rootView.findViewById(R.id.title);
        describe_text = rootView.findViewById(R.id.describe_text);
        ImageButton editButton = rootView.findViewById(R.id.editButton);
        ImageButton deleteButton = rootView.findViewById(R.id.deleteButton);
        lecturerName = rootView.findViewById(R.id.lecturerName);
        course = rootView.findViewById(R.id.course);
        date = rootView.findViewById(R.id.deadline);
        ImageButton viewButton = rootView.findViewById(R.id.viewButton);
        img_stats = rootView.findViewById(R.id.stats);

        viewButton.setOnClickListener(v -> {
            AssignmentModel assignment = (AssignmentModel) aList.get( getAbsoluteAdapterPosition());
            assignment.setChronologicalOrder( getAbsoluteAdapterPosition());
            new AssignmentViewDialog().show(mActivity, assignment);
        });

        deleteButton.setOnClickListener(v -> doDeleteAssignment());

        editButton.setOnClickListener(
                v -> mActivity.startActivity(new Intent(mActivity, AddAssignmentActivity.class)
                                                     .putExtra(LECTURER_NAME,
                                                               assignment.getLecturerName())
                                                     .putExtra(TITLE, title.getText().toString())
                                                     .putExtra(DESCRIPTION,
                                                               describe_text.getText().toString())
                                                     .putExtra(DATE, date.getText().toString())
                                                     .putExtra(COURSE_CODE,
                                                               course.getText().toString())
                                                     .putExtra(EDIT_POS,
                                                               getAbsoluteAdapterPosition())
                                                     .setAction("Edit")));
    }

    // builder for the RowHolder
    public AssignmentRowHolder with(FragmentActivity mActivity,
                                    CoordinatorLayout coordinator,
                                    AssignmentRowAdapter assignmentRowAdapter,
                                    List<DataModel> aList) {
        this.mActivity = mActivity;
        this.coordinator = coordinator;
        this.assignmentRowAdapter = assignmentRowAdapter;
        this.aList = aList;
        this.assignment = (AssignmentModel) aList.get( getAbsoluteAdapterPosition());
        return this;
    }

    public void bindView() {
        title.setText(assignment.getTitle());
        describe_text.setText(assignment.getDescription());
        date.setText(assignment.getDate());
        course.setText(assignment.getCourseCode());
        int rowColor = COLORS[ getAbsoluteAdapterPosition() % COLORS.length];
        header.setBackgroundColor(ContextCompat.getColor(mActivity, rowColor));
        // Truncate lecturer's name based on length
        lecturerName.setText(truncateName(assignment.getLecturerName()));

        img_stats.setImageResource(assignment.isSubmitted() ? R.drawable.ic_round_check_circle
                                                            : R.drawable.ic_pending);
        TooltipCompat.setTooltipText(img_stats, "Submission status");
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

        int nameLimit = lecturerName.getContext().getResources().getInteger(R.integer.name_limit);
        if (fullName.length() > nameLimit && nameTokens.length > 2) {
            if (startsWithAny(titles, fullName)) {
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
            } else {
                for (int i = 0; i <= iMax; i++) {
                    if (i == iMax) {
                        nameBuilder.append(" ");
                        break;
                    }
                    nameBuilder.append(Character.toUpperCase(nameTokens[i].charAt(0))).append(".");
                    nameBuilder.append(" ");
                }
            }

            int bMaxLen = nameBuilder.length();

            return nameBuilder.replace(bMaxLen - 1, bMaxLen, nameTokens[iMax]).toString();

        } else {
            return fullName;
        }
    }

    private void doDeleteAssignment() {
        // post a delete request on the assignment database
        RequestRunner runner = RequestRunner.getInstance();
        Snackbar snackbar = Snackbar.make(coordinator, "Assignment Deleted",
                                          Snackbar.LENGTH_LONG)
                .setAction("undo", (view) -> runner.undoRequest())
                .setActionTextColor(Color.YELLOW);

        snackbar.show();

        runner.with(mActivity, this, assignmentRowAdapter, aList)
                .setAssignmentData(assignment)
                .runRequest(DELETE_REQUEST);
    }
}