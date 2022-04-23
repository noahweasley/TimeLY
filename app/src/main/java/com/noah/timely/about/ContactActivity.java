package com.noah.timely.about;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.noah.timely.R;

public class ContactActivity extends AppCompatActivity {

   @Override
   protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_contact);
      setSupportActionBar(findViewById(R.id.toolbar));
      getSupportActionBar().setTitle("Contact us");
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

   }

   @Override
   public boolean onSupportNavigateUp() {
      super.onBackPressed();
      return super.onSupportNavigateUp();
   }

}
