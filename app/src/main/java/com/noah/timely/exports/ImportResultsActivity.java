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
import com.noah.timely.core.ChoiceMode;
import com.noah.timely.core.DataModel;
import com.noah.timely.core.MultiChoiceMode;
import com.noah.timely.core.SchoolDatabase;
import com.noah.timely.error.ErrorDialog;
import com.noah.timely.io.IOUtils;
import com.noah.timely.io.Zipper;
import com.noah.timely.settings.SettingsActivity;
import com.noah.timely.util.CollectionUtils;
import com.noah.timely.util.Constants;
import com.noah.timely.util.TimelyUpdateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ImportResultsActivity extends AppCompatActivity implements View.OnClickListener {
   private ImportListRowAdapter listRowAdapter;
   List<Map.Entry<String, List<? extends DataModel>>> entryList = new ArrayList<>();
   ViewGroup importView, initView, dataLayerView;
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

      if ((fileUri = getIntent().getData()) != null) {
         // noinspection StatementWithEmptyBody
         if (IOUtils.getFileExtension(new File(fileUri.getPath())).equals(Zipper.FILE_EXTENSION)) {

         } else {
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
      super.onBackPressed();
      return super.onSupportNavigateUp();
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
         new ActionProcessorDialog2().execute(this, this::persistSelectedToLocalDatabase);
      }
   }

   private void persistSelectedToLocalDatabase() {
      SchoolDatabase database = new SchoolDatabase(this);
      Integer[] indices = listRowAdapter.getChoiceMode().getCheckedChoicesIndices();
      List<Map.Entry<String, List<? extends DataModel>>> filteredEntryList = new ArrayList<>();
      for (int i = 0; i < indices.length; i++) {
         filteredEntryList.add(entryList.get(i));
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

   }

   private void persistScheduledTimetable(List<? extends DataModel> value) {

   }

   private void persistTimetable(List<? extends DataModel> value) {

   }

   private void persistExam(List<? extends DataModel> value) {

   }

   private void persistCourses(List<? extends DataModel> value) {

   }

   private void persistAssignments(List<? extends DataModel> dataModels) {

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
