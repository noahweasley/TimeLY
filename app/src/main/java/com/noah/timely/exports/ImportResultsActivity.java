package com.noah.timely.exports;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.noah.timely.R;
import com.noah.timely.error.ErrorDialog;
import com.noah.timely.settings.SettingsActivity;
import com.noah.timely.util.TimelyUpdateUtils;

import java.io.File;

public class ImportResultsActivity extends AppCompatActivity implements View.OnClickListener {
   ViewGroup importView, initView;
   private final ActivityResultLauncher<Intent> requestpermissionlauncher2 =
           registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
              if (result.getData() != null) {
                 Intent uploadfileIntent = result.getData();
                 Uri uri = uploadfileIntent.getData();
                 File file = new File(uri.getPath());

                 if (getFileExtension(file).equals("tmly")) {
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
      rv_importList.setAdapter(new ImportListRowAdapter());

      Button btn_import = findViewById(R.id.import_);
      importView = findViewById(R.id.import_view);
      initView = findViewById(R.id.init_view);

      Toolbar toolbar = findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
      getSupportActionBar().setTitle(R.string.import_title);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      btn_import.setOnClickListener(this);

      Uri fileUri = null;

      if ((fileUri = getIntent().getData()) != null) {

         File file = new File(fileUri.getPath());
         if (getFileExtension(file).equals("tmly")) {
            performFileImport(file);
         } else {
            // import unsuccessful. Error occurred
            ErrorDialog.Builder errorBuilder = new ErrorDialog.Builder();
            errorBuilder.setShowSuggestions(true)
                        .setDialogMessage("An Error occurred while importing data")
                        .setSuggestionCount(1)
                        .setSuggestion1("File extension might not be supported");

            new ErrorDialog().showErrorMessage(this, errorBuilder.build());
         }

      } else {
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
   public void onClick(View view) {
      Intent i = new Intent(Intent.ACTION_GET_CONTENT);
      i.setType("*/*");
      requestpermissionlauncher2.launch(Intent.createChooser(i, getString(R.string.file_select_title)));
   }

   public void performFileImport(File file) {
      new FileImportDialog().execute(this, file.getAbsolutePath()).setOnResultReceived(results -> {
         // noinspection StatementWithEmptyBody
         if (results != null) {

         } else {
            // import unsuccessful. Error occurred
            ErrorDialog.Builder errorBuilder = new ErrorDialog.Builder();
            errorBuilder.setShowSuggestions(true)
                        .setDialogMessage("An empty data file was received")
                        .setSuggestionCount(2)
                        .setSuggestion1("Exported data might be empty")
                        .setSuggestion2("File might be corrupt");

            new ErrorDialog().showErrorMessage(this, errorBuilder.build());
         }
         initView.setVisibility(View.GONE);
         importView.setVisibility(View.VISIBLE);
      });
   }

   private String getFileExtension(File file) {
      String ssFile = file.toString();
      int i = ssFile.lastIndexOf('.');
      String extension = ssFile.substring(i + 1);
      return i > 0 ? extension : null;
   }

   private class ImportListRowAdapter extends RecyclerView.Adapter<ImportListRowHolder> {

      @NonNull
      @Override
      public ImportListRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         return new ImportListRowHolder(getLayoutInflater().inflate(R.layout.import_list_row, parent, false));
      }

      @Override
      public void onBindViewHolder(@NonNull ImportListRowHolder holder, int position) {
         holder.with(position).bindView();
      }

      @Override
      public int getItemCount() {
         return 7;
      }
   }
}
