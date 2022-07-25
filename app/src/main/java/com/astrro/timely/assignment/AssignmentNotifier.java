package com.astrro.timely.assignment;

import static com.astrro.timely.assignment.AssignmentFragment.LECTURER_NAME;
import static com.astrro.timely.assignment.AssignmentFragment.TITLE;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.preference.PreferenceManager;

import com.astrro.timely.R;
import com.astrro.timely.main.App;
import com.astrro.timely.main.SchoolUtilitesActivity;

abstract class AssignmentNotifier extends BroadcastReceiver {

   @Override
   public void onReceive(Context context, Intent intent) {
      String lecturer = intent.getStringExtra(LECTURER_NAME);
      String title = intent.getStringExtra(TITLE);

      Uri SYSTEM_DEFAULT = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
      Uri APP_DEFAULT = new Uri.Builder()
              .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
              .authority(context.getPackageName())
              .path(String.valueOf(R.raw.arpeggio1))
              .build();

      String type = PreferenceManager.getDefaultSharedPreferences(context)
              .getString("Uri Type", "TimeLY's Default");

      final Uri DEFAULT_URI = type.equals("TimeLY's Default") || SYSTEM_DEFAULT == null ? APP_DEFAULT
                                                                                        : SYSTEM_DEFAULT;

      // random ID is used so that an instance of assignment notification won't be updated,
      // instead another notification is sent
      final int NOTIFY_ID = (int) (Math.random() * Integer.MAX_VALUE);
      Intent viewIntent = new Intent(context, SchoolUtilitesActivity.class).setAction("com.astrro.timely.assignments");

      PendingIntent viewPI = PendingIntent.getActivity(context, 111, viewIntent, 0);
      // send a notification as a reminder
      NotificationManagerCompat mgr = NotificationManagerCompat.from(context);

      NotificationCompat.Builder notifier = new NotificationCompat.Builder(context, App.ASIGNMENT_CHANNEL_ID);

      String message
              = "<b>" + lecturer + "'s</b> assignment on " + "<b>" + title + "</b>" + "," +
              " will be submitted <b>" + getDay() + "</b>";

      CharSequence spannedMessage = HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY);

      notifier.setContentTitle("Assignment Reminder")
              .setStyle(new NotificationCompat.BigTextStyle().bigText(spannedMessage))
              .setContentText(spannedMessage)
              .setSound(DEFAULT_URI)
              .setAutoCancel(true)
              .setPriority(NotificationCompat.PRIORITY_DEFAULT)
              .setSmallIcon(R.drawable.ic_n_assignment)
              .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
              .setContentIntent(viewPI);

      mgr.notify(NOTIFY_ID, notifier.build());

   }

   /**
    * Override this method to set the day at which the assignment should be submitted. This will
    * be used to display a notification
    *
    * @return the day at which the assignment is to be submitted
    */
   abstract String getDay();
}
