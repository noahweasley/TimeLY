package com.noah.timely.timetable;

import static com.noah.timely.util.MiscUtil.DAYS;

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

public class DaysFragment extends Fragment implements ActionMode.Callback {
   public static final String DELETE_REQUEST = "delete timetable";
   public static final String MULTIPLE_DELETE_REQUEST = "Delete Multiple Timetable";
   public static final String ARG_POSITION = "List position";
   public static final String ARG_CLASS = "Current class";
   public static final String ARG_PAGE_POSITION = "Tab position";
   public static final String ARG_DAY = "Schedule Day";
   public static final String ARG_TIME = "Schedule Time";
   static final String ARG_TO_EDIT = "Editor stat";
   static final String ARG_DATA = "Timetable Data";
   static final String ARG_CHRONOLOGY = "Chronological Order";
   private static ActionMode actionMode;
   private final ChoiceMode choiceMode = ChoiceMode.DATA_MULTI_SELECT;
   private TimeTableRowAdapter rowAdapter;
   private List<DataModel> tList;
   private TextView itemCount;
   private ViewGroup noTimetableView;
   private RecyclerView rV_timetable;
   private SchoolDatabase database;
   private CoordinatorLayout coordinator;
   private AppCompatActivity context;

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
      context = (AppCompatActivity) getActivity();
      Resources resources = getResources();
      boolean isInLandscape = resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

      ProgressBar indeterminateProgress = view.findViewById(R.id.indeterminateProgress);
      ThreadUtils.runBackgroundTask(() -> {
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
               indeterminateProgress.setVisibility(View.GONE);
               rowAdapter.notifyDataSetChanged();
               if (itemCount != null) itemCount.setText(String.valueOf(tList.size()));
               // invalidate options menu if list is empty
               if (isEmpty) getActivity().invalidateOptionsMenu();
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
         rV_timetable.setLayoutManager(new LinearLayoutManager(getActivity()));
      }

      int pagePos = getArguments().getInt(ARG_POSITION);

      view.findViewById(R.id.fab_add_new).setOnClickListener(v -> {

         Context context = getContext();
         float[] resolution = DeviceInfoUtil.getDeviceResolutionDP(context);
         float requiredWidthDP = 368, requiredHeightDP = 750;

         SharedPreferences preferences =
                 PreferenceManager.getDefaultSharedPreferences(context);

         boolean useDialog = preferences.getBoolean("prefer_dialog", true);

         // choose what kind of task-add method to use base on device width and user pref
         if (resolution[0] < requiredWidthDP || resolution[1] < requiredHeightDP) {
            startActivity(new Intent(context, AddTimetableActivity.class)
                                  .putExtra(ARG_PAGE_POSITION, pagePos));
         } else {
            if (useDialog) {
               new AddTimetableDialog().show(getContext(), pagePos);
            } else {
               startActivity(new Intent(context, AddTimetableActivity.class)
                                     .putExtra(ARG_PAGE_POSITION, pagePos));
            }
         }

      });
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
      menu.findItem(R.id.select_all).setVisible(tList.isEmpty() ? false : true);
      TooltipCompat.setTooltipText(itemCount, getString(R.string.timetable_count) + tList.size());

      super.onCreateOptionsMenu(menu, inflater);
   }

   @Override
   public void onPrepareOptionsMenu(@NonNull Menu menu) {
      menu.findItem(R.id.select_all).setVisible(tList.isEmpty() ? false : true);
   }

   @Override
   public boolean onOptionsItemSelected(@NonNull MenuItem item) {
      if (item.getItemId() == R.id.select_all) {
         rowAdapter.selectAllItems();
      } else if (item.getItemId() == R.id.export) {
         new TMLYDataGeneratorDialog().show(getContext(), Constants.TIMETABLE);
      }
      return super.onOptionsItemSelected(item);
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
   public void doLayoutRefresh(LayoutRefreshEvent event) {
      rowAdapter.notifyDataSetChanged();
   }

   @Subscribe(threadMode = ThreadMode.MAIN)
   public void doCountUpdate(CountEvent countEvent) {
      if (itemCount != null)
         itemCount.setText(String.valueOf(countEvent.getSize()));
   }

   @Subscribe(threadMode = ThreadMode.MAIN)
   public void doTimetableUpdate(TUpdateMessage update) {
      int pagePosition = update.getPagePosition();
      TimetableModel data = update.getData();
      // Because an update message would be posted to the all existing fragments in viewpager,
      // instead of updating UI for all fragments, update only a particular fragments view
      // which was specified by the currently selected day in the add-timetable dialog
      if (getArguments().getInt(ARG_POSITION) == pagePosition) {
         int changePos = data.getChronologicalOrder();
         switch (update.getType()) {
            case NEW:
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
               rowAdapter.notifyItemInserted(changePos);
               rowAdapter.notifyDataSetChanged();
               break;
            case INSERT:
               rowAdapter.notifyItemInserted(changePos);
               rowAdapter.notifyDataSetChanged();
               break;
            case REMOVE:
               rowAdapter.notifyItemRemoved(changePos);
               rowAdapter.notifyDataSetChanged();
               break;
            default:
               tList.remove(changePos);
               tList.add(changePos, data);
               rowAdapter.notifyItemChanged(changePos);
               break;
         }
         // reflect change count of data
         if (itemCount != null)
            itemCount.setText(String.valueOf(tList.size()));
         // hide or reveal select-all menu itemn
         getActivity().invalidateOptionsMenu();
      }

   }

   // Get current timetable from the current selected tab
   private String getCurrentTableDay() {
      return DAYS[getArguments().getInt(ARG_POSITION)];
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
         rowAdapter.deleteMultiple();
      } else {
         rowAdapter.selectAllItems();
      }

      return true;
   }

   @Override
   public void onDestroyActionMode(ActionMode mode) {
      actionMode = null;
      rowAdapter.reset();
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
       * Reset this adapter to initial state
       */
      public void reset() {
         choiceMode.clearChoices();
         setMultiSelectionEnabled(false);
         notifyDataSetChanged();
      }

      /**
       * @param adapterPosition the position of the view holder
       * @return the checked status of a particular image int he list
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
      private Integer[] getCheckedTimetableIndices() {
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
            actionMode = context.startSupportActionMode(DaysFragment.this);
            actionMode.setTitle(String.format(Locale.US, "%d %s", choiceMode.getCheckedChoiceCount(), "selected"));
         } else if (isAdded() && actionMode != null) {
            // select all action performed, but action mode is activated, only set title to length of list
            actionMode.setTitle(String.format(Locale.US, "%d %s", choiceMode.getCheckedChoiceCount(), "selected"));
         }
      }

      /**
       * Deletes multiple images from the list of selected items
       */
      public void deleteMultiple() {
         RequestRunner runner = RequestRunner.createInstance();
         RequestRunner.Builder builder = new RequestRunner.Builder();
         builder.setOwnerContext(getActivity())
                .setAdapterPosition(rowHolder.getAbsoluteAdapterPosition())
                .setModelList(tList)
                .setTimetable(getCurrentTableDay())
                .setMetadataType(RequestParams.MetaDataType.TIMETABLE)
                .setItemIndices(getCheckedTimetableIndices())
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