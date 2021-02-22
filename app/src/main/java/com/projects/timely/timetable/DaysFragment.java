package com.projects.timely.timetable;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Process;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.projects.timely.R;
import com.projects.timely.core.CountEvent;
import com.projects.timely.core.DataModel;
import com.projects.timely.core.EmptyListEvent;
import com.projects.timely.core.SchoolDatabase;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.projects.timely.core.Globals.DAYS;
import static com.projects.timely.core.Globals.runBackgroundTask;

@SuppressWarnings({"ConstantConditions"})
public class DaysFragment extends Fragment {
    public static final String DELETE_REQUEST = "delete timetable";
    public static final String ARG_POSITION = "List position";
    public static final String ARG_CLASS = "Current class";
    static final String ARG_PAGE_POSITION = "Tab position";
    static final String ARG_DAY = "Schedule Day";
    static final String ARG_TIME = "Schedule Time";
    static final String ARG_TO_EDIT = "Editor stat";
    static final String ARG_DATA = "Timetable Data";
    static final String ARG_CHRONOLOGY = "Chronological Order";
    private TimeTableRowAdapter rowAdapter;
    private List<DataModel> tList;
    private TextView itemCount;
    private ViewGroup noTimetableView;
    private RecyclerView rV_timetable;
    private SchoolDatabase database;
    private CoordinatorLayout coordinator;

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
        rowAdapter = new TimeTableRowAdapter();
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
            tList = database.getTimeTableData(getCurrentTable());
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
    public void onResume() {
        setHasOptionsMenu(true);
        super.onResume();
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
    private String getCurrentTable() {
        return DAYS[getArguments().getInt(ARG_POSITION)];
    }

    private void dismissProgressbar(ProgressBar progressBar, boolean empty) {
        if (empty) progressBar.setVisibility(View.GONE);
        else progressBar.animate()
                .scaleX(0.0f)
                .scaleY(0.0f)
                .setDuration(1000);
    }

    private class TimeTableRowAdapter extends RecyclerView.Adapter<TimeTableRowHolder> {

        @NonNull
        @Override
        public TimeTableRowHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int ignored) {
            View view = getLayoutInflater().inflate(R.layout.timetable_row, viewGroup, false);
            return new TimeTableRowHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TimeTableRowHolder timeTableRowHolder, int position) {
            timeTableRowHolder.with(DaysFragment.this,
                                    rowAdapter,
                                    tList,
                                    coordinator,
                                    position)
                    .setTimetableDay(getCurrentTable())
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