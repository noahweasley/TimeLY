package com.projects.timely.courses;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Process;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.TooltipCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.projects.timely.R;
import com.projects.timely.core.ChoiceMode;
import com.projects.timely.core.DataModel;
import com.projects.timely.core.DataMultiChoiceMode;
import com.projects.timely.core.EmptyListEvent;
import com.projects.timely.core.MultiUpdateMessage;
import com.projects.timely.core.RequestParams;
import com.projects.timely.core.RequestRunner;
import com.projects.timely.core.RequestUpdateEvent;
import com.projects.timely.core.SchoolDatabase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.projects.timely.core.AppUtils.runBackgroundTask;

@SuppressWarnings("ConstantConditions")
public class SemesterFragment extends Fragment implements ActionMode.Callback {
    public static final String ARG_POSITION = "page position";
    public static final String MULTIPLE_DELETE_REQUEST = "Delete Multiple Courses";
    private static ActionMode actionMode;
    private List<DataModel> cList;
    private SchoolDatabase database;
    private TextView itemCount;
    private ViewGroup noCourseView;
    private RecyclerView rv_Courses;
    private CoordinatorLayout coordinator;
    private CourseAdapter courseAdapter;
    private AppCompatActivity context;
    private final ChoiceMode choiceMode = ChoiceMode.DATA_MULTI_SELECT;

    public static SemesterFragment newInstance(int position) {
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        SemesterFragment fragment = new SemesterFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cList = new ArrayList<>();
        courseAdapter = new CourseAdapter(choiceMode);
        database = new SchoolDatabase(getContext());
        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_semesters, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // if modelList was empty, a layout indicating an empty list will be displayed, to avoid
        // displaying an empty list, which is not just quite good for UX design.
        context = (AppCompatActivity) getActivity();
        ProgressBar indeterminateProgress = view.findViewById(R.id.indeterminateProgress);
        boolean isPage1 = getArguments().getInt(ARG_POSITION) == 0;
        runBackgroundTask(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            cList = database.getCoursesData((isPage1 ? SchoolDatabase.FIRST_SEMESTER
                                                     : SchoolDatabase.SECOND_SEMESTER));
            // post a message to the message queue to update the table's ui
            getActivity().runOnUiThread(() -> {
                boolean isEmpty = cList.isEmpty();
                doEmptyCourseUpdate(null);
                // animate progress bar dismissal
                dismissProgressbar(indeterminateProgress, isEmpty);
                courseAdapter.notifyDataSetChanged();
                if (itemCount != null)
                    itemCount.setText(String.valueOf(cList.size()));
            });
        });

        noCourseView = view.findViewById(R.id.no_courses_view);
        rv_Courses = view.findViewById(R.id.courses);
        coordinator = view.findViewById(R.id.coordinator);
        view.findViewById(R.id.add_course)
                .setOnClickListener(
                        v -> new AddCourseDialog().show(getContext(), getPagePosition()));
        // set  list to have a fixed size to increase performance and set stable id, to use same
        // view holder on adapter change
        rv_Courses.setHasFixedSize(true);
        courseAdapter.setHasStableIds(true);
        rv_Courses.setAdapter(courseAdapter);

        rv_Courses.setLayoutManager(new LinearLayoutManager(getActivity(),
                                                            LinearLayoutManager.VERTICAL,
                                                            false));
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null)
            courseAdapter.getChoiceMode().onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {
        // Prevent glitch on adding menu to the toolbar. Only show a particular semester's course
        // count, if that is the only visible semester
        setHasOptionsMenu(true); // onCreateOptionsMenu will be called after this
        // could have used ViewPager.OnPageChangedListener to increase code readability, but
        // this was used to reduce code size as there is not much work to be done when ViewPager
        // scrolls
        if(actionMode != null) actionMode.finish();
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        choiceMode.onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        EventBus.getDefault().unregister(this);
        database.close();
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.list_menu_courses, menu);
        View layout = menu.findItem(R.id.list_item_count).getActionView();
        itemCount = layout.findViewById(R.id.counter);
        itemCount.setText(String.valueOf(cList.size()));

        TooltipCompat.setTooltipText(itemCount, "Courses Count");

        super.onCreateOptionsMenu(menu, inflater);
    }

    private int getPagePosition() {
        return getArguments().getInt(ARG_POSITION);
    }

    public String getSemester() {
        if (getPagePosition() == 0)
            return SchoolDatabase.FIRST_SEMESTER;
        else return SchoolDatabase.SECOND_SEMESTER;
    }

    private void dismissProgressbar(ProgressBar progressBar, boolean isEmpty) {
        if (isEmpty) progressBar.setVisibility(View.GONE);
        else progressBar.animate()
                .scaleX(0.0f)
                .scaleY(0.0f)
                .setDuration(1000);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doEmptyCourseUpdate(EmptyListEvent o) {
        noCourseView.setVisibility(cList.isEmpty() ? View.VISIBLE : View.GONE);
        rv_Courses.setVisibility(cList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doListUpdate(MultiUpdateMessage mUpdate) {
        if (mUpdate.getType() == MultiUpdateMessage.EventType.REMOVE
                || mUpdate.getType() == MultiUpdateMessage.EventType.INSERT) {
            if (actionMode != null)
                actionMode.finish(); // require onDestroyActionMode() callback
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doCourseUpdate(UpdateMessage update) {
        int pagePosition = update.getPagePosition();
        // Because an update message would be posted to the two existing fragments in viewpager,
        // instead of updating UI for both fragments, update only a particular fragments view
        // which was specified by the currently checked radio button in the add-course dialog
        if (getPagePosition() == pagePosition) {
            CourseModel data = update.getData();
            int changePos = data.getId();

            if (update.getType() == UpdateMessage.EventType.NEW) {
                cList.add(changePos, data);
                itemCount.setText(String.valueOf(cList.size()));

                if (cList.isEmpty()) {
                    noCourseView.setVisibility(View.VISIBLE);
                    rv_Courses.setVisibility(View.GONE);
                } else {
                    noCourseView.setVisibility(View.GONE);
                    rv_Courses.setVisibility(View.VISIBLE);
                }
                courseAdapter.notifyItemInserted(changePos);
                courseAdapter.notifyDataSetChanged();
            } else {
                // This else block is never used, but is left here for future app updates, where I
                // would need to edit course.
                cList.remove(changePos);
                cList.add(changePos, data);
                courseAdapter.notifyItemChanged(changePos);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doOnRequestUpdate(RequestUpdateEvent request) {
        switch (request.getUpdateType()) {
            case INSERT:
                itemCount.setText(String.valueOf(cList.size()));
                courseAdapter.notifyItemInserted(request.getChangePosition());
                courseAdapter.notifyDataSetChanged();
                break;
            case REMOVE:
                itemCount.setText(String.valueOf(cList.size()));
                courseAdapter.notifyItemRemoved(request.getChangePosition());
                courseAdapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        context.getMenuInflater().inflate(R.menu.deleted_items, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        courseAdapter.deleteMultiple();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;
        courseAdapter.getChoiceMode().clearChoices();
        courseAdapter.notifyDataSetChanged();
    }

    // For the vertical scrolling list (timetable)
    public class CourseAdapter extends RecyclerView.Adapter<CourseRowHolder> {
        private final ChoiceMode choiceMode;
        private boolean multiSelectionEnabled;
        private CourseRowHolder rowHolder;

        public CourseAdapter(ChoiceMode choiceMode) {
            super();
            this.choiceMode = choiceMode;
        }

        @NonNull
        @Override
        public CourseRowHolder onCreateViewHolder(@NonNull ViewGroup container, int ignored) {
            View view = getLayoutInflater().inflate(R.layout.course_list_row, container, false);
            return (rowHolder = new CourseRowHolder(view));
        }

        @Override
        public void onBindViewHolder(@NonNull CourseRowHolder viewHolder, int position) {
            viewHolder.with(SemesterFragment.this, courseAdapter, cList, coordinator)
                    .bindView();
        }

        @Override
        public long getItemId(int position) {
            if (cList.size() > 0) {
                return cList.get(position).getId();
            }
            return -2;
        }

        @Override
        public int getItemCount() {
            return cList.size();
        }

        /**
         * @return the choice-mode that was set
         */
        public ChoiceMode getChoiceMode() {
            return choiceMode;
        }

        /**
         * @return the status of the multi-selection mode.
         */
        public boolean isMultiSelectionEnabled() {
            return multiSelectionEnabled;
        }

        /**
         * Sets the multi-selection mode status
         *
         * @param status the status of the multi-selection mode
         */
        public void setMultiSelectionEnabled(boolean status) {
            this.multiSelectionEnabled = status;
        }

        /**
         * @param adapterPosition the position of the view holder
         * @return the checked status of a particular image int he list
         */
        public boolean isChecked(int adapterPosition) {
            return choiceMode.isChecked(adapterPosition);
        }

        /**
         * @return the number of images that was selected
         */
        public int getCheckedCoursesCount() {
            return choiceMode.getCheckedChoiceCount();
        }

        /**
         * @return an array of the checked indices as seen by database
         */
        public Integer[] getCheckedCoursesPositions() {
            return choiceMode.getCheckedChoicePositions();
        }

        /**
         * @return an array of the checked indices
         */
        private Integer[] getCheckedCoursesIndices() {
            return choiceMode.getCheckedChoicesIndices();
        }

        /**
         * @param position           the position where the change occurred
         * @param state              the new state of the change
         * @param coursePosition the position of the assignment in database.
         */
        public void onChecked(int position, boolean state, int coursePosition) {
            boolean isFinished = false;

            DataMultiChoiceMode dmcm = (DataMultiChoiceMode) choiceMode;
            dmcm.setChecked(position, state, coursePosition);

            int choiceCount = dmcm.getCheckedChoiceCount();

            if (actionMode == null && choiceCount == 1) {
                AppCompatActivity context = (AppCompatActivity) getActivity();
                if (isAdded()) {
                    actionMode = context.startSupportActionMode(SemesterFragment.this);
                }
                notifyDataSetChanged();
            } else if (actionMode != null && choiceCount == 0) {
                actionMode.finish();
                isFinished = true;
                choiceMode.clearChoices(); // added this, might be solution to my problem
            }

            if (!isFinished && actionMode != null)
                actionMode.setTitle(String.format(Locale.US, "%d %s", choiceCount, "selected"));
        }

        /**
         * Deletes multiple images from the list of selected items
         */
        public void deleteMultiple() {
            RequestRunner runner = RequestRunner.getInstance();
            RequestRunner.Builder builder = new RequestRunner.Builder();
            builder.setOwnerContext(getActivity())
                    .setAdapterPosition(rowHolder.getAbsoluteAdapterPosition())
                    .setAdapter(courseAdapter)
                    .setModelList(cList)
                    .setCourseSemester(SemesterFragment.this.getSemester())
                    .setMetadataType(RequestParams.MetaDataType.COURSE)
                    .setItemIndices(getCheckedCoursesIndices())
                    .setPositionIndices(getCheckedCoursesPositions())
                    .setDataProvider(CourseModel.class);

            runner.setRequestParams(builder.getParams())
                    .runRequest(MULTIPLE_DELETE_REQUEST);

            final int count = getCheckedCoursesCount();
            Snackbar snackbar
                    = Snackbar.make(coordinator,
                                    count + " Course" + (count > 1 ? "s" : "") + " Deleted",
                                    Snackbar.LENGTH_LONG);

            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.setAction("UNDO", v -> runner.undoRequest());
            snackbar.show();
        }
    }
}
