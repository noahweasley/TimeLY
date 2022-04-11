package com.noah.timely.auth.ui.login;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.noah.timely.R;

public class RecoveryActivity extends AppCompatActivity {

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_recovery);

      ImageButton exit = findViewById(R.id.exit);
      exit.setOnClickListener(v -> onBackPressed());

      Button btn_signUp = findViewById(R.id.sign_up);

      CheckBox cbx_lcAgree = findViewById(R.id.cbx_lc_agree);
      cbx_lcAgree.setOnCheckedChangeListener((v, isChecked) -> btn_signUp.setEnabled(isChecked));
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
   }
}
