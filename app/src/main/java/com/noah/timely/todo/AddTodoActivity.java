package com.noah.timely.todo;

import static com.noah.timely.todo.TodoModel.SPINNER_CATEGORIES;
import static com.noah.timely.todo.TodoModel.SPINNER_CATEGORIES_ALT;
import static com.noah.timely.util.CollectionUtils.linearSearch;
import static com.noah.timely.util.Converter.convertTime;
import static com.noah.timely.util.MiscUtil.isUserPreferred24Hours;
import static com.noah.timely.util.MiscUtil.playAlertTone;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.textfield.TextInputLayout;
import com.noah.timely.R;
import com.noah.timely.core.SchoolDatabase;
import com.noah.timely.error.ErrorDialog;
import com.noah.timely.util.Constants;
import com.noah.timely.util.Converter;
import com.noah.timely.util.MiscUtil;
import com.noah.timely.util.PatternUtils;
import com.noah.timely.util.adapters.SimpleOnItemSelectedListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
   private static final String EXTRA_TAB_POSITION = "Todo tab position";
   private static final String EXTRA_TASK_COMPLETED = "Todo task completed";
   private static final String EXTRA_CHANGE_POS = "Todo row position";
   private ViewGroup vg_timeContainer;
   private EditText edt_startTime, edt_endTime;
   private EditText edt_taskTitle, edt_taskDescription;
   private SchoolDatabase database;
   private boolean isEditable;
   private String category;

   public static void start(Context context, boolean toEdit, String defCategory) {
      Intent starter = new Intent(context, AddTodoActivity.class);
      starter.putExtra(EXTRA_IS_EDITABLE, toEdit);
      starter.putExtra(EXTRA_DEFAULT_CATEGORY, defCategory);
      context.startActivity(starter);
   }

   public static void start(Context context, boolean toEdit, int adapterPosition, TodoModel todoToEdit) {
      Intent starter = new Intent(context, AddTodoActivity.class);
      starter.putExtra(EXTRA_TODO_TITLE, todoToEdit.getTaskTitle());
      starter.putExtra(EXTRA_TODO_DESCRIPTION, todoToEdit.getTaskDescription());
      starter.putExtra(EXTRA_START_TIME, todoToEdit.getStartTime());
      starter.putExtra(EXTRA_END_TIME, todoToEdit.getEndTime());
      starter.putExtra(EXTRA_DATE, todoToEdit.getCompletionDate());
      starter.putExtra(EXTRA_IS_EDITABLE, toEdit);
      starter.putExtra(EXTRA_DEFAULT_CATEGORY, todoToEdit.getDBcategory());
      starter.putExtra(EXTRA_TAB_POSITION, todoToEdit.isTaskCompleted() ? 1 : 0);
      starter.putExtra(EXTRA_CHANGE_POS, adapterPosition);
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
      edt_taskTitle = findViewById(R.id.task_editor);
      edt_taskDescription = findViewById(R.id.task_description);
      vg_timeContainer = findViewById(R.id.time_container);
      edt_startTime = findViewById(R.id.start_date_time);
      edt_endTime = findViewById(R.id.end_date_time);

      setupOnFocusChangedListeners();
      setupSpinner(isEditable);

      findViewById(R.id.start_time_picker).setOnClickListener(v -> onTimeRangeClick(v, edt_startTime));
      findViewById(R.id.end_time_picker).setOnClickListener(v -> onTimeRangeClick(v, edt_endTime));
      findViewById(R.id.start_date_picker).setOnClickListener(v -> onTimeRangeClick(v, edt_startTime));
      findViewById(R.id.end_date_picker).setOnClickListener(v -> onTimeRangeClick(v, edt_endTime));

      // set the default selected category; if action to start the activity was an 'Edit Action', then the
      // defCategory would be the same as the category of that clicked _todo, but if it was a 'New Task' action, then
      // the defCateory would just be placed on Miscellaneous _todo, because user hasn't specified which ones yet :)
      String defCategory = getIntent().getStringExtra(EXTRA_DEFAULT_CATEGORY);
      category = TextUtils.isEmpty(defCategory) || defCategory.equals(Constants.TODO_GENERAL) ?
                 TodoModel.SPINNER_CATEGORIES[0] : defCategory;
      // editing current _todo
      btn_addTask.setOnClickListener(v -> addOrUppdateTask(isEditable));
      btn_addTask.setText(isEditable ? R.string.update_task : R.string.add_task);
      if (isEditable) {
         Intent intent = getIntent();
         edt_taskTitle.setText(intent.getStringExtra(EXTRA_TODO_TITLE));
         edt_taskDescription.setText(intent.getStringExtra(EXTRA_TODO_DESCRIPTION));

         String startTime = intent.getStringExtra(EXTRA_START_TIME);
         String endTime = intent.getStringExtra(EXTRA_END_TIME);

         if (!TextUtils.isEmpty(startTime) && !TextUtils.isEmpty(endTime)) {
            edt_startTime.setText(startTime);
            edt_endTime.setText(endTime);
         }

         edt_taskTitle.requestFocus();
      }
   }

   private void setupOnFocusChangedListeners() {
      Drawable formLight = ContextCompat.getDrawable(this, R.drawable.form_light);
      Drawable formDark = ContextCompat.getDrawable(this, R.drawable.form_dark);

      View.OnFocusChangeListener focusChangeListener = (view, hasFocus) -> {
         // FrameLayout is the immediate parent of a TextInputEditText, so it's grand-parent view, which is
         // the required TextInputLayout
         TextInputLayout timeBox = (TextInputLayout) view.getParent().getParent();
         LinearLayout container = (LinearLayout) timeBox.getParent();
         container.setBackground(hasFocus ? formLight : formDark);
      };

      edt_startTime.setOnFocusChangeListener(focusChangeListener);
      edt_endTime.setOnFocusChangeListener(focusChangeListener);

   }

   private void setupSpinner(boolean isEditable) {
      Spinner spin_category = findViewById(R.id.category);
      ImageView img_tag = findViewById(R.id.tag);
      if (isEditable) {
         // prevent user from editing the category of the current _todo. If user can then that would be
         // a flaw in the UX design
         spin_category.setVisibility(View.GONE);
         img_tag.setVisibility(View.GONE);
      } else {
         ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item,
                                                                 SPINNER_CATEGORIES_ALT);
         courseAdapter.setDropDownViewResource(R.layout.simple_dropdown_item_1line);
         spin_category.setAdapter(courseAdapter);
         int selectionIndex = linearSearch(SPINNER_CATEGORIES, getIntent().getStringExtra(EXTRA_DEFAULT_CATEGORY));
         spin_category.setSelection(selectionIndex);
         spin_category.setOnItemSelectedListener(new SimpleOnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               category = SPINNER_CATEGORIES[position];
            }
         });
      }

   }

   private void onTimeRangeClick(View caller, View target) {
      final boolean is24HourMode = isUserPreferred24Hours(this);

      EditText edt_time = (EditText) target;
      int targetId = edt_time.getId();
      int callerId = caller.getId();

      // Time picker dialog listener, when time icon is clicked
      TimePickerDialog.OnTimeSetListener tsl = (timePicker, hourOfDay, minute, second) -> {
         Calendar calendar = Calendar.getInstance();
         calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
         calendar.set(Calendar.MINUTE, minute);

         SimpleDateFormat format12 = new SimpleDateFormat("hh:mm aa");
         SimpleDateFormat format24 = new SimpleDateFormat("HH:mm");
         SimpleDateFormat format_currentDate = new SimpleDateFormat("MMM dd");

         Date date = calendar.getTime();
         String parsedTime = is24HourMode ? format24.format(date) : format12.format(date);
         String parsedDate = format_currentDate.format(date);

         String timeRange = null;
         String timeRangeInputText = String.valueOf(edt_time.getText());

         if (TextUtils.isEmpty(timeRangeInputText)) {
            timeRange = String.format("%s, %s", parsedDate, parsedTime);
         } else {
            // if user enters wrong matching text and still clicks on the time and date selector buttons...
            if (!PatternUtils.test(PatternUtils.DATE_SHORT_12_24_HoursClock, edt_time.getText())) {
               timeRangeInputText = String.format("%s, %s", parsedDate, parsedTime);
            }

            timeRange = timeRangeInputText.replaceFirst(PatternUtils.TIME_ALL, parsedTime);
         }

         edt_time.setText(timeRange);
      };

      // Date picker dialog listene, wwhen date icon is clicked
      DatePickerDialog.OnDateSetListener dsl = (datePicker, year, month, day) -> {
         Calendar calendar = Calendar.getInstance();
         calendar.set(Calendar.YEAR, year);
         calendar.set(Calendar.MONTH, month);
         calendar.set(Calendar.DAY_OF_MONTH, day);

         SimpleDateFormat format12_current = new SimpleDateFormat("hh:mm aa");
         SimpleDateFormat format24_current = new SimpleDateFormat("HH:mm");
         SimpleDateFormat formatDate = new SimpleDateFormat("MMM dd");

         Date date = calendar.getTime();
         String parsedTime = is24HourMode ? format24_current.format(date) : format12_current.format(date);
         String parsedDate = formatDate.format(date);

         String timeRange = null;
         String timeRangeInputText = String.valueOf(edt_time.getText());

         if (TextUtils.isEmpty(timeRangeInputText)) {
            timeRange = String.format("%s, %s", parsedDate, parsedTime);
         } else {
            // if user enters wrong matching text and still clicks on the time and date selector buttons...
            if (!PatternUtils.test(PatternUtils.DATE_SHORT_12_24_HoursClock, edt_time.getText())) {
               timeRangeInputText = String.format("%s, %s", parsedDate, parsedTime);
            }

            timeRange = timeRangeInputText.replaceFirst(PatternUtils.DATE_SHORT, parsedDate);
         }

         edt_time.setText(timeRange);
      };

      Calendar calendar = Calendar.getInstance();
      FragmentManager manager = getSupportFragmentManager();

      if (callerId == R.id.start_time_picker || callerId == R.id.end_time_picker) {
         TimePickerDialog tpd = TimePickerDialog.newInstance(tsl,
                                                             calendar.get(Calendar.HOUR_OF_DAY),
                                                             calendar.get(Calendar.MINUTE),
                                                             is24HourMode);
         tpd.setVersion(TimePickerDialog.Version.VERSION_2);
         tpd.show(manager, "TimePickerDialog");
      } else {
         DatePickerDialog dpd = DatePickerDialog.newInstance(dsl,
                                                             calendar.get(Calendar.YEAR),
                                                             calendar.get(Calendar.MONTH),
                                                             calendar.get(Calendar.DAY_OF_MONTH));
         dpd.setVersion(DatePickerDialog.Version.VERSION_2);
         dpd.show(manager, "DatePickerDialog");
      }
   }

   // action performed while task is being added
   private void addOrUppdateTask(boolean toEdit) {

      boolean errorOccurred = false, use24 = isUserPreferred24Hours(this);

      String startTimeInput = edt_startTime.getText().toString();
      String endTimeInput = edt_endTime.getText().toString();

      String timeRegex24 = PatternUtils.DATE_SHORT_24_HoursClock;
      String timeRegex12 = PatternUtils.DATE_SHORT_12_HoursClock;

      boolean x = !TextUtils.isEmpty(startTimeInput);
      boolean y = !TextUtils.isEmpty(endTimeInput);
      boolean atLeastOneFilled = (x ^ y) | (x & y);   // First logic looks like an uwu:  \\ ( ^ w ^ ) //

      if (TextUtils.isEmpty(edt_taskTitle.getText())) {
         TextInputLayout titleBox = (TextInputLayout) edt_taskTitle.getParent().getParent();
         titleBox.setError("Field required!");
         errorOccurred = true;
      }

      if (atLeastOneFilled) {
         TextInputLayout startBox = (TextInputLayout) edt_startTime.getParent().getParent();

         if (use24 && !startTimeInput.matches(timeRegex24)) {
            startBox.setError("Format: MMM dd, HH:SS");
            errorOccurred = true;
         } else {
            if (!use24 && !startTimeInput.matches(timeRegex12)) {
               startBox.setError("12 hours mode with date");
               errorOccurred = true;
            }
         }

         TextInputLayout endBox = (TextInputLayout) edt_endTime.getParent().getParent();

         if (use24 && !endTimeInput.matches(timeRegex24)) {
            endBox.setError("Format: MMM dd, HH:SS");
            errorOccurred = true;
         } else {
            if (!use24 && !endTimeInput.matches(timeRegex12)) {
               endBox.setError("12 hours mode with date");
               errorOccurred = true;
            }
         }
      }

      if (errorOccurred) return;

      // convert time to 24 hours because TimeLY saves time in 24 hours clock.
      // Algorithm: convert the first time match in the input string to 24 hours clock
      if (!TextUtils.isEmpty(startTimeInput) && !TextUtils.isEmpty(endTimeInput)) {
         startTimeInput = use24 ? startTimeInput
                                : convertTime(PatternUtils.findMatch(PatternUtils._12_HoursClock, startTimeInput),
                                              Converter.UNIT_24);
         endTimeInput = use24 ? endTimeInput
                              : convertTime(PatternUtils.findMatch(PatternUtils._12_HoursClock, endTimeInput),
                                            Converter.UNIT_24);
      }

      TodoModel todoModel = new TodoModel();

      String taskTitle = edt_taskTitle.getText().toString();
      String taskDescription = edt_taskDescription.getText().toString();

      String completionTime = TextUtils.isEmpty(startTimeInput)
                                      && TextUtils.isEmpty(endTimeInput) ? null : startTimeInput + " - " + endTimeInput;

      todoModel.setTaskTitle(taskTitle);
      todoModel.setDBcategory(category);
      todoModel.setTaskDescription(taskDescription);
      todoModel.setTaskCompleted(getIntent().getBooleanExtra(EXTRA_TASK_COMPLETED, false));
      todoModel.setCompletionTime(completionTime);
      todoModel.setStartTime(startTimeInput);
      todoModel.setOnEditTaskTitle(getIntent().getStringExtra(EXTRA_TODO_TITLE));
      todoModel.setEndTime(endTimeInput);

      boolean isSuccessful;
      // update or add new _todo
      if (toEdit) {
         int pagePosition = getIntent().getIntExtra(EXTRA_TAB_POSITION, 0);
         int changePosition = getIntent().getIntExtra(EXTRA_CHANGE_POS, 0);

         isSuccessful = database.updateTodo(todoModel);
         if (isSuccessful) {
            Toast toast = Toast.makeText(this, "Todo updated", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 0, 100);
            toast.show();

            // Refresh the _todo list size
            if (EventBus.getDefault().hasSubscriberForEvent(TDUpdateMessage.class))
               EventBus.getDefault().post(new TDUpdateMessage(todoModel, changePosition, pagePosition,
                                                              TDUpdateMessage.EventType.UPDATE_CURRENT));

            playAlertTone(this, MiscUtil.Alert.TODO);

         } else Toast.makeText(this, "An Error Occurred", Toast.LENGTH_SHORT).show();

      } else {

         if (database.isTodoAbsent(todoModel)) {
            long[] insertId = database.addTodo(todoModel, category);
            if (insertId[0] != -1) {
               todoModel.setUID((int) insertId[0]); // required to be used in list's getItemId()
               todoModel.setId((int) insertId[1]);
               Toast toast = Toast.makeText(this, "Todo added", Toast.LENGTH_LONG);
               toast.setGravity(Gravity.BOTTOM, 0, 100);
               toast.show();

               // Refresh the _todo list size
               if (EventBus.getDefault().hasSubscriberForEvent(TDUpdateMessage.class))
                  EventBus.getDefault()
                          .post(new TDUpdateMessage(todoModel, 0 /* 1st tab */, TDUpdateMessage.EventType.NEW));
               // Refresh the _todo group size
               if (EventBus.getDefault().hasSubscriberForEvent(TodoRefreshEvent.class))
                  EventBus.getDefault().post(new TodoRefreshEvent(todoModel));

               playAlertTone(this, MiscUtil.Alert.TODO);

            } else Toast.makeText(this, "An Error occurred", Toast.LENGTH_SHORT).show();

         } else {
            // Error message when duplicates are found
            ErrorDialog.Builder errorBuilder = new ErrorDialog.Builder();
            errorBuilder.setDialogMessage("Duplicate todo found");
            errorBuilder.setShowSuggestions(true);
            errorBuilder.setSuggestionCount(1);
            errorBuilder.setSuggestion1("Check for duplicate todo titles");
            new ErrorDialog().showErrorMessage(this, errorBuilder.build());
            Toast.makeText(this, "An Error occurred", Toast.LENGTH_SHORT).show();
            return;
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