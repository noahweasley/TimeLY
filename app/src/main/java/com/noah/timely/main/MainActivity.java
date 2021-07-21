package com.noah.timely.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
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

import com.google.android.material.navigation.NavigationView;
import com.noah.timely.R;
import com.noah.timely.about.TimelyBasicInfoDialog;
import com.noah.timely.about.TimelyUpdateInfoDialog;
import com.noah.timely.alarms.AlarmHolderFragment;
import com.noah.timely.alarms.AlarmReSchedulerService;
import com.noah.timely.alarms.TimeChangeDetector;
import com.noah.timely.assignment.AssignmentFragment;
import com.noah.timely.core.SchoolDatabase;
import com.noah.timely.courses.CoursesFragment;
import com.noah.timely.error.ErrorDialog;
import com.noah.timely.exam.ExamFragment;
import com.noah.timely.scheduled.ScheduledTimetableFragment;
import com.noah.timely.settings.SettingsActivity;
import com.noah.timely.timetable.TimetableFragment;
import com.noah.timely.util.Constants;
import com.noah.timely.util.DeviceInfoUtil;
import com.noah.timely.util.PreferenceUtils;
import com.noah.timely.util.TimelyUpdateUtils;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private TimeChangeDetector timeChangeDetector;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // FIXME: 6/7/2021 Remove this section of code when app is released
        startService(new Intent(this, AlarmReSchedulerService.class));

        setContentView(R.layout.activity_main);

        TimelyUpdateUtils.checkForUpdates(this); // check for updates

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NavigationView navView = findViewById(R.id.nav_view);
        drawer = findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        navView.setNavigationItemSelectedListener(this);
        // Set the first viewed fragment on app start
        if (savedInstanceState == null) {
            doUpdateFragment(getIntent()); // update the fragment attached to this activity
        }
        tryActivateTimeChangeDetector(); // start OS time and date detection

        // ignore power management service to trigger alarms properly
        String cancelText = getString(android.R.string.cancel);
        String goText = getString(R.string.go);
        String noticeTitle = getString(R.string.noticeTitle);
        String noticeMessage = getString(R.string.noticeMessage);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                // notify user of their actions to remove restrictions on battery optimizations
                new AlertDialog.Builder(this)
                        .setTitle(noticeTitle)
                        .setMessage(noticeMessage)
                        .setNegativeButton(cancelText, this::requestAction)
                        .setPositiveButton(goText, this::requestAction)
                        .create()
                        .show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestAction(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE)
            startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
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
        tryActivateTimeChangeDetector();
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

        } else if (menuItemId == R.id.alarms) {
//          loadFragment(AlarmHolderFragment.newInstance());
            String message = "This feature is experimental and contains a lot of bugs." +
                    " In a future release, the bugs would be fixed";
            ErrorDialog.Builder errorBuilder = new ErrorDialog.Builder();
            errorBuilder.setShowSuggestions(false)
                        .setDialogMessage(message);

            new ErrorDialog().showErrorMessage(this, errorBuilder.build());

        } else if (menuItemId == R.id.settings) {

            open_upSetting();

        } else if (menuItemId == R.id.whats_new) {

            new TimelyUpdateInfoDialog().show(this);

        }/* else if (menuItemId == R.id.generate) {

            Toast.makeText(this, "No action yet", Toast.LENGTH_LONG).show();

        }*/ else if (menuItemId == R.id.report) {

            new AlertDialog.Builder(this)
                    .setTitle(R.string.report_title)
                    .setMessage(R.string.report_message)
                    .setPositiveButton(R.string.yes, this::reportAction)
                    .setNegativeButton(android.R.string.cancel, this::reportAction)
                    .create().show();

        } else if (menuItemId == R.id.about) {

            new TimelyBasicInfoDialog().show(this);

        }

        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void reportAction(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            // start WhatsApp
            PackageManager packageManager = getPackageManager();
            String whatsappPkgName = "com.whatsapp", gbwhatsappPkgName = "com.gbwhatsapp", devCon = "+2347065478947";
            // send message with emojis
            int waveEmojiUnicode = 0x1F3FC, clapEmojiUnicode = 0x1F44F,
                    faceTongueEmojiUnicode = 0x1F60B, mobilePhoneEmojiUnicode = 0x1F4F1;

            char[] waveEmojiChars = Character.toChars(waveEmojiUnicode);
            char[] clapEmojiChars = Character.toChars(clapEmojiUnicode);
            char[] mobilePhoneEmojiChars = Character.toChars(mobilePhoneEmojiUnicode);
            char[] faceTongueEmojiChars = Character.toChars(faceTongueEmojiUnicode);

            float[] deviceRes = DeviceInfoUtil.getDeviceResolutionDP(this);
            String devSpecs = "\n\n Device specs " + String.valueOf(mobilePhoneEmojiChars) +
                    "\n"
                    + "Api Level: " + Build.VERSION.SDK_INT
                    + "\n"
                    + "Device Screen Density: " + DeviceInfoUtil.getScreenDensity(this)
                    + "\n"
                    + "Screen Resolution (dp) : " + deviceRes[0] + " x " + deviceRes[1];

            String s1 = "Hi Noah ", s2 = ", TimeLY is a nice app ", s3 = ". However, I would like" +
                    " to report a bug .... My name is ... by the way.";

            String message = s1 + String.valueOf(waveEmojiChars) + s2
                    + String.valueOf(clapEmojiChars) + s3 + String.valueOf(faceTongueEmojiChars) + devSpecs;

            String dataString = String.format("https://api.whatsapp.com/send?phone=%s&text=%s", devCon, message);

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(dataString));
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(whatsappPkgName, PackageManager.GET_META_DATA);

                intent.setPackage(whatsappPkgName);
                startActivity(intent);

            } catch (PackageManager.NameNotFoundException e) {
                // if whatsapp not installed, try to open GBWhatsapp
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(gbwhatsappPkgName,
                                                                            PackageManager.GET_META_DATA);
                    intent.setPackage(gbwhatsappPkgName);
                    startActivity(intent);
                } catch (PackageManager.NameNotFoundException nameNotFoundException) {
                    Toast.makeText(this, "Whatsapp not installed", Toast.LENGTH_LONG).show();
                }
            }

        }
        // whatever the button clicked, close dialog
        dialog.cancel();

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

    // open up settings with a custom animation
    private void open_upSetting() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        final String TAG = fragment.getClass().getName();
        Fragment fragment1 = manager.findFragmentByTag(TAG);

        if (fragment1 == null) {
            transaction.replace(R.id.frame, fragment, TAG).commit();
        }
    }
}