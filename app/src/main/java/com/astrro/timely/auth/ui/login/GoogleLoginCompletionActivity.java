package com.astrro.timely.auth.ui.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.astrro.timely.R;
import com.astrro.timely.auth.data.model.UserAccount;

public class GoogleLoginCompletionActivity extends AppCompatActivity {
   private static final String EXTRA_USER_ACCOUNT = "User account details";

   /**
    * Helper method used in starting this activity while passing the required parameters
    * <code>firstName</code> and <code>lastName</code> can be null. But it also means that there
    * would be no username generated
    *
    * @param context   the starter of this activity
    * @param firstName the required first name of the user that would be used to generate a random username
    * @param lastName  the required last name of the user hat would be used to generate a random username
    */
   public static void start(Context context, UserAccount userAccount) {
      Intent starter = new Intent(context, GoogleLoginCompletionActivity.class);
      starter.putExtra(EXTRA_USER_ACCOUNT, userAccount);
      context.startActivity(starter);
   }

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_google_login);

      View btn_exit = findViewById(R.id.exit);
      btn_exit.setOnClickListener(v -> onBackPressed());

      UserAccount userAccount = (UserAccount) getIntent().getSerializableExtra(EXTRA_USER_ACCOUNT);
      String randomUsername =  userAccount.getRandomUserName();

      EditText userName = findViewById(R.id.user_name), edt_phoneNumber = findViewById(R.id.phone_number);
      Button signUp = findViewById(R.id.sign_up);

      userName.setText(randomUsername);
      userName.setSelection(randomUsername.length());

      signUp.setOnClickListener(v -> {
         userAccount.setPhoneNumber(edt_phoneNumber.getText().toString());
         CompleteRegistrationActivity.start(this, userAccount);
      });

   }

   @Override
   public void onBackPressed() {
      super.onBackPressed();
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
   }
}
