package com.astrro.timely.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.astrro.timely.R;
import com.astrro.timely.alarms.AlarmHolderFragment;
import com.astrro.timely.assignment.AssignmentFragment;
import com.astrro.timely.calculator.ResultCalculatorContainerFragment;
import com.astrro.timely.courses.CoursesFragment;
import com.astrro.timely.exam.ExamFragment;
import com.astrro.timely.exports.ImportResultsActivity;
import com.astrro.timely.exports.TMLYDataGeneratorDialog;
import com.astrro.timely.scheduled.ScheduledTimetableFragment;
import com.astrro.timely.settings.SettingsActivity;
import com.astrro.timely.timetable.TimetableFragment;
import com.astrro.timely.todo.TodoFragment;
import com.astrro.timely.util.Constants;
import com.astrro.timely.util.TimelyUpdateUtils;
import com.google.android.material.navigation.NavigationView;

public class SchoolUtilitesActivity extends AppCompatActivity implements MenuProvider, NavigationView.OnNavigationItemSelectedListener {
   public static final String EXTRA_MENU_ITEM = "Menu_Item";
   private DrawerLayout drawer;
   private ActionBarDrawerToggle toggle;

   public static void start(Context context, int menuItem) {
      Intent starter = new Intent(context, SchoolUtilitesActivity.class);
      starter.putExtra(EXTRA_MENU_ITEM, menuItem);
      context.startActivity(starter);
   }

   @Override
   protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_school_utilities);
      Toolbar toolbar = findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);

      addMenuProvider(this, (LifecycleOwner) this, Lifecycle.State.RESUMED);
      NavigationView navView = findViewById(R.id.nav_view);
      navView.setNavigationItemSelectedListener(this);
      drawer = findViewById(R.id.drawer);
      toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);
      drawer.addDrawerListener(toggle);

      int menuItemId = getIntent().getIntExtra(EXTRA_MENU_ITEM, -1);
      if (menuItemId != -1) {
         if (menuItemId == R.id.courses) {
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
         }
      }
   }

   @Override
   protected void onPostCreate(Bundle state) {
      super.onPostCreate(state);
      toggle.syncState();
   }

   @Override
   public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
      // Inflate the menu; this adds items to the action bar if it is present.
      menuInflater.inflate(R.menu.menu_main, menu);
   }

   @Override
   public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
      int id = menuItem.getItemId();
      if (id == R.id.action_settings) {
         startActivity(new Intent(this, SettingsActivity.class));
      } else if (id == R.id.update) {
         TimelyUpdateUtils.checkForUpdates(this);
      }
      return false;
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

      final String TAG = fragment.getClass().getName();
      Fragment fragment1 = manager.findFragmentByTag(TAG);
      if (fragment1 == null) {
         transaction.replace(R.id.frame, fragment, TAG).commit();
      }
   }

   @Override
   public boolean onSupportNavigateUp() {
      super.onBackPressed();
      return true;
   }

   @Override
   public boolean onNavigationItemSelected(@NonNull MenuItem item) {
      int itemId = item.getItemId();
      if (itemId == R.id.courses) {
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
      } else if (itemId == R.id.__export) {
         new TMLYDataGeneratorDialog().show(this);
      } else if (itemId == R.id.__import) {
         startActivity(new Intent(this, ImportResultsActivity.class));
         return true;
      }
      if (drawer.isDrawerOpen(GravityCompat.START)) {
         drawer.closeDrawer(GravityCompat.START);
      }
      return true;
   }
}
