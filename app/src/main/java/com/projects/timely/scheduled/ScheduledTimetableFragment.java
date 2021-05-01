package com.projects.timely.scheduled;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.TooltipCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.projects.timely.main.MainActivity;
import com.projects.timely.timetable.TimeTableRowHolder;
import com.projects.timely.timetable.TimetableModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.projects.timely.core.AppUtils.runBackgroundTask;

@SuppressWarnings({"ConstantConditions"})
public class ScheduledTimetableFragment extends Fragment implements ActionMode.Callback {
    public static final String DELETE_REQUEST = "delete scheduled timetable";
    public static final String MULTIPLE_DELETE_REQUEST = "delete multiple timetable";
    static final String ARG_TO_EDIT = "Editor stat";
    static final String ARG_DATA = "Timetable Data";
    private ActionMode actionMode;
    private ViewGroup noTimetableView;
    private ScheduledTimetableFragment.TimeTableRowAdapter tableRowAdapter;
    private List<DataModel> tList;
    private SchoolDatabase database;
    private TextView itemCount;
    private RecyclerView rV_timetable;
    private CoordinatorLayout coordinator;
    private final ChoiceMode choiceMode = ChoiceMode.DATA_MULTI_SELECT;

    public static ScheduledTimetableFragment newInstance() {
        return new ScheduledTimetableFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tableRowAdapter = new TimeTableRowAdapter(choiceMode);
        tList = new ArrayList<>();
        //createInstance all the saved timetable data from the database
        database = new SchoolDatabase(getContext());
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
        setHasOptionsMenu(true);
        Resources resources = getResources();
        coordinator = view.findViewById(R.id.coordinator);
        boolean isInLandscape =
                resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        ProgressBar indeterminateProgress = view.findViewById(R.id.indeterminateProgress);
        runBackgroundTask(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            tList = database.getTimeTableData(SchoolDatabase.SCHEDULED_TIMETABLE);
            // Sort the timetable according to the day and start time
            Collections.sort(tList, (t1, t2) -> {
                TimetableModel tm1 = ((TimetableModel) t1);
                TimetableModel tm2 = ((TimetableModel) t2);
                int cmpDay = Integer.compare(tm1.getDayIndex(), tm2.getDayIndex());
                if (cmpDay == 0)
                    return Integer.compare(tm1.getStartTimeAsInt(), tm2.getStartTimeAsInt());
                else return cmpDay;
            });
            // post a message to the message queue to update the table's ui
            if (isAdded())
                getActivity().runOnUiThread(() -> {
                    boolean isEmpty = tList.isEmpty();
                    doEmptyTimetableUpdate(null);
                    dismissProgressbar(indeterminateProgress, isEmpty);
                    tableRowAdapter.notifyDataSetChanged();
                    if (itemCount != null)
                        itemCount.setText(String.valueOf(tList.size()));
                });
        });

        noTimetableView = view.findViewById(R.id.no_timetable_view);
        rV_timetable = view.findViewById(R.id.timetable);

        ItemTouchHelper swiper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                        @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                // post a delete request on the assignment database
                RequestRunner runner = RequestRunner.getInstance();
                Snackbar.make(coordinator, "Timetable Deleted", Snackbar.LENGTH_LONG)
                        .setAction("undo", (view) -> runner.undoRequest())
                        .setActionTextColor(Color.YELLOW)
                        .show();

                RequestRunner.Builder builder = new RequestRunner.Builder();
                builder.setOwnerContext(getActivity())
                        .setAdapter(tableRowAdapter)
                        .setAdapterPosition(viewHolder.getAbsoluteAdapterPosition())
                        .setModelList(tList);

                runner.setRequestParams(builder.getParams())
                        .runRequest(DELETE_REQUEST);
            }
        });

        tableRowAdapter.setHasStableIds(true);
        rV_timetable.setHasFixedSize(true);
        rV_timetable.setAdapter(tableRowAdapter);
        swiper.attachToRecyclerView(rV_timetable);
        if (isInLandscape) {
            rV_timetable.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        } else {
            rV_timetable.setLayoutManager(new LinearLayoutManager(getActivity(),
                    LinearLayoutManager.VERTICAL,
                    false));
        }
        view.findViewById(R.id.fab_add_new)
                .setOnClickListener(v -> new AddScheduledDialog().show(getContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.classes);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null)
            tableRowAdapter.getChoiceMode().onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        tableRowAdapter.getChoiceMode().onSaveInstanceState(outState);
    }

    @Override
    public void onDetach() {
        EventBus.getDefault().unregister(this);
        database.close();
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.list_menu_scheduled, menu);
        View layout = menu.findItem(R.id.list_item_count).getActionView();
        itemCount = layout.findViewById(R.id.counter);
        itemCount.setText(String.valueOf(tList.size()));

        TooltipCompat.setTooltipText(itemCount, "Classes Count");

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doEmptyTimetableUpdate(EmptyListEvent event) {
        noTimetableView.setVisibility(tList.isEmpty() ? View.VISIBLE : View.GONE);
        rV_timetable.setVisibility(tList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doCountUpdate(CountEvent countEvent) {
        itemCount.setText(String.valueOf(countEvent.getSize()));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doTimetableUpdate(UpdateMessage update) {
        TimetableModel data = update.getData();
        int changePos = data.getChronologicalOrder();

        if (update.getType() == UpdateMessage.EventType.NEW) {
            tList.add(changePos, data);
            itemCount.setText(String.valueOf(tList.size()));

            if (tList.isEmpty()) {
                noTimetableView.setVisibility(View.VISIBLE);
                rV_timetable.setVisibility(View.GONE);
            } else {
                noTimetableView.setVisibility(View.GONE);
                rV_timetable.setVisibility(View.VISIBLE);
            }
            tableRowAdapter.notifyItemInserted(changePos);
            tableRowAdapter.notifyDataSetChanged();
        } else {
            tList.remove(changePos);
            tList.add(changePos, data);
            tableRowAdapter.notifyItemChanged(changePos);
        }
    }

    private void dismissProgressbar(ProgressBar progressBar, boolean empty) {
        if (empty) progressBar.setVisibility(View.GONE);
        else progressBar.animate()
                .scaleX(0.0f)
                .scaleY(0.0f)
                .setDuration(1000);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doListUpdate(MultiUpdateMessage mUpdate) {
        if (mUpdate.getType() == MultiUpdateMessage.EventType.REMOVE
                || mUpdate.getType() == MultiUpdateMessage.EventType.INSERT) {
            if (actionMode != null)
                actionMode.finish(); // require onDestroyActionMode() callback
        }
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
        tableRowAdapter.deleteMultiple();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;
        tableRowAdapter.getChoiceMode().clearChoices();
        tableRowAdapter.notifyDataSetChanged();
    }

    // For the vertical scrolling list (timetable)
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
            View view = getLayoutInflater().inflate(R.layout.scheduled_timetable_row, viewGroup,
                    false);
            return (rowHolder = new TimeTableRowHolder(view));
        }

        @Override
        public void onBindViewHolder(@NonNull TimeTableRowHolder timeTableRowHolder, int position) {
            timeTableRowHolder.with(ScheduledTimetableFragment.this, tableRowAdapter, tList,
                    coordinator, position)
                    .bindView();
        }

        @Override
        public long getItemId(int position) {
            return tList.get(position).getId();
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
                    actionMode = context.startSupportActionMode(ScheduledTimetableFragment.this);
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
                    .setAdapter(tableRowAdapter)
                    .setModelList(tList)
                    .setTimetable(SchoolDatabase.SCHEDULED_TIMETABLE)
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