package com.noah.timely.exports;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.noah.timely.R;
import com.noah.timely.alarms.AlarmReSchedulerService;
import com.noah.timely.assignment.AUpdateMessage;
import com.noah.timely.assignment.AssignmentModel;
import com.noah.timely.core.ChoiceMode;
import com.noah.timely.core.DataModel;
import com.noah.timely.core.MultiChoiceMode;
import com.noah.timely.core.SchoolDatabase;
import com.noah.timely.courses.CUpdateMessage;
import com.noah.timely.courses.CourseModel;
import com.noah.timely.error.ErrorDialog;
import com.noah.timely.exam.EUpdateMessage;
import com.noah.timely.exam.ExamModel;
import com.noah.timely.io.IOUtils;
import com.noah.timely.io.Zipper;
import com.noah.timely.main.MainActivity;
import com.noah.timely.scheduled.SUpdateMessage;
import com.noah.timely.settings.SettingsActivity;
import com.noah.timely.timetable.TUpdateMessage;
import com.noah.timely.timetable.TimetableModel;
import com.noah.timely.util.CollectionUtils;
import com.noah.timely.util.Constants;
import com.noah.timely.util.TimelyUpdateUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ImportResultsActivity extends AppCompatActivity implements View.OnClickListener {
   private ImportListRowAdapter listRowAdapter;
   private boolean intentReceived;
   private List<Map.Entry<String, List<? extends DataModel>>> entryList = new ArrayList<>();
   private ViewGroup importView, initView, dataLayerView;
   private final ActivityResultLauncher<Intent> resourceChooserLauncher =
           registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
              if (result.getData() != null) {
                 Intent uploadfileIntent = result.getData();
                 File file = null;
                 try {
                    file = resolveDataToTempFile(uploadfileIntent.getData());
                 } catch (IOException e) {
                    Toast.makeText(this, "An internal error occurred", Toast.LENGTH_LONG).show();
                    return;
                 }

                 if (file != null) {
                    performFileImport(file);
                 } else {
                    // import unsuccessful. Error occurred
                    ErrorDialog.Builder errorBuilder = new ErrorDialog.Builder();
                    errorBuilder.setShowSuggestions(true)
                                .setDialogMessage("An error occurred while importing data")
                                .setSuggestionCount(1)
                                .setSuggestion1("File extension might not be supported");

                    new ErrorDialog().showErrorMessage(this, errorBuilder.build());
                 }

              }
           });

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.menu_main, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(@NonNull MenuItem item) {
      int id = item.getItemId();
      if (id == R.id.action_settings) {
         startActivity(new Intent(this, SettingsActivity.class));
      } else if (id == R.id.update) {
         TimelyUpdateUtils.checkForUpdates(this);
      }
      return super.onOptionsItemSelected(item);
   }

   @Override
   public void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_import);

      RecyclerView rv_importList = findViewById(R.id.import_list);
      rv_importList.setHasFixedSize(true);
      rv_importList.setLayoutManager(new LinearLayoutManager(this));
      rv_importList.setAdapter((listRowAdapter = new ImportListRowAdapter(new MultiChoiceMode())));

      Button btn_filePick = findViewById(R.id.file_pick), btn_importSelected = findViewById(R.id.import_selected);
      importView = findViewById(R.id.import_view);
      initView = findViewById(R.id.init_view);
      dataLayerView = findViewById(R.id.data_layer);

      Toolbar toolbar = findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
      getSupportActionBar().setTitle(R.string.import_title);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      btn_importSelected.setOnClickListener(this);
      btn_filePick.setOnClickListener(this);

      Uri fileUri = null;
      File file = null;

      if ((fileUri = getIntent().getData()) != null) {
         // make it possible to navigate to main activity on back pressed or on navigate up because the main activity
         // isn't part of the activity stack
         intentReceived = true;
         try {
            file = resolveUriToTempFile(fileUri);
         } catch (IOException e) {
            Toast.makeText(this, "An internal error occurred", Toast.LENGTH_LONG).show();
            dataLayerView.setVisibility(View.GONE);
            initView.setVisibility(View.GONE);
            importView.setVisibility(View.VISIBLE);
         }

         // noinspection StatementWithEmptyBody
         if (file != null) {
            performFileImport(file);
         } else {
            IOUtils.deleteTempFiles(this);
            dataLayerView.setVisibility(View.GONE);
            initView.setVisibility(View.GONE);
            importView.setVisibility(View.VISIBLE);
            // import unsuccessful. Error occurred
            ErrorDialog.Builder errorBuilder = new ErrorDialog.Builder();
            errorBuilder.setShowSuggestions(true)
                        .setDialogMessage("An error occurred while importing data")
                        .setSuggestionCount(1)
                        .setSuggestion1("File extension might not be supported");

            new ErrorDialog().showErrorMessage(this, errorBuilder.build());
         }
      } else {
         dataLayerView.setVisibility(View.GONE);
         initView.setVisibility(View.GONE);
         importView.setVisibility(View.VISIBLE);
      }

   }

   @Override
   public boolean onSupportNavigateUp() {
      onBackPressed();
      return super.onSupportNavigateUp();
   }

   @Override
   public void onBackPressed() {
      tryNavigateToMainActivity();
   }

   private void tryNavigateToMainActivity() {
      if (intentReceived) {
         intentReceived = false;
         startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
      } else {
         super.onBackPressed();
      }
   }

   @Override
   public void onSaveInstanceState(@NonNull Bundle outState) {
      super.onSaveInstanceState(outState);
      listRowAdapter.getChoiceMode().onSaveInstanceState(outState);
   }

   @Override
   protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      if (savedInstanceState != null)
         listRowAdapter.getChoiceMode().onRestoreInstanceState(savedInstanceState);
   }

   @Override
   public void onClick(View view) {
      if (view.getId() == R.id.file_pick) {
         Intent i = new Intent(Intent.ACTION_GET_CONTENT);
         i.setType("*/*");
         resourceChooserLauncher.launch(Intent.createChooser(i, getString(R.string.file_select_title)));
      } else if (view.getId() == R.id.import_selected) {
         // save to local database, removing duplicates
         new ActionProcessorDialog()
                 .execute(this, this::persistSelectedToLocalDatabase)
                 .setOnActionProcessedListener(() -> {
                    // just a beautiful custom made Toast, but in dialog form :)
                    new ImportSuccessDialog().show(this, R.string.import_success_message);
                 });
      }
   }

   private void persistSelectedToLocalDatabase() {
      Integer[] indices = listRowAdapter.getChoiceMode().getCheckedChoicesIndices();
      List<Map.Entry<String, List<? extends DataModel>>> filteredEntryList = new ArrayList<>();
      for (int i = 0; i < indices.length; i++) {
         filteredEntryList.add(entryList.get(indices[i]));
      }

      for (Map.Entry<String, List<? extends DataModel>> entry : filteredEntryList) {
         switch (entry.getKey() /* datamodel constant */) {
            case Constants.ASSIGNMENT:
               persistAssignments(entry.getValue());
               break;
            case Constants.COURSE:
               persistCourses(entry.getValue());
               break;
            case Constants.EXAM:
               persistExam(entry.getValue());
               break;
            case Constants.TIMETABLE:
               persistTimetable(entry.getValue());
               break;
            case Constants.SCHEDULED_TIMETABLE:
               persistScheduledTimetable(entry.getValue());
               break;
            default:
               throw new IllegalStateException("Unexpected value: " + entry.getKey());
         }
      }

      // after persisting to local database, use a start a service to schedule notifications for event remninders
      Intent serviceIntent = new Intent(this, AlarmReSchedulerService.class);
      serviceIntent.setAction(Constants.ACTION.SHOW_NOTIFICATION);
      startService(serviceIntent);
   }

   private void persistScheduledTimetable(List<? extends DataModel> datamodelList) {
      SchoolDatabase database = new SchoolDatabase(this);
      EventBus eventBus = EventBus.getDefault();

      for (DataModel data : datamodelList) {
         TimetableModel timetable = (TimetableModel) data;
         if (database.isTimeTableAbsent(SchoolDatabase.SCHEDULED_TIMETABLE, timetable)) {
            int[] d = database.addTimeTableData(timetable, SchoolDatabase.SCHEDULED_TIMETABLE);
            // post an application wide event to notify datamodel lists of change in order of list items
            // after sorting it's elements in database
            if (eventBus.hasSubscriberForEvent(SUpdateMessage.class)) {
               timetable.setChronologicalOrder(d[0]);
               timetable.setId(d[1]);
               eventBus.post(new SUpdateMessage(timetable, SUpdateMessage.EventType.NEW));
            }
         }
      }
   }

   private void persistTimetable(List<? extends DataModel> datamodelList) {
      SchoolDatabase database = new SchoolDatabase(this);
      EventBus eventBus = EventBus.getDefault();

      for (DataModel data : datamodelList) {
         TimetableModel timetable = (TimetableModel) data;
         if (database.isTimeTableAbsent(timetable.getDay(), timetable)) {
            int[] d = database.addTimeTableData(timetable, timetable.getDay());
            // post an application wide event to notify datamodel lists of change in order of list items
            // after sorting it's elements in database
            if (eventBus.hasSubscriberForEvent(TUpdateMessage.class)) {
               timetable.setChronologicalOrder(d[0]);
               timetable.setId(d[1]);
               int pagePosition = getPagePostion(timetable);
               eventBus.post(new TUpdateMessage(timetable, pagePosition, TUpdateMessage.EventType.NEW));
            }
         }
      }
   }

   private void persistExam(List<? extends DataModel> datamodelList) {
      SchoolDatabase database = new SchoolDatabase(this);
      EventBus eventBus = EventBus.getDefault();

      for (DataModel data : datamodelList) {
         ExamModel exam = (ExamModel) data;
         if (database.isExamAbsent(exam.getWeek(), exam)) {
            int[] d = database.addExam(exam, exam.getWeek());
            // post an application wide event to notify datamodel lists of change in order of list items
            // after sorting it's elements in database
            if (eventBus.hasSubscriberForEvent(EUpdateMessage.class)) {
               exam.setChronologicalOrder(d[0]);
               exam.setId(d[1]);
               int pagePosition = getPagePostion(exam);
               eventBus.post(new EUpdateMessage(exam, EUpdateMessage.EventType.NEW, pagePosition));
            }
         }
      }
   }

   private void persistCourses(List<? extends DataModel> datamodelList) {
      SchoolDatabase database = new SchoolDatabase(this);
      EventBus eventBus = EventBus.getDefault();

      for (DataModel data : datamodelList) {
         CourseModel course = (CourseModel) data;
         if (database.isCourseAbsent(course)) {
            int[] d = database.addCourse(course, course.getSemester());
            // post an application wide event to notify datamodel lists of change in order of list items
            // after sorting it's elements in database
            if (eventBus.hasSubscriberForEvent(CUpdateMessage.class)) {
               course.setChronologicalOrder(d[0]);
               course.setId(d[1]);
               int pagePosition = getPagePostion(course);
               eventBus.post(new CUpdateMessage(course, CUpdateMessage.EventType.NEW, pagePosition));
            }
         }
      }
   }

   private int getPagePostion(DataModel dataModel) {
      if (dataModel instanceof ExamModel) {
         ExamModel examModel = (ExamModel) dataModel;
         return examModel.getWeekIndex();
      } else if (dataModel instanceof TimetableModel) {
         TimetableModel timetableModel = (TimetableModel) dataModel;
         return timetableModel.getTimetableIndex();
      } else if (dataModel instanceof CourseModel) {
         CourseModel courseModel = (CourseModel) dataModel;
         return courseModel.getSemesterIndex();
      }

      return 0;
   }

   private void persistAssignments(List<? extends DataModel> datamodelList) {
      SchoolDatabase database = new SchoolDatabase(this);
      EventBus eventBus = EventBus.getDefault();

      for (DataModel data : datamodelList) {
         AssignmentModel assignment = (AssignmentModel) data;
         if (database.isAssignmentAbsent(assignment)) {
            boolean b = database.addAssignmentData(assignment);
            // post an application wide event to notify datamodel lists of change in order of list items
            // after sorting it's elements in database
            if (eventBus.hasSubscriberForEvent(AUpdateMessage.class)) {
               eventBus.post(new AUpdateMessage(assignment, AUpdateMessage.EventType.NEW));
            }
         }
      }
   }

   /*
    *  The resulting URI received from the Android device's file chooser would never be a correct URI to be used
    *  directly to get the file path because in Android, not all URIs points to a valid file. So a temp file
    *  was used to copy the data in the stream gotten from the URI, and then the temp file's path was used instead.
    */
   private File resolveDataToTempFile(Uri uri) throws IOException {
      // return immediately if the file extension is not supported
      if (!IOUtils.getFileExtension(new File(uri.getPath())).equals(Zipper.FILE_EXTENSION)) return null;

      String parentFolder = getExternalFilesDir(null) + File.separator + "temp" + File.separator;
      String tempFilePath = String.format(Locale.US, "%stemp%d.tmp", parentFolder, SystemClock.elapsedRealtime());

      File tempFile = new File(tempFilePath);
      File tempFileDir = tempFile.getParentFile();

      boolean isCreated = true;
      if (!tempFileDir.exists()) {
         isCreated = tempFileDir.mkdirs();
      }

      if (!isCreated) return null;
      else IOUtils.copy(getContentResolver().openInputStream(uri), new FileOutputStream(tempFile));

      return tempFile;
   }

   /*
    *  The resulting URI received from the Android device's file chooser would never be a correct URI to be used
    *  directly to get the file path because in Android, not all URIs points to a valid file. So a temp file
    *  was used to copy the data in the stream gotten from the URI, and then the temp file's path was used instead.
    */
   private File resolveUriToTempFile(Uri uri) throws IOException {
      String parentFolder = getExternalFilesDir(null) + File.separator + "temp" + File.separator;
      String tempFilePath = String.format(Locale.US, "%stemp%d.tmp", parentFolder, SystemClock.elapsedRealtime());

      File tempFile = new File(tempFilePath);
      File tempFileDir = tempFile.getParentFile();

      boolean isCreated = true;
      if (!tempFileDir.exists()) {
         isCreated = tempFileDir.mkdirs();
      }

      if (!isCreated) return null;
      else IOUtils.copy(getContentResolver().openInputStream(uri), new FileOutputStream(tempFile));

      return tempFile;
   }

   private void performFileImport(File file) {
      dataLayerView.setVisibility(View.GONE);
      initView.setVisibility(View.VISIBLE);
      importView.setVisibility(View.GONE);
      new FileImportDialog().execute(this, file.getAbsolutePath()).setOnResultReceived(results -> {
         if (!CollectionUtils.isEmpty(results)) {
            entryList = results;
            // delete temp files used in file import operation, to free up memory in disk
            boolean isDeleted = IOUtils.deleteTempFiles(this);
            listRowAdapter.notifyDataSetChanged();
            dataLayerView.setVisibility(View.VISIBLE);
            initView.setVisibility(View.GONE);
            importView.setVisibility(View.GONE);
            // display count of data to be imported on titlebar
            String message = "Found %d item" + (entryList.size() > 1 ? "s" : "");
            getSupportActionBar().setTitle(String.format(Locale.US, message, entryList.size()));
         } else {
            dataLayerView.setVisibility(View.GONE);
            initView.setVisibility(View.GONE);
            importView.setVisibility(View.VISIBLE);
            // import unsuccessful. error occurred
            Toast.makeText(this, "IOException occurred, Try again", Toast.LENGTH_SHORT).show();

            ErrorDialog.Builder errorBuilder = new ErrorDialog.Builder();
            errorBuilder.setShowSuggestions(true)
                        .setDialogMessage("An empty data file was received")
                        .setSuggestionCount(2)
                        .setSuggestion1("Exported data might be empty")
                        .setSuggestion2("File might be corrupt");

            new ErrorDialog().showErrorMessage(this, errorBuilder.build());
         }
      });
   }

   public class ImportListRowAdapter extends RecyclerView.Adapter<ImportListRowHolder> {
      private final ChoiceMode choiceMode;

      public ImportListRowAdapter(ChoiceMode choiceMode) {
         this.choiceMode = choiceMode;
      }

      public ChoiceMode getChoiceMode() {
         return choiceMode;
      }

      public void onChecked(int position, boolean isChecked) {
         MultiChoiceMode multiChoiceMode = (MultiChoiceMode) choiceMode;
         multiChoiceMode.setChecked(position, isChecked);
      }

      public boolean isChecked(int position) {
         return choiceMode.isChecked(position);
      }

      @NonNull
      @Override
      public ImportListRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         return new ImportListRowHolder(getLayoutInflater().inflate(R.layout.import_list_row, parent, false));
      }

      @Override
      public void onBindViewHolder(@NonNull ImportListRowHolder holder, int position) {
         holder.with(position, ImportResultsActivity.this, this, entryList).bindView();
      }

      @Override
      public int getItemCount() {
         return entryList.size();
      }

   }

}
