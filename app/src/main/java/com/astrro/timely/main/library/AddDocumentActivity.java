package com.astrro.timely.main.library;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.astrro.timely.R;

public class AddDocumentActivity extends AppCompatActivity {

   public static void start(Context context) {
       Intent starter = new Intent(context, AddDocumentActivity.class);
       context.startActivity(starter);
   }

   @Override
   protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_add_document);
      setSupportActionBar(findViewById(R.id.toolbar));
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setTitle(R.string.upload_capitalize);
   }

   @Override
   public boolean onNavigateUp() {
      super.onBackPressed();
      return true;
   }
}
