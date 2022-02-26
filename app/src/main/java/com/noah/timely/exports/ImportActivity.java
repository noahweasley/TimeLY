package com.noah.timely.exports;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.noah.timely.R;
import com.noah.timely.error.ErrorDialog;

public class ImportActivity extends AppCompatActivity implements View.OnClickListener {
   private static final String TYPE = "*/*";
   private final ActivityResultLauncher<Intent> requestpermissionlauncher2 =
           registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
              if (result.getData() != null) {
                 Intent uploadfileIntent = result.getData();
                 Uri uri = uploadfileIntent.getData();
                 performFileImport(uri.getPath());
              }
           });

   @Override
   public void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_import);

      Toolbar toolbar = findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
      getSupportActionBar().setTitle(R.string.import_title);

      Button btn_import = findViewById(R.id.import_);
      btn_import.setOnClickListener(this);
   }

   @Override
   public void onClick(View view) {
      Intent i = new Intent(Intent.ACTION_GET_CONTENT);
      i.setType(TYPE);
      requestpermissionlauncher2.launch(Intent.createChooser(i, "Select file"));
   }

   public void performFileImport(String filePath) {
      new FileImportDialog().execute(this, filePath).setOnResultReceived(results -> {
         //noinspection StatementWithEmptyBody
         if (results != null) {

         } else {
            // Export unsuccessful. Error occurred
            ErrorDialog.Builder errorBuilder = new ErrorDialog.Builder();
            errorBuilder.setShowSuggestions(true)
                        .setDialogMessage("An Error occurred while importing data")
                        .setSuggestionCount(2)
                        .setSuggestion1("File extension might not be supported")
                        .setSuggestion2("File might be corrupt");

            new ErrorDialog().showErrorMessage(this, errorBuilder.build());
         }
      });
   }

}
