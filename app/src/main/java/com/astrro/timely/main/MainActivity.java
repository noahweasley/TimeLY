package com.astrro.timely.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.astrro.timely.R;
import com.astrro.timely.about.BasicInfoListActivity;
import com.astrro.timely.alarms.TimeChangeDetector;
import com.astrro.timely.alarms.TimeChangeDetectorService;
import com.astrro.timely.auth.ui.login.LoginActivity;
import com.astrro.timely.core.SchoolDatabase;
import com.astrro.timely.main.chats.ChatsFragment;
import com.astrro.timely.main.library.StudentLibraryFragment;
import com.astrro.timely.main.marketplace.MarketPlaceFragment;
import com.astrro.timely.main.notification.NotificationsFragment;
import com.astrro.timely.settings.SettingsActivity;
import com.astrro.timely.util.PreferenceUtils;
import com.astrro.timely.util.TimelyUpdateUtils;
import com.astrro.timely.util.sound.SoundUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements MenuProvider {
   private Fragment visibleFragment;
   private boolean dismissable;
   public static final String ACTION_LOGIN = "Login_user_action";
   private BottomNavigationView bottomNavigationView;

   static {
      AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
   }

   private DrawerLayout drawer;
   private ActionBarDrawerToggle toggle;
   private TimeChangeDetector timeChangeDetector;
   private View vg_headerView;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      launchIntroActivity();
      setTheme(R.style.AppTheme_FadeIn);
      setContentView(R.layout.activity_main);
      addMenuProvider(this);

      NavigationView navView = findViewById(R.id.nav_view);
      bottomNavigationView = findViewById(R.id.bottom_navigation);

      bottomNavigationView.setOnItemSelectedListener(new NavigationBarItemListener());
      // navigate to user sign up | login screen
      vg_headerView = navView.getHeaderView(0);
      detectUserLoginClickRequest();
      checkedUserLoggingStatus();

      navView.setNavigationItemSelectedListener(new NavigationItemListener());
      // check for app updates
      if (PreferenceUtils.getBooleanValue(this, PreferenceUtils.UPDATE_ON_STARTUP, true))
         TimelyUpdateUtils.checkForUpdates(this);

      // then ...
      Toolbar toolbar = findViewById(R.id.toolbar);
      drawer = findViewById(R.id.drawer);
      toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);
      drawer.addDrawerListener(toggle);
      setSupportActionBar(toolbar);

      if (savedInstanceState == null) {
         showFragment(FeedsFragment.getInstance());
      } else {
         if (visibleFragment != null) {
            showFragment(visibleFragment);
         }
      }
   }

   @Override
   protected void onResume() {
      super.onResume();
      startService(new Intent(this, TimeChangeDetectorService.class)); // start OS time and date detection
   }

   @Override
   protected void onPause() {
      super.onPause();
   }

   private void showFragment(Fragment fragment) {
      FragmentManager manager = getSupportFragmentManager();
      FragmentTransaction transaction = manager.beginTransaction();
      transaction.setReorderingAllowed(true);

      final String TAG = fragment.getClass().getSimpleName();

      if (visibleFragment != null) {
         transaction.hide(visibleFragment);
         Log.d(getClass().getSimpleName(), visibleFragment.getClass().getSimpleName() + " was saved");
      }

      if (fragment.isAdded()) {
         transaction.show(fragment);
         if (visibleFragment != null)
            Log.d(getClass().getSimpleName(), visibleFragment.getClass().getSimpleName() + " will show");
      } else {
         transaction.add(R.id.frame, fragment, TAG);
         if (visibleFragment != null)
            Log.d(getClass().getSimpleName(), fragment.getClass().getSimpleName() + ", has been added now");
      }

      transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out).commit();
      updateToolbarTitle(fragment);
      visibleFragment = fragment;
   }

   private void updateToolbarTitle(Fragment fragment) {
      String title = null;
      if (fragment.getClass() == MarketPlaceFragment.class) title = MarketPlaceFragment.getToolbarTitle();
      else if (fragment.getClass() == StudentLibraryFragment.class) title = StudentLibraryFragment.getToolbarTitle();
      else if (fragment.getClass() == FeedsFragment.class) title = FeedsFragment.getToolbarTitle();
      else if (fragment.getClass() == ChatsFragment.class) title = ChatsFragment.getToolbarTitle();
      else if (fragment.getClass() == NotificationsFragment.class) title = NotificationsFragment.getToolbarTitle();

      getSupportActionBar().setTitle(title);
   }

   private void detectUserLoginClickRequest() {
      ViewGroup header = (ViewGroup) vg_headerView;
      ViewGroup vg_authGroup = (ViewGroup) header.getChildAt(0);
      ViewGroup vg_userProfile = (ViewGroup) header.getChildAt(1);

      vg_authGroup.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
      vg_userProfile.setOnClickListener(v -> startActivity(new Intent(this, UserProfileActivity.class)));
   }

   @Override
   public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
   }

   @Override
   public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
      toggle.onOptionsItemSelected(menuItem);
      return true;
   }

   private void checkedUserLoggingStatus() {
      // check if user is logged in
      boolean isUserLoggedIn =
              Boolean.valueOf(PreferenceUtils.getStringValue(this, PreferenceUtils.USER_IS_LOGGED_IN, "false"));
      ViewGroup header = (ViewGroup) vg_headerView;
      ViewGroup vg_authGroup = (ViewGroup) header.getChildAt(0);
      ViewGroup vg_userProfile = (ViewGroup) header.getChildAt(1);

      if (isUserLoggedIn) {
         View[] userProfileViews = getUserProfileViews(vg_userProfile);

         String username = PreferenceUtils.getStringValue(this, PreferenceUtils.USER_NAME, null);
         ((TextView) userProfileViews[1]).setText(username);

         String userSchool = PreferenceUtils.getStringValue(this, PreferenceUtils.USER_SCHOOL, null);
         ((TextView) userProfileViews[2]).setText(userSchool);

         vg_userProfile.setVisibility(View.VISIBLE);
         vg_authGroup.setVisibility(View.GONE);
      } else {
         vg_userProfile.setVisibility(View.GONE);
         vg_authGroup.setVisibility(View.VISIBLE);
      }

   }

   private View[] getUserProfileViews(ViewGroup userProfileGroup) {
      ImageView img_userProfilePicture = (ImageView) userProfileGroup.getChildAt(0);

      ViewGroup vg_userDetails = (ViewGroup) userProfileGroup.getChildAt(1);
      TextView tv_username = (TextView) vg_userDetails.getChildAt(0);
      TextView tv_userSchool = (TextView) vg_userDetails.getChildAt(1);

      return new View[]{ img_userProfilePicture, tv_username, tv_userSchool };
   }

   private void launchIntroActivity() {
      boolean isFirstLaunch = PreferenceUtils.getFirstLaunchKey(getApplicationContext());
      // start next screen based on the app's first time launch saved preference
      if (isFirstLaunch) {
         Intent launchIntent = new Intent(this, IntroPageActivity.class);
         launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         startActivity(launchIntent);
         finish();
      }
   }

   @Override
   protected void onPostCreate(Bundle state) {
      super.onPostCreate(state);
      toggle.syncState();
   }

   @Override
   protected void onStop() {
      // drop unwanted exam week tables from database
      new SchoolDatabase(this.getApplicationContext()).dropRedundantExamTables();
      super.onStop();
   }

   @Override
   protected void onDestroy() {
      // clean up resources used in playing alert tones
      SoundUtils.doCleanUp();
      // TimeLY is being exited, set the first launch key to false
      PreferenceUtils.setFirstLaunchKey(getApplicationContext(), false);
      // cannot activate, stop OS time and date detection
      stopService(new Intent(this, TimeChangeDetectorService.class));
      super.onDestroy();
   }

   @Override
   public void onBackPressed() {
      if (drawer.isDrawerOpen(GravityCompat.START))
         drawer.closeDrawer(GravityCompat.START);
      else {
         if (dismissable) {
            super.onBackPressed();
         } else {
            if (visibleFragment == FeedsFragment.getInstance()) {
               String app_name = getString(R.string.app_name);
               String exit_message = getString(R.string.exit_message);
               String full_exit_message = String.format(Locale.US, "%s %s", exit_message, app_name);
               Toast.makeText(this, full_exit_message, Toast.LENGTH_SHORT).show();
            } else {
               showFragment(FeedsFragment.getInstance());
               bottomNavigationView.setSelectedItemId(R.id.home);
               return;
            }
         }
         dismissable = true;
         new Handler(getMainLooper()).postDelayed(() -> dismissable = false, 2000);
      }

   }

   // Get intent actions and update UI
   private void doHandleRequestAction(Intent intent) {
      final String reqAction = intent.getAction();
      if (reqAction != null && reqAction.equals(MainActivity.ACTION_LOGIN)) {
         checkedUserLoggingStatus();
      }
   }

   private class NavigationItemListener implements NavigationView.OnNavigationItemSelectedListener {

      @Override
      public boolean onNavigationItemSelected(@NonNull MenuItem item) {
         int itemId = item.getItemId();
         if (itemId == R.id.home) {
            showFragment(FeedsFragment.getInstance());
         } else if (itemId == R.id.courses || itemId == R.id.timetable || itemId == R.id.scheduled_classes
                 || itemId == R.id.assignment || itemId == R.id.exam_timetable || itemId == R.id.todo_list
                 || itemId == R.id.calculator || itemId == R.id.alarms) {

            SchoolUtilitesActivity.start(MainActivity.this, itemId);
         } else if (itemId == R.id.settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
         } else if (itemId == R.id.help) {
            startActivity(new Intent(MainActivity.this, BasicInfoListActivity.class));
            return true;
         }

         if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
         }
         return true;
      }
   }

   private class NavigationBarItemListener implements NavigationBarView.OnItemSelectedListener {

      @Override
      public boolean onNavigationItemSelected(@NonNull MenuItem item) {
         int itemId = item.getItemId();
         if (itemId == R.id.notification) {
            showFragment(NotificationsFragment.getInstance());
         } else if (itemId == R.id.chats) {
            showFragment(ChatsFragment.getInstance());
         } else if (itemId == R.id.home) {
            showFragment(FeedsFragment.getInstance());
         } else if (itemId == R.id.library) {
            showFragment(StudentLibraryFragment.getInstance());
         } else if (itemId == R.id.marketplace) {
            showFragment(MarketPlaceFragment.getInstance());
         }

         return true;
      }

   }
}