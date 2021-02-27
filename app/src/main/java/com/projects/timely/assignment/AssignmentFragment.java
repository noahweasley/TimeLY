package com.projects.timely.assignment;

import android.content.Intent;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.projects.timely.R;
import com.projects.timely.core.DataModel;
import com.projects.timely.core.DataMultiChoiceMode;
import com.projects.timely.core.PositionMessageEvent;
import com.projects.timely.core.RequestRunner;
import com.projects.timely.core.SchoolDatabase;
import com.projects.timely.gallery.ChoiceMode;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.TooltipCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.projects.timely.core.Globals.runBackgroundTask;

@SuppressWarnings("ConstantConditions")
public class AssignmentFragment extends Fragment implements ActionMode.Callback{
    public static final String DELETE_REQUEST = "Delete Assignment";
    public static final String MULTIPLE_DELETE_REQUEST = "Delete Multiple Assignments";
    public static final String DESCRIPTION = "Description";
    public static final String TITLE = "Title";
    public static final String COURSE_CODE = "Course Code";
    public static final String LECTURER_NAME = "Lecturer Name";
    public static final String DATE = "Date";
    private List<DataModel> aList;
    private AssignmentRowAdapter assignmentAdapter;
    private CoordinatorLayout coordinator;
    private ViewGroup noAssignmentView;
    private SchoolDatabase database;
    private TextView itemCount;
    private RecyclerView rV_assignmentList;
    private ActionMode actionMode;
    private AppCompatActivity context;

    public static AssignmentFragment newInstance() {
        return new AssignmentFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = new SchoolDatabase(getContext());
        assignmentAdapter = new AssignmentRowAdapter(ChoiceMode.DATA_MULTI_SELECT);
        aList = new ArrayList<>();
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup parent,
                             Bundle savedState) {
        return inflater.inflate(R.layout.fragment_assignment, parent, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle state) {
        setHasOptionsMenu(true);
        context = (AppCompatActivity) getActivity();
        rV_assignmentList = view.findViewById(R.id.assignment_list);
        coordinator = view.findViewById(R.id.coordinator2);
        noAssignmentView = view.findViewById(R.id.no_assignment_view);
        ProgressBar indeterminateProgress = view.findViewById(R.id.indeterminateProgress);

        runBackgroundTask(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            aList = database.getAssignmentData();
            getActivity().runOnUiThread(() -> {
                boolean empty = aList.isEmpty();
                // animate progress bar dismissal
                dismissProgressbar(indeterminateProgress, empty);
                noAssignmentView.setVisibility(empty ? View.VISIBLE : View.GONE);
                rV_assignmentList.setVisibility(empty ? View.GONE : View.VISIBLE);
                assignmentAdapter.notifyDataSetChanged();
                if (itemCount != null)
                    itemCount.setText(String.valueOf(aList.size()));
            });
        });

        FloatingActionButton fab_add = view.findViewById(R.id.fab_add);
        fab_add.setOnClickListener(
                v -> startActivity(new Intent(getActivity(), AddAssignmentActivity.class)
                                           .setAction("Create")));

        assignmentAdapter.setHasStableIds(true);
        rV_assignmentList.setHasFixedSize(true);
        rV_assignmentList.setLayoutManager(new LinearLayoutManager(getActivity()));
        rV_assignmentList.setAdapter(assignmentAdapter);

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
                // post a delete request to the assignment database
                RequestRunner runner = RequestRunner.getInstance();
                Snackbar snackbar
                        = Snackbar.make(coordinator, "Assignment Deleted", Snackbar.LENGTH_LONG)
                        .setAction("undo", (view) -> runner.undoRequest())
                        .setActionTextColor(Color.YELLOW);
                snackbar.show();

                runner.with(getActivity(), viewHolder, assignmentAdapter, aList)
                        .setAssignmentData((AssignmentModel)
                                                   aList.get(
                                                           viewHolder.getAbsoluteAdapterPosition()))
                        .runRequest(DELETE_REQUEST);
            }
        });

        swiper.attachToRecyclerView(rV_assignmentList);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Assignments");
    }

    @Override
    public void onDetach() {
        EventBus.getDefault().unregister(this);
        database.close();
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.list_menu_assignment, menu);
        View layout = menu.findItem(R.id.list_item_count).getActionView();
        itemCount = layout.findViewById(R.id.counter);
        itemCount.setText(String.valueOf(aList.size()));

        TooltipCompat.setTooltipText(itemCount, "Assignment Count");

        super.onCreateOptionsMenu(menu, inflater);
    }

    // dismiss the content-loading progress bar
    private void dismissProgressbar(ProgressBar bar, boolean empty) {
        if (empty) bar.setVisibility(View.GONE);
        else bar.animate()
                .scaleX(0.0f)
                .scaleY(0.0f)
                .setDuration(1000);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doUriListUpdate(UriUpdateEvent event) {
        ((AssignmentModel) aList.get(event.getPosition())).setAttachedImage(event.getUris());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doStatUpdate(PositionMessageEvent event) {
        int pos = event.getPosition();
        AssignmentModel assignment = (AssignmentModel) aList.get(pos);
        assignment.setSubmitted(true);
        aList.remove(pos);
        aList.add(pos, assignment);
        assignmentAdapter.notifyItemChanged(pos);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doAssignmentUpdate(UpdateMessage update) {
        AssignmentModel data = update.getData();
        int changePos = data.getPosition();

        switch (update.getType()) {
            case NEW:

                aList.add(data);
                itemCount.setText(String.valueOf(aList.size()));

                if (aList.isEmpty()) {
                    noAssignmentView.setVisibility(View.VISIBLE);
                    rV_assignmentList.setVisibility(View.GONE);
                } else {
                    noAssignmentView.setVisibility(View.GONE);
                    rV_assignmentList.setVisibility(View.VISIBLE);
                }
                assignmentAdapter.notifyItemInserted(changePos);

                break;
            case REMOVE:

                aList.remove(changePos);
                itemCount.setText(String.valueOf(aList.size()));
                assignmentAdapter.notifyItemRemoved(changePos);
                assignmentAdapter.notifyDataSetChanged();

                if (aList.isEmpty()) {
                    noAssignmentView.setVisibility(View.VISIBLE);
                    rV_assignmentList.setVisibility(View.GONE);
                }

                break;
            case INSERT:

                if (noAssignmentView.getVisibility() == View.VISIBLE)
                    noAssignmentView.setVisibility(View.GONE);

                if (rV_assignmentList.getVisibility() == View.GONE)
                    rV_assignmentList.setVisibility(View.VISIBLE);

                aList.add(changePos, data);
                itemCount.setText(String.valueOf(aList.size()));
                assignmentAdapter.notifyItemInserted(changePos);
                assignmentAdapter.notifyDataSetChanged();

                break;
            default:

                AssignmentModel am = (AssignmentModel) aList.remove(changePos);
                am.setAttachedImage(data.getAttachedImages());
                am.setAttachedPDF(data.getAttachedPDF());
                am.setCourseCode(data.getCourseCode());
                am.setDate(data.getDate());
                am.setDescription(data.getDescription());
                am.setPosition(data.getPosition());
                am.setLecturerName(data.getLecturerName());
                am.setTitle(data.getTitle());
                am.setSubmissionDate(data.getSubmissionDate());
                aList.add(changePos, am);
                assignmentAdapter.notifyItemChanged(changePos);

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
        assignmentAdapter.deleteMultiple();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;
        assignmentAdapter.getChoiceMode().clearChoices();
        assignmentAdapter.notifyDataSetChanged();
    }

    class AssignmentRowAdapter extends RecyclerView.Adapter<AssignmentRowHolder> {
        private final ChoiceMode choiceMode;
        private boolean multiSelectionEnabled;
        private AssignmentRowHolder rowHolder;

        public AssignmentRowAdapter(ChoiceMode choiceMode) {
            super();
            this.choiceMode = choiceMode;
        }

        @NonNull
        @Override
        public AssignmentRowHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int ignored) {
            View view = getLayoutInflater().inflate(R.layout.assignment_list_row, viewGroup, false);
            return (rowHolder = new AssignmentRowHolder(view));
        }

        @Override
        public void onBindViewHolder(@NonNull AssignmentRowHolder rowHolder, int position) {
            rowHolder.with(getActivity(), coordinator, assignmentAdapter, aList).bindView();
        }

        @Override
        public long getItemId(int position) {
            return ((AssignmentModel) aList.get(position)).getPosition();
        }

        @Override
        public int getItemCount() {
            return aList.size();
        }

        /**
         * @return the choice-mode that was set
         */
        public ChoiceMode getChoiceMode() {
            return choiceMode;
        }

        /**
         * @return the status of the multi-selection mode
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
        public int getCheckedAssignmentsCount() {
            return choiceMode.getCheckedChoiceCount();
        }

        /**
         * @return an array of the checked indices
         */
        private Integer[] getCheckedImagesIndices() {
            return choiceMode.getCheckedChoicesIndices();
        }

        /**
         * @param position the position where the change occurred
         * @param state    the new state of the change
         */
        public void onChecked(int position, boolean state, AssignmentModel assignment) {
            boolean isFinished = false;

            DataMultiChoiceMode dmcm = (DataMultiChoiceMode) choiceMode;
            dmcm.setChecked(position, state);

            int choiceCount = dmcm.getCheckedChoiceCount();

            if (actionMode == null && choiceCount == 1) {
                AppCompatActivity context = (AppCompatActivity) getActivity();
                if (isAdded())
                    actionMode = context.startSupportActionMode(AssignmentFragment.this);
            } else if (actionMode != null && choiceCount <= 1) {
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
            runner.with(getActivity(), rowHolder, assignmentAdapter, null)
                    .setItemsIndices(getCheckedImagesIndices())
                    .runRequest(MULTIPLE_DELETE_REQUEST);

            final int count = getCheckedAssignmentsCount();
            Snackbar snackbar
                    = Snackbar.make(coordinator,
                                    count + " Assignment" + (count > 1 ? "s" : "") + " Deleted",
                                    Snackbar.LENGTH_LONG);

            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.setAction("UNDO", v -> runner.undoRequest());
            snackbar.show();
        }
    }
}