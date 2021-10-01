package com.noah.timely.todo;

import static com.noah.timely.util.CollectionUtils.linearSearch;
import static com.noah.timely.util.MiscUtil.isUserPreferred24Hours;
import static com.noah.timely.util.MiscUtil.playAlertTone;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.noah.timely.util.Converter;
import com.noah.timely.util.MiscUtil;
import com.noah.timely.util.SimpleOnItemSelectedListener;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTodoActivity extends AppCompatActivity {
   public static final String EXTRA_IS_EDITABLE = "com.noah.timely.todo.edit";
   public static final String EXTRA_DEFAULT_CATEGORY = "com.noah.timely.todo.category.default";
   public static final String[] CATEGORIES = {"Miscelaneous", "Work", "Music", "Creativity", "Travel", "Study"
           , "Leisure and Fun", "Home", "Shopping"};
   private static final String EXTRA_TODO_TITLE = "Todo title";
   private static final String EXTRA_TODO_DESCRIPTION = "Todo description";
   private static final String EXTRA_START_TIME = "Todo start time";
   private static final String EXTRA_END_TIME = "Todo end time";
   private static final String EXTRA_DATE = "Todo date";
   private ViewGroup vg_timeContainer;
   private TextView tv_startTime, tv_endTime;
   private EditText edt_taskTitle, edt_taskDescription;
   private SchoolDatabase database;
   private boolean isEditable;
   private String category = TodoModel.CATEGORIES[0];

   public static void start(Context context, boolean toEdit, String defCategory) {
      Intent starter = new Intent(context, AddTodoActivity.class);
      starter.putExtra(EXTRA_IS_EDITABLE, toEdit);
      starter.putExtra(EXTRA_DEFAULT_CATEGORY, defCategory);
      context.startActivity(starter);
   }

   public static void start(Context context, boolean toEdit, TodoModel todoToEdit) {
      Intent starter = new Intent(context, AddTodoActivity.class);
      starter.putExtra(EXTRA_TODO_TITLE, todoToEdit.getTaskTitle());
      starter.putExtra(EXTRA_TODO_DESCRIPTION, todoToEdit.getTaskDescription());
      starter.putExtra(EXTRA_START_TIME, todoToEdit.getStartTime());
      starter.putExtra(EXTRA_END_TIME, todoToEdit.getEndTime());
      starter.putExtra(EXTRA_DATE, todoToEdit.getCompletionDate());
      starter.putExtra(EXTRA_IS_EDITABLE, toEdit);
      context.startActivity(starter);
   }

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_add_todo);
      database = new SchoolDatabase(this);
      setSupportActionBar(findViewById(R.id.toolbar));

      getSupportActionBar().setTitle(isEditable ? "Update Task" : "New Task");
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      Button btn_addTask = findViewById(R.id.add_task);
      edt_taskTitle = findViewById(R.id.task_editor);
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

      setupSpinner();

      // editing current todoTask
      isEditable = getIntent().getBooleanExtra(EXTRA_IS_EDITABLE, false);
      if (isEditable) {
         Intent intent = getIntent();
         edt_taskTitle.setText(intent.getStringExtra(EXTRA_TODO_TITLE));
         edt_taskDescription.setText(intent.getStringExtra(EXTRA_TODO_DESCRIPTION));

         String startTime = intent.getStringExtra(EXTRA_START_TIME);
         String endTime = intent.getStringExtra(EXTRA_END_TIME);

         if (!TextUtils.isEmpty(startTime) && !TextUtils.isEmpty(endTime)) {
            int visibilityFlag = btn_addTimeFrame.getVisibility();
            if (visibilityFlag == View.VISIBLE) {
               btn_addTimeFrame.setVisibility(View.GONE);
               vg_timeContainer.setVisibility(View.VISIBLE);
            }
            tv_startTime.setText(startTime);
            tv_endTime.setText(endTime);
         }
      }
   }

   private void setupSpinner() {
      Spinner spin_category = findViewById(R.id.category);
      ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, CATEGORIES);
      courseAdapter.setDropDownViewResource(R.layout.simple_dropdown_item_1line);
      spin_category.setAdapter(courseAdapter);
      spin_category.setSelection(linearSearch(CATEGORIES, getIntent().getStringExtra(EXTRA_DEFAULT_CATEGORY)));

      spin_category.setOnItemSelectedListener(new SimpleOnItemSelectedListener() {
         @Override
         public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            category = TodoModel.CATEGORIES_2[position];
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
      TodoModel todoModel = new TodoModel();

      int tc_VisibilityFlag = vg_timeContainer.getVisibility();
      String startTime = tv_startTime.getText().toString();
      String endTime = tv_endTime.getText().toString();
      String taskTitle = edt_taskTitle.getText().toString();
      String taskDescription = edt_taskDescription.getText().toString();
      String completionTime = startTime + " - " + endTime;

      todoModel.setDBcategory(category);
      todoModel.setTaskTitle(taskTitle);
      todoModel.setTaskDescription(taskDescription);
      todoModel.setTaskCompleted(false);
      todoModel.setCompletionTime(tc_VisibilityFlag == View.VISIBLE ? completionTime : "");
      todoModel.setStartTime(Converter.convertTime(startTime, Converter.UNIT_24));
      todoModel.setEndTime(Converter.convertTime(endTime, Converter.UNIT_24));

      boolean added = database.addTodo(todoModel, category);

      if (added) {
         Toast toast = Toast.makeText(this, isEditable ? "Todo updated" : "Todo added", Toast.LENGTH_LONG);
         toast.setGravity(Gravity.BOTTOM, 0, 100);
         toast.show();

         if (EventBus.getDefault().hasSubscriberForEvent(TDUpdateMessage.class))
            EventBus.getDefault().post(new TDUpdateMessage(todoModel, TDUpdateMessage.EventType.NEW));

         if (EventBus.getDefault().hasSubscriberForEvent(LayoutRefreshEvent.class))
            EventBus.getDefault().post(new TodoRefreshEvent(todoModel));

         playAlertTone(this, MiscUtil.Alert.TODO);
      }

      finish();
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
      database.close();
   }

   @Override
   public boolean onSupportNavigateUp() {
      onBackPressed();
      return true;
   }
}