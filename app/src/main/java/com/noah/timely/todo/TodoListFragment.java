package com.noah.timely.todo;

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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.noah.timely.R;
import com.noah.timely.core.ChoiceMode;
import com.noah.timely.core.DataModel;
import com.noah.timely.core.DataMultiChoiceMode;
import com.noah.timely.core.EmptyListEvent;
import com.noah.timely.core.MultiUpdateMessage;
import com.noah.timely.core.RequestParams;
import com.noah.timely.core.RequestRunner;
import com.noah.timely.core.SchoolDatabase;
import com.noah.timely.util.Constants;
import com.noah.timely.util.LogUtils;
import com.noah.timely.util.ThreadUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TodoListFragment#newInstance} factory method to create an instance of this fragment.
 */
public class TodoListFragment extends Fragment implements ActionMode.Callback {
   public static final String MULTIPLE_DELETE_REQUEST = "Delete multiple todos";
   private static final String ARG_TODO_CATEGORY = "Todo category";
   private static final String ARG_TODO_TAB_POSITION = "Todo tab position";
   private static final ChoiceMode choiceMode = ChoiceMode.DATA_MULTI_SELECT;
   private static final String DELETE_REQUEST = "Delete todo";
   public String category;
   public int tabPosition;
   private List<DataModel> tdList = new ArrayList<>();
   private ActionMode actionMode;
   private CoordinatorLayout coordinator;
   private SchoolDatabase database;
   private AppCompatActivity context;
   private ViewGroup notodoView;
   private RecyclerView rv_todoList;
   private TextView itemCount;
   private TodoListAdapter adapter;

   /**
    * Use this factory method to create a new instance of this fragment
    *
    * @return A new instance of fragment TodoListFragment.
    */
   public static TodoListFragment newInstance(int position, String category) {
      TodoListFragment fragment = new TodoListFragment();
      Bundle bundle = new Bundle();
      bundle.putString(ARG_TODO_CATEGORY, category);
      bundle.putInt(ARG_TODO_TAB_POSITION, position);
      fragment.setArguments(bundle);
      return fragment;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      EventBus.getDefault().register(this);
      if (getArguments() != null) {
         category = getArguments().getString(ARG_TODO_CATEGORY);
         tabPosition = getArguments().getInt(ARG_TODO_TAB_POSITION);
      }
   }

   @Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
      return inflater.inflate(R.layout.fragment_todo_list, container, false);
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      context = (AppCompatActivity) getActivity();
      database = new SchoolDatabase(getContext());

      coordinator = view.findViewById(R.id.coordinator);
      notodoView = view.findViewById(R.id.no_todo_view);
      ProgressBar indeterminateProgress = view.findViewById(R.id.indeterminateProgress);

      FloatingActionButton fab_add = view.findViewById(R.id.fab_add_todo);
      fab_add.setOnClickListener(v -> AddTodoActivity.start(getContext(), false, category));

      rv_todoList = view.findViewById(R.id.todo_list);
      rv_todoList.setLayoutManager(new LinearLayoutManager(getContext()));
      rv_todoList.setAdapter(adapter = new TodoListAdapter(choiceMode));

      ThreadUtils.runBackgroundTask(() -> {
         Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

         if (tabPosition == 0) tdList = database.getFilteredTodos(category, /* Tast completed */ false);
         else tdList = database.getFilteredTodos(category, /* Tast completed */ true);

         getActivity().runOnUiThread(() -> {
            boolean empty = tdList.isEmpty();
            indeterminateProgress.setVisibility(View.GONE);
            notodoView.setVisibility(empty ? View.VISIBLE : View.GONE);
            rv_todoList.setVisibility(empty ? View.GONE : View.VISIBLE);
            adapter.notifyDataSetChanged();

            if (itemCount != null) itemCount.setText(String.valueOf(tdList.size()));
         });
      });
   }

   @Override
   public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
      super.onViewStateRestored(savedInstanceState);
      if (savedInstanceState != null)
         adapter.getChoiceMode().onRestoreInstanceState(savedInstanceState);
   }

   @Override
   public void onResume() {
      super.onResume();
      setHasOptionsMenu(true);
      ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(retrieveToolbarTitle(category));
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
      inflater.inflate(R.menu.list_menu_todo, menu);
      View layout = menu.findItem(R.id.list_item_count).getActionView();
      itemCount = layout.findViewById(R.id.counter);
      TooltipCompat.setTooltipText(itemCount, "Todo Count");
      super.onCreateOptionsMenu(menu, inflater);
   }

   @Subscribe(threadMode = ThreadMode.MAIN)
   public void doTodoUpdate(TDUpdateMessage update) {
      LogUtils.debug(this, "Received update: " + update.getType());
      if (tabPosition == 0) {
         TodoModel data = update.getData();
         // data position is not the same as the absolute adapter position in list so, use the change ID that was
         // gotten from the absolute adapter position to make changes to the list.
         int changePos = data.getPosition();
         boolean listEmpty = tdList.isEmpty();
         switch (update.getType()) {
            case NEW:
               tdList.add(data);
               itemCount.setText(String.valueOf(tdList.size()));

               notodoView.setVisibility(listEmpty ? View.VISIBLE : View.GONE);
               rv_todoList.setVisibility(listEmpty ? View.GONE : View.VISIBLE);

               adapter.notifyItemInserted(changePos);
               break;
            case REMOVE:
               tdList.remove(changePos);
               itemCount.setText(String.valueOf(tdList.size()));
               adapter.notifyItemRemoved(changePos);
               adapter.notifyDataSetChanged();

               if (listEmpty) doEmptyListUpdate(null);

               break;
            case INSERT:
               tdList.add(changePos, data);
               itemCount.setText(String.valueOf(tdList.size()));
               adapter.notifyItemInserted(changePos);
               adapter.notifyDataSetChanged();
               doEmptyListUpdate(null);
               break;
            default:
               TodoModel tm = (TodoModel) tdList.remove(changePos);
               tm.setTaskCompleted(data.isTaskCompleted());
               tm.setTaskTitle(data.getTaskTitle());
               tm.setStartTime(data.getStartTime());
               tm.setEndTime(data.getEndTime());
               tm.setCompletionTime(data.getCompletionTime());
               tm.setCompletionDate(data.getCompletionDate());
               tm.setTaskDescription(data.getTaskDescription());
               tdList.add(tm);
               adapter.notifyItemChanged(changePos);
               break;
         }
      }
   }

   @Subscribe(threadMode = ThreadMode.MAIN)
   public void doEmptyListUpdate(EmptyListEvent e) {
      notodoView.setVisibility(tdList.isEmpty() ? View.VISIBLE : View.GONE);
      rv_todoList.setVisibility(tdList.isEmpty() ? View.GONE : View.VISIBLE);
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
      context.getMenuInflater().inflate(R.menu.deleted_items, menu);
      return true;
   }

   @Override
   public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
      return false;
   }

   @Override
   public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
      adapter.deleteMultiple();
      return true;
   }

   @Override
   public void onDestroyActionMode(ActionMode mode) {
      actionMode = null;
      adapter.getChoiceMode().clearChoices();
      adapter.notifyDataSetChanged();
   }

   private String retrieveToolbarTitle(String category) {
      switch (category) {
         case Constants.TODO_GENERAL:
            return "All";
         case Constants.TODO_WORK:
            return "Work";
         case Constants.TODO_MUSIC:
            return "Music";
         case Constants.TODO_CREATIVITY:
            return "Creativity";
         case Constants.TODO_TRAVEL:
            return "Travel";
         case Constants.TODO_STUDY:
            return "Study";
         case Constants.TODO_FUN:
            return "Leisure and Fun";
         case Constants.TODO_HOME:
            return "Home";
         case Constants.TODO_MISCELLANEOUS:
            return "Miscelaneous";
         case Constants.TODO_SHOPPING:
            return "Shopping";
      }
      return null;
   }

   class TodoListAdapter extends RecyclerView.Adapter<TodoListRowHolder> {
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
         View rowView = getLayoutInflater().inflate(R.layout.todo_list_row, parent, false);
         return rowHolder = new TodoListRowHolder(rowView);
      }

      @Override
      public void onBindViewHolder(@NonNull TodoListRowHolder holder, int position) {
         holder.with(this, position, tdList, database, coordinator, getActivity()).bindView();
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
       * @param position     the position where the change occurred
       * @param state        the new state of the change
       * @param todoPosition the position of the assignment in database.
       */
      public void onChecked(int position, boolean state, int todoPosition) {
         boolean isFinished = false;

         DataMultiChoiceMode dmcm = (DataMultiChoiceMode) choiceMode;
         dmcm.setChecked(position, state, todoPosition);

         int choiceCount = dmcm.getCheckedChoiceCount();

         if (actionMode == null && choiceCount == 1) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            actionMode = activity.startSupportActionMode(TodoListFragment.this);
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
         builder.setOwnerContext(getActivity())
                 .setAdapterPosition(rowHolder.getAbsoluteAdapterPosition())
                 .setModelList(tdList)
                 .setMetadataType(RequestParams.MetaDataType.TODO)
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
         snackbar.setAction("UNDO", x -> runner.undoRequest());
         snackbar.show();
      }
   }

}