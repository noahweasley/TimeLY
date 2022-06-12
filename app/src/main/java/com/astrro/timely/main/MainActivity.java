package com.astrro.timely.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.astrro.timely.R;
import com.astrro.timely.about.BasicInfoListActivity;
import com.astrro.timely.alarms.AlarmHolderFragment;
import com.astrro.timely.alarms.TimeChangeDetector;
import com.astrro.timely.alarms.TimeChangeDetectorService;
import com.astrro.timely.assignment.AssignmentFragment;
import com.astrro.timely.auth.ui.login.LoginActivity;
import com.astrro.timely.calculator.ResultCalculatorContainerFragment;
import com.astrro.timely.core.SchoolDatabase;
import com.astrro.timely.courses.CoursesFragment;
import com.astrro.timely.exam.ExamFragment;
import com.astrro.timely.exports.ImportResultsActivity;
import com.astrro.timely.exports.TMLYDataGeneratorDialog;
import com.astrro.timely.main.notification.NotificationsActivity;
import com.astrro.timely.scheduled.ScheduledTimetableFragment;
import com.astrro.timely.settings.SettingsActivity;
import com.astrro.timely.timetable.TimetableFragment;
import com.astrro.timely.todo.TodoFragment;
import com.astrro.timely.util.Constants;
import com.astrro.timely.util.PreferenceUtils;
import com.astrro.timely.util.ReportActionUtil;
import com.astrro.timely.util.TimelyUpdateUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
   private boolean dismissable;
   public static final String ACTION_LOGIN = "Login_user_action";

   static {
      AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
   }

   DrawerLayout drawer;
   private ActionBarDrawerToggle toggle;
   private TimeChangeDetector timeChangeDetector;
   private View vg_headerView;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setTheme(R.style.AppTheme_FadeIn);
      setContentView(R.layout.activity_main);

      NavigationView navView = findViewById(R.id.nav_view);
      BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

      bottomNavigationView.setOnItemSelectedListener(new NavigationBarItemListener());
      // navigate to user sign up | login screen
      vg_headerView = navView.getHeaderView(0);
      vg_headerView.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
      checkedUserLoggingStatus();

      navView.setNavigationItemSelectedListener(this);

      // check for app updates
      if (PreferenceUtils.getBooleanValue(this, PreferenceUtils.UPDATE_ON_STARTUP, true))
         TimelyUpdateUtils.checkForUpdates(this);

      // then ...
      Toolbar toolbar = findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
      drawer = findViewById(R.id.drawer);
      toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);
      drawer.addDrawerListener(toggle);
      // Set the first viewed fragment on app start
      doHandleRequestAction(getIntent()); // update the fragment attached to this activity

      startService(new Intent(this, TimeChangeDetectorService.class)); // start OS time and date detection

      // ignore power management service to trigger alarms properly
      String cancelText = getString(R.string.later);
      String goText = getString(R.string.go);
      String noticeTitle = getString(R.string.noticeTitle);
      String neutralText = getString(R.string.never);
      String noticeMessage = getString(R.string.noticeMessage);
      final String RESTRICTION_GRANT = PreferenceUtils.getStringValue(this, PreferenceUtils.RESTRICTION_ACCESS_KEY,
                                                                      PreferenceUtils.GRANT_ACCESS);
      final boolean ACCESS_DENIED = RESTRICTION_GRANT.equals(PreferenceUtils.DENY_ACCESS);
      PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
         if (!ACCESS_DENIED && !powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
            // notify user of their actions to remove restrictions on battery optimizations
            new AlertDialog.Builder(this)
                    .setTitle(noticeTitle)
                    .setMessage(noticeMessage)
                    .setIcon(R.drawable.ic_baseline_info_24)
                    .setNegativeButton(cancelText, this::requestAction)
                    .setNeutralButton(neutralText, this::requestAction)
                    .setPositiveButton(goText, this::requestAction)
                    .create()
                    .show();
         }
      }
   }

   @Override
   protected void onResume() {
      super.onResume();
      launchIntroActivity();
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
         launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
         startActivity(launchIntent);
      }
   }

   // Because launch mode of this activity is set to single task, this callback will be invoked. When will this be
   // invoked ? It will be invoked when a user clicked on the notification to view a particular fragment, but
   // onCreate won't be invoked if the app is already running but onNewIntent will be invoked so, bring MainActivity
   // to front, and replace fragment to view.
   @Override
   protected void onNewIntent(Intent intent) {
      super.onNewIntent(intent);
      doHandleRequestAction(intent);
   }

   @RequiresApi(api = Build.VERSION_CODES.M)
   private void requestAction(DialogInterface dialog, int which) {
      if (which == DialogInterface.BUTTON_POSITIVE)
         startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
      else if (which == DialogInterface.BUTTON_NEUTRAL)
         PreferenceUtils.setStringValue(this, PreferenceUtils.RESTRICTION_ACCESS_KEY, PreferenceUtils.DENY_ACCESS);
      dialog.cancel();
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
            FragmentManager manager = getSupportFragmentManager();
            Fragment fragment1 = manager.findFragmentByTag("Todo");
            if (fragment1 != null) super.onBackPressed();
            else {
               String app_name = getString(R.string.app_name);
               String exit_message = getString(R.string.exit_message);
               String full_exit_message = String.format(Locale.US, "%s %s", exit_message, app_name);
               Toast.makeText(this, full_exit_message, Toast.LENGTH_SHORT).show();
            }
         }
         dismissable = true;
         new Handler(getMainLooper()).postDelayed(() -> dismissable = false, 2000);
      }

   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.menu_main, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(@NonNull MenuItem item) {
      int id = item.getItemId();
      if (id == R.id.action_settings) {
         open_upSetting();
      } else if (id == R.id.update) {
         TimelyUpdateUtils.checkForUpdates(this);
      }
      return super.onOptionsItemSelected(item);
   }

   @Override
   public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

      int menuItemId = menuItem.getItemId();

      if (menuItemId == R.id.home) {

         loadFragment(LandingPageFragment.newInstance());

      } else if (menuItemId == R.id.courses) {

         loadFragment(CoursesFragment.newInstance());

      } else if (menuItemId == R.id.timetable) {

         loadFragment(TimetableFragment.newInstance());

      } else if (menuItemId == R.id.scheduled_classes) {

         loadFragment(ScheduledTimetableFragment.newInstance());

      } else if (menuItemId == R.id.assignment) {

         loadFragment(AssignmentFragment.newInstance());

      } else if (menuItemId == R.id.exam_timetable) {

         loadFragment(ExamFragment.newInstance());

      } else if (menuItemId == R.id.todo_list) {

         loadFragment(TodoFragment.newInstance());

      } else if (menuItemId == R.id.calculator) {

         loadFragment(ResultCalculatorContainerFragment.newInstance());

      } else if (menuItemId == R.id.alarms) {

         loadFragment(AlarmHolderFragment.newInstance());

      } else if (menuItemId == R.id.settings) {

         open_upSetting();
         return true; // immediately return from callback so that the drawer doesn't close

      } else if (menuItemId == R.id.__export) {

         new TMLYDataGeneratorDialog().show(this);

      } else if (menuItemId == R.id.__import) {

         startActivity(new Intent(this, ImportResultsActivity.class));
         return true; // immediately return from callback so that the drawer doesn't close

      } else if (menuItemId == R.id.help) {

         startActivity(new Intent(this, BasicInfoListActivity.class));
         return true; // immediately return from callback so that the drawer doesn't close

      }

      if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);

      return true;
   }

   private void reportAction(DialogInterface dialog, int which) {
      if (which == DialogInterface.BUTTON_POSITIVE) {
         ReportActionUtil.reportBug(this, "");

      }
      // whatever the button clicked, close dialog
      dialog.cancel();
   }

   // Get intent actions and update UI
   private void doHandleRequestAction(Intent intent) {
      final String reqAction = intent.getAction();

      if (reqAction != null) {
         switch (reqAction) {
            case Constants.ASSIGNMENT:
               loadFragment(AssignmentFragment.newInstance());
               break;
            case Constants.SCHEDULED_TIMETABLE:
               loadFragment(ScheduledTimetableFragment.newInstance());
               break;
            case Constants.TIMETABLE:
               loadFragment(TimetableFragment.newInstance());
               break;
            case Constants.EXAM:
               loadFragment(ExamFragment.newInstance());
               break;
            case Constants.COURSE:
               loadFragment(CoursesFragment.newInstance());
               break;
            case Constants.ALARM:
               loadFragment(AlarmHolderFragment.newInstance());
               break;
            case MainActivity.ACTION_LOGIN:
               checkedUserLoggingStatus();
               break;
            default:
               loadFragment(LandingPageFragment.newInstance());
               break;
         }
      } else {
         // Loads landing page on start up.
         loadFragment(LandingPageFragment.newInstance());
      }
   }

   // open up settings with a custom animation
   private void open_upSetting() {
      startActivity(new Intent(this, SettingsActivity.class));
   }

   public void loadFragment(Fragment fragment) {
      FragmentManager manager = getSupportFragmentManager();
      FragmentTransaction transaction = manager.beginTransaction();

      //  Remove TodoFragment from the backstack on navigation item click
      Fragment fragment2 = manager.findFragmentByTag("Todo");
      if (fragment2 != null) {
         for (int fx = 0; fx < manager.getBackStackEntryCount(); ++fx) {
            manager.popBackStack();
         }
      }

      final String TAG = fragment.getClass().getName();
      Fragment fragment1 = manager.findFragmentByTag(TAG);
      if (fragment1 == null) {
         transaction.replace(R.id.frame, fragment, TAG).commit();
      }
   }

   private class NavigationBarItemListener implements NavigationBarView.OnItemSelectedListener {

      @Override
      public boolean onNavigationItemSelected(@NonNull MenuItem item) {
         int itemId = item.getItemId();
         if (itemId == R.id.notification) {
            startActivity(new Intent(MainActivity.this, NotificationsActivity.class));
         }

         return true;
      }
   }
}