package com.astrro.timely.util;

import android.util.Log;

/**
 * A simple android debugging utility class
 */
public class LogUtils {
   private static final boolean loggingEnabled = false;

   /**
    * Utility method that can be used to log messages very easily
    *
    * @param origin  the originator of the log output. This should just be the <code>this</code> java keyword
    * @param message the output
    */
   public static void debug(Object origin, String message) {
      if (!loggingEnabled) return;

      if (origin != null) {
         Log.d(origin.getClass().getSimpleName(), message);
      } else {
         Log.d(LogUtils.class.getSimpleName(), message);
      }
   }

   /**
    * Utility method that can be used to log messages very easily
    *
    * @param origin  the originator of the log output. This should just be the <code>this</code> java keyword
    * @param message the output
    */
   public static void warn(Object origin, String message) {
      if (!loggingEnabled) return;

      if (origin != null) {
         Log.w(origin.getClass().getSimpleName(), message);
      } else {
         Log.w(LogUtils.class.getSimpleName(), message);
      }
   }

   /**
    * Utility method that can be used to log messages very easily
    *
    * @param origin  the originator of the log output. This should just be the <code>this</code> java keyword
    * @param message the output
    */
   public static void info(Object origin, String message) {
      if (!loggingEnabled) return;

      if (origin != null) {
         Log.i(origin.getClass().getSimpleName(), message);
      } else {
         Log.i(LogUtils.class.getSimpleName(), message);
      }
   }
}
