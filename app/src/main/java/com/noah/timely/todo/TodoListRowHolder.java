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

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class TodoListRowHolder extends RecyclerView.ViewHolder {

   public static final String DELETE_REQUEST = "Delete Todo";
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
   /*      views       */
   private final View header;
   private final CheckBox cbx_state;
   private final TextView tv_title, tv_category, tv_description, tv_time, tv_date;
   private final ImageView img_overflow;
   private final ExpandableLayout expl_detailLayout;
   private final View bottomDivider, v_selectionOverlay;
   private final ImageButton btn_delete, btn_edit;
   private TodoListAdapter todoRowAdapter;
   private FragmentActivity activity;
   private CoordinatorLayout coordinator;
   /*      others      */
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
      tv_date = itemView.findViewById(R.id.date);
      btn_delete = itemView.findViewById(R.id.delete);
      btn_edit = itemView.findViewById(R.id.edit);
      v_selectionOverlay = itemView.findViewById(R.id.checked_overlay);

      btn_edit.setOnClickListener(c -> AddTodoActivity.start(btn_edit.getContext(), true, todo));

      btn_delete.setOnClickListener(c -> doTodoDelete());

      img_overflow.setOnClickListener(v -> {
         boolean isExpanded = expl_detailLayout.isExpanded();
         img_overflow.animate()
                     .rotation(isExpanded ? 180 : 0)
                     .setDuration(expl_detailLayout.getDuration());
         // no-op
         // don't toggle description layout visibility if description is available
         if (TextUtils.isEmpty(todo.getTaskDescription())) {
            Toast.makeText(img_overflow.getContext(), "Description not available", Toast.LENGTH_LONG).show();
            return;
         }
         expl_detailLayout.toggle();
      });

      cbx_state.setOnClickListener(c -> {
         boolean checked = cbx_state.isChecked();
         boolean isUpdated = db.updateTodoState(todo, checked);
         if (checked && isUpdated) MiscUtil.playAlertTone(activity, MiscUtil.Alert.TODO);
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
      btn_delete.setEnabled(!disable);
      btn_delete.setFocusable(!disable);
   }

   private void trySelectTodo() {
      isChecked = !isChecked;
      v_selectionOverlay.setVisibility(isChecked ? View.VISIBLE : View.GONE);
      todoRowAdapter.onChecked(getAbsoluteAdapterPosition(), isChecked, todo.getPosition());
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
      this.todo = (TodoModel) tdList.get(getAbsoluteAdapterPosition());
      // random row decoration
      int rowDrawable = DRAWABLE[getAbsoluteAdapterPosition() % DRAWABLE.length];
      header.setBackground(ContextCompat.getDrawable(activity, rowDrawable));

      tv_title.setText(todo.getTaskTitle());
      tv_description.setText(todo.getTaskDescription());
      tv_category.setText(todo.getCategory());
      cbx_state.setChecked(todo.isTaskCompleted());

      if (TextUtils.isEmpty(todo.getCompletionDate())) {
         tv_date.setVisibility(View.GONE);
      } else {
         tv_date.setText(todo.getCompletionDate());
      }

      if (TextUtils.isEmpty(todo.getCompletionTime())) {
         tv_time.setVisibility(View.GONE);
      } else {
         String start = convertTime(todo.getStartTime(), Converter.UNIT_12);
         String end = convertTime(todo.getEndTime(), Converter.UNIT_12);
         tv_time.setText(start + " - " + end);
      }
   }

}