package com.astrro.timely.timetable;

import static com.astrro.timely.timetable.DaysFragment.ARG_CLASS;
import static com.astrro.timely.timetable.DaysFragment.ARG_DAY;
import static com.astrro.timely.timetable.DaysFragment.ARG_PAGE_POSITION;
import static com.astrro.timely.timetable.DaysFragment.ARG_POSITION;
import static com.astrro.timely.timetable.DaysFragment.ARG_TIME;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.preference.PreferenceManager;

import com.astrro.timely.R;
import com.astrro.timely.main.App;
import com.astrro.timely.main.SchoolUtilitesActivity;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class TimetableNotifier extends BroadcastReceiver {

   @Override
   public void onReceive(Context context, Intent intent) {

      String course = intent.getStringExtra(ARG_CLASS);
      String time = intent.getStringExtra(ARG_TIME);

      int day = intent.getIntExtra(ARG_DAY, -1);
      int position = intent.getIntExtra(ARG_POSITION, -1);
      int tabPosition = intent.getIntExtra(ARG_PAGE_POSITION, -1);

      String message = "<b>" + course + "</b> starts in <b>10 minutes</b>";
      CharSequence spannedMessage = HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY);

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

      Intent contentIntent = new Intent(context, SchoolUtilitesActivity.class).setAction("com.astrro.timely.timetable");

      PendingIntent contentPI = PendingIntent.getActivity(context, 200, contentIntent, 0);

      NotificationManagerCompat manager = NotificationManagerCompat.from(context);

      NotificationCompat.Builder builder = new NotificationCompat.Builder(context, App.TIMETABLE_CHANNEL_ID);
      builder.setStyle(new NotificationCompat.BigTextStyle().bigText(spannedMessage))
             .setSmallIcon(R.drawable.ic_n_table)
             .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
             .setContentTitle("Timetable")
             .setPriority(NotificationCompat.PRIORITY_DEFAULT)
             .setContentText(spannedMessage)
             .setSound(DEFAULT_URI)
             .setAutoCancel(true)
             .setContentIntent(contentPI);

      manager.notify(-20, builder.build());
      scheduleFuture(context, time, course, day, position, tabPosition);
   }

   // schedule the next alarm, which will be next week for the day this alarm goes off.
   // Technically, this is a repeating alarm.
   private void scheduleFuture(Context context, String time, String course, int day, int position, int tabPosition) {
      String[] sTime = time.split(":");
      Calendar calendar = Calendar.getInstance();
      calendar.set(Calendar.DAY_OF_WEEK, day);
      calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(sTime[0]));
      calendar.set(Calendar.MINUTE, Integer.parseInt(sTime[1]));
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);
      calendar.setTimeInMillis(calendar.getTimeInMillis() + TimeUnit.DAYS.toMillis(7) - TimeUnit.MINUTES.toMillis(10));

      AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

      Intent timetableIntent =
              new Intent(context, TimetableNotifier.class)
                      .putExtra(ARG_TIME, time)
                      .putExtra(ARG_CLASS, course)
                      .putExtra(ARG_DAY, day)
                      .putExtra(ARG_POSITION, position)
                      .putExtra(ARG_PAGE_POSITION, tabPosition)
                      .addCategory("com.astrro.timely.timetable")
                      .setAction("com.astrro.timely.timetable.addAction")
                      .setDataAndType(Uri.parse("content://com.astrro.timely.add." + calendar.getTimeInMillis()),
                                      "com.astrro.timely.dataType");

      PendingIntent pi = PendingIntent.getBroadcast(context, 555, timetableIntent, 0);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
         manager.setExactAndAllowWhileIdle(AlarmManager.RTC, calendar.getTimeInMillis(), pi);
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
         manager.setExact(AlarmManager.RTC, calendar.getTimeInMillis(), pi);
      } else {
         manager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pi);
      }
   }
}
