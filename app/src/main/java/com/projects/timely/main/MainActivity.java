package com.projects.timely.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.projects.timely.R;
import com.projects.timely.alarms.AlarmHolderFragment;
import com.projects.timely.alarms.TimeChangeDetector;
import com.projects.timely.assignment.AssignmentFragment;
import com.projects.timely.core.Constants;
import com.projects.timely.core.SchoolDatabase;
import com.projects.timely.courses.CoursesFragment;
import com.projects.timely.exam.ExamFragment;
import com.projects.timely.scheduled.ScheduledTimetableFragment;
import com.projects.timely.settings.SettingsActivity;
import com.projects.timely.timetable.TimetableFragment;

public class MainActivity
        extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ActionBarDrawerToggle toggle;
    DrawerLayout drawer;
    private TimeChangeDetector timeChangeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NavigationView navView = findViewById(R.id.nav_view);
        drawer = findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                                           R.string.open,
                                           R.string.close);
        drawer.addDrawerListener(toggle);
        navView.setNavigationItemSelectedListener(this);
        // Set the first viewed fragment on app start
        if (savedInstanceState == null) {
            doUpdateFragment(getIntent()); // update the fragment attached to this activity
        }
        tryActivateTimeChangeDetector(); // start OS time and date detection
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
        tryActivateTimeChangeDetector(); // cannot activate, stop OS time and date detection
        super.onDestroy();
    }

    private void tryActivateTimeChangeDetector() {
        if (timeChangeDetector == null) {
            (timeChangeDetector = new TimeChangeDetector().with(this)).start();
        } else {
            timeChangeDetector.pauseOperation();
            timeChangeDetector = null;
        }
    }

    // Because launch mode of this activity is set to single task, this callback will be
    // invoked. When will this be invoked ? It will be invoked when a user clicked on the
    // notification to view a particular fragment, but onCreate won't be invoked if the
    // app is already running but onNewIntent will be invoked so, bring MainActivity to front,
    // and replace fragment to view.
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        doUpdateFragment(intent);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.update) {
            Toast.makeText(this, "No action yet", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("all")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Toast toast = Toast.makeText(this, "No action yet", Toast.LENGTH_LONG);
        switch (menuItem.getItemId()) {
            case R.id.home:
                loadFragment(LandingPageFragment.newInstance());
                break;
            case R.id.courses:
                loadFragment(CoursesFragment.newInstance());
                break;
            case R.id.timetable:
                loadFragment(TimetableFragment.newInstance());
                break;
            case R.id.scheduled_classes:
                loadFragment(ScheduledTimetableFragment.newInstance());
                break;
            case R.id.assignment:
                loadFragment(AssignmentFragment.newInstance());
                break;
            case R.id.exam_timetable:
                loadFragment(ExamFragment.newInstance());
                break;
            case R.id.alarms:
                loadFragment(AlarmHolderFragment.newInstance());
                break;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.whats_new:
                toast.show();
                break;
            case R.id.generate:
                toast.show();
                break;
            case R.id.report:
                toast.show();
                break;
            case R.id.about:
                toast.show();
                break;
        }

        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Get intent actions and update UI
    private void doUpdateFragment(Intent intent) {
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
        } else {
            // Loads landing page on start up.
            loadFragment(LandingPageFragment.newInstance());
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        // transaction.setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit);
        final String TAG = fragment.getClass().getName();
        Fragment fragment1 = manager.findFragmentByTag(TAG);

        if (fragment1 == null) {
            transaction.replace(R.id.frame, fragment, TAG).commit();
        }
    }
}
