package com.noah.timely.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.RawRes;
import androidx.preference.PreferenceManager;

import com.noah.timely.R;

import java.io.IOException;

/**
 * This is just a simple class to hold all variables that can be assessed by all the classes in the program. It is used
 * so that in no way would the Context classes in <strong>Android</strong> be assessed through any other means other
 * than how it was meant to be assessed to prevent <strong><em>memory leaks</em> </strong> caused by static instance
 * variables and it also provides a neater way to assess Global variables
 */

public class MiscUtil {
   public static final boolean isLoggingEnabled = true;
   public static final String[] DAYS = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
   public static final String[] DAYS_3 = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday" };
   public static final char[] DAYS_2 = { 'S', 'M', 'T', 'W', 'T', 'F', 'S' };
   public static boolean deleteTaskRunning;
   @SuppressWarnings("FieldCanBeLocal")
   private static MediaPlayer alertPlayer;

   /**
    * @return true if a background task is still running
    */
   public static boolean requireContext() {
      return deleteTaskRunning;
   }

   /**
    * Get the user preferred settings for time format
    *
    * @return true if user preferred 24 hours, false otherwise. Default value "true"
    */
   public static boolean isUserPreferred24Hours(Context context) {
      // Immediately update the UI when user goes to the settings UI
      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
      return preferences.getBoolean("time_format", true);
   }

   /**
    * Play an alert tone when item is added to a list
    *
    * @param context the context which the tone is being played
    */
   public static void playAlertTone(Context context, Alert type) {
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
      // If alert tone are disabled, don't play any alert tone
      if (!prefs.getBoolean("enable alerts", true)) return;
      if (alertPlayer == null) alertPlayer = new MediaPlayer();

      try {

         if (type == Alert.DELETE)
            alertPlayer.setDataSource(context, getUri(context, R.raw.piece_of_cake1));
         else if (type == Alert.NOTIFICATION)
            alertPlayer.setDataSource(context, getUri(context, R.raw.echoed_ding1));
         else if (type == Alert.TODO_UPDATE)
            alertPlayer.setDataSource(context, getUri(context, R.raw.pristine));
         else
            alertPlayer.setDataSource(context, getUri(context, R.raw.accomplished1));

         alertPlayer.prepare();

      } catch (IOException e) {
         Log.w(MiscUtil.class.getSimpleName(), e.getMessage(), e);
         return;
      }

      alertPlayer.start();
   }

   private static Uri getUri(Context context, @RawRes int rawRes) {
      return new Uri.Builder()
              .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
              .authority(context.getPackageName())
              .path(String.valueOf(rawRes))
              .build();
   }

   public enum Alert {
      TIMETABLE, SCHEDULED_TIMETABLE, ALARM, NOTIFICATION, ASSIGNMENT, DELETE, COURSE, UNDO, EXAM, TODO, TODO_UPDATE;
   }

}