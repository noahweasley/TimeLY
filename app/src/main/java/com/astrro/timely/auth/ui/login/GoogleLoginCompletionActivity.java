package com.astrro.timely.auth.ui.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.astrro.timely.R;
import com.astrro.timely.auth.data.model.RegistrationResponse;
import com.astrro.timely.auth.data.api.TimelyApi;
import com.astrro.timely.auth.data.model.UserAccount;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;

public class GoogleLoginCompletionActivity extends AppCompatActivity {
   private static final String EXTRA_USER_ACCOUNT = "User account details";
   private EditText edt_userName;
   private EditText edt_phoneNumber;

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
      String randomUsername = userAccount.getRandomUserName();

      edt_userName = findViewById(R.id.user_name);
      edt_phoneNumber = findViewById(R.id.phone_number);
      Button signUp = findViewById(R.id.sign_up);

      edt_userName.setText(randomUsername);
      edt_userName.setSelection(randomUsername.length());

      signUp.setOnClickListener(v -> registerNewUser(userAccount));

   }

   private void registerNewUser(UserAccount userAccount) {
      boolean isErrorOccurred = false;

      if (TextUtils.isEmpty(edt_userName.getText())) {
         ViewGroup container = (ViewGroup) edt_userName.getParent();
         TextInputLayout til_userNameParent = ((TextInputLayout) container.getParent());
         til_userNameParent.setError("Field can't be empty");

         isErrorOccurred = true;
      }

      if (Patterns.PHONE.matcher(edt_phoneNumber.getText()).matches()) {
         ViewGroup container = (ViewGroup) edt_phoneNumber.getParent();
         TextInputLayout til_phoneNumberParent = ((TextInputLayout) container.getParent());
         til_phoneNumberParent.setError("Input a valid phone number");

         isErrorOccurred = true;
      }

      if (!isErrorOccurred) {
         userAccount.setPhoneNumber(edt_phoneNumber.getText());
         userAccount.setUserName(edt_userName.getText());

         new NetworkRequestDialog<RegistrationResponse>()
                 .setProgressInfo(getString(R.string.registering))
                 .execute(this, () -> {
                    try {
                       return TimelyApi.registerNewUser(userAccount);
                    } catch (IOException ioException) {
                       Toast.makeText(this, "Network error occurred", Toast.LENGTH_LONG).show();
                       return null;
                    }
                 }).setOnResponseProcessedListener(loginResponse -> {

            if (loginResponse.getStatusCode() == HttpStatusCodes.OK && loginResponse.isUserRegistered()) {
               CompleteRegistrationActivity.start(this, userAccount);
            }
         });
      }
   }

}
