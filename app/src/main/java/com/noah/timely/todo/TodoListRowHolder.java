package com.noah.timely.todo;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.noah.timely.R;
import com.noah.timely.core.DataModel;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.List;

@SuppressWarnings("all")
public class TodoListRowHolder extends RecyclerView.ViewHolder {
    private TodoListFragment.TodoListAdapter todoRowAdapter;
    /*      views       */
    private CheckBox cbx_state;
    private TextView tv_title, tv_category, tv_description, tv_time, tv_date;
    private ImageView img_overflow;
    private ExpandableLayout expl_detailLayout;
    private View bottomDivider, v_selectionOverlay;
    private ImageButton btn_delete, btn_edit;
    /*      others      */
    private int position;
    private boolean isChecked;
    private List<DataModel> tdList;
    private TodoModel todo;

    public TodoListRowHolder(@NonNull View itemView) {
        super(itemView);
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

        img_overflow.setOnClickListener(v -> {
            expl_detailLayout.toggle();
            boolean isExpanded = expl_detailLayout.isExpanded();
            img_overflow.animate()
                        .rotation(isExpanded ? 180 : 0)
                        .setDuration(expl_detailLayout.getDuration());
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

    private void tryDisableViews(boolean disable) {
        btn_edit.setEnabled(!disable);
        btn_edit.setFocusable(!disable);
        btn_delete.setEnabled(!disable);
        btn_delete.setFocusable(!disable);
    }

    private void trySelectTodo() {
        isChecked = !isChecked;
        v_selectionOverlay.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        todoRowAdapter.onChecked(getAbsoluteAdapterPosition(), isChecked, todo.getPosition());
    }

    public TodoListRowHolder with(TodoListFragment.TodoListAdapter todoRowAdapter, int position, List<DataModel> tdList) {
        this.todoRowAdapter = todoRowAdapter;
        this.position = position;
        this.tdList = tdList;

        return this;
    }

    public void bindView() {
        this.todo = (TodoModel) tdList.get(getAbsoluteAdapterPosition());

    }
}