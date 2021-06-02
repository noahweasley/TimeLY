package com.projects.timely.core;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

/**
 * All utilities to access TimeLY's preferences
 */
public class PreferenceUtils {
    private static final String FIRST_LAUNCH_KEY = "Fist Launch";

    /**
     * Retrieves TimeLY's first launch preference
     *
     * @param context the context to be used to access the preference file
     * @return true if TimeLY has been launched before, false otherwise
     */
    public static boolean getFirstLaunchKey(@NonNull Context context) {
        // default preference file
        SharedPreferences sharedPreferences
                = PreferenceManager.getDefaultSharedPreferences(context);

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
        SharedPreferences sharedPreferences
                = PreferenceManager.getDefaultSharedPreferences(context);
        // preference editor
        SharedPreferences.Editor spEditor = sharedPreferences.edit();
        // if preference key is not created yet, create it and insert a false value
        spEditor.putBoolean(FIRST_LAUNCH_KEY, state);
        // apply changes
        spEditor.apply();
    }
}
