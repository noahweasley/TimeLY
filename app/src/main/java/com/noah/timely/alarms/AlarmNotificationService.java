package com.noah.timely.alarms;

import static com.noah.timely.alarms.AlarmReceiver.ALARM_POS;
import static com.noah.timely.alarms.AlarmReceiver.ID;
import static com.noah.timely.alarms.AlarmReceiver.NOTIFICATION_ID;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Process;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;

import androidx.preference.PreferenceManager;

import com.noah.timely.R;
import com.noah.timely.core.SchoolDatabase;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

public class AlarmNotificationService extends Service implements Runnable {
    private static int notificationID;
    private MediaPlayer alarmRingtonePlayer;
    private Vibrator vibrator;
    private NotificationManager manager;
    private SchoolDatabase database;
    private int alarmPos = -1;
    private Thread worker;
    private SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        database = new SchoolDatabase(getApplicationContext());
        manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        preferences = PreferenceManager.getDefaultSharedPreferences(AlarmNotificationService.this);
    }

    @Override
    @SuppressWarnings("deprecation")
    public int onStartCommand(Intent intent, int flags, int startId) {
        Context aCtxt = getApplicationContext();
        alarmPos = intent.getIntExtra(ALARM_POS, -1);
        String uriString = database.getRingtoneURIAt(alarmPos);
        boolean isAlarmVibrate = database.getAlarmVibrateStatus(alarmPos);

        if (!TextUtils.isEmpty(uriString)) {
            // Use this if alarm ringtone was set
            Uri ringtoneURI = Uri.parse(uriString);
            if (ringtoneURI != null) alarmRingtonePlayer = MediaPlayer.create(aCtxt, ringtoneURI);

        } else {
            // Fallback to the default alarm ringtone uri when user didn't set a new ringtone to
            // be played when alarm goes off. But if there is no default ringtone, play silent
            Uri SYSTEM_DEFAULT = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            Uri APP_DEFAULT = new Uri.Builder()
                    .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                    .authority(aCtxt.getPackageName())
                    .path(String.valueOf(R.raw.swinging))
                    .build();

            String type = PreferenceManager.getDefaultSharedPreferences(aCtxt)
                                           .getString("Alarm Ringtone", "TimeLY's Default");

            final Uri DEFAULT_URI = type.equals("TimeLY's Default") || SYSTEM_DEFAULT == null ? APP_DEFAULT
                                                                                              : SYSTEM_DEFAULT;

            alarmRingtonePlayer = new MediaPlayer();

            try {

                alarmRingtonePlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                alarmRingtonePlayer.setDataSource(this, DEFAULT_URI);
                alarmRingtonePlayer.setLooping(true); // repeatedly play alarm tone
                alarmRingtonePlayer.prepare();
                alarmRingtonePlayer.start();

            } catch (IOException ignored) { }

        }

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (isAlarmVibrate) {

            final int DELAY = 0, VIBRATE = 1000, SLEEP = 1000, START = 0;
            long[] vibratePattern = {DELAY, VIBRATE, SLEEP};

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(vibratePattern, START));
            } else {
                // backward compatibility for Android API < 26
                // noinspection deprecation
                vibrator.vibrate(vibratePattern, START);
            }
        }

        notificationID = intent.getIntExtra(ID, NOTIFICATION_ID); // get id of notification to cancel

        worker = new Thread(this);
        worker.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        worker.interrupt(); // Stop waiting for user action
        worker = null;
        stopNotificationAlert();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not used for anything, so return null
    }

    private void stopNotificationAlert() {
        alarmRingtonePlayer.stop();
        alarmRingtonePlayer.release();
        vibrator.cancel();
        // Remove the notification from the shade
        if (manager == null)
            manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        manager.cancel(notificationID);
        EventBus.getDefault().post(new MessageEvent());
    }

    // Worker thread for idle alarm action
    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        try {
            Thread.sleep(59990);
        } catch (InterruptedException e) {
            return;
        }
        String ss = preferences.getString("snoozeOnStop", "Snooze");

        if (ss.equals("Snooze")) sendBroadcast(new Intent(this, NotificationActionReceiver.class)
                                                       .putExtra("action", "Snooze")
                                                       .putExtra(ID, NOTIFICATION_ID)
                                                       .putExtra(ALARM_POS, alarmPos));

        else sendBroadcast(new Intent(this, NotificationActionReceiver.class)
                                   .putExtra("action", "Dismiss")
                                   .putExtra(ID, NOTIFICATION_ID)
                                   .putExtra(ALARM_POS, alarmPos));
        stopSelf();
    }

}
