package com.astrro.timely.about;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.astrro.timely.R;
import com.astrro.timely.util.AppInfoUtils;

import java.util.Calendar;
import java.util.Locale;

public class BasicInfoActivity extends AppCompatActivity implements View.OnClickListener {

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

      Button btn_whatsNew = findViewById(R.id.update_check);
      btn_whatsNew.setOnClickListener(this);
   }

   @Override
   public void onClick(View v) {

   }

   @Override
   public boolean onSupportNavigateUp() {
      super.onBackPressed();
      return super.onSupportNavigateUp();
   }

}
