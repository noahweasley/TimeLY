package com.noah.timely.todo;

import static com.noah.timely.util.Utility.isUserPreferred24Hours;
import static com.noah.timely.util.Utility.playAlertTone;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.noah.timely.R;
import com.noah.timely.assignment.LayoutRefreshEvent;
import com.noah.timely.core.SchoolDatabase;
import com.noah.timely.util.CollectionUtils;
import com.noah.timely.util.LogUtils;
import com.noah.timely.util.Utility;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTodoActivity extends AppCompatActivity {
    private SchoolDatabase database;
    public static final String EXTRA_IS_EDITABLE = "com.noah.timely.todo.edit";
    public static final String EXTRA_DEFAULT_CATEGORY = "com.noah.timely.todo.category.default";
    public static final String[] CATEGORIES = {"Miscelaneous", "Work", "Music", "Creativity", "Travel", "Study"
            , "Leisure and Fun", "Home", "Shopping"};
    private String category = TodoModel.CATEGORIES[0];
    private EditText edt_taskEditor, edt_taskDescription;
    private TextView tv_startTime, tv_endTime;
    private boolean isEditable;
    private ViewGroup vg_timeContainer;

    public static void start(Context context, boolean toEdit, String defCategory) {
        Intent starter = new Intent(context, AddTodoActivity.class);
        starter.putExtra(EXTRA_IS_EDITABLE, toEdit);
        starter.putExtra(EXTRA_DEFAULT_CATEGORY, defCategory);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_todo);
        database = new SchoolDatabase(this);
        setSupportActionBar(findViewById(R.id.toolbar));

        isEditable = getIntent().getBooleanExtra(EXTRA_IS_EDITABLE, false);
        getSupportActionBar().setTitle(isEditable ? "Update Task" : "New Task");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button btn_addTask = findViewById(R.id.add_task);
        edt_taskEditor = findViewById(R.id.task_editor);
        edt_taskDescription = findViewById(R.id.task_description);
        tv_startTime = findViewById(R.id.start_time);
        tv_endTime = findViewById(R.id.end_time);

        btn_addTask.setOnClickListener(v -> addTask());

        vg_timeContainer = findViewById(R.id.time_container);
        ImageButton btn_remove = (ImageButton) vg_timeContainer.getChildAt(0);
        Button btn_addTimeFrame = findViewById(R.id.add_timeframe);

        btn_addTimeFrame.setOnClickListener(v -> {
            int visibilityFlag = btn_addTimeFrame.getVisibility();
            if (visibilityFlag == View.VISIBLE) {
                btn_addTimeFrame.setVisibility(View.GONE);
                vg_timeContainer.setVisibility(View.VISIBLE);
            }
        });

        btn_remove.setOnClickListener(v -> {
            int visibilityFlag = vg_timeContainer.getVisibility();
            if (visibilityFlag == View.VISIBLE) {
                vg_timeContainer.setVisibility(View.GONE);
                btn_addTimeFrame.setVisibility(View.VISIBLE);
            }
        });

        TextView tv_startTime = findViewById(R.id.start_time);
        TextView tv_endTime = findViewById(R.id.end_time);

        if (isUserPreferred24Hours(this)) {
            tv_startTime.setText(R.string.default_start_time_24);
            tv_endTime.setText(R.string.default_end_time_24);
        }

        tv_startTime.setOnClickListener(this::onClick);
        tv_endTime.setOnClickListener(this::onClick);

        Spinner spin_category = findViewById(R.id.category);

        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, CATEGORIES);

        courseAdapter.setDropDownViewResource(R.layout.simple_dropdown_item_1line);

        spin_category.setAdapter(courseAdapter);
        spin_category.setSelection(CollectionUtils.linearSearch(CATEGORIES,
                                                                getIntent().getStringExtra(EXTRA_DEFAULT_CATEGORY)));
        spin_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = TodoModel.CATEGORIES_2[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void onClick(View view) {
        TextView text = (TextView) view;
        boolean is24HourMode = isUserPreferred24Hours(this);
        TimePickerDialog.OnTimeSetListener tsl = (TimePickerDialog timePicker, int hourOfDay, int minute, int second) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            SimpleDateFormat timeFormat24 = new SimpleDateFormat("HH:mm", Locale.US);
            SimpleDateFormat timeFormat12 = new SimpleDateFormat("hh:mm aa", Locale.US);

            String parsedTime = is24HourMode ? timeFormat24.format(calendar.getTime())
                                             : timeFormat12.format(calendar.getTime());

            text.setText(parsedTime);
        };

        Calendar calendar = Calendar.getInstance();

        FragmentManager manager = getSupportFragmentManager();
        TimePickerDialog dpd = TimePickerDialog.newInstance(tsl,
                                                            calendar.get(Calendar.HOUR_OF_DAY),
                                                            calendar.get(Calendar.MINUTE),
                                                            is24HourMode);
        dpd.setVersion(TimePickerDialog.Version.VERSION_2);
        dpd.show(manager, "TimePickerDialog");
    }

    private void addTask() {
        LogUtils.debug(this, "Adding to: " + category);
        TodoModel todoModel = new TodoModel();

        int tc_VisibilityFlag = vg_timeContainer.getVisibility();
        String startTime = tv_startTime.getText().toString();
        String endTime = tv_endTime.getText().toString();
        String taskTitle = edt_taskEditor.getText().toString();
        String taskDescription = edt_taskDescription.getText().toString();
        String completionTime = startTime + " - " + endTime;

        todoModel.setCategory(category);
        todoModel.setTaskTitle(taskTitle);
        todoModel.setTaskDescription(taskDescription);
        todoModel.setStartTime(startTime);
        todoModel.setEndTime(endTime);
        todoModel.setTaskDescription(null);
        todoModel.setCompletionTime(tc_VisibilityFlag == View.VISIBLE ? completionTime : "");
        todoModel.setTaskCompleted(false);

        boolean added = database.addTodo(todoModel, category);

        if (added) {
            Toast toast = Toast.makeText(this, isEditable ? "Todo added" : "Todo updated", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 0, 100);
            toast.show();

            if (EventBus.getDefault().hasSubscriberForEvent(TDUpdateMessage.class))
                EventBus.getDefault().post(new TDUpdateMessage(todoModel, TDUpdateMessage.EventType.NEW));

            if (EventBus.getDefault().hasSubscriberForEvent(LayoutRefreshEvent.class))
                EventBus.getDefault().post(new LayoutRefreshEvent());

            playAlertTone(this, Utility.Alert.TODO);
        }

        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
    }
}