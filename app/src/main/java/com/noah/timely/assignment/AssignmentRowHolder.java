package com.noah.timely.assignment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.noah.timely.R;
import com.noah.timely.assignment.AssignmentFragment.AssignmentRowAdapter;
import com.noah.timely.core.DataModel;
import com.noah.timely.core.RequestRunner;
import com.noah.timely.error.ErrorDialog;

import java.util.List;

import static com.noah.timely.assignment.AddAssignmentActivity.EDIT_POS;
import static com.noah.timely.assignment.AssignmentFragment.COURSE_CODE;
import static com.noah.timely.assignment.AssignmentFragment.DATE;
import static com.noah.timely.assignment.AssignmentFragment.DELETE_REQUEST;
import static com.noah.timely.assignment.AssignmentFragment.DESCRIPTION;
import static com.noah.timely.assignment.AssignmentFragment.LECTURER_NAME;
import static com.noah.timely.assignment.AssignmentFragment.TITLE;

public class AssignmentRowHolder extends RecyclerView.ViewHolder {

    private static final int[] DRAWABLE = {
            R.drawable.rounded_ct_bb,
            R.drawable.rounded_ct_ol,
            R.drawable.rounded_ct_pi,
            R.drawable.rounded_ct_gd,
            R.drawable.rounded_ct_pu,
            R.drawable.rounded_ct_gl,
            R.drawable.rounded_ct_bd,
            R.drawable.rounded_ct_od,
            R.drawable.rounded_ct_rl
    };

    private final View header;
    private final TextView tv_title, tv_description, tv_date, tv_lecturerName, tv_course;
    private final ImageView img_stats;
    private AssignmentModel assignment;
    private FragmentActivity mActivity;
    private CoordinatorLayout coordinator;
    private AssignmentRowAdapter assignmentRowAdapter;
    private List<DataModel> aList;
    private final View v_selectionOverlay;
    private boolean isChecked;
    private final ImageButton editButton, deleteButton, viewButton;

    @SuppressWarnings({"ClickableViewAccessibility", "ConstantConditions"})
    public AssignmentRowHolder(@NonNull View rootView) {
        super(rootView);
        header = rootView.findViewById(R.id.header);
        tv_title = rootView.findViewById(R.id.title);
        tv_description = rootView.findViewById(R.id.describe_text);
        editButton = rootView.findViewById(R.id.editButton);
        deleteButton = rootView.findViewById(R.id.deleteButton);
        tv_lecturerName = rootView.findViewById(R.id.lecturerName);
        tv_course = rootView.findViewById(R.id.course);
        tv_date = rootView.findViewById(R.id.deadline);
        v_selectionOverlay = rootView.findViewById(R.id.checked_overlay);
        viewButton = rootView.findViewById(R.id.viewButton);
        img_stats = rootView.findViewById(R.id.stats);

        viewButton.setOnClickListener(v -> {
            AssignmentModel assignment = (AssignmentModel) aList.get(getAbsoluteAdapterPosition());
            assignment.setChronologicalOrder(getAbsoluteAdapterPosition());
            new AssignmentViewDialog().show(mActivity, assignment);
        });

        deleteButton.setOnClickListener(v -> doDeleteAssignment());

        editButton.setOnClickListener(
                v -> mActivity.startActivity(new Intent(mActivity, AddAssignmentActivity.class)
                                                     .putExtra(LECTURER_NAME, assignment.getLecturerName())
                                                     .putExtra(TITLE, tv_title.getText().toString())
                                                     .putExtra(DESCRIPTION, tv_description.getText().toString())
                                                     .putExtra(DATE, tv_date.getText().toString())
                                                     .putExtra(COURSE_CODE, tv_course.getText().toString())
                                                     .putExtra(EDIT_POS, getAbsoluteAdapterPosition())
                                                     .setAction("Edit")));

        tv_course.setOnClickListener(v -> {
            if (TextUtils.equals(tv_course.getText(), "NIL")) {
                ErrorDialog.Builder builder = new ErrorDialog.Builder();
                builder.setDialogMessage("No matching course code found")
                       .setShowSuggestions(true)
                       .setSuggestionCount(2)
                       .setSuggestion1("Register courses first")
                       .setSuggestion2("After registration, use that course title");
                new ErrorDialog().showErrorMessage(mActivity, builder.build());
            }
        });

        // Multi - Select actions
        rootView.setOnLongClickListener(l -> {
            trySelectAssignment();
            assignmentRowAdapter
                    .setMultiSelectionEnabled(!assignmentRowAdapter.isMultiSelectionEnabled()
                                                      || assignmentRowAdapter.getCheckedAssignmentsCount() != 0);
            return true;
        });

        rootView.setOnClickListener(c -> {
            if (assignmentRowAdapter.isMultiSelectionEnabled()) {
                trySelectAssignment();
                if (assignmentRowAdapter.getCheckedAssignmentsCount() == 0) {
                    assignmentRowAdapter.setMultiSelectionEnabled(false);
                }
            }
        });
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
        this.assignment = (AssignmentModel) aList.get(getAbsoluteAdapterPosition());
        return this;
    }

    public void bindView() {
        tv_title.setText(assignment.getTitle());
        tv_description.setText(assignment.getDescription());
        String[] spd = assignment.getDate().split("[/._-]");
        String parsedDate;

        String dateKey = "a_date_format";
        String ddf = mActivity.getString(R.string.default_date_format);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        switch (sharedPreferences.getString(dateKey, ddf)) {
            case "dd_mm_yyyy":
                parsedDate = TextUtils.join("_", spd);
                break;
            case "dd/mm/yyyy":
                parsedDate = TextUtils.join("/", spd);
                break;
            case "dd.mm.yyyy":
                parsedDate = TextUtils.join(".", spd);
                break;
            default:
                parsedDate = TextUtils.join("-", spd);
                break;
        }

        tv_date.setText(parsedDate);
        tv_course.setText(assignment.getCourseCode());
        int rowDrawable = DRAWABLE[getAbsoluteAdapterPosition() % DRAWABLE.length];
        header.setBackground(ContextCompat.getDrawable(mActivity, rowDrawable));
        // Truncate lecturer's name based on length
        tv_lecturerName.setText(truncateName(assignment.getLecturerName()));

        img_stats.setImageResource(assignment.isSubmitted() ? R.drawable.ic_round_check_circle
                                                            : R.drawable.ic_pending);
        TooltipCompat.setTooltipText(img_stats, "Submission status");
        isChecked = assignmentRowAdapter.isChecked(getAbsoluteAdapterPosition());
        v_selectionOverlay.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        tryDisableViews(assignmentRowAdapter.isMultiSelectionEnabled());
    }

    // Determines if there was an added title in the lecturer's name
    private boolean startsWithAny(String[] titles, String s) {
        for (String title : titles) if (s.startsWith(title)) return true;
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

        int nameLimit = tv_lecturerName.getContext().getResources().getInteger(R.integer.name_limit);
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

    private void tryDisableViews(boolean disable) {
        editButton.setEnabled(!disable);
        editButton.setFocusable(!disable);
        viewButton.setEnabled(!disable);
        viewButton.setFocusable(!disable);
        deleteButton.setEnabled(!disable);
        deleteButton.setFocusable(!disable);
    }

    private void trySelectAssignment() {
        isChecked = !isChecked;
        v_selectionOverlay.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        assignmentRowAdapter.onChecked(getAbsoluteAdapterPosition(), isChecked, assignment.getPosition());
    }

    private void doDeleteAssignment() {
        // post a delete request on the assignment database7
        RequestRunner runner = RequestRunner.createInstance();
        Snackbar snackbar = Snackbar.make(coordinator, "Assignment Deleted", Snackbar.LENGTH_LONG)
                                    .setAction("undo", (view) -> runner.undoRequest())
                                    .setActionTextColor(Color.YELLOW);

        snackbar.show();

        RequestRunner.Builder builder = new RequestRunner.Builder();
        builder.setOwnerContext(mActivity)
               .setAdapterPosition(getAbsoluteAdapterPosition())
               .setModelList(aList)
               .setAssignmentData(assignment);

        runner.setRequestParams(builder.getParams())
              .runRequest(DELETE_REQUEST);
    }
}