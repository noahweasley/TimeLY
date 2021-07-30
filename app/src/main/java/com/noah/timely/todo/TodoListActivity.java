package com.noah.timely.todo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Process;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.noah.timely.R;
import com.noah.timely.assignment.AddAssignmentActivity;
import com.noah.timely.core.ChoiceMode;
import com.noah.timely.core.DataModel;
import com.noah.timely.core.DataMultiChoiceMode;
import com.noah.timely.core.RequestParams;
import com.noah.timely.core.RequestRunner;
import com.noah.timely.core.SchoolDatabase;
import com.noah.timely.util.ThreadUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TodoListActivity extends AppCompatActivity implements ActionMode.Callback {
    private List<DataModel> tdList = new ArrayList<>();
    private ActionMode actionMode;
    private CoordinatorLayout coordinator;
    private SchoolDatabase database;
    private Context context;
    private ViewGroup notodoView;
    private RecyclerView rv_todoList;
    private TextView itemCount;
    private TodoListAdapter adapter;
    private static final ChoiceMode choiceMode = ChoiceMode.DATA_MULTI_SELECT;
    private static final String MULTIPLE_DELETE_REQUEST = "Delete multiple todos";
    private static final String DELETE_REQUEST = "Delete todo";

    public static TodoListActivity newInstance() {
        return new TodoListActivity();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        coordinator = findViewById(R.id.coordinator2);
        notodoView = findViewById(R.id.no_assignment_view);
        ProgressBar indeterminateProgress = findViewById(R.id.indeterminateProgress);

        ThreadUtils.runBackgroundTask(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            tdList = database.getTodos();
            runOnUiThread(() -> {
                boolean empty = tdList.isEmpty();
                dismissProgressbar(indeterminateProgress);
                notodoView.setVisibility(empty ? View.VISIBLE : View.GONE);
                rv_todoList.setVisibility(empty ? View.GONE : View.VISIBLE);
                adapter.notifyDataSetChanged();

                if (itemCount != null) itemCount.setText(String.valueOf(tdList.size()));
            });
        });

        FloatingActionButton fab_add = findViewById(R.id.fab_add);
        fab_add.setOnClickListener(v -> startActivity(new Intent(this, AddAssignmentActivity.class)));

        rv_todoList = findViewById(R.id.list_todo);
        rv_todoList.setLayoutManager(new LinearLayoutManager(this));
        rv_todoList.setAdapter(new TodoListAdapter(choiceMode));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void dismissProgressbar(ProgressBar indeterminateProgress) {

    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }

    private class TodoListAdapter extends RecyclerView.Adapter<TodoListRowHolder> {
        private final ChoiceMode choiceMode;
        private boolean multiSelectionEnabled;
        private TodoListRowHolder rowHolder;

        public TodoListAdapter(ChoiceMode choiceMode) {
            super();
            this.choiceMode = choiceMode;
        }

        @NonNull
        @Override
        public TodoListRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull TodoListRowHolder holder, int position) {

        }

        @Override
        public long getItemId(int position) {
            return tdList.get(position).getPosition();
        }

        @Override
        public int getItemCount() {
            return tdList.size();
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
         * @return the checked status of a particular todolist
         */
        public boolean isChecked(int adapterPosition) {
            return choiceMode.isChecked(adapterPosition);
        }

        /**
         * @return the number of todos that was selected
         */
        public int getCheckedTodosCount() {
            return choiceMode.getCheckedChoiceCount();
        }

        /**
         * @return an array of the checked indices as seen by database
         */
        public Integer[] getCheckedTodosPositions() {
            return choiceMode.getCheckedChoicePositions();
        }

        /**
         * @return an array of the checked indices
         */
        private Integer[] getCheckedTodosIndices() {
            return choiceMode.getCheckedChoicesIndices();
        }

        /**
         * @param position           the position where the change occurred
         * @param state              the new state of the change
         * @param assignmentPosition the position of the assignment in database.
         */
        public void onChecked(int position, boolean state, int todoPosition) {
            boolean isFinished = false;

            DataMultiChoiceMode dmcm = (DataMultiChoiceMode) choiceMode;
            dmcm.setChecked(position, state, todoPosition);

            int choiceCount = dmcm.getCheckedChoiceCount();

            if (actionMode == null && choiceCount == 1) {
                actionMode = startSupportActionMode(TodoListActivity.this);
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
         * Deletes multiple todos from the list of selected items
         */
        public void deleteMultiple() {
            RequestRunner runner = RequestRunner.createInstance();
            RequestRunner.Builder builder = new RequestRunner.Builder();
            builder.setOwnerContext(TodoListActivity.this)
                   .setAdapterPosition(rowHolder.getAbsoluteAdapterPosition())
                   .setModelList(tdList)
                   .setMetadataType(RequestParams.MetaDataType.NO_DATA)
                   .setItemIndices(getCheckedTodosIndices())
                   .setPositionIndices(getCheckedTodosPositions())
                   .setDataProvider(TodoModel.class);

            runner.setRequestParams(builder.getParams())
                  .runRequest(MULTIPLE_DELETE_REQUEST);

            final int count = getCheckedTodosCount();
            Snackbar snackbar = Snackbar.make(coordinator,
                                              count + " Todo" + (count > 1 ? "s" : "") + " Deleted",
                                              Snackbar.LENGTH_LONG);

            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.setAction("UNDO", v -> runner.undoRequest());
            snackbar.show();
        }
    }
}