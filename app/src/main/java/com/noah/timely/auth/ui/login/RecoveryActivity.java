package com.noah.timely.auth.ui.login;

import android.os.Bundle;
import android.widget.Button;
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

   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
   }
}
