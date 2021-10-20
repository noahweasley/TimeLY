package com.noah.timely.todo;

import static com.noah.timely.util.Converter.convertTime;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.noah.timely.R;
import com.noah.timely.core.DataModel;
import com.noah.timely.core.RequestRunner;
import com.noah.timely.core.SchoolDatabase;
import com.noah.timely.todo.TodoListFragment.TodoListAdapter;
import com.noah.timely.util.Converter;
import com.noah.timely.util.MiscUtil;
import com.noah.timely.util.PatternUtils;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class TodoListRowHolder extends RecyclerView.ViewHolder {

   private static final int[] DRAWABLE = {
           R.drawable.rounded_ct_bb,
           R.drawable.rounded_ct_ol,
           R.drawable.rounded_ct_pi,
           R.drawable.rounded_ct_gd,
           R.drawable.rounded_ct_pu,
           R.drawable.rounded_ct_gl,
           R.drawable.rounded_ct_bd,
           R.drawable.rounded_ct_od,
           R.drawable.rounded_ct_rl
   };

   private static final int[][] COLOR2D = {
           { android.R.color.holo_orange_dark, R.color.holo_orange_dark_5 },
           { android.R.color.holo_red_light, R.color.holo_red_light_5 },
           { android.R.color.holo_green_dark, R.color.holo_green_dark_5 },
           { android.R.color.holo_purple, R.color.holo_purple_5 },
           { android.R.color.holo_red_light, R.color.holo_red_light_5 },
           { android.R.color.holo_blue_dark, R.color.holo_blue_dark_5 },
           { android.R.color.holo_green_dark, R.color.holo_green_dark_5 },
           { R.color.tomato_red, R.color.tomato_red_5 },
           { R.color.teal_700, R.color.teal_700_5 }
   };

   /*      views       */
   private final View header;
   private final CheckBox cbx_state;
   private final TextView tv_title, tv_category, tv_description, tv_time;
   private final ImageView img_overflow;
   private final ExpandableLayout expl_detailLayout;
   private final View bottomDivider, v_selectionOverlay;
   private final ImageButton btn_delete, btn_edit;
   private TodoListAdapter todoRowAdapter;
   private FragmentActivity activity;
   private CoordinatorLayout coordinator;
   /*      others      */
   public static final String DELETE_REQUEST = "Delete Todo";
   private int position;
   private boolean isChecked;
   private List<DataModel> tdList;
   private TodoModel todo;
   private SchoolDatabase db;

   public TodoListRowHolder(@NonNull View itemView) {
      super(itemView);
      header = itemView.findViewById(R.id.header);
      cbx_state = itemView.findViewById(R.id.state);
      tv_title = itemView.findViewById(R.id.title);
      img_overflow = itemView.findViewById(R.id.overflow);
      tv_category = itemView.findViewById(R.id.category);
      expl_detailLayout = itemView.findViewById(R.id.detail_layout);
      tv_description = itemView.findViewById(R.id.description);
      bottomDivider = itemView.findViewById(R.id.bottom_divider);
      tv_time = itemView.findViewById(R.id.time);
      btn_delete = itemView.findViewById(R.id.delete);
      btn_edit = itemView.findViewById(R.id.edit);
      v_selectionOverlay = itemView.findViewById(R.id.checked_overlay);

      btn_edit.setOnClickListener(c -> AddTodoActivity.start(btn_edit.getContext(), true, todo));

      btn_delete.setOnClickListener(c -> doTodoDelete());

      img_overflow.setOnClickListener(v -> {
         // no-op
         // don't toggle description layout visibility if description is available
         if (TextUtils.isEmpty(todo.getTaskDescription())) {
            Toast.makeText(img_overflow.getContext(), "Description not available", Toast.LENGTH_LONG).show();
            return;
         }

         boolean isExpanded = expl_detailLayout.isExpanded();
         img_overflow.animate()
                     .rotation(isExpanded ? 180 : 0)
                     .setDuration(expl_detailLayout.getDuration());
         expl_detailLayout.toggle();
      });

      expl_detailLayout.setOnExpansionUpdateListener((expFraction, state) -> {
         if (state == ExpandableLayout.State.COLLAPSED) {
            bottomDivider.setVisibility(View.GONE);
         } else if (state == ExpandableLayout.State.EXPANDING || state == ExpandableLayout.State.EXPANDED) {
            if (bottomDivider.getVisibility() == View.GONE) {
               bottomDivider.setVisibility(View.VISIBLE);
            }
         }
      });

      cbx_state.setOnClickListener(c -> {
         boolean checked = cbx_state.isChecked();
         boolean isUpdated = db.updateTodoState(todo, checked);
         if (checked && isUpdated) MiscUtil.playAlertTone(activity, MiscUtil.Alert.TODO_UPDATE);
      });

      // Multi - Select actions
      itemView.setOnLongClickListener(l -> {
         trySelectTodo();
         todoRowAdapter.setMultiSelectionEnabled(!todoRowAdapter.isMultiSelectionEnabled()
                                                         || todoRowAdapter.getCheckedTodosCount() != 0);
         return true;
      });

      itemView.setOnClickListener(c -> {
         if (todoRowAdapter.isMultiSelectionEnabled()) {
            trySelectTodo();
            if (todoRowAdapter.getCheckedTodosCount() == 0) {
               todoRowAdapter.setMultiSelectionEnabled(false);
            }
         } else {
            // Just show full details of _todo
            new TodoViewDialog().show(activity, todo);
         }
      });

   }

   private void doTodoDelete() {
      RequestRunner runner = RequestRunner.createInstance();
      RequestRunner.Builder builder = new RequestRunner.Builder();
      builder.setOwnerContext(activity)
             .setAdapterPosition(getAbsoluteAdapterPosition())
             .setModelList(tdList)
             .setTodoCategory(todo.getDBcategory());

      runner.setRequestParams(builder.getParams())
            .runRequest(DELETE_REQUEST);

      Snackbar snackbar = Snackbar.make(coordinator, "Todo Deleted", Snackbar.LENGTH_LONG)
                                  .setAction("undo", (view) -> runner.undoRequest())
                                  .setActionTextColor(Color.YELLOW);
      snackbar.show();
   }

   private void tryDisableViews(boolean disable) {
      btn_edit.setEnabled(!disable);
      btn_edit.setFocusable(!disable);
      cbx_state.setEnabled(!disable);
      cbx_state.setFocusable(!disable);
      cbx_state.setClickable(!disable);
      btn_delete.setEnabled(!disable);
      btn_delete.setFocusable(!disable);
   }

   private void trySelectTodo() {
      isChecked = !isChecked;
      v_selectionOverlay.setVisibility(isChecked ? View.VISIBLE : View.GONE);
      todoRowAdapter.onChecked(getAbsoluteAdapterPosition(), isChecked, todo.getId());
   }

   public TodoListRowHolder with(TodoListAdapter todoRowAdapter, int position, List<DataModel> tdList,
                                 SchoolDatabase db, CoordinatorLayout coordinator, FragmentActivity activity) {
      this.todoRowAdapter = todoRowAdapter;
      this.position = position;
      this.tdList = tdList;
      this.db = db;
      this.coordinator = coordinator;
      this.activity = activity;
      return this;
   }

   public void bindView() {
      tryDisableViews(todoRowAdapter.isMultiSelectionEnabled());
      this.todo = (TodoModel) tdList.get(getAbsoluteAdapterPosition());
      // random row decoration
      int rowDrawable = DRAWABLE[getAbsoluteAdapterPosition() % DRAWABLE.length];
      header.setBackground(ContextCompat.getDrawable(activity, rowDrawable));

      tv_title.setText(todo.getTaskTitle());
      tv_description.setText(todo.getTaskDescription());
      cbx_state.setChecked(todo.isTaskCompleted());

      int categoryOrder = todo.getCategoryOrder();
      int[] color2d = COLOR2D[categoryOrder];

      tv_category.setTextColor(ContextCompat.getColor(activity, color2d[0]));
      tv_category.setBackgroundColor(ContextCompat.getColor(activity, color2d[1]));
      tv_category.setText(todo.getCategory());

      bottomDivider.setVisibility(TextUtils.isEmpty(todo.getTaskDescription()) ? View.GONE : View.VISIBLE);

      if (TextUtils.isEmpty(todo.getCompletionTime())) {
         tv_time.setVisibility(View.GONE);
      } else {
         String startTime = todo.getStartTime(), endTime = todo.getEndTime();

         boolean use24 = MiscUtil.isUserPreferred24Hours(activity);
         // convert time to 24 hours because TimeLY saves time in 24 hours clock.
         // Algorithm: convert the first time match in the input string to 24 hours clock
         startTime = use24 ? startTime
                           : convertTime(PatternUtils.findMatch(PatternUtils._12_HoursClock, startTime),
                                         Converter.UNIT_24);
         endTime = use24 ? endTime
                         : convertTime(PatternUtils.findMatch(PatternUtils._12_HoursClock, endTime),
                                       Converter.UNIT_24);

         tv_time.setText(startTime + " - " + endTime);
      }
   }

}