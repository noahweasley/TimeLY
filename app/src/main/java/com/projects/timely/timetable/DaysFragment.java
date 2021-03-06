package com.projects.timely.timetable;

import android.content.res.Configuration;
import android.content.res.Resources;
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

import com.google.android.material.snackbar.Snackbar;
import com.projects.timely.R;
import com.projects.timely.core.ChoiceMode;
import com.projects.timely.core.CountEvent;
import com.projects.timely.core.DataModel;
import com.projects.timely.core.DataMultiChoiceMode;
import com.projects.timely.core.EmptyListEvent;
import com.projects.timely.core.MultiUpdateMessage;
import com.projects.timely.core.RequestParams;
import com.projects.timely.core.RequestRunner;
import com.projects.timely.core.SchoolDatabase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.TooltipCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.projects.timely.core.Globals.DAYS;
import static com.projects.timely.core.Globals.runBackgroundTask;

@SuppressWarnings({"ConstantConditions"})
public class DaysFragment extends Fragment implements ActionMode.Callback {
    public static final String DELETE_REQUEST = "delete timetable";
    public static final String MULTIPLE_DELETE_REQUEST = "Delete Multiple Timetable";
    public static final String ARG_POSITION = "List position";
    public static final String ARG_CLASS = "Current class";
    static final String ARG_PAGE_POSITION = "Tab position";
    static final String ARG_DAY = "Schedule Day";
    static final String ARG_TIME = "Schedule Time";
    static final String ARG_TO_EDIT = "Editor stat";
    static final String ARG_DATA = "Timetable Data";
    static final String ARG_CHRONOLOGY = "Chronological Order";
    private static ActionMode actionMode;
    private TimeTableRowAdapter rowAdapter;
    private List<DataModel> tList;
    private TextView itemCount;
    private ViewGroup noTimetableView;
    private RecyclerView rV_timetable;
    private SchoolDatabase database;
    private CoordinatorLayout coordinator;
    private ChoiceMode choiceMode = ChoiceMode.DATA_MULTI_SELECT;

    public static DaysFragment newInstance(int position) {
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);

        DaysFragment fragment = new DaysFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tList = new ArrayList<>();
        database = new SchoolDatabase(getContext());
        rowAdapter = new TimeTableRowAdapter(choiceMode);
        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_table, parent, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        coordinator = view.findViewById(R.id.coordinator);
        Resources resources = getResources();
        boolean isInLandscape =
                resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        ProgressBar indeterminateProgress = view.findViewById(R.id.indeterminateProgress);
        runBackgroundTask(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            tList = database.getTimeTableData(getCurrentTableDay());
            // Sort according to classes' start time
            Collections.sort(tList, (t1, t2) -> {
                TimetableModel tm1 = ((TimetableModel) t1);
                TimetableModel tm2 = ((TimetableModel) t2);
                return Integer.compare(tm1.getStartTimeAsInt(), tm2.getStartTimeAsInt());
            });

            if (isAdded()) {
                getActivity().runOnUiThread(() -> {
                    boolean isEmpty = tList.isEmpty();
                    doEmptyTimetableUpdate(null);
                    dismissProgressbar(indeterminateProgress, isEmpty);
                    rowAdapter.notifyDataSetChanged();
                    if (itemCount != null)
                        itemCount.setText(String.valueOf(tList.size()));
                });
            }
        });

        noTimetableView = view.findViewById(R.id.no_timetable_view);
        rowAdapter.setHasStableIds(true);
        rV_timetable = view.findViewById(R.id.timetable);
        rV_timetable.setHasFixedSize(true);
        rV_timetable.setAdapter(rowAdapter);

        if (isInLandscape) {
            rV_timetable.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        } else {
            rV_timetable.setLayoutManager(new LinearLayoutManager(getActivity(),
                                                                  LinearLayoutManager.VERTICAL,
                                                                  false));
        }

        int pagePos = getArguments().getInt(ARG_POSITION);
        view.findViewById(R.id.fab_add_new)
                .setOnClickListener(v -> new AddTimetableDialog().show(getContext(), pagePos));
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null)
            rowAdapter.getChoiceMode().onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {
        setHasOptionsMenu(true);
        // could have used ViewPager.OnPageChangedListener to increase code readability, but
        // this was used to reduce code size as there is not much work to be done when ViewPager
        // scrolls
        if (actionMode != null) actionMode.finish();
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
        inflater.inflate(R.menu.list_menu_timetable, menu);
        View layout = menu.findItem(R.id.list_item_count).getActionView();
        itemCount = layout.findViewById(R.id.counter);
        itemCount.setText(String.valueOf(tList.size()));

        TooltipCompat.setTooltipText(itemCount, "Timetable Count");

        super.onCreateOptionsMenu(menu, inflater);
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
    public void doEmptyCourseUpdate(EmptyListEvent o) {
        noTimetableView.setVisibility(tList.isEmpty() ? View.VISIBLE : View.GONE);
        rV_timetable.setVisibility(tList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doEmptyTimetableUpdate(EmptyListEvent event) {
        noTimetableView.setVisibility(tList.isEmpty() ? View.VISIBLE : View.GONE);
        rV_timetable.setVisibility(tList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doCountUpdate(CountEvent countEvent) {
        if (itemCount != null)
            itemCount.setText(String.valueOf(countEvent.getSize()));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doTimetableUpdate(UpdateMessage update) {
        int pagePosition = update.getPagePosition();
        TimetableModel data = update.getData();
        // Because an update message would be posted to the all existing fragments in viewpager,
        // instead of updating UI for all fragments, update only a particular fragments view
        // which was specified by the currently selected day in the add-timetable dialog
        if (getArguments().getInt(ARG_POSITION) == pagePosition) {
            if (update.getType() == UpdateMessage.EventType.NEW) {
                tList.add(data.getChronologicalOrder(), data);
                if (itemCount != null)
                    itemCount.setText(String.valueOf(tList.size()));
                if (tList.isEmpty()) {
                    noTimetableView.setVisibility(View.VISIBLE);
                    rV_timetable.setVisibility(View.GONE);
                } else {
                    noTimetableView.setVisibility(View.GONE);
                    rV_timetable.setVisibility(View.VISIBLE);
                }
                rowAdapter.notifyItemInserted(data.getChronologicalOrder());
                rowAdapter.notifyDataSetChanged();
            } else {
                int changePos = data.getChronologicalOrder();
                tList.remove(changePos);
                tList.add(changePos, data);
                rowAdapter.notifyItemChanged(changePos);
            }
        }
    }

    // Get current timetable from the current selected tab
    private String getCurrentTableDay() {
        return DAYS[getArguments().getInt(ARG_POSITION)];
    }

    private void dismissProgressbar(ProgressBar progressBar, boolean empty) {
        if (empty) progressBar.setVisibility(View.GONE);
        else progressBar.animate()
                .scaleX(0.0f)
                .scaleY(0.0f)
                .setDuration(1000);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        AppCompatActivity context = (AppCompatActivity) getActivity();
        context.getMenuInflater().inflate(R.menu.deleted_items, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        rowAdapter.deleteMultiple();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;
        rowAdapter.getChoiceMode().clearChoices();
        rowAdapter.notifyDataSetChanged();
    }

    public class TimeTableRowAdapter extends RecyclerView.Adapter<TimeTableRowHolder> {
        private final ChoiceMode choiceMode;
        private boolean multiSelectionEnabled;
        private TimeTableRowHolder rowHolder;

        public TimeTableRowAdapter(ChoiceMode choiceMode) {
            super();
            this.choiceMode = choiceMode;
        }

        @NonNull
        @Override
        public TimeTableRowHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int ignored) {
            View view = getLayoutInflater().inflate(R.layout.timetable_row, viewGroup, false);
            return (rowHolder = new TimeTableRowHolder(view));
        }

        @Override
        public void onBindViewHolder(@NonNull TimeTableRowHolder timeTableRowHolder, int position) {
            timeTableRowHolder.with(DaysFragment.this, rowAdapter, tList, coordinator, position)
                    .setTimetableDay(getCurrentTableDay())
                    .bindView();
        }

        @Override
        public long getItemId(int position) {
            return ((TimetableModel) tList.get(position)).getId();
        }

        @Override
        public int getItemCount() {
            return tList.size();
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
         * @param assignmentPosition the position of the assignment in database.
         */
        public void onChecked(int position, boolean state, int assignmentPosition) {
            boolean isFinished = false;

            DataMultiChoiceMode dmcm = (DataMultiChoiceMode) choiceMode;
            dmcm.setChecked(position, state, assignmentPosition);

            int choiceCount = dmcm.getCheckedChoiceCount();

            if (actionMode == null && choiceCount == 1) {
                AppCompatActivity context = (AppCompatActivity) getActivity();
                if (isAdded()) {
                    actionMode = context.startSupportActionMode(DaysFragment.this);
                }

                notifyDataSetChanged(); // Used to deactivate all the button's in the list

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
                    .setAdapter(rowAdapter)
                    .setModelList(tList)
                    .setTimetable(getCurrentTableDay())
                    .setMetadataType(RequestParams.MetaDataType.TIMETABLE)
                    .setItemIndices(getCheckedCoursesIndices())
                    .setPositionIndices(getCheckedCoursesPositions())
                    .setDataProvider(TimetableModel.class);

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