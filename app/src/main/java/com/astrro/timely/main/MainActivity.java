package com.astrro.timely.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.astrro.timely.about.BasicInfoActivity;
import com.astrro.timely.alarms.AlarmHolderFragment;
import com.astrro.timely.alarms.TimeChangeDetector;
import com.astrro.timely.alarms.TimeChangeDetectorService;
import com.astrro.timely.assignment.AssignmentFragment;
import com.astrro.timely.calculator.ResultCalculatorContainerFragment;
import com.astrro.timely.core.SchoolDatabase;
import com.astrro.timely.courses.CoursesFragment;
import com.astrro.timely.exam.ExamFragment;
import com.astrro.timely.scheduled.ScheduledTimetableFragment;
import com.astrro.timely.settings.SettingsActivity;
import com.astrro.timely.timetable.TimetableFragment;
import com.astrro.timely.todo.TodoFragment;
import com.astrro.timely.util.Constants;
import com.astrro.timely.util.PreferenceUtils;
import com.astrro.timely.util.TimelyUpdateUtils;
import com.astrro.timely.util.sound.SoundUtils;
import com.google.android.material.navigation.NavigationView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements MenuProvider {

   static {
      AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
   }

   private Fragment visibleFragment;
   private boolean dismissable;
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
         loadFragment(FeedsFragment.getInstance());
      } else {
         if (visibleFragment != null) {
            loadFragment(visibleFragment);
         }
      }
   }

   @Override
   protected void onPause() {
      super.onPause();
   }

   @Override
   protected void onResume() {
      super.onResume();
      startService(new Intent(this, TimeChangeDetectorService.class)); // start OS time and date detection
   }

   @Override
   public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
   }

   @Override
   public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
      toggle.onOptionsItemSelected(menuItem);
      return true;
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
      if (drawer.isDrawerOpen(GravityCompat.START)) {
         drawer.closeDrawer(GravityCompat.START);
      } else {
         if (dismissable) {
            super.onBackPressed();
         } else {
            if (visibleFragment == FeedsFragment.getInstance()) {
               String app_name = getString(R.string.app_name);
               String exit_message = getString(R.string.exit_message);
               String full_exit_message = String.format(Locale.US, "%s %s", exit_message, app_name);
               Toast.makeText(this, full_exit_message, Toast.LENGTH_SHORT).show();
            } else {
               loadFragment(FeedsFragment.getInstance());
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
         }
      }
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

      final String FRAGMENT_TAG = fragment.getClass().getSimpleName();
      transaction.replace(R.id.frame, fragment, FRAGMENT_TAG).commit();

      visibleFragment = fragment;
   }

   private class NavigationItemListener implements NavigationView.OnNavigationItemSelectedListener {

      @Override

      public boolean onNavigationItemSelected(@NonNull MenuItem item) {
         int itemId = item.getItemId();
         if (itemId == R.id.home) {
            loadFragment(FeedsFragment.getInstance());
         } else if (itemId == R.id.courses) {
            loadFragment(CoursesFragment.newInstance());
         } else if (itemId == R.id.timetable) {
            loadFragment(TimetableFragment.newInstance());
         } else if (itemId == R.id.scheduled_classes) {
            loadFragment(ScheduledTimetableFragment.newInstance());
         } else if (itemId == R.id.assignment) {
            loadFragment(AssignmentFragment.newInstance());
         } else if (itemId == R.id.exam_timetable) {
            loadFragment(ExamFragment.newInstance());
         } else if (itemId == R.id.todo_list) {
            loadFragment(TodoFragment.newInstance());
         } else if (itemId == R.id.calculator) {
            loadFragment(ResultCalculatorContainerFragment.newInstance());
         } else if (itemId == R.id.alarms) {
            loadFragment(AlarmHolderFragment.newInstance());
         } else if (itemId == R.id.settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
         } else if (itemId == R.id.help) {
            startActivity(new Intent(MainActivity.this, BasicInfoActivity.class));
            return true;
         }

         if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
         }
         return true;
      }
   }

}
