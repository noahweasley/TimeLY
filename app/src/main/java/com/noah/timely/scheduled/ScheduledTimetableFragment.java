package com.noah.timely.scheduled;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.noah.timely.R;
import com.noah.timely.assignment.LayoutRefreshEvent;
import com.noah.timely.core.ChoiceMode;
import com.noah.timely.core.CountEvent;
import com.noah.timely.core.DataModel;
import com.noah.timely.core.DataMultiChoiceMode;
import com.noah.timely.core.EmptyListEvent;
import com.noah.timely.core.MultiUpdateMessage;
import com.noah.timely.core.RequestParams;
import com.noah.timely.core.RequestRunner;
import com.noah.timely.core.SchoolDatabase;
import com.noah.timely.exports.TMLYDataGeneratorDialog;
import com.noah.timely.main.MainActivity;
import com.noah.timely.timetable.TimeTableRowHolder;
import com.noah.timely.timetable.TimetableModel;
import com.noah.timely.util.collections.CollectionUtils;
import com.noah.timely.util.Constants;
import com.noah.timely.util.DeviceInfoUtil;
import com.noah.timely.util.ThreadUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ScheduledTimetableFragment extends Fragment implements ActionMode.Callback {
   public static final String DELETE_REQUEST = "delete scheduled timetable";
   public static final String MULTIPLE_DELETE_REQUEST = "delete multiple timetable";
   public static final String ARG_DATA = "Timetable Data";
   static final String ARG_TO_EDIT = "Editor stat";
   private final ChoiceMode choiceMode = ChoiceMode.DATA_MULTI_SELECT;
   private ActionMode actionMode;
   private ViewGroup noTimetableView;
   private ScheduledTimetableFragment.TimeTableRowAdapter tableRowAdapter;
   private List<DataModel> tList;
   private SchoolDatabase database;
   private TextView itemCount;
   private RecyclerView rV_timetable;
   private CoordinatorLayout coordinator;
   private AppCompatActivity context;

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
      context = (AppCompatActivity) getActivity();
      Resources resources = getResources();
      coordinator = view.findViewById(R.id.coordinator);
      boolean isInLandscape = resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

      ProgressBar indeterminateProgress = view.findViewById(R.id.indeterminateProgress);
      ThreadUtils.runBackgroundTask(() -> {
         Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
         tList = database.getTimeTableData(SchoolDatabase.SCHEDULED_TIMETABLE);
         // Sort the timetable according to the day and start time
         Collections.sort(tList, (t1, t2) -> {
            TimetableModel tm1 = ((TimetableModel) t1);
            TimetableModel tm2 = ((TimetableModel) t2);
            int cmpDay = Integer.compare(tm1.getDayIndex(), tm2.getDayIndex());
            if (cmpDay == 0) return Integer.compare(tm1.getStartTimeAsInt(), tm2.getStartTimeAsInt());
            else return cmpDay;
         });
         // post a message to the message queue to update the table's ui
         if (isAdded())
            getActivity().runOnUiThread(() -> {
               boolean isEmpty = tList.isEmpty();
               doEmptyTimetableUpdate(null);
               indeterminateProgress.setVisibility(View.GONE);
               tableRowAdapter.notifyDataSetChanged();
               if (itemCount != null) itemCount.setText(String.valueOf(tList.size()));
               // invalidate options menu if list is empty
               if (isEmpty) getActivity().invalidateOptionsMenu();
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
            RequestRunner runner = RequestRunner.createInstance();
            Snackbar.make(coordinator, "Timetable Deleted", Snackbar.LENGTH_LONG)
                    .setAction("undo", (view) -> runner.undoRequest())
                    .setActionTextColor(Color.YELLOW)
                    .show();

            RequestRunner.Builder builder = new RequestRunner.Builder();
            builder.setOwnerContext(getActivity())
                   .setAdapterPosition(viewHolder.getAbsoluteAdapterPosition())
                   .setModelList(tList);

            runner.setRequestParams(builder.getParams())
                  .runRequest(DELETE_REQUEST);
         }
      });
      swiper.attachToRecyclerView(rV_timetable);

      tableRowAdapter.setHasStableIds(true);
      rV_timetable.setHasFixedSize(true);
      rV_timetable.setAdapter(tableRowAdapter);
      setUpSwipeHelper(rV_timetable);

      if (isInLandscape) {
         rV_timetable.setLayoutManager(new GridLayoutManager(getActivity(), 2));
      } else {
         rV_timetable.setLayoutManager(new LinearLayoutManager(getActivity()));
      }

      view.findViewById(R.id.fab_add_new).setOnClickListener(v -> {
         Context context = getContext();

         float[] resolution = DeviceInfoUtil.getDeviceResolutionDP(context);
         float requiredWidthDP = 368, requiredHeightDP = 750;

         SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
         boolean useDialog = preferences.getBoolean("prefer_dialog", true);
         // choose what kind of task-add method to use base on device width and user pref
         if (resolution[0] < requiredWidthDP || resolution[1] < requiredHeightDP) {
            startActivity(new Intent(context, AddScheduledActivity.class));
         } else {
            if (useDialog) {
               new AddScheduledDialog().show(context);
            } else {
               startActivity(new Intent(context, AddScheduledActivity.class));
            }
         }
      });
   }

   private void setUpSwipeHelper(RecyclerView recyclerView) {
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
            RequestRunner runner = RequestRunner.createInstance();
            Snackbar.make(coordinator, "Timetable Deleted", Snackbar.LENGTH_LONG)
                    .setAction("undo", (view) -> runner.undoRequest())
                    .setActionTextColor(Color.YELLOW)
                    .show();

            RequestRunner.Builder builder = new RequestRunner.Builder();
            builder.setOwnerContext(getActivity())
                   .setAdapterPosition(viewHolder.getAbsoluteAdapterPosition())
                   .setModelList(tList);

            runner.setRequestParams(builder.getParams())
                  .runRequest(DELETE_REQUEST);
         }
      });

      swiper.attachToRecyclerView(recyclerView);
   }

   @Override
   public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
      super.onViewStateRestored(savedInstanceState);
      if (savedInstanceState != null)
         tableRowAdapter.getChoiceMode().onRestoreInstanceState(savedInstanceState);
   }

   @Override
   public void onResume() {
      super.onResume();
      ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.classes);
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
      menu.findItem(R.id.select_all).setVisible(tList.isEmpty() ? false : true);
      TooltipCompat.setTooltipText(itemCount, getString(R.string.classes_count) + tList.size());

      super.onCreateOptionsMenu(menu, inflater);
   }

   @Override
   public void onPrepareOptionsMenu(@NonNull Menu menu) {
      menu.findItem(R.id.select_all).setVisible(tList.isEmpty() ? false : true);
   }

   @Override
   public boolean onOptionsItemSelected(@NonNull MenuItem item) {
      if (item.getItemId() == R.id.select_all) {
         tableRowAdapter.selectAllItems();
      } else if (item.getItemId() == R.id.export) {
         new TMLYDataGeneratorDialog().show(getContext(), Constants.SCHEDULED_TIMETABLE);
      }
      return super.onOptionsItemSelected(item);
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
   public void doLayoutRefresh(LayoutRefreshEvent event) {
      tableRowAdapter.notifyDataSetChanged();
   }

   @Subscribe(threadMode = ThreadMode.MAIN)
   public void doTimetableUpdate(SUpdateMessage update) {
      TimetableModel data = update.getData();
      int changePos = data.getChronologicalOrder();

      switch (update.getType()) {
         case NEW:
            tList.add(changePos, data);
            if (tList.isEmpty()) {
               noTimetableView.setVisibility(View.VISIBLE);
               rV_timetable.setVisibility(View.GONE);
            } else {
               noTimetableView.setVisibility(View.GONE);
               rV_timetable.setVisibility(View.VISIBLE);
            }
            tableRowAdapter.notifyItemInserted(changePos);
            tableRowAdapter.notifyDataSetChanged();
            break;
         case INSERT:
            tableRowAdapter.notifyItemInserted(changePos);
            tableRowAdapter.notifyDataSetChanged();
            break;
         case REMOVE:
            tableRowAdapter.notifyItemRemoved(changePos);
            tableRowAdapter.notifyDataSetChanged();
            break;
         default:
            tList.remove(changePos);
            tList.add(changePos, data);
            tableRowAdapter.notifyItemChanged(changePos);
            break;
      }
      // reflect data count
      if (itemCount != null)
         itemCount.setText(String.valueOf(tList.size()));
      // hide or reveal select-all menu itemn
      getActivity().invalidateOptionsMenu();
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
      if (item.getItemId() == R.id.delete_multiple_action) {
         tableRowAdapter.deleteMultiple();
      } else {
         tableRowAdapter.selectAllItems();
      }

      return true;
   }

   @Override
   public void onDestroyActionMode(ActionMode mode) {
      actionMode = null;
      tableRowAdapter.reset();
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
         View view = getLayoutInflater().inflate(R.layout.scheduled_timetable_row, viewGroup, false);
         return (rowHolder = new TimeTableRowHolder(view));
      }

      @Override
      public void onBindViewHolder(@NonNull TimeTableRowHolder timeTableRowHolder, int position) {
         timeTableRowHolder.with(ScheduledTimetableFragment.this, tableRowAdapter, tList, coordinator, position)
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
       * Reset this adapter to initial state
       */
      public void reset() {
         choiceMode.clearChoices();
         setMultiSelectionEnabled(false);
         notifyDataSetChanged();
      }

      /**
       * @param adapterPosition the position of the view holder
       * @return the checked status of a particular timetable in the list
       */
      public boolean isChecked(int adapterPosition) {
         return choiceMode.isChecked(adapterPosition);
      }

      /**
       * @return the number of timetables that was selected
       */
      public int getCheckedTimetablesCount() {
         return choiceMode.getCheckedChoiceCount();
      }

      /**
       * @return an array of the checked indices as seen by database
       */
      public Integer[] getCheckedTimetablesPositions() {
         return choiceMode.getCheckedChoicePositions();
      }

      /**
       * @return an array of the checked indices
       */
      private Integer[] getCheckedTimetablesIndices() {
         return choiceMode.getCheckedChoicesIndices();
      }

      /**
       * @param position          the position where the change occurred
       * @param state             the new state of the change
       * @param timetablePosition the position of the timetable in database.
       */
      public void onChecked(int position, boolean state, int timetablePosition) {
         boolean isFinished = false;

         DataMultiChoiceMode dmcm = (DataMultiChoiceMode) choiceMode;
         dmcm.setChecked(position, state, timetablePosition);

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
       * Selects all items on the list
       */
      public void selectAllItems() {
         DataMultiChoiceMode dmcm = (DataMultiChoiceMode) choiceMode;
         dmcm.selectAll(tList.size(), CollectionUtils.map(tList, DataModel::getId));
         notifyDataSetChanged();
         setMultiSelectionEnabled(true);
         // also start action mode
         if (isAdded() && actionMode == null) {
            // select all action peformed, create ation mode, because it wasn't already created
            actionMode = context.startSupportActionMode(ScheduledTimetableFragment.this);
            actionMode.setTitle(String.format(Locale.US, "%d %s", choiceMode.getCheckedChoiceCount(), "selected"));
         } else if (isAdded() && actionMode != null) {
            // select all action performed, but action mode is activated, only set title to length of list
            actionMode.setTitle(String.format(Locale.US, "%d %s", choiceMode.getCheckedChoiceCount(), "selected"));
         }
      }

      /**
       * Deletes multiple items from checked
       */
      public void deleteMultiple() {
         RequestRunner runner = RequestRunner.createInstance();
         RequestRunner.Builder builder = new RequestRunner.Builder();
         builder.setOwnerContext(getActivity())
                .setAdapterPosition(rowHolder.getAbsoluteAdapterPosition())
                .setModelList(tList)
                .setTimetable(SchoolDatabase.SCHEDULED_TIMETABLE)
                .setMetadataType(RequestParams.MetaDataType.TIMETABLE)
                .setItemIndices(getCheckedTimetablesIndices())
                .setPositionIndices(getCheckedTimetablesPositions())
                .setDataProvider(TimetableModel.class);

         runner.setRequestParams(builder.getParams())
               .runRequest(MULTIPLE_DELETE_REQUEST);

         final int count = getCheckedTimetablesCount();
         Snackbar snackbar = Snackbar.make(coordinator,
                                           count + " Course" + (count > 1 ? "s" : "") + " Deleted",
                                           Snackbar.LENGTH_LONG);

         snackbar.setActionTextColor(Color.YELLOW);
         snackbar.setAction("UNDO", v -> runner.undoRequest());
         snackbar.show();
      }
   }
}