package com.noah.timely.todo;

import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.noah.timely.R;
import com.noah.timely.core.DataModel;

import java.util.List;

@SuppressWarnings("all")
public class TodoListRowHolder extends RecyclerView.ViewHolder {
    private View indicator_start, indicator_inner;
    private TextView tv_completionTime, tv_todoDescription;
    private CheckBox cbx_todoState;
    private ImageButton btn_view, btn_edit, btn_delete;
    private int position;
    private List<DataModel> tdList;
    private TodoModel todo;

    public TodoListRowHolder(@NonNull View itemView) {
        super(itemView);
        indicator_inner = itemView.findViewById(R.id.indicator_inner);
        indicator_start = itemView.findViewById(R.id.indicator_start);
        tv_completionTime = itemView.findViewById(R.id.time);
        tv_todoDescription = itemView.findViewById(R.id.todo_description);
        cbx_todoState = itemView.findViewById(R.id.todo_state);
        btn_view = itemView.findViewById(R.id.view);
        btn_edit = itemView.findViewById(R.id.edit);
        btn_delete = itemView.findViewById(R.id.delete);

    }

    public TodoListRowHolder with(int position, List<DataModel> tdList) {
        this.position = position;
        this.tdList = tdList;
        this.todo = (TodoModel) tdList.get(getAbsoluteAdapterPosition());

        return this;
    }

    public void bindView() {
        String completionTime = todo.getCompletionTime();

        if (TextUtils.isEmpty(completionTime)) tv_completionTime.setVisibility(View.GONE);
        else tv_completionTime.setText(completionTime);

        tv_todoDescription.setText(todo.getTaskDescription());
        cbx_todoState.setChecked(todo.isTaskCompleted());
    }
}