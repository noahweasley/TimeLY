package com.projects.timely.scheduled;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.projects.timely.R;
import com.projects.timely.core.CountEvent;
import com.projects.timely.core.DataModel;
import com.projects.timely.core.EmptyListEvent;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.TooltipCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.projects.timely.core.Globals.runBackgroundTask;

@SuppressWarnings({"ConstantConditions"})
public class ScheduledTimetableFragment extends Fragment {
    public static final String DELETE_REQUEST = "delete scheduled timetable";
    static final String ARG_TO_EDIT = "Editor stat";
    static final String ARG_DATA = "Timetable Data";
    private ViewGroup noTimetableView;
    private ScheduledTimetableFragment.TimeTableRowAdapter tableRowAdapter;
    private List<DataModel> tList;
    private SchoolDatabase database;
    private TextView itemCount;
    private RecyclerView rV_timetable;
    private CoordinatorLayout coordinator;

    public static ScheduledTimetableFragment newInstance() {
        return new ScheduledTimetableFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tableRowAdapter = new TimeTableRowAdapter();
        tList = new ArrayList<>();
        //getInstance all the saved timetable data from the database
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

                runner.with(getActivity(),
                            viewHolder,
                            tableRowAdapter,
                            tList)
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

        Log.d(getClass().getSimpleName(), "order: " + changePos);

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

    // For the vertical scrolling list (timetable)
    public class TimeTableRowAdapter extends RecyclerView.Adapter<TimeTableRowHolder> {

        @NonNull
        @Override
        public TimeTableRowHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int ignored) {
            View view = getLayoutInflater().inflate(R.layout.scheduled_timetable_row, viewGroup,
                                                    false);
            return new TimeTableRowHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TimeTableRowHolder timeTableRowHolder, int position) {
            timeTableRowHolder.with(ScheduledTimetableFragment.this,
                                    tableRowAdapter,
                                    tList,
                                    coordinator,
                                    position)
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
    }
}
