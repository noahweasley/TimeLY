package com.noah.timely.auth.ui.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.noah.timely.R;
import com.noah.timely.auth.data.model.UserAccount;

public class VerificationActivity extends AppCompatActivity {
   private static final String EXTRA_USER_ACCOUNT = "User Account";

   /**
    * Helper method to start this activity
    *
    * @param context     the originating context
    * @param phoneNumber the phone number of the user that would receive the verification message
    */
   public static void start(Context context, UserAccount userAccount) {
      Intent starter = new Intent(context, VerificationActivity.class);
      starter.putExtra(EXTRA_USER_ACCOUNT, userAccount);
      context.startActivity(starter);
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_verification);

      Button signUp = findViewById(R.id.sign_up);
      TextView tv_phoneNumber = findViewById(R.id.phone_number);

      signUp.setOnClickListener(t -> {
         UserAccount userAccount = (UserAccount) getIntent().getSerializableExtra(EXTRA_USER_ACCOUNT);
         CompleteRegistrationActivity.start(this, userAccount);
      });

      String phoneNumber = getIntent().getStringExtra(EXTRA_USER_ACCOUNT);
      if (!TextUtils.isEmpty(phoneNumber)) tv_phoneNumber.setText(phoneNumber);
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
   }

}
