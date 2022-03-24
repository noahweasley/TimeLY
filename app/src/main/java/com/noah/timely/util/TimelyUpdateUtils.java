package com.noah.timely.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.github.javiersantos.appupdater.AppUpdaterUtils;
import com.github.javiersantos.appupdater.enums.AppUpdaterError;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.github.javiersantos.appupdater.objects.Update;
import com.noah.timely.R;
import com.noah.timely.main.App;

import java.util.Locale;

/**
 * Utility class wrapping TimeLY's update service
 */
public class TimelyUpdateUtils {
   private static final int UPDATE_ID = 222;
   private static final String USER = "noahweasley";
   private static final String REPO = "TimeLY";
   private static boolean nPosted;

   public static void checkForUpdates(Context context) {
      AppUpdaterUtils updaterUtils = new AppUpdaterUtils(context);

      if (!nPosted) postNotification(context, "Checking for updates", null);

      updaterUtils.setUpdateFrom(UpdateFrom.GITHUB);
      updaterUtils.setGitHubUserAndRepo(USER, REPO);
      updaterUtils.withListener(new TimelyUpdateListener(context));
      updaterUtils.start();
   }

   private static void dismissNotification(Context context) {
      NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      manager.cancel(UPDATE_ID);
      nPosted = false;
   }

   private static void postNotification(Context context, String updateTitle, Update update) {
      nPosted = true;

      NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

      NotificationCompat.Builder builder = new NotificationCompat.Builder(context, App.GENERAL_CHANNEL_ID);

      Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.app_icon);

      if (updateTitle.equals("Update available")) {
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

         String v = update.getLatestVersion();
         // corresponding link to direct download link which was in sync with the actual build version
         String githubLink = "https://github.com/noahweasley/TimeLY/releases/download/"
                 + "v" + v + "/" + REPO + "_v" + v + ".apk";
         Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
         notificationIntent.setData(Uri.parse(githubLink));
         PendingIntent pi = PendingIntent.getActivity(context, 0, notificationIntent, 0);

         String contentText = String.format(Locale.US, "TimeLY_v%s is out, Click to update", v);

         builder.setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setAutoCancel(true)
                .setContentTitle(updateTitle)
                .setContentText(contentText)
                .setChannelId(App.GENERAL_CHANNEL_ID)
                .setSound(DEFAULT_URI)
                .setSmallIcon(R.drawable.ic_n_upgrade)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLargeIcon(icon)
                .setContentIntent(pi);
      } else {
         builder.setContentTitle(updateTitle)
                .setOngoing(true)
                .setSilent(true)
                .setChannelId(App.GENERAL_CHANNEL_ID)
                .setAutoCancel(false)
                .setSmallIcon(R.drawable.ic_baseline_info_24)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLargeIcon(icon);
      }

      manager.notify(UPDATE_ID, builder.build());
   }

   private static class TimelyUpdateListener implements AppUpdaterUtils.UpdateListener {
      private final Context context;

      public TimelyUpdateListener(Context context) {
         this.context = context;
      }

      @Override
      public void onSuccess(Update update, Boolean isUpdateAvailable) {
         if (isUpdateAvailable) postNotification(context, "Update available", update);
         else dismissNotification(context);
      }

      @Override
      public void onFailed(AppUpdaterError error) {
         dismissNotification(context);
      }

   }

}
