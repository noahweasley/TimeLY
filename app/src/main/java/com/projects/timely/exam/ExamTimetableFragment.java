package com.projects.timely.exam;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.projects.timely.core.Globals.runBackgroundTask;

@SuppressWarnings("ConstantConditions")
public class ExamTimetableFragment extends Fragment {
    public static final String ARG_POSITION = "page position";
    private List<DataModel> eList;
    private SchoolDatabase database;
    private TextView itemCount;
    private ViewGroup noExamView;
    private RecyclerView rv_Exams;
    private CoordinatorLayout coordinator;
    private ExamRowAdapter examRowAdapter;

    public static ExamTimetableFragment newInstance(int position) {
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        ExamTimetableFragment fragment = new ExamTimetableFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eList = new ArrayList<>();
        examRowAdapter = new ExamRowAdapter();
        database = new SchoolDatabase(getContext());
        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_exams, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ProgressBar indeterminateProgress = view.findViewById(R.id.indeterminateProgress);
        int position = getArguments().getInt(ARG_POSITION);
        runBackgroundTask(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            eList = database.getExamTimetableDataFor(position);
            // Sort by start time
            Collections.sort(eList, (e1, e2) -> {
                ExamModel em1 = (ExamModel) e1;
                ExamModel em2 = (ExamModel) e2;
                int cmp = Integer.compare(em1.getDayIndex(), em2.getDayIndex());
                if (cmp != 0)
                    return cmp;
                else return Integer.compare(em1.getStartAsInt(), em2.getStartAsInt());
            });
            // post a message to the message queue to update the table's ui
            if (isAdded()) {
                getActivity().runOnUiThread(() -> {
                    boolean isEmpty = eList.isEmpty();
                    doEmptyExamsUpdate(null);
                    // animate progress bar dismissal
                    dismissProgressbar(indeterminateProgress, isEmpty);
                    examRowAdapter.notifyDataSetChanged();
                    if (itemCount != null)
                        itemCount.setText(String.valueOf(eList.size()));
                });
            }
        });
        noExamView = view.findViewById(R.id.no_exams_view);
        rv_Exams = view.findViewById(R.id.exams);
        coordinator = view.findViewById(R.id.coordinator);

        int pagePos = getArguments().getInt(ARG_POSITION);
        view.findViewById(R.id.add_exams)
                .setOnClickListener(v -> new AddExamDialog().show(getContext(), pagePos));
        // set  list to have a fixed size to increase performance and set stable id, to use same
        // view holder on adapter change
        rv_Exams.setHasFixedSize(true);
        examRowAdapter.setHasStableIds(true);
        rv_Exams.setAdapter(examRowAdapter);

        rv_Exams.setLayoutManager(new LinearLayoutManager(getActivity(),
                                                          LinearLayoutManager.VERTICAL,
                                                          false));
    }

    @Override
    public void onResume() {
        // Prevent glitch on adding menu to the toolbar. Only show a particular scheduled exam
        // count, if that is the only visible exam timetable
        setHasOptionsMenu(true); // onCreateOptionsMenu will be called after this
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
        inflater.inflate(R.menu.list_menu_exams, menu);
        View layout = menu.findItem(R.id.list_item_count).getActionView();
        itemCount = layout.findViewById(R.id.counter);
        itemCount.setText(String.valueOf(eList.size()));

        TooltipCompat.setTooltipText(itemCount, "Exams Count");

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doEmptyExamsUpdate(EmptyListEvent o) {
        noExamView.setVisibility(eList.isEmpty() ? View.VISIBLE : View.GONE);
        rv_Exams.setVisibility(eList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doExamsUpdate(UpdateMessage update) {
        int pagePosition = update.getPagePosition();
        // Because an update message would be posted to the two existing fragments in viewpager,
        // instead of updating UI for both fragments, update only a particular fragments view
        // which was specified by the currently viewed exam week timetable.
        if (getArguments().getInt(ARG_POSITION) == pagePosition) {
            ExamModel data = update.getData();
            int changePos = data.getChronologicalOrder();

            if (changePos >= 0) {
                if (update.getType() == UpdateMessage.EventType.NEW) {
                    eList.add(changePos, data);
                    itemCount.setText(String.valueOf(eList.size()));

                    if (eList.isEmpty()) {
                        noExamView.setVisibility(View.VISIBLE);
                        rv_Exams.setVisibility(View.GONE);
                    } else {
                        noExamView.setVisibility(View.GONE);
                        rv_Exams.setVisibility(View.VISIBLE);
                    }
                    examRowAdapter.notifyItemInserted(changePos);
                    examRowAdapter.notifyDataSetChanged();
                } else {
                    // This else block is never used, but is left here for future app updates, where I
                    // would need to edit exams.
                    eList.remove(changePos);
                    eList.add(changePos, data);
                    examRowAdapter.notifyItemChanged(changePos);
                }
            } else {
                Log.w(getClass().getSimpleName(),
                      "Couldn't update list for position: " + changePos);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doCountUpdate(CountEvent countEvent) {
        itemCount.setText(String.valueOf(countEvent.getSize()));
    }

    private void dismissProgressbar(ProgressBar progressBar, boolean isEmpty) {
        if (isEmpty) progressBar.setVisibility(View.GONE);
        else progressBar.animate()
                .scaleX(0.0f)
                .scaleY(0.0f)
                .setDuration(1000);
    }

    class ExamRowAdapter extends RecyclerView.Adapter<ExamRowHolder> {

        @NonNull
        @Override
        public ExamRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.exam_list_row, parent,
                                                    false);
            return new ExamRowHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ExamRowHolder holder, int position) {
            holder.with(ExamTimetableFragment.this,
                        examRowAdapter,
                        eList,
                        coordinator)
                    .bindView();
        }

        @Override
        public long getItemId(int position) {
            if (eList.size() > 0) {
                return ((ExamModel) eList.get(position)).getId();
            }
            return -2;
        }

        @Override
        public int getItemCount() {
            return eList.size();
        }
    }
}
