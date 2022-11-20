package com.astrro.timely.auth.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.astrro.timely.R;
import com.astrro.timely.auth.data.api.TimelyApi;
import com.astrro.timely.auth.data.model.LoginResponse;
import com.astrro.timely.auth.data.model.UserAccount;
import com.astrro.timely.main.MainActivity;
import com.astrro.timely.util.PreferenceUtils;
import com.astrro.timely.util.adapters.SimpleTextWatcher;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
   private GoogleSignInClient mGoogleSignInClient;
   public static final String TAG = "LoginActivity";
   private ActivityResultLauncher<Intent> resultLauncher;
   private EditText edt_userName;
   private EditText edt_passwword;
   private Button btn_login;

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_login);

      View recovery = findViewById(R.id.recovery), exit = findViewById(
              R.id.exit), createAcc = findViewById(R.id.create_acc);

      btn_login = findViewById(R.id.login);

      CardView gSignParent = (CardView) findViewById(R.id.g_sign_parent);
      edt_userName = findViewById(R.id.user_name);
      edt_passwword = findViewById(R.id.password);

      TextChangeListener listener = new TextChangeListener();
      edt_userName.addTextChangedListener(listener);
      edt_passwword.addTextChangedListener(listener);

      registerGoogleSignInCallback();
      recovery.setOnClickListener(this);
      btn_login.setOnClickListener(this);
      gSignParent.setOnClickListener(this);
      exit.setOnClickListener(this);
      createAcc.setOnClickListener(this);

      // Configure sign-in to request the user's ID, email address, and basic profile. ID and basic profile are
      // included in DEFAULT_SIGN_IN.
      GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
              .requestEmail()
              .requestProfile()
//              .requestScopes(new Scope(Scopes.EMAIL), new Scope(Scopes.PROFILE))
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
         performUserLoginAsync(null);
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

   private void performUserLoginAsync(UserAccount googleSignInAccount) {
      NetworkRequestDialog<LoginResponse> loginResponseNetworkRequestDialog = new NetworkRequestDialog<>();
      loginResponseNetworkRequestDialog.setProgressInfo(getString(R.string.log_in_text));
      loginResponseNetworkRequestDialog.setOnResponseProcessedListener(this::onResponseProcessed);
      loginResponseNetworkRequestDialog.execute(this, () -> getLoggedInUser(googleSignInAccount));
   }

   private LoginResponse getLoggedInUser(UserAccount userAccount) {
      if (userAccount == null) {
         userAccount = new UserAccount();
         userAccount.setEmail(edt_userName.getText());
         userAccount.setPassword(edt_passwword.getText());
      }

      try {
         return TimelyApi.loginUser(userAccount);
      } catch (IOException ioException) {
         Toast.makeText(this, "Network error occurred", Toast.LENGTH_LONG).show();
         return null;
      }

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
         Log.w(TAG, "signInResult: failed code = " + e.getStatusCode());
         updateUI(null);
      }

   }

   // if already logged in, just go to the main screen
   private void updateUI(GoogleSignInAccount account) {
      if (account != null) {
         performUserLoginAsync(UserAccount.createFromGoogleSignIn(account));
      } else {
         Toast.makeText(this, "Google login failed", Toast.LENGTH_LONG).show();
      }
   }

   private void onResponseProcessed(LoginResponse loginResponse) {

      if (loginResponse != null) {
         if (loginResponse.getStatusCode() == HttpStatusCodes.OK) {
            Map<String, String> map = new HashMap<>();
            UserAccount userAccount1 = loginResponse.getUserAccount();

            map.put(PreferenceUtils.USER_ID, userAccount1.getUserId());
            map.put(PreferenceUtils.USER_PASSWORD, userAccount1.getPassword());
            map.put(PreferenceUtils.USER_NAME, userAccount1.getEmail());
            map.put(PreferenceUtils.USER_SCHOOL, userAccount1.getSchool());
            map.put(PreferenceUtils.USER_IS_LOGGED_IN, String.valueOf(true));

            PreferenceUtils.setStringArraySync(this, map);

            Intent intent = new Intent(this, MainActivity.class);
            intent.setAction(MainActivity.ACTION_LOGIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
         } else {
            ViewParent viewParent = edt_userName.getParent();
            TextInputLayout til_userName = (TextInputLayout) viewParent.getParent();
            til_userName.setError("Username might be incorrect");

            ViewParent viewParent2 = edt_passwword.getParent();
            TextInputLayout til_password = (TextInputLayout) viewParent2.getParent();
            til_password.setError("Password might be incorrect");
         }

      }
   }

   private class TextChangeListener extends SimpleTextWatcher {

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
         if (!TextUtils.isEmpty(edt_userName.getText()) && !TextUtils.isEmpty(edt_passwword.getText())) {
            btn_login.setEnabled(true);
         } else {
            btn_login.setEnabled(false);
         }
      }

   }
}