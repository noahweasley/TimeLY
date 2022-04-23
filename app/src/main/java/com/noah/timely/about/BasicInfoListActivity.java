package com.noah.timely.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.noah.timely.R;

public class BasicInfoListActivity extends AppCompatActivity implements View.OnClickListener {

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_basic_info_list);
      setSupportActionBar(findViewById(R.id.toolbar));
      getSupportActionBar().setTitle("Help");
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      // inflate views
      ViewGroup vg_legal = findViewById(R.id.legal);
      ViewGroup vg_contact = findViewById(R.id.contact);
      ViewGroup vg_helpCenter = findViewById(R.id.centre);
      ViewGroup vg_appInfo = findViewById(R.id.app_info);
      ViewGroup vg_updateInfo = findViewById(R.id.update_info);
      // avoiding stupid duplicate code
      ViewGroup[] vgs = { vg_legal, vg_contact, vg_helpCenter, vg_appInfo, vg_updateInfo };
      for (ViewGroup vg : vgs) vg.setOnClickListener(this);
   }

   @Override
   public boolean onSupportNavigateUp() {
      super.onBackPressed();
      return super.onSupportNavigateUp();
   }

   @Override
   public void onClick(View view) {
      int viewId = view.getId();
      if (viewId == R.id.update_info) {
         startActivity(new Intent(this, TimelyUpdateInfoActivity.class));
      } else if (viewId == R.id.app_info) {
         startActivity(new Intent(this, TimelyBasicInfoActivity.class));
      } else if (viewId == R.id.contact) {
         startActivity(new Intent(this, ContactActivity.class));
      } else if (viewId == R.id.legal) {
         Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.legal_notice_address)));
         startActivity(Intent.createChooser(intent, getString(R.string.link_open_text)));
      } else if (viewId == R.id.help) {
         Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.help_address)));
         startActivity(Intent.createChooser(intent, getString(R.string.link_open_text)));
      }

   }

}