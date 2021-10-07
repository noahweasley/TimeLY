package com.noah.timely.todo;

import static com.noah.timely.todo.TodoModel.CATEGORIES_2;
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
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.noah.timely.R;
import com.noah.timely.assignment.LayoutRefreshEvent;
import com.noah.timely.core.SchoolDatabase;
import com.noah.timely.util.Converter;
import com.noah.timely.util.MiscUtil;
import com.noah.timely.util.adapters.SimpleOnItemSelectedListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@SuppressWarnings("FieldCanBeLocal")
public class AddTodoActivity extends AppCompatActivity {
   public static final String EXTRA_IS_EDITABLE = "com.noah.timely.todo.edit";
   public static final String EXTRA_DEFAULT_CATEGORY = "com.noah.timely.todo.category.default";
   public static final String[] CATEGORIES = { "Miscelaneous", "Work", "Music", "Creativity", "Travel", "Study"
           , "Leisure and Fun", "Home", "Shopping" };
   private static final String EXTRA_TODO_TITLE = "Todo title";
   private static final String EXTRA_TODO_DESCRIPTION = "Todo description";
   private static final String EXTRA_START_TIME = "Todo start time";
   private static final String EXTRA_END_TIME = "Todo end time";
   private static final String EXTRA_DATE = "Todo date";
   private ViewGroup vg_timeContainer;
   private EditText edt_startTime, edt_endTime;
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
      starter.putExtra(EXTRA_DEFAULT_CATEGORY, todoToEdit.getDBcategory());
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
      vg_timeContainer = findViewById(R.id.time_container);

      edt_startTime = findViewById(R.id.start_date_time);
      edt_endTime = findViewById(R.id.end_date_time);

      findViewById(R.id.start_time_picker).setOnClickListener(this::onTimeRangeClick);
      findViewById(R.id.end_time_picker).setOnClickListener(this::onTimeRangeClick);
      findViewById(R.id.start_date_picker).setOnClickListener(this::onTimeRangeClick);
      findViewById(R.id.end_date_picker).setOnClickListener(this::onTimeRangeClick);

      Calendar calendar = Calendar.getInstance();

      if (isUserPreferred24Hours(this)) {
         SimpleDateFormat formatter = new SimpleDateFormat("mm dd, HH:MM");
         edt_startTime.setText(formatter.format(calendar.getTime()));
         edt_endTime.setText(formatter.format(calendar.getTime()));
      } else {
         SimpleDateFormat formatter = new SimpleDateFormat("mm dd, hh:MM a");
         edt_startTime.setText(formatter.format(calendar.getTime()));
         edt_endTime.setText(formatter.format(calendar.getTime()));
      }

      setupSpinner();

