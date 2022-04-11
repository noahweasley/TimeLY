package com.noah.timely.auth.ui.login;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.noah.timely.R;

public class PasswordResetActivity extends AppCompatActivity {

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_password_reset);

      ImageButton exit = findViewById(R.id.exit);
      exit.setOnClickListener(v -> onBackPressed());
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
   }
}
