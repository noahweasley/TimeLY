package com.astrro.timely.auth.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.astrro.timely.R;
import com.astrro.timely.auth.data.model.UserAccount;
import com.astrro.timely.main.MainActivity;
import com.astrro.timely.util.PreferenceUtils;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
   private GoogleSignInClient mGoogleSignInClient;
   public static final String TAG = "LoginActivity";
   private ActivityResultLauncher<Intent> resultLauncher;
   private CardView gSignParent;

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_login);

      View recovery = findViewById(R.id.recovery), login = findViewById(R.id.login)
              , exit = findViewById(R.id.exit), createAcc = findViewById(R.id.create_acc);


      gSignParent = (CardView) findViewById(R.id.g_sign_parent);

      registerGoogleSignInCallback();
      recovery.setOnClickListener(this);
      login.setOnClickListener(this);
      gSignParent.setOnClickListener(this);
      exit.setOnClickListener(this);
      createAcc.setOnClickListener(this);

      // Configure sign-in to request the user's ID, email address, and basic profile. ID and basic profile are
      // included in DEFAULT_SIGN_IN.
      GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
              .requestEmail()
              .requestIdToken(getString(R.string.SIGN_IN_CLIENT_ID))
              .build();
      // Build a GoogleSignInClient with the options specified by gso.
      mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
      // Check for existing Google Sign In account, if the user is already signed in
      // the GoogleSignInAccount will be non-null.
      GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
      // if account is non null, update UI accordingly
//      updateUI(account);

   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
   }

   @Override
   public void onClick(View v) {
      int id = v.getId();
      if (id == R.id.recovery) {
         startActivity(new Intent(this, RecoveryActivity.class));
      } else if (id == R.id.login) {
         performUserLogin();
      } else if (id == R.id.g_sign_parent) {
         doGoogleSignIn();
      } else if (id == R.id.exit) {
         onBackPressed();
      } else if (id == R.id.create_acc) {
         startActivity(new Intent(this, RegistrationActivity.class));
      } else {
         throw new IllegalStateException("Unexpected value: " + v.getId());
      }
   }

   private void performUserLogin() {
//      gSignParent.setCardElevation(0.0f);
      // PreferenceUtils#USER_IS_LOGGED_IN would be checked when app is starting up, if the user is logged in,
      // then show the full app to the user

      PreferenceUtils.setBooleanValue(this, PreferenceUtils.USER_IS_LOGGED_IN, true);
      // ... then perform app reboot
      Intent navigateIntent = new Intent(this, MainActivity.class);
      navigateIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(navigateIntent);
   }

   private void doGoogleSignIn() {
      Intent signInIntent = mGoogleSignInClient.getSignInIntent();
      resultLauncher.launch(signInIntent);
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

   // if already logged in, just go to the main screen
   private void updateUI(GoogleSignInAccount account) {
      if (account != null)
         GoogleLoginCompletionActivity.start(this, UserAccount.createFromGoogleSignIn(account));
   }

}
