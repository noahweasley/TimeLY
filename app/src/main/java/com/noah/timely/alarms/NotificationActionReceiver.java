package com.noah.timely.alarms;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.view.Gravity;
import android.widget.Toast;

import androidx.core.os.ConfigurationCompat;
import androidx.preference.PreferenceManager;

import com.noah.timely.R;
import com.noah.timely.core.PositionMessageEvent;
import com.noah.timely.core.SchoolDatabase;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.noah.timely.alarms.AlarmReceiver.ALARM_POS;
import static com.noah.timely.alarms.AlarmReceiver.ID;

public class NotificationActionReceiver extends BroadcastReceiver {
    private SchoolDatabase database;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onReceive(Context context, Intent intent) {
        database = new SchoolDatabase(context); // The apps data

        String action = intent.getStringExtra("action");

        if (action != null && action.equals("Snooze")) {
            // Get the amount of time (in minutes) to snooze the alarm when the user
            // is probably lazy, like me :), and wants to keep on sleeping at least for
            // a little while.
            SharedPreferences preferences
                    = PreferenceManager.getDefaultSharedPreferences(context);
            final int snoozeTime = Integer.parseInt(preferences.getString("snooze_time", "5"));

            String message
                    = "Snoozing for " + snoozeTime + " minute" + (snoozeTime > 1 ? "s" : "");

            snoozeAlarmFor(snoozeTime, intent, context); // now perform the snooze operation proper

            int yOffset = context.getResources().getInteger(R.integer.toast_y_offset);
            Toast alert = Toast.makeText(context, message, Toast.LENGTH_LONG);
            alert.setGravity(Gravity.CENTER_HORIZONTAL, 0, yOffset);
            alert.show();

        } else {
            // When alarm is dismissed, update database to reflect the presence of
            // the alarm. By alarm presence, I meant that the pending intent has been fired
            int dataPos = intent.getIntExtra(ALARM_POS, -1);
            database.updateAlarmStateFromInitialPosition(dataPos, false);
            EventBus.getDefault().post(new PositionMessageEvent(dataPos));
        }

        // after any action close the notification tray
        NotificationManager manager
                = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(intent.getIntExtra(ID, -1));
        // After everything, stop service
        context.stopService(new Intent(context, AlarmNotificationService.class));
        EventBus.getDefault().post(new MessageEvent());

    }

    private void snoozeAlarmFor(int snoozeTime, Intent intent, Context context) {
        long snoozeTimeInMillis = TimeUnit.MINUTES.toMillis(snoozeTime);

        int dataPos = intent.getIntExtra(ALARM_POS, -1);
        // retrieve the data stored at the requested database position
        AlarmModel model = (AlarmModel) database.getAlarmAt(dataPos);
        String label = model.getLabel();
        String RTime = model.getTime();
        // Now set the alarm
        Intent alarmReceiverIntent = new Intent(context, AlarmReceiver.class);
        alarmReceiverIntent.putExtra(ALARM_POS, dataPos);
        alarmReceiverIntent.putExtra("Time", RTime);
        alarmReceiverIntent.putExtra("Label", label.equals("Label") ? null : label);
        alarmReceiverIntent.putExtra("action", "snooze");

        // Set new alarm (snoozed) based on the system clock + the snooze time
        long time = System.currentTimeMillis() + snoozeTimeInMillis;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        // Remove the extra seconds and milliseconds before alarm trigger
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long rTime = calendar.getTimeInMillis();

        alarmReceiverIntent.addCategory("com.noah.timely.alarm.category");
        alarmReceiverIntent.setAction("com.noah.timely.alarm.cancel");
        Uri data = Uri.parse("content://com.noah.timely/Alarms/alarm" + rTime);
        alarmReceiverIntent.setDataAndType(data, "com.noah.timely.alarm.dataType");

        AlarmManager alarmManager
                = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmPI = PendingIntent.getBroadcast(context,
                                                           1189765,
                                                           alarmReceiverIntent,
                                                           PendingIntent.FLAG_CANCEL_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // alarm has to be triggered even when device is in idle or doze mode.
            // This alarm is very important
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, rTime, alarmPI);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, rTime, alarmPI);
            }
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, rTime, alarmPI);
        }

        Configuration config = context.getResources().getConfiguration();
        Locale currentLocale = ConfigurationCompat.getLocales(config).get(0);

        SimpleDateFormat timeFormat24 = new SimpleDateFormat("HH:mm", currentLocale);

        // after snoozing alarm, still update it's snooze status and time in database
        database.updateSnoozedTime(dataPos, timeFormat24.format(calendar.getTime()));
    }

}