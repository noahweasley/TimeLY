package com.astrro.timely.todo;

import static com.astrro.timely.util.Converter.convertTime;

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

import com.astrro.timely.R;
import com.astrro.timely.core.DataModel;
import com.astrro.timely.core.RequestRunner;
import com.astrro.timely.core.SchoolDatabase;
import com.astrro.timely.todo.TodoListFragment.TodoListAdapter;
import com.astrro.timely.util.Converter;
import com.astrro.timely.util.MiscUtil;
import com.astrro.timely.util.PatternUtils;
import com.astrro.timely.util.sound.AlertType;
import com.astrro.timely.util.sound.SoundUtils;
import com.google.android.material.snackbar.Snackbar;

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
   private int position, pagePosition;
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

      btn_edit.setOnClickListener(c -> AddTodoActivity.start(btn_edit.getContext(), true,
                                                             getAbsoluteAdapterPosition(), todo));

      btn_delete.setOnClickListener(c -> doTodoDelete());

      img_overflow.setOnClickListener(v -> {
         // no-op
         // don't toggle description layout visibility if description is available
         if (TextUtils.isEmpty(todo.getTaskDescription())) {
            Toast.makeText(img_overflow.getContext(), "Description not available", Toast.LENGTH_SHORT).show();
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
         if (checked && isUpdated)
            SoundUtils.playAlertTone(activity.getApplicationContext(), AlertType.TODO_UPDATE);
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
             .setPagePosition(pagePosition)
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

   public TodoListRowHolder with(TodoListAdapter todoRowAdapter, int position, int pagePosition, List<DataModel> tdList,
                                 SchoolDatabase db, CoordinatorLayout coordinator, FragmentActivity activity) {
      this.todoRowAdapter = todoRowAdapter;
      this.position = position;
      this.pagePosition = pagePosition;
      this.tdList = tdList;
      this.db = db;
      this.coordinator = coordinator;
      this.activity = activity;
      return this;
   }

   public void bindView() {
      isChecked = todoRowAdapter.isChecked(getAbsoluteAdapterPosition());
      v_selectionOverlay.setVisibility(isChecked ? View.VISIBLE : View.GONE);
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
         // convert time to 12 hours if user uses 12 hours mode, because TimeLY saves time in 24 hours clock.
         // Algorithm: convert the first time match in the input string to 12 hours clock
         String match = PatternUtils.findMatch(PatternUtils._24_HoursClock, startTime);
         String match1 = PatternUtils.findMatch(PatternUtils._24_HoursClock, endTime);

         String convertTime = convertTime(match, Converter.UNIT_12);
         String convertTime1 = convertTime(match1, Converter.UNIT_12);

         String convertedStart = startTime.substring(0, startTime.indexOf(match)) + convertTime;
         String convertedEnd = endTime.substring(0, endTime.indexOf(match1)) + convertTime;

         startTime = use24 ? startTime : convertedStart;
         endTime = use24 ? endTime : convertedEnd;

         String completionTime = startTime + " - " + endTime;

         tv_time.setText(completionTime);
      }
   }

}