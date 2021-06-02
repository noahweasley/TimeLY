package com.projects.timely.assignment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.text.HtmlCompat;
import androidx.preference.PreferenceManager;

import com.projects.timely.R;
import com.projects.timely.main.MainActivity;

import static com.projects.timely.assignment.AssignmentFragment.LECTURER_NAME;
import static com.projects.timely.assignment.AssignmentFragment.TITLE;

@SuppressWarnings("ConstantConditions")
abstract class AssignmentNotifier extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctxt, Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                // Reset the alarm here.
                Log.d(getClass().getSimpleName(), "Boot completed");
            }
        } else {
            String lecturer = intent.getStringExtra(LECTURER_NAME);
            String title = intent.getStringExtra(TITLE);

            // send a notification as a reminder
            NotificationManager mgr
                    = (NotificationManager) ctxt.getSystemService(Context.NOTIFICATION_SERVICE);
            final String CHANNEL = "TimeLY's assignments";
            final String ID = "com.projects.timely.assignments";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                    mgr.getNotificationChannel(CHANNEL) == null) {
                mgr.createNotificationChannel(
                        new NotificationChannel(ID, CHANNEL,
                                                NotificationManager.IMPORTANCE_DEFAULT));
            }

            Uri SYSTEM_DEFAULT = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Uri APP_DEFAULT = new Uri.Builder()
                    .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                    .authority(ctxt.getPackageName())
                    .path(String.valueOf(R.raw.arpeggio1))
                    .build();

            String type = PreferenceManager.getDefaultSharedPreferences(ctxt)
                                           .getString("Uri Type", "TimeLY's Default");

            final Uri DEFAULT_URI = type.equals("TimeLY's Default") || SYSTEM_DEFAULT == null
                                    ? APP_DEFAULT : SYSTEM_DEFAULT;

            // random ID is used so that an instance of assignment notification won't be updated,
            // instead another notification is sent
            final int NOTIFY_ID = (int) (Math.random() * Integer.MAX_VALUE);
            Intent viewIntent = new Intent(ctxt, MainActivity.class)
                    .setAction("com.projects.timely.assignments");

            PendingIntent viewPI = PendingIntent.getActivity(ctxt, 111, viewIntent, 0);

            NotificationCompat.Builder notifier = new NotificationCompat.Builder(ctxt, CHANNEL);

            String message
                    = "<b>" + lecturer + "'s</b> assignment on " + "<b>" + title + "</b>" + "," +
                    " will be submitted <b>" + getDay() + "</b>";

            CharSequence spannedMessage
                    = HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY);

            notifier.setContentTitle("Assignment Reminder")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(spannedMessage))
                    .setContentText(spannedMessage)
                    .setSound(DEFAULT_URI)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_assignment)
                    .setContentIntent(viewPI);

            mgr.notify(NOTIFY_ID, notifier.build());
        }
    }

    /**
     * Override this method to set the day at which the assignment should be submitted. This will
     * be used to display a notification
     *
     * @return the day at which the assignment is to be submitted
     */
    abstract String getDay();
}
