package com.noah.timely.scheduled;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static com.noah.timely.scheduled.AddScheduledDialog.ARG_COURSE;
import static com.noah.timely.scheduled.AddScheduledDialog.ARG_DAY;
import static com.noah.timely.scheduled.AddScheduledDialog.ARG_TIME;

import android.app.AlarmManager;
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

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.preference.PreferenceManager;

import com.noah.timely.R;
import com.noah.timely.main.MainActivity;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ConstantConditions")
public class ScheduledTaskNotifier extends BroadcastReceiver {

   @Override
   public void onReceive(Context context, Intent intent) {
      String course = intent.getStringExtra(ARG_COURSE);
      String time = intent.getStringExtra(ARG_TIME);
      int calendarDay = intent.getIntExtra(ARG_DAY, -1);

      NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
      String CHANNEL = "TimeLY's Scheduled Classes";
      String ID = "com.noah.timely.scheduled";

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager.getNotificationChannel(CHANNEL) == null) {
         manager.createNotificationChannel(new NotificationChannel(ID, CHANNEL,
                 NotificationManager.IMPORTANCE_DEFAULT));
      }

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

      Intent sIntent = new Intent(context, MainActivity.class).setAction("com.noah.timely.scheduled");
      PendingIntent pi = PendingIntent.getActivity(context, 1156, sIntent, PendingIntent.FLAG_UPDATE_CURRENT);
      // Notification message
      String message = "You have a scheduled class, <b>" + course + "</b> in <b>10 minutes</b>";
      CharSequence spannedMessage = HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY);

      NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL);
      builder.setStyle(new NotificationCompat.BigTextStyle().bigText(spannedMessage))
              .setContentTitle("Scheduled class reminder")
              .setContentText(spannedMessage)
              .setAutoCancel(true)
              .setSmallIcon(R.drawable.ic_n_schedule)
              .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
              .setSound(DEFAULT_URI)
              .setContentIntent(pi);
      manager.notify(-299, builder.build());

      scheduleFuture(context, time, course, calendarDay);     // schedule next alarm
   }

   private void scheduleFuture(Context context, String time, String courseCode, int day) {
      String[] sTime = time.split(":");

      Calendar calendar = Calendar.getInstance();
      calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(sTime[0]));
      calendar.set(Calendar.MINUTE, Integer.parseInt(sTime[1]));
      calendar.set(Calendar.DAY_OF_WEEK, day);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);
      calendar.setTimeInMillis(calendar.getTimeInMillis() - TimeUnit.MINUTES.toMillis(10));

      long calendarTime = calendar.getTimeInMillis();
      long triggerTime = calendarTime < System.currentTimeMillis() ? calendarTime + TimeUnit.DAYS.toMillis(7)
                                                                   : calendarTime;

      AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

      Intent taskIntent = new Intent(context, ScheduledTaskNotifier.class)
              .putExtra(ARG_TIME, time)
              .putExtra(ARG_COURSE, courseCode)
              .putExtra(ARG_DAY, day)
              .addCategory("com.noah.timely.scheduled")
              .setAction("com.noah.timely.scheduled.addAction")
              .setDataAndType(Uri.parse("content://com.noah.timely.scheduled.add." + triggerTime),
                      "com.noah.timely.scheduled.dataType");

      PendingIntent pi = PendingIntent.getBroadcast(context, 1156, taskIntent, 0);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
         manager.setExact(AlarmManager.RTC, triggerTime, pi);
      manager.set(AlarmManager.RTC, triggerTime, pi);
   }
}