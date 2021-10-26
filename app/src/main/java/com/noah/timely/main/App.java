package com.noah.timely.main;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.content.ContextCompat;

import com.noah.timely.R;
import com.noah.timely.util.LogUtils;

public class App extends Application {
   public static final String ASSIGNMENT_CHANNEL = "TimeLY's assignments";
   public static final String ASIGNMENT_CHANNEL_ID = "com.noah.timely.assignments";
   public static final String ALARMS_CHANNEL = "TimeLY's study alarm";
   public static final String ALARMS_CHANNEL_ID = "com.noah.timely.alarms";
   public static final String SCHEDULED_TIMETABLE_CHANNEL = "TimeLY's Scheduled Classes";
   public static final String SCHEDULED_TIMETABLE_CHANNEL_ID = "com.noah.timely.scheduled";
   public static final String TIMETABLE_CHANNEL = "TimeLY's Timetable";
   public static final String TIMETABLE_CHANNEL_ID = "com.noah.timely.timetable";
   public static final String GENERAL_CHANNEL = "Miscellaneous";
   public static final String GENERAL_CHANNEL_ID = "com.noah.timely";

   @Override
   public void onCreate() {
      super.onCreate();
      createNotificationChannels();
   }

   private void createNotificationChannels() {
      LogUtils.debug(this, "Creating notification channels");

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

         NotificationManager mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
         NotificationChannel channel1 = new NotificationChannel(ASIGNMENT_CHANNEL_ID, ASSIGNMENT_CHANNEL,
                                                                NotificationManager.IMPORTANCE_DEFAULT);
         channel1.setLightColor(ContextCompat.getColor(this, android.R.color.holo_purple));
         mgr.createNotificationChannel(channel1);

         NotificationChannel channel2 = new NotificationChannel(ALARMS_CHANNEL_ID, ALARMS_CHANNEL,
                                                                NotificationManager.IMPORTANCE_HIGH);
         channel1.setLightColor(ContextCompat.getColor(this, android.R.color.holo_orange_light));
         mgr.createNotificationChannel(channel2);

         NotificationChannel channel3 = new NotificationChannel(SCHEDULED_TIMETABLE_CHANNEL_ID,
                                                                SCHEDULED_TIMETABLE_CHANNEL,
                                                                NotificationManager.IMPORTANCE_DEFAULT);
         channel1.setLightColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
         mgr.createNotificationChannel(channel3);

         NotificationChannel channel4 = new NotificationChannel(TIMETABLE_CHANNEL_ID, TIMETABLE_CHANNEL,
                                                                NotificationManager.IMPORTANCE_DEFAULT);
         channel1.setLightColor(ContextCompat.getColor(this, android.R.color.holo_red_light));
         mgr.createNotificationChannel(channel4);

         NotificationChannel channel5 = new NotificationChannel(GENERAL_CHANNEL_ID, GENERAL_CHANNEL,
                                                                NotificationManager.IMPORTANCE_DEFAULT);
         channel5.setDescription(getString(R.string.miscellaneous_channel_desc));
         mgr.createNotificationChannel(channel5);
      }
   }

}
