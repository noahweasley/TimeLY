package com.astrro.timely.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;

import androidx.preference.PreferenceManager;

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

}