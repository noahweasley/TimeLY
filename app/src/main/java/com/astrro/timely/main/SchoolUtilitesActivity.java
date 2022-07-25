package com.astrro.timely.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.astrro.timely.R;
import com.astrro.timely.alarms.AlarmHolderFragment;
import com.astrro.timely.assignment.AssignmentFragment;
import com.astrro.timely.calculator.ResultCalculatorContainerFragment;
import com.astrro.timely.courses.CoursesFragment;
import com.astrro.timely.exam.ExamFragment;
import com.astrro.timely.scheduled.ScheduledTimetableFragment;
import com.astrro.timely.settings.SettingsActivity;
import com.astrro.timely.timetable.TimetableFragment;
import com.astrro.timely.todo.TodoFragment;
import com.astrro.timely.util.Constants;
import com.astrro.timely.util.TimelyUpdateUtils;

public class SchoolUtilitesActivity extends AppCompatActivity implements View.OnClickListener {
   public static final String EXTRA_MENU_ITEM = "Menu_Item";

   public static void start(Context context, int menuItem) {
      Intent starter = new Intent(context, SchoolUtilitesActivity.class);
      starter.putExtra(EXTRA_MENU_ITEM, menuItem);
      context.startActivity(starter);
   }

   @Override
   protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_school_utilities);
      setSupportActionBar(findViewById(R.id.toolbar));
      getSupportActionBar().setTitle(R.string.school_utilities);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      ViewGroup vg_assignment = findViewById(R.id.assignment);
      ViewGroup vg_registeredCourses = findViewById(R.id.registered_courses);
      ViewGroup vg_scheduled = findViewById(R.id.scheduled_classes);
      ViewGroup vg_timetable = findViewById(R.id.timetable);
      ViewGroup vg_todo = findViewById(R.id.todo_list);
      ViewGroup vg_examTimetable = findViewById(R.id.exam_timetable);
      ViewGroup vg_gpa = findViewById(R.id.gpa_calculator);
      ViewGroup vg_alarm = findViewById(R.id.alarms);

      ViewGroup[] containers = new ViewGroup[]{ vg_assignment, vg_registeredCourses, vg_scheduled, vg_timetable,
                                                vg_todo, vg_gpa, vg_alarm };

      for (int x = 0; x < containers.length; x++) {
         containers[x].setOnClickListener(this);
      }

      ScrollView sv_utilityList = findViewById(R.id.utility_list);
      FrameLayout fr_frame = findViewById(R.id.frame);

      int menuItemId = getIntent().getIntExtra(EXTRA_MENU_ITEM, -1);
      if (menuItemId != -1) {
         sv_utilityList.setVisibility(View.GONE);
         fr_frame.setVisibility(View.VISIBLE);
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
      } else {
         sv_utilityList.setVisibility(View.VISIBLE);
         fr_frame.setVisibility(View.GONE);
      }
   }

   @Override
   public boolean onCreateOptionsMenu(@NonNull Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.menu_main, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(@NonNull MenuItem item) {
      int id = item.getItemId();
      if (id == R.id.action_settings) {
         startActivity(new Intent(this, SettingsActivity.class));
      } else if (id == R.id.update) {
         TimelyUpdateUtils.checkForUpdates(this);
      }
      return super.onOptionsItemSelected(item);
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
            default:
               loadFragment(MainPageFragment.getInstance());
               break;
         }
      } else {
         loadFragment(MainPageFragment.getInstance());
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
         transaction.replace(R.id.frame, fragment, TAG)
                    .commit();
      }
   }

   @Override
   public boolean onSupportNavigateUp() {
      super.onBackPressed();
      return true;
   }

   @Override
   public void onClick(View v) {
      int itemId = v.getId();
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
      }
   }
}
