package com.noah.timely.settings;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.noah.timely.R;
import com.noah.timely.alarms.TimeChangeDetector;
import com.noah.timely.assignment.LayoutRefreshEvent;
import com.noah.timely.core.Time;
import com.noah.timely.core.TimeRefreshEvent;

import org.greenrobot.eventbus.EventBus;

@SuppressWarnings("ConstantConditions")
public class SettingsActivity extends AppCompatActivity {
   private static boolean onStart = true;

   @Override
   public void onCreate(Bundle state) {
      super.onCreate(state);
      setContentView(R.layout.activity_settings);
      Toolbar toolbar = findViewById(R.id.s_toolbar);
      setSupportActionBar(toolbar);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      getSupportActionBar().setTitle("Preferences");
      getSupportFragmentManager()
              .beginTransaction()
              .replace(R.id.settings_container, new SettingsFragment())
              .commit();
   }

   @Override
   public boolean onSupportNavigateUp() {
      onBackPressed();
      return true;
   }

   @Override
   public void onBackPressed() {
      super.onBackPressed();
   }

   public static class SettingsFragment extends PreferenceFragmentCompat
           implements Preference.OnPreferenceChangeListener {

      @SuppressWarnings("FieldCanBeLocal")
      private Preference pref_EnableNotifications, pref_TimeFormat, pref_eTone,
              pref_DateFormat, pref_SnoozeTime, pref_snoozeOnStop, pref_azz_date_format,
              pref_uriType, pref_weekNum, pref_ringtoneType, pref_prefer_dialog;

      @Override
      public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
         setPreferencesFromResource(R.xml.preferences, rootKey);
         pref_DateFormat = findPreference("date_format");
         pref_EnableNotifications = findPreference("enable_notifications");
         pref_eTone = findPreference("enable alerts");
         pref_SnoozeTime = findPreference("snooze_time");
         pref_TimeFormat = findPreference("time_format");
         pref_snoozeOnStop = findPreference("snoozeOnStop");
         pref_uriType = findPreference("Uri Type");
         pref_weekNum = findPreference("exam weeks");
         pref_ringtoneType = findPreference("Alarm Ringtone");
         pref_prefer_dialog = findPreference("prefer_dialog");
         pref_azz_date_format = findPreference("a_date_format");

         pref_DateFormat.setOnPreferenceChangeListener(this);
         pref_EnableNotifications.setOnPreferenceChangeListener(this);
         pref_SnoozeTime.setOnPreferenceChangeListener(this);
         pref_eTone.setOnPreferenceChangeListener(this);
         pref_TimeFormat.setOnPreferenceChangeListener(this);
         pref_snoozeOnStop.setOnPreferenceChangeListener(this);
         pref_uriType.setOnPreferenceChangeListener(this);
         pref_weekNum.setOnPreferenceChangeListener(this);
         pref_ringtoneType.setOnPreferenceChangeListener(this);
         pref_prefer_dialog.setOnPreferenceChangeListener(this);
         pref_azz_date_format.setOnPreferenceChangeListener(this);

         initialize();
      }

      @Override
      public void onDestroyView() {
         onStart = true;
         super.onDestroyView();
      }

      private void initialize() {
         String prefValue = String.valueOf(pref_EnableNotifications
                 .getSharedPreferences()
                 .getBoolean(pref_EnableNotifications.getKey(), true));
         updatePreferenceSummary(pref_EnableNotifications, prefValue);

         prefValue = String.valueOf(pref_TimeFormat.getSharedPreferences()
                 .getBoolean(pref_TimeFormat.getKey(), true));
         updatePreferenceSummary(pref_TimeFormat, prefValue);

         prefValue = pref_DateFormat.getSharedPreferences().getString(pref_DateFormat.getKey(), "Medium");
         updatePreferenceSummary(pref_DateFormat, prefValue);

         prefValue = pref_SnoozeTime.getSharedPreferences().getString(pref_SnoozeTime.getKey(), "5");
         updatePreferenceSummary(pref_SnoozeTime, prefValue);
         prefValue = pref_snoozeOnStop.getSharedPreferences().getString(pref_snoozeOnStop.getKey(), "Snooze");
         updatePreferenceSummary(pref_snoozeOnStop, prefValue);

         prefValue = pref_uriType.getSharedPreferences().getString(pref_uriType.getKey(), "TimeLY's Default");
         updatePreferenceSummary(pref_uriType, prefValue);

         prefValue = pref_weekNum.getSharedPreferences().getString(pref_weekNum.getKey(), "8");
         updatePreferenceSummary(pref_weekNum, prefValue);

         prefValue = pref_ringtoneType.getSharedPreferences()
                 .getString(pref_ringtoneType.getKey(), "TimeLY's Default");
         updatePreferenceSummary(pref_ringtoneType, prefValue);

         String ddf = getContext().getString(R.string.default_date_format);
         prefValue = pref_azz_date_format.getSharedPreferences().getString("a_date_format", ddf);
         updatePreferenceSummary(pref_azz_date_format, prefValue);

         onStart = false;
      }

      private void updatePreferenceSummary(Preference pref, String state) {
         if (!TextUtils.isEmpty(state)) {
            switch (pref.getKey()) {
               case "enable_notifications": {
                  String append = "Notifications are ";
                  String realNewValue = Boolean.parseBoolean(state) ? "ON" : "OFF";
                  pref.setSummary(append + realNewValue);
               }
               break;
               case "time_format": {
                  boolean b_state = Boolean.parseBoolean(state);
                  String append
                          = "Turning this " + (b_state ? "OFF" : "ON") + " will ensure that "
                          + (b_state ? "12" : "24") + "-hours format is used";
                  pref.setSummary(append);
                  if (!onStart) EventBus.getDefault().post(new LayoutRefreshEvent());
                  performTimeRefresh();
               }
               break;
               case "date_format":
                  pref.setSummary(state);
                  performTimeRefresh();
                  break;
               case "snooze_time": {
                  pref.setSummary("All alarms will be snoozed for " + state + " minute"
                          + (state.equals("1") ? "" : "s"));
               }
               break;
               case "exam weeks": {
                  pref.setSummary(state);
                  if (!onStart) {
                     Toast message = Toast.makeText(getContext(),
                             R.string.change_notification,
                             Toast.LENGTH_LONG);
                     message.setGravity(Gravity.CENTER, 0, 0);
                     message.show();
                  }
               }
               break;
               case "snoozeOnStop":
               case "Alarm Ringtone":
               case "a_date_format": {
                  pref.setSummary(state);
                  if (!onStart) EventBus.getDefault().post(new LayoutRefreshEvent());
               }
               break;
               case "Uri Type": {
                  pref.setSummary(state);
               }
               break;
            }
         }
      }

      // Perform time refresh
      private void performTimeRefresh() {
         Time time = TimeChangeDetector.requestImmediateTime(getContext());
         Log.d(getClass().getSimpleName(), "Use 24 hours: " + time.isMilitaryTime());
         EventBus.getDefault().post(new TimeRefreshEvent());
         EventBus.getDefault().post(time);
      }

      @Override
      public boolean onPreferenceChange(@NonNull Preference preference, Object value) {
         updatePreferenceSummary(preference, value.toString());
         return true;
      }
   }

}
