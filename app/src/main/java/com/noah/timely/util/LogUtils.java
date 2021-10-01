package com.noah.timely.util;

import android.util.Log;

/**
 * A simple android debugging utility class
 */
public class LogUtils {

   /**
    * Utility method that can be used to log messages very easily
    *
    * @param origin  the originator of the log output. This should just be the <code>this</code> java keyword
    * @param message the output
    */
   public static void debug(Object origin, String message) {
      if (origin != null) {
         Log.d(origin.getClass().getSimpleName(), message);
      } else {
         Log.d("Logger", message);
      }
   }
}
