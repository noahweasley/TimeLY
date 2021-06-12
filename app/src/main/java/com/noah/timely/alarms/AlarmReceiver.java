package com.noah.timely.alarms;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.os.ConfigurationCompat;

import com.noah.timely.R;
import com.noah.timely.core.SchoolDatabase;

import java.util.Locale;

import static com.noah.timely.core.AppUtils.isUserPreferred24Hours;

public class AlarmReceiver extends BroadcastReceiver {
    static final int NOTIFICATION_ID = 1189765;
    static final String ID = "Notification ID";
    static final String REPEAT_DAYS = "Alarm Repeat Days";
    public static String ALARM_POS = "com.noah.timely.position";

    @Override
    public void onReceive(Context context, Intent intent) {
        SchoolDatabase database = new SchoolDatabase(context);

        // Handle the alarm and then show the UI to wake the user up, that is, if he/she
        // is asleep :)
        int dataPos = intent.getIntExtra(ALARM_POS, -1); // The position of the alarm

        String action = intent.getStringExtra("action");
        boolean is24 = isUserPreferred24Hours(context);

        String _24H = database.getSnoozedTimeAtInitialPosition(dataPos);

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
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        final String CHANNEL = "TimeLY's alarm";
        final String UNIQUE_ID = "TimeLY's alarm";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && manager.getNotificationChannel(CHANNEL) == null) {

            // Create a notification channel if it doesn't exist yet, so as to abide to the new
            // rule of android api level 26: "All notifications must have a channel"
            manager.createNotificationChannel(
                    new NotificationChannel(UNIQUE_ID,
                                            CHANNEL,
                                            NotificationManager.IMPORTANCE_HIGH));
        }

        String alarmLabel = database.getInitialAlarmLabelAt(dataPos);
        boolean isSnoozeAction = false;

        if (action != null)
            isSnoozeAction = action.equals("snooze");

        Intent viewAlarmIntent = new Intent(context, AlarmActivity.class)
                .putExtra("time", is24 ? _24H : _12H)
                .putExtra(ALARM_POS, dataPos)
                .putExtra("Label", alarmLabel)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (isSnoozeAction) viewAlarmIntent.setAction("Snooze");

        PendingIntent pi = PendingIntent.getActivity(context,
                                                     112,
                                                     viewAlarmIntent,
                                                     PendingIntent.FLAG_UPDATE_CURRENT);

        Intent receiverSnooze = new Intent(context, NotificationActionReceiver.class);
        receiverSnooze.putExtra("action", "Snooze")
                      .putExtra(ID, NOTIFICATION_ID)
                      .putExtra(ALARM_POS, dataPos);

        Intent receiverDismiss = new Intent(context, NotificationActionReceiver.class);
        receiverDismiss.putExtra("action", "Dismiss")
                       .putExtra(ID, NOTIFICATION_ID)
                       .putExtra(ALARM_POS, dataPos);

        PendingIntent actionDismiss = PendingIntent.getBroadcast(context,
                                                                 113,
                                                                 receiverDismiss,
                                                                 PendingIntent.FLAG_ONE_SHOT);
        PendingIntent actionSnooze = PendingIntent.getBroadcast(context,
                                                                114,
                                                                receiverSnooze,
                                                                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL);
        builder.setContentTitle("Alarm for: " + (is24 ? _24H : _12H)
                                        + (isSnoozeAction ? " (Snoozed)" : ""))
               .setContentText(alarmLabel)
               .setSmallIcon(R.drawable.ic_alarm_black)
               .setFullScreenIntent(pi, true)
               .addAction(new NotificationCompat.Action(R.drawable.ic_snooze_black,
                                                        "Snooze",
                                                        actionSnooze))
               .addAction(new NotificationCompat.Action(R.drawable.ic_cancel,
                                                        "Dismiss",
                                                        actionDismiss));

        manager.notify(NOTIFICATION_ID, builder.build());
        // start playing alarm tone using the AlarmNotificationService
        context.startService(new Intent(context, AlarmNotificationService.class)
                                     .putExtra(ID, NOTIFICATION_ID)
                                     .putExtra(ALARM_POS, dataPos));
    }
}
