package com.astrro.timely.core;

import androidx.appcompat.app.AppCompatActivity;

public class NavigationActivity extends AppCompatActivity {

   @Override
   public boolean onSupportNavigateUp() {
      onBackPressed();
      return true;
   }

}
