package com.noah.timely.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

/**
 * All utilities to access TimeLY's preferences
 */
public class PreferenceUtils {
   public static final String FIRST_LAUNCH_KEY = "Fist Launch";
   public static final String RESTRICTION_ACCESS_KEY = "Remove alarm restrictions";
   public static final String DENY_ACCESS = "Deny alarm restriction access";
   public static final String GRANT_ACCESS = "Grant alarm restriction access";
   public static final String UPDATE_ON_STARTUP = "update_startup";
   public static final String EASTER_EGG_KEY = "View new features";
   public static final String GPA_INFO_SHOWN_1 = "show gpa calculator 1";
   public static final String GPA_INFO_SHOWN_2 = "show gpa calculator 2";

   /**
    * Retrieves TimeLY's first launch preference
    *
    * @param context the context to be used to access the preference file
    * @return true if TimeLY has been launched before, false otherwise
    */
   public static boolean getFirstLaunchKey(@NonNull Context context) {
      // default preference file
      SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
      return sharedPreferences.getBoolean(FIRST_LAUNCH_KEY, true);
   }

   /**
    * Sets TimeLY's first launch preference
    *
    * @param context the context to be used to access the preference file
    * @param state   the current state of the first launch key
    */
   public static void setFirstLaunchKey(@NonNull Context context, boolean state) {
      // default preference file
      SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
      // preference editor
      SharedPreferences.Editor spEditor = sharedPreferences.edit();
      spEditor.putBoolean(FIRST_LAUNCH_KEY, state);
      // apply changes
      spEditor.apply();
   }

   /**
    * Retrieves a boolean preference value
    *
    * @param context      the context to be used to access the preference file
    * @param key          the preference to be accessed
    * @param defaultValue the default value to use, when key doesn't exist yet
    * @return the value of the preference with (<code>key</code>)
    */
   public static boolean getBooleanValue(@NonNull Context context, String key, boolean defaultValue) {
      SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
      return sharedPreferences.getBoolean(key, defaultValue);
   }

   /**
    * sets a boolean preference value
    *
    * @param context      the context to be used to access the preference file
    * @param key          the preference to be accessed
    * @param defaultValue the default value to use, when key doesn't exist yet
    */
   public static void setBooleanValue(@NonNull Context context, String key, boolean value) {
      // default preference file
      SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
      // preference editor
      SharedPreferences.Editor spEditor = sharedPreferences.edit();
      spEditor.putBoolean(key, value);
      // apply changes
      spEditor.apply();
   }

   /**
    * Sets or create a string preference
    *
    * @param context the context to be used to access the preference file
    * @param key     the preference key to be accessed
    * @param value   the value to be written
    */
   public static void setStringValue(Context context, String key, String value) {
      SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
      // preference editor
      SharedPreferences.Editor spEditor = sharedPreferences.edit();
      // if preference key is not created yet, create it and insert a default value
      spEditor.putString(RESTRICTION_ACCESS_KEY, value);
      // apply changes
      spEditor.apply();
   }

   /**
    * Sets or create a integer preference
    *
    * @param context the context to be used to access the preference file
    * @param key     the preference key to be accessed
    * @param value   the value to be written
    */
   public static void setIntegerValue(Context context, String key, int value) {
      SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
      // preference editor
      SharedPreferences.Editor spEditor = sharedPreferences.edit();
      // if preference key is not created yet, create it and insert a default value
      spEditor.putInt(key, value);
      // apply changes
      spEditor.apply();
   }

   /**
    * Retrieves a string preference value
    *
    * @param context      the context to be used to access the preference file
    * @param key          the preference to be accessed
    * @param defaultValue the default value to use, when key doesn't exist yet
    * @return the value of the preference with (<code>key</code>)
    */
   public static String getStringValue(Context context, String key, String defaultValue) {
      SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
      return sharedPreferences.getString(key, defaultValue);
   }

   /**
    * Retrieves a integer preference value
    *
    * @param context      the context to be used to access the preference file
    * @param key          the preference to be accessed
    * @param defaultValue the default value to use, when key doesn't exist yet
    * @return the value of the preference with (<code>key</code>)
    */
   public static int getIntegerValue(Context context, String key, int defaultValue) {
      SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
      return sharedPreferences.getInt(key, defaultValue);
   }
}
