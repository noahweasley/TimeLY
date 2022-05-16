package com.astrro.timely.auth.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.astrro.timely.R;
import com.astrro.timely.auth.data.model.UserAccount;
import com.astrro.timely.util.PatternUtils;

public class RegistrationActivity extends AppCompatActivity {
   private GoogleSignInClient mGoogleSignInClient;
   public static final String TAG = "RegistrationActivity";
   private ActivityResultLauncher<Intent> resultLauncher;
   private EditText edt_firstName;
   private EditText edt_password;
   private EditText edt_lastName;
   private EditText edt_userName;
   private EditText edt_phoneNumber;
   private EditText edt_email;

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_registration);
      registerGoogleSignInCallback();

      ImageButton exit = findViewById(R.id.exit);
      View gSignParent = findViewById(R.id.g_sign_parent);
      Button signUp = findViewById(R.id.sign_up);
      CheckBox cbxLcAgree = findViewById(R.id.cbx_lc_agree);
      TextView signNow = findViewById(R.id.sign_now);

      exit.setOnClickListener(v -> onBackPressed());
      gSignParent.setOnClickListener(v -> doGoogleSignIn());

      edt_firstName = findViewById(R.id.first_name);
      edt_lastName = findViewById(R.id.last_name);
      edt_userName = findViewById(R.id.user_name);
      edt_phoneNumber = findViewById(R.id.phone_number);
      edt_email = findViewById(R.id.email);
      edt_password = findViewById(R.id.password);

      UserAccount userAccount = new UserAccount();
      userAccount.setFirstName(edt_firstName.getText());
      userAccount.setLastName(edt_lastName.getText());
      userAccount.setUserName(edt_userName.getText());
      userAccount.setPhoneNumber(edt_phoneNumber.getText());
      userAccount.setEmail(edt_email.getText());
      userAccount.setPassowrd(edt_password.getText());

      signUp.setOnClickListener(v -> registerAcount(userAccount));

      cbxLcAgree.setOnCheckedChangeListener((buttonView, isChecked) -> signUp.setEnabled(isChecked));
      // Make Licence agreement statements and login text clickable links
      setLinkOnText(cbxLcAgree);
      setLinkOnText(signNow);

      // Configure sign-in to request the user's ID, email address, and basic profile. ID and basic profile are
      // included  in DEFAULT_SIGN_IN.
      GoogleSignInOptions gso =
              new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                      .requestEmail()
                      .requestIdToken(getString(R.string.SIGN_IN_CLIENT_ID))
                      .build();
      // Build a GoogleSignInClient with the options specified by gso.
      mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
      // Check for existing Google Sign In account, if the user is already signed in the GoogleSignInAccount will be
      // non-null.
      GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
      // if account is non null, update UI accordingly
   }

   private void registerAcount(UserAccount userAccount) {
      // first, we verify that the user entered valid inputs
      boolean isErrorOccurred = false;
      if (!userAccount.getPhoneNumber().matches(Patterns.PHONE.pattern())) {
         ViewGroup container = (ViewGroup) edt_phoneNumber.getParent();
         TextInputLayout til_phoneNumberParent = ((TextInputLayout) container.getParent());
         til_phoneNumberParent.setError("Input phone number format");

         isErrorOccurred = true;
      }

      if (TextUtils.isEmpty(edt_userName.getText())) {
         ViewGroup container = (ViewGroup) edt_userName.getParent();
         TextInputLayout til_userNameParent = ((TextInputLayout) container.getParent());
         til_userNameParent.setError("Input phone number format");

         isErrorOccurred = true;
      }

      if (TextUtils.isEmpty(edt_firstName.getText())) {
         ViewGroup container = (ViewGroup) edt_firstName.getParent();
         TextInputLayout til_firstNameParent = ((TextInputLayout) container.getParent());
         til_firstNameParent.setError("Field can't be empty");

         isErrorOccurred = true;
      }

      if (TextUtils.isEmpty(edt_lastName.getText())) {
         ViewGroup container = (ViewGroup) edt_lastName.getParent();
         TextInputLayout til_lastNameParent = ((TextInputLayout) container.getParent());
         til_lastNameParent.setError("Field can't be empty");

         isErrorOccurred = true;
      }

      if (!userAccount.getEmail().matches(Patterns.EMAIL_ADDRESS.pattern())) {
         ViewGroup container = (ViewGroup) edt_email.getParent();
         TextInputLayout til_emailParent = ((TextInputLayout) container.getParent());
         til_emailParent.setError("Input correct e-mail address");

         isErrorOccurred = true;
      }

      if (!userAccount.getPassowrd().matches(PatternUtils.STRONG_PASSWORD)) {
         ViewGroup container = (ViewGroup) edt_password.getParent();
         TextInputLayout til_passwordParent = ((TextInputLayout) container.getParent());
         til_passwordParent.setError("Must have a minimum of 8 characters, 1 letter and 1 number");

         isErrorOccurred = true;
      }

      // ... then we naviagate to next page
//      if (!isErrorOccurred) {
      VerificationActivity.start(this, userAccount);
//      }
   }

   private void registerGoogleSignInCallback() {
      // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
      resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
         if (result.getResultCode() == Activity.RESULT_OK) {
            // There are no request codes
            Intent data = result.getData();
            // The Task returned from this call is always completed, no need to attach a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
         }
      });
   }

   private void doGoogleSignIn() {
      Intent signInIntent = mGoogleSignInClient.getSignInIntent();
      resultLauncher.launch(signInIntent);
   }

   private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
      try {
         GoogleSignInAccount account = completedTask.getResult(ApiException.class);
         // Signed in successfully, show authenticated UI.
         updateUI(account);
      } catch (ApiException e) {
         // The ApiException status code indicates the detailed failure reason.
         // Please refer to the GoogleSignInStatusCodes class reference for more information.
         Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
         updateUI(null);
      }

   }

   private void updateUI(GoogleSignInAccount account) {
      if (account != null)
         GoogleLoginCompletionActivity.start(this, UserAccount.createFromGoogleSignIn(account));
   }

   private void detectLinkClick(SpannableStringBuilder strBuilder, final URLSpan span) {
      int start = strBuilder.getSpanStart(span);
      int end = strBuilder.getSpanEnd(span);
      int flags = strBuilder.getSpanFlags(span);
      ClickableSpan clickable = new ClickableSpan() {
         public void onClick(View view) {
            // Do something with links retrieved from span.getURL(), to handle link click...
            String clickedUrl = span.getURL();
            switch (clickedUrl) {
               case "@login_page":
                  Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                  startActivity(intent);
                  break;
               case "http://www.privacy-options.com":
                  Uri link1 = Uri.parse("http://www.privacy-options.com");
                  Intent intent1 = new Intent(Intent.ACTION_VIEW, link1);
                  startActivity(Intent.createChooser(intent1, getString(R.string.link_open_text)));
                  break;
               case "http://www.terms-and-conditions.com":
                  Uri link2 = Uri.parse("http://www.terms-and-conditions.com");
                  Intent intent2 = new Intent(Intent.ACTION_VIEW, link2);
                  startActivity(Intent.createChooser(intent2, getString(R.string.link_open_text)));
                  break;

            }
         }
      };

      strBuilder.setSpan(clickable, start, end, flags);
      strBuilder.removeSpan(span);
   }

   protected void setLinkOnText(TextView text) {
      CharSequence sequence = text.getText();
      SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
      URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
      for (URLSpan span : urls) {
         detectLinkClick(strBuilder, span);
      }

      text.setText(strBuilder);
      text.setMovementMethod(LinkMovementMethod.getInstance());
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
   }
}