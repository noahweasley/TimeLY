package com.projects.timely.courses;

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
public class SemesterFragment extends Fragment {
    public static final String ARG_POSITION = "page position";
    private List<DataModel> cList;
    private SchoolDatabase database;
    private TextView itemCount;
    private ViewGroup noCourseView;
    private RecyclerView rv_Courses;
    private CoordinatorLayout coordinator;
    private CourseAdapter courseAdapter;

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
        courseAdapter = new CourseAdapter();
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
    public void onResume() {
        // Prevent glitch on adding menu to the toolbar. Only show a particular semester's course
        // count, if that is the only visible semester
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
    public void doCourseUpdate(UpdateMessage update) {
        int pagePosition = update.getPagePosition();
        // Because an update message would be posted to the two existing fragments in viewpager,
        // instead of updating UI for both fragments, update only a particular fragments view
        // which was specified by the currently checked radio button in the add-course dialog
        if (getPagePosition() == pagePosition) {
            CourseModel data = update.getData();
            int changePos = data.getChronologicalOrder();

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
    public void doCountUpdate(CountEvent countEvent) {
        itemCount.setText(String.valueOf(countEvent.getSize()));
    }

    // For the vertical scrolling list (timetable)
    public class CourseAdapter extends RecyclerView.Adapter<CourseRowHolder> {

        @NonNull
        @Override
        public CourseRowHolder onCreateViewHolder(@NonNull ViewGroup container, int ignored) {
            View view = getLayoutInflater().inflate(R.layout.course_list_row, container,
                                                    false);
            return new CourseRowHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CourseRowHolder viewHolder, int position) {
            viewHolder.with(SemesterFragment.this,
                            courseAdapter,
                            cList,
                            coordinator,
                            position)
                    .bindView();
        }

        @Override
        public long getItemId(int position) {
            if (cList.size() > 0) {
                return ((CourseModel) cList.get(position)).getId();
            }
            return -2;
        }

        @Override
        public int getItemCount() {
            return cList.size();
        }
    }
}
