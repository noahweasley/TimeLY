package com.astrro.timely.main;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.astrro.timely.R;

public class UserProfileActivity extends AppCompatActivity {

   @Override
   protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_user_profile);

      setSupportActionBar(findViewById(R.id.toolbar));
      getSupportActionBar().setTitle("User Profile");
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
   }

   @Override
   public boolean onSupportNavigateUp() {
      super.onBackPressed();
      return true;
   }
}
