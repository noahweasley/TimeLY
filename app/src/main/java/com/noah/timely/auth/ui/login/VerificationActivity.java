package com.noah.timely.auth.ui.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.noah.timely.R;

public class VerificationActivity extends AppCompatActivity {
   private static final String ARG_PHONE_NUMBER = "Recipient's phone number";

   /**
    * Helper method to start this activity
    *
    * @param context     the originating context
    * @param phoneNumber the phone number of the user that would receive the verification message
    */
   public static void start(Context context, String phoneNumber) {
      Intent starter = new Intent(context, VerificationActivity.class);
      starter.putExtra(ARG_PHONE_NUMBER, phoneNumber);
      context.startActivity(starter);
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_verification);

      Button signUp = findViewById(R.id.sign_up);
      TextView tv_phoneNumber = findViewById(R.id.phone_number);

      signUp.setOnClickListener(t -> CompleteRegistrationActivity.start(this));

      String phoneNumber = getIntent().getStringExtra(ARG_PHONE_NUMBER);
      if (!TextUtils.isEmpty(phoneNumber)) tv_phoneNumber.setText(phoneNumber);
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
   }
}