      // editing current _todo
      isEditable = getIntent().getBooleanExtra(EXTRA_IS_EDITABLE, false);
      btn_addTask.setOnClickListener(v -> addOrUppdateTask(isEditable));
      if (isEditable) {
         Intent intent = getIntent();
         edt_taskTitle.setText(intent.getStringExtra(EXTRA_TODO_TITLE));
         edt_taskDescription.setText(intent.getStringExtra(EXTRA_TODO_DESCRIPTION));

         String startTime = intent.getStringExtra(EXTRA_START_TIME);
         String endTime = intent.getStringExtra(EXTRA_END_TIME);

         if (!TextUtils.isEmpty(startTime) && !TextUtils.isEmpty(endTime)) {
            edt_startTime.setText(startTime);
            edt_endTime.setText(endTime);
            btn_addTask.setText(R.string.update_task);
         }

         edt_taskTitle.requestFocus();
      }
   }

   private void setupSpinner() {
      Spinner spin_category = findViewById(R.id.category);
      ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, CATEGORIES);
      courseAdapter.setDropDownViewResource(R.layout.simple_dropdown_item_1line);
      spin_category.setAdapter(courseAdapter);
      int selectionIndex = linearSearch(CATEGORIES_2, getIntent().getStringExtra(EXTRA_DEFAULT_CATEGORY));
      spin_category.setSelection(selectionIndex);
      spin_category.setOnItemSelectedListener(new SimpleOnItemSelectedListener() {
         @Override
         public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            category = CATEGORIES_2[position];
         }
      });
   }

   private void onTimeRangeClick(View view) {
      boolean is24HourMode = isUserPreferred24Hours(this);
      EditText text = (EditText) view;
      int viewId = text.getId();

      // Time picker dialog listener, when time icon is clicked
      TimePickerDialog.OnTimeSetListener tsl = (timePicker, hourOfDay, minute, second) -> {
         Calendar calendar = Calendar.getInstance();
         calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
         calendar.set(Calendar.MINUTE, minute);

         SimpleDateFormat formatter24 = new SimpleDateFormat("HH:mm");
         SimpleDateFormat formatter12 = new SimpleDateFormat("hh:mm aaa");

         String parsedTime = is24HourMode ? formatter24.format(calendar.getTime())
                                          : formatter12.format(calendar.getTime());

         String prevTime = text.getText().toString();
         String newTime = prevTime.replaceFirst("[0-9]{2}:[0-9]{2}(?i: am|pm)*", parsedTime);

         text.setText(newTime);
      };

      // Date picker dialog listene, wwhen date icon is clicked
      DatePickerDialog.OnDateSetListener dsl = (datePicker, year, month, day) -> {
         Calendar calendar = Calendar.getInstance();
         calendar.set(Calendar.YEAR, year);
         calendar.set(Calendar.MONTH, month);
         calendar.set(Calendar.DAY_OF_MONTH, day);

         SimpleDateFormat formatter24 = new SimpleDateFormat("MM dd");
         SimpleDateFormat formatter12 = new SimpleDateFormat("MM dd");

         String parsedTime = is24HourMode ? formatter24.format(calendar.getTime())
                                          : formatter12.format(calendar.getTime());

         String prevTime = text.getText().toString();
         String newTime = prevTime.replaceFirst("[A-z] [0-9]{2}", parsedTime);

         text.setText(newTime);
      };

      Calendar calendar = Calendar.getInstance();

      FragmentManager manager = getSupportFragmentManager();
      TimePickerDialog tpd = TimePickerDialog.newInstance(tsl,
                                                          calendar.get(Calendar.HOUR_OF_DAY),
                                                          calendar.get(Calendar.MINUTE),
                                                          is24HourMode);

      DatePickerDialog dpd = DatePickerDialog.newInstance(dsl,
                                                          calendar.get(Calendar.YEAR),
                                                          calendar.get(Calendar.MONTH),
                                                          calendar.get(Calendar.DAY_OF_MONTH));

      if (viewId == R.id.start_time_picker || viewId == R.id.end_time_picker) {
         tpd.setVersion(TimePickerDialog.Version.VERSION_2);
         tpd.show(manager, "TimePickerDialog");
      } else {
         dpd.setVersion(DatePickerDialog.Version.VERSION_2);
         dpd.show(manager, "DatePickerDialog");
      }
   }

   private void addOrUppdateTask(boolean toEdit) {
      TodoModel todoModel = new TodoModel();

      int tc_VisibilityFlag = vg_timeContainer.getVisibility();
      String startTime = edt_startTime.getText().toString();
      String endTime = edt_endTime.getText().toString();
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

      boolean isSuccessful;

      // update or add new _todo
      if (toEdit) {
         isSuccessful = database.updateTodo(todoModel, category);
         if (isSuccessful) {
            Toast toast = Toast.makeText(this, "Todo updated", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 0, 100);
            toast.show();

            // Refresh the _todo list size
            if (EventBus.getDefault().hasSubscriberForEvent(TDUpdateMessage.class))
               EventBus.getDefault().post(new TDUpdateMessage(todoModel, TDUpdateMessage.EventType.UPDATE_CURRENT));

            playAlertTone(this, MiscUtil.Alert.TODO);
         }
      } else {
         isSuccessful = database.addTodo(todoModel, category);
         if (isSuccessful) {
            Toast toast = Toast.makeText(this, "Todo added", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 0, 100);
            toast.show();

            // Refresh the _todo list size
            if (EventBus.getDefault().hasSubscriberForEvent(TDUpdateMessage.class))
               EventBus.getDefault().post(new TDUpdateMessage(todoModel, TDUpdateMessage.EventType.NEW));
            // Refresh the _todo group size
            if (EventBus.getDefault().hasSubscriberForEvent(LayoutRefreshEvent.class))
               EventBus.getDefault().post(new TodoRefreshEvent(todoModel));

            playAlertTone(this, MiscUtil.Alert.TODO);
         }
      }


      onBackPressed(); // simulate when the user taps the back button
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