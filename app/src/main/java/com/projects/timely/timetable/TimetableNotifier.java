package com.projects.timely.timetable;

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
import android.text.Html;

import com.projects.timely.R;
import com.projects.timely.main.MainActivity;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import androidx.core.app.NotificationCompat;
import androidx.core.text.HtmlCompat;
import androidx.preference.PreferenceManager;

import static com.projects.timely.timetable.DaysFragment.ARG_CLASS;
import static com.projects.timely.timetable.DaysFragment.ARG_DAY;
import static com.projects.timely.timetable.DaysFragment.ARG_PAGE_POSITION;
import static com.projects.timely.timetable.DaysFragment.ARG_POSITION;
import static com.projects.timely.timetable.DaysFragment.ARG_TIME;

@SuppressWarnings("ConstantConditions")
public class TimetableNotifier extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String course = intent.getStringExtra(ARG_CLASS);
        String time = intent.getStringExtra(ARG_TIME);

        int day = intent.getIntExtra(ARG_DAY, -1);
        int position = intent.getIntExtra(ARG_POSITION, -1);
        int tabPosition = intent.getIntExtra(ARG_PAGE_POSITION, -1);

        String message = "<b>" + course + "</b> starts in <b>" + "10 minutes</b>";
        CharSequence spannedMessage
                = HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY);

        NotificationManager manager
                = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL = "TimeLY's Timetable";
        String ID = "com.projects.timely.timetable";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                manager.getNotificationChannel(CHANNEL) == null) {
            manager.createNotificationChannel(
                    new NotificationChannel(ID, CHANNEL, NotificationManager.IMPORTANCE_DEFAULT));
        }

        Uri SYSTEM_DEFAULT = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Uri APP_DEFAULT = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(context.getPackageName())
                .path(String.valueOf(R.raw.arpeggio1))
                .build();

        String type = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("Uri Type", "TimeLY's Default");

        final Uri DEFAULT_URI = type.equals("TimeLY's Default") || SYSTEM_DEFAULT == null
                                ? APP_DEFAULT : SYSTEM_DEFAULT;

        Intent contentIntent = new Intent(context, MainActivity.class)
                .setAction("com.projects.timely.timetable");

        PendingIntent contentPI = PendingIntent.getActivity(context, 200, contentIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(spannedMessage))
                .setSmallIcon(R.drawable.ic_table)
                .setContentTitle("Timetable")
                .setContentText(spannedMessage)
                .setSound(DEFAULT_URI)
                .setAutoCancel(true)
                .setContentIntent(contentPI);

        manager.notify(-20, builder.build());
        scheduleFuture(context, time, course, day, position, tabPosition);
    }

    // schedule the next alarm, which will be next week for the day this alarm goes off.
    // Technically, this is a repeating alarm.
    private void scheduleFuture(Context context, String time, String course, int day, int position,
                                int tabPosition) {
        String[] sTime = time.split(":");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, day);
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(sTime[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(sTime[1]));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.setTimeInMillis(calendar.getTimeInMillis() + TimeUnit.DAYS.toMillis(7)
                                         - TimeUnit.MINUTES.toMillis(10));

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent timetableIntent =
                new Intent(context, TimetableNotifier.class)
                        .putExtra(ARG_TIME, time)
                        .putExtra(ARG_CLASS, course)
                        .putExtra(ARG_DAY, day)
                        .putExtra(ARG_POSITION, position)
                        .putExtra(ARG_PAGE_POSITION, tabPosition)
                        .addCategory("com.projects.timely.timetable")
                        .setAction("com.projects.timely.timetable.addAction")
                        .setDataAndType(
                                Uri.parse("content://com.projects.timely.add."
                                                  + calendar.getTimeInMillis()),
                                "com.projects.timely.dataType");

        PendingIntent pi = PendingIntent.getBroadcast(context, 555, timetableIntent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            manager.setExact(AlarmManager.RTC, calendar.getTimeInMillis(), pi);
        else manager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pi);
    }
}