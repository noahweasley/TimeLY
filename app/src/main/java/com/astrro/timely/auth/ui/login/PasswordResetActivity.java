package com.astrro.timely.auth.ui.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.astrro.timely.R;
import com.astrro.timely.util.PatternUtils;

public class PasswordResetActivity extends AppCompatActivity implements View.OnClickListener {
   private EditText edt_newPassword, edt_confirmNewPassword;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_password_reset);

      ImageButton exit = findViewById(R.id.exit);
      exit.setOnClickListener(v -> onBackPressed());

      edt_newPassword = findViewById(R.id.new_password);
      edt_confirmNewPassword = findViewById(R.id.confirm_new_password);

      Button btn_submit = findViewById(R.id.submit);
      btn_submit.setOnClickListener(this);
   }

   @Override
   public void onClick(View view) {
      String password1 = edt_newPassword.getText().toString();
      String password2 = edt_confirmNewPassword.getText().toString();

      boolean isErrorOccurred = false;

      if (!password1.matches(PatternUtils.STRONG_PASSWORD)) {
         ViewGroup container = (ViewGroup) edt_newPassword.getParent();
         TextInputLayout til_passwordParent = ((TextInputLayout) container.getParent());
         til_passwordParent.setError("Must have a minimum of 8 characters, 1 letter and 1 number");

         isErrorOccurred = true;

      } else {
         if (!TextUtils.equals(password1, password2)) {
            ViewGroup container = (ViewGroup) edt_confirmNewPassword.getParent();
            TextInputLayout til_passwordParent = ((TextInputLayout) container.getParent());
            til_passwordParent.setError("Password doesn't match new password");

            isErrorOccurred = true;
         }
      }

      if (!password2.matches(PatternUtils.STRONG_PASSWORD)) {
         ViewGroup container = (ViewGroup) edt_confirmNewPassword.getParent();
         TextInputLayout til_passwordParent = ((TextInputLayout) container.getParent());
         til_passwordParent.setError("Must have a minimum of 8 characters, 1 letter and 1 number");

         isErrorOccurred = true;
      }

      // ... then we naviagate to next page
      if (!isErrorOccurred) {
         Toast.makeText(this, "Successful", Toast.LENGTH_LONG).show();
      }
   }

}
