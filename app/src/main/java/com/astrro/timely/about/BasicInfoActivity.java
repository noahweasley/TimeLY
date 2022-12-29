package com.astrro.timely.about;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.astrro.timely.R;
import com.astrro.timely.util.AppInfoUtils;

import java.util.Calendar;
import java.util.Locale;

public class BasicInfoActivity extends AppCompatActivity {

   @Override
   protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_basic_info);
      setSupportActionBar(findViewById(R.id.toolbar));
      getSupportActionBar().setTitle("About");
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      TextView tv_versionName = findViewById(R.id.version_name);
      tv_versionName.setText(String.format(Locale.US, "%s %s", "Version", AppInfoUtils.getAppVesionName(this)));

      Calendar calendar = Calendar.getInstance();
      int year = calendar.get(Calendar.YEAR);
      String copyRight = getString(R.string.copyright);
      String companyName = getString(R.string.company_name);

      TextView tv_productionData = findViewById(R.id.production_date);
      tv_productionData.setText(String.format(Locale.US, "%s 2018 - %d %s", copyRight, year, companyName));
   }

   @Override
   public boolean onSupportNavigateUp() {
      super.onBackPressed();
      return super.onSupportNavigateUp();
   }

}
