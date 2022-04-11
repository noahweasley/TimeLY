package com.noah.timely.auth.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
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
import com.noah.timely.R;

public class RegistrationActivity extends AppCompatActivity {
   private GoogleSignInClient mGoogleSignInClient;
   public static final String TAG = "RegistrationActivity";
   private ActivityResultLauncher<Intent> resultLauncher;

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_registration);
      registerGoogleSignInCallback();

      ImageButton exit = findViewById(R.id.exit);
      View gSignParent = findViewById(R.id.g_sign_parent);
      EditText edt_phoneNumber = findViewById(R.id.phone_number);
      Button signUp = findViewById(R.id.sign_up);
      CheckBox cbxLcAgree = findViewById(R.id.cbx_lc_agree);
      TextView txtLcAgree = findViewById(R.id.txt_lc_agree), signNow = findViewById(R.id.sign_now);

      exit.setOnClickListener(v -> onBackPressed());
      gSignParent.setOnClickListener(v -> doGoogleSignIn());

      String phoneNumber = edt_phoneNumber.getText().toString();
      signUp.setOnClickListener(v -> VerificationActivity.start(this, phoneNumber));
      cbxLcAgree.setOnCheckedChangeListener((buttonView, isChecked) -> signUp.setEnabled(isChecked));

      // Make Licence agreement statements and login text clickable links
      setLinkOnText(txtLcAgree);
      setLinkOnText(signNow);

      // Configure sign-in to request the user's ID, email address, and basic
      // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
      GoogleSignInOptions gso =
              new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                      .requestEmail()
                      .requestIdToken(getString(R.string.SIGN_IN_CLIENT_ID))
                      .build();
      // Build a GoogleSignInClient with the options specified by gso.
      mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
      // Check for existing Google Sign In account, if the user is already signed in
      // the GoogleSignInAccount will be non-null.
      GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
      // if account is non null, update UI accordingly
      updateUI(account);

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
         GoogleLoginCompletionActivity.start(this, account.getGivenName(), account.getFamilyName());
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
                  startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                  break;
               case "http://www.privacy-options.com":
                  Uri link1 = Uri.parse("http://www.privacy-options.com");
                  startActivity(new Intent(Intent.ACTION_VIEW, link1));
                  break;
               case "http://www.terms-and-conditions.com":
                  Uri link2 = Uri.parse("http://www.terms-and-conditions.com");
                  startActivity(new Intent(Intent.ACTION_VIEW, link2));
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