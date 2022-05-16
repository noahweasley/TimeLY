package com.astrro.timely.alarms;

import static com.astrro.timely.util.MiscUtil.isUserPreferred24Hours;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.os.ConfigurationCompat;

import com.astrro.timely.R;
import com.astrro.timely.core.SchoolDatabase;
import com.astrro.timely.main.App;

import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver {
   public static String ALARM_POS = "com.astrro.timely.position";
   static final int NOTIFICATION_ID = 11789;
   static final String ID = "Notification ID";
   static final String REPEAT_DAYS = "Alarm Repeat Days";

   @Override
   public void onReceive(Context context, Intent intent) {
      SchoolDatabase database = new SchoolDatabase(context);
      // Handle the alarm and then show the UI to wake the user up, that is, if he/she is asleep :)
      int dataPos = intent.getIntExtra(ALARM_POS, -1); // The position of the alarm

      String action = intent.getStringExtra("action");
      boolean is24 = isUserPreferred24Hours(context);

      String _24H = database.getSnoozedTimeAtPosition(dataPos);

      String[] time = _24H.split(":");
      int hh = Integer.parseInt(time[0]);
      int mm = Integer.parseInt(time[1]);

      Resources aResources = context.getResources();
      Configuration config = aResources.getConfiguration();
      Locale locale = ConfigurationCompat.getLocales(config).get(0);

      String formattedHrAM = String.format(locale, "%02d", (hh == 0 ? 12 : hh));
      String formattedHrPM = String.format(locale, "%02d", (hh % 12 == 0 ? 12 : hh % 12));
      String formattedMinAM = String.format(locale, "%02d", mm) + " AM";
      String formattedMinPM = String.format(locale, "%02d", mm) + " PM";

      boolean isAM = hh >= 0 && hh < 12;
      String _12H = isAM ? formattedHrAM + ":" + formattedMinAM
                         : formattedHrPM + ":" + formattedMinPM;


      String alarmLabel = database.getInitialAlarmLabelAt(dataPos);
      boolean isSnoozeAction = false;

      if (action != null) isSnoozeAction = action.equals("snooze");

      Intent viewAlarmIntent = new Intent(context, AlarmActivity.class)
              .putExtra("time", is24 ? _24H : _12H)
              .putExtra(ALARM_POS, dataPos)
              .putExtra("Label", alarmLabel)
              .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      if (isSnoozeAction) viewAlarmIntent.setAction("Snooze");

      PendingIntent pi = PendingIntent.getActivity(context, 112, viewAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

      Intent receiverSnooze = new Intent(context, NotificationActionReceiver.class);
      receiverSnooze.putExtra("action", "Snooze")
                    .putExtra(ID, NOTIFICATION_ID)
                    .putExtra(ALARM_POS, dataPos);

      Intent receiverDismiss = new Intent(context, NotificationActionReceiver.class);
      receiverDismiss.putExtra("action", "Dismiss")
                     .putExtra(ID, NOTIFICATION_ID)
                     .putExtra(ALARM_POS, dataPos);

      PendingIntent actionDismiss =
              PendingIntent.getBroadcast(context, 113, receiverDismiss, PendingIntent.FLAG_ONE_SHOT);

      PendingIntent actionSnooze =
              PendingIntent.getBroadcast(context, 114, receiverSnooze, PendingIntent.FLAG_ONE_SHOT);

      NotificationManagerCompat manager = NotificationManagerCompat.from(context);

      NotificationCompat.Builder builder = new NotificationCompat.Builder(context, App.ALARMS_CHANNEL_ID);
      builder.setContentTitle("Alarm for: " + (is24 ? _24H : _12H) + (isSnoozeAction ? " (Snoozed)" : ""))
             .setContentText(alarmLabel)
             .setSmallIcon(R.drawable.ic_n_alarm)
             .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
             .setContentIntent(pi)
             .setPriority(NotificationCompat.PRIORITY_HIGH)
             .setSilent(true)
             .setFullScreenIntent(pi, true)
             .addAction(new NotificationCompat.Action(R.drawable.ic_n_snooze, "Snooze", actionSnooze))
             .addAction(new NotificationCompat.Action(R.drawable.ic_n_cancel, "Dismiss", actionDismiss));

      manager.notify(NOTIFICATION_ID, builder.build());
      // start playing alarm tone using the AlarmNotificationService
      context.startService(new Intent(context, AlarmNotificationService.class)
                                   .putExtra(ID, NOTIFICATION_ID)
                                   .putExtra(ALARM_POS, dataPos));
   }
}