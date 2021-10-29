package com.noah.timely.exam;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.noah.timely.R;
import com.noah.timely.core.ChoiceMode;
import com.noah.timely.core.CountEvent;
import com.noah.timely.core.DataModel;
import com.noah.timely.core.DataMultiChoiceMode;
import com.noah.timely.core.EmptyListEvent;
import com.noah.timely.core.MultiUpdateMessage;
import com.noah.timely.core.RequestParams;
import com.noah.timely.core.RequestRunner;
import com.noah.timely.core.SchoolDatabase;
import com.noah.timely.util.CollectionUtils;
import com.noah.timely.util.DeviceInfoUtil;
import com.noah.timely.util.ThreadUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("ConstantConditions")
public class ExamTimetableFragment extends Fragment implements ActionMode.Callback {
   public static final String ARG_POSITION = "page position";
   public static final String MULTIPLE_DELETE_REQUEST = "Delete Multiple exams";
   private static ActionMode actionMode;
   private final ChoiceMode choiceMode = ChoiceMode.DATA_MULTI_SELECT;
   private List<DataModel> eList;
   private SchoolDatabase database;
   private TextView itemCount;
   private ViewGroup noExamView;
   private RecyclerView rv_Exams;
   private CoordinatorLayout coordinator;
   private ExamRowAdapter examRowAdapter;
   private AppCompatActivity context;

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
      examRowAdapter = new ExamRowAdapter(choiceMode);
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
      context = (AppCompatActivity) getActivity();
      ProgressBar indeterminateProgress = view.findViewById(R.id.indeterminateProgress);
      int position = getArguments().getInt(ARG_POSITION);

      ThreadUtils.runBackgroundTask(() -> {
         Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
         eList = database.getExamTimetableDataFor(position);
         // Sort by start time
         Collections.sort(eList, (e1, e2) -> {
            ExamModel em1 = (ExamModel) e1;
            ExamModel em2 = (ExamModel) e2;
            int cmp = Integer.compare(em1.getDayIndex(), em2.getDayIndex());
            if (cmp != 0) return cmp;
            else return Integer.compare(em1.getStartAsInt(), em2.getStartAsInt());
         });
         // post a message to the message queue to update the table's ui
         if (isAdded()) {
            getActivity().runOnUiThread(() -> {
               boolean isEmpty = eList.isEmpty();
               doEmptyExamsUpdate(null);
               indeterminateProgress.setVisibility(View.GONE);
               examRowAdapter.notifyDataSetChanged();
               if (itemCount != null) itemCount.setText(String.valueOf(eList.size()));
               // hide or reveal select-all menu itemn
               getActivity().invalidateOptionsMenu();
            });
         }
      });

      noExamView = view.findViewById(R.id.no_exams_view);
      rv_Exams = view.findViewById(R.id.exams);
      coordinator = view.findViewById(R.id.coordinator);

      int pagePos = getArguments().getInt(ARG_POSITION);

      view.findViewById(R.id.add_exams).setOnClickListener(v -> {
         Context context = getContext();
         float[] resolution = DeviceInfoUtil.getDeviceResolutionDP(context);
         float requiredWidthDP = 368, requiredHeightDP = 750;

         SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
         boolean useDialog = preferences.getBoolean("prefer_dialog", true);
         // choose what kind of task-add method to use base on device width and user pref
         if (resolution[0] < requiredWidthDP || resolution[1] < requiredHeightDP) {
            startActivity(new Intent(context, AddExamActivity.class).putExtra(ARG_POSITION, pagePos));
         } else {
            if (useDialog) {
               new AddExamDialog().show(getContext(), pagePos);
            } else {
               startActivity(new Intent(context, AddExamActivity.class).putExtra(ARG_POSITION, pagePos));
            }
         }
      });

      // set  list to have a fixed size to increase performance and set stable id, to use same
      // view holder on adapter change
      rv_Exams.setHasFixedSize(true);
      examRowAdapter.setHasStableIds(true);
      rv_Exams.setAdapter(examRowAdapter);

      rv_Exams.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
   }

   @Override
   public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
      super.onViewStateRestored(savedInstanceState);
      if (savedInstanceState != null)
         examRowAdapter.getChoiceMode().onRestoreInstanceState(savedInstanceState);
   }

   @Override
   public void onResume() {
      // Prevent glitch on adding menu to the toolbar. Only show a particular semester's course
      // count, if that is the only visible semester
      setHasOptionsMenu(true); // onCreateOptionsMenu will be called after this
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
      inflater.inflate(R.menu.list_menu_exams, menu);
      View layout = menu.findItem(R.id.list_item_count).getActionView();
      itemCount = layout.findViewById(R.id.counter);
      itemCount.setText(String.valueOf(eList.size()));
      menu.findItem(R.id.select_all).setVisible(eList.isEmpty() ? false : true);
      TooltipCompat.setTooltipText(itemCount, "Exams Count");

      super.onCreateOptionsMenu(menu, inflater);
   }

   @Override
   public void onPrepareOptionsMenu(@NonNull Menu menu) {
      menu.findItem(R.id.select_all).setVisible(eList.isEmpty() ? false : true);
   }

   @Override
   public boolean onOptionsItemSelected(@NonNull MenuItem item) {
      if (item.getItemId() == R.id.select_all) {
         examRowAdapter.selectAllItems();
      }
      return super.onOptionsItemSelected(item);
   }

   @Subscribe(threadMode = ThreadMode.MAIN)
   public void doEmptyExamsUpdate(EmptyListEvent o) {
      noExamView.setVisibility(eList.isEmpty() ? View.VISIBLE : View.GONE);
      rv_Exams.setVisibility(eList.isEmpty() ? View.GONE : View.VISIBLE);
   }

   @Subscribe(threadMode = ThreadMode.MAIN)
   public void doCountUpdate(CountEvent countEvent) {
      itemCount.setText(String.valueOf(countEvent.getSize()));
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
      if (item.getItemId() == R.id.delete_multiple_action) {
         examRowAdapter.deleteMultiple();
      } else {
         examRowAdapter.selectAllItems();
      }

      return true;
   }

   @Override
   public void onDestroyActionMode(ActionMode mode) {
      actionMode = null;
      examRowAdapter.getChoiceMode().clearChoices();
      examRowAdapter.notifyDataSetChanged();
   }

   @Subscribe(threadMode = ThreadMode.MAIN)
   public void doExamsUpdate(EUpdateMessage update) {
      int pagePosition = update.getPagePosition();
      // Because an update message would be posted to all the existing fragments in viewpager, instead of updating UI
      // for all fragments, update only a particular fragments view which was specified by the currently viewed exam
      // week timetable.
      if (getArguments().getInt(ARG_POSITION) == pagePosition) {
         ExamModel data = update.getData();
         int changePos = data.getChronologicalOrder();

         if (changePos >= 0) {
            switch (update.getType()) {
               case NEW:
                  eList.add(changePos, data);
                  examRowAdapter.notifyItemInserted(changePos);
                  examRowAdapter.notifyDataSetChanged();
                  doEmptyExamsUpdate(null);
                  break;
               case INSERT:
                  examRowAdapter.notifyItemInserted(changePos);
                  examRowAdapter.notifyDataSetChanged();
                  break;
               case REMOVE:
                  examRowAdapter.notifyItemRemoved(changePos);
                  examRowAdapter.notifyDataSetChanged();
                  break;
               default:
                  // This else block is never used, but is left here for future app updates, where I would need
                  // to edit exams.
                  eList.remove(changePos);
                  eList.add(changePos, data);
                  examRowAdapter.notifyItemChanged(changePos);
                  break;
            }
            // reflect data count
            if (itemCount != null)
               itemCount.setText(String.valueOf(eList.size()));
         } else {
            Log.w(getClass().getSimpleName(), "Couldn't update list for position: " + changePos);
         }
      }
   }

   class ExamRowAdapter extends RecyclerView.Adapter<ExamRowHolder> {
      private final ChoiceMode choiceMode;
      private boolean multiSelectionEnabled;
      private ExamRowHolder rowHolder;

      public ExamRowAdapter(ChoiceMode choiceMode) {
         super();
         this.choiceMode = choiceMode;
      }

      @NonNull
      @Override
      public ExamRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         View view = getLayoutInflater().inflate(R.layout.exam_list_row, parent, false);
         return (rowHolder = new ExamRowHolder(view));
      }

      @Override
      public void onBindViewHolder(@NonNull ExamRowHolder holder, int position) {
         holder.with(ExamTimetableFragment.this, examRowAdapter, eList, coordinator)
               .bindView();
      }

      @Override
      public long getItemId(int position) {
         if (eList.size() > 0) {
            return eList.get(position).getId();
         }
         return -2;
      }

      @Override
      public int getItemCount() {
         return eList.size();
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
      public Integer[] getCheckedExamsPositions() {
         return choiceMode.getCheckedChoicePositions();
      }

      /**
       * @return an array of the checked indices
       */
      private Integer[] getCheckedExamsIndices() {
         return choiceMode.getCheckedChoicesIndices();
      }

      /**
       * @param position     the position where the change occurred
       * @param state        the new state of the change
       * @param examPosition the position of the assignment in database.
       */
      public void onChecked(int position, boolean state, int examPosition) {
         boolean isFinished = false;

         DataMultiChoiceMode dmcm = (DataMultiChoiceMode) choiceMode;
         dmcm.setChecked(position, state, examPosition);

         int choiceCount = dmcm.getCheckedChoiceCount();

         if (actionMode == null && choiceCount == 1) {
            AppCompatActivity context = (AppCompatActivity) getActivity();
            if (isAdded()) {
               actionMode = context.startSupportActionMode(ExamTimetableFragment.this);
            }
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
       * Selects all items on the list
       */
      public void selectAllItems() {
         DataMultiChoiceMode dmcm = (DataMultiChoiceMode) choiceMode;
         dmcm.selectAll(eList.size(), CollectionUtils.map(eList, DataModel::getPosition));
         notifyDataSetChanged();
         setMultiSelectionEnabled(true);
         // also start action mode
         if (isAdded() && actionMode == null) {
            // select all action peformed, create ation mode, because it wasn't already created
            actionMode = context.startSupportActionMode(ExamTimetableFragment.this);
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
                .setModelList(eList)
                .setMetadataType(RequestParams.MetaDataType.EXAM)
                .setItemIndices(getCheckedExamsIndices())
                .setPositionIndices(getCheckedExamsPositions())
                .setDataProvider(ExamModel.class);

         runner.setRequestParams(builder.getParams())
               .runRequest(MULTIPLE_DELETE_REQUEST);

         final int count = getCheckedCoursesCount();
         Snackbar snackbar = Snackbar.make(coordinator,
                                           count + " Exam" + (count > 1 ? "s" : "") + " Deleted",
                                           Snackbar.LENGTH_LONG);
         snackbar.setActionTextColor(Color.YELLOW);
         snackbar.setAction("UNDO", v -> runner.undoRequest());
         snackbar.show();
      }
   }
}