package com.noah.timely.util;

import org.intellij.lang.annotations.MagicConstant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class to perform conversions from one unit to another
 */
public class Converter {
   public static final int UNIT_12 = 12;
   public static final int UNIT_24 = 24;

   /**
    * Convert time from 12 hours clock to 24 hours clock or vise versa
    *
    * @param time        the time to be converted
    * @param to_unitTime to time format to convert to
    * @return input time if the conversion was not successful
    */
   public static String convertTime(String time, @MagicConstant(intValues = { UNIT_12, UNIT_24 }) int to_unitTime) {
      SimpleDateFormat timeFormat24 = new SimpleDateFormat("HH:mm", Locale.US);
      SimpleDateFormat timeFormat12 = new SimpleDateFormat("hh:mm aa", Locale.US);

      Date date;
      try {
         date = to_unitTime == UNIT_24 ? timeFormat12.parse(time) : timeFormat24.parse(time);
      } catch (ParseException e) {
         throw new IllegalArgumentException(time + " is not parsable");
      }
      return to_unitTime == UNIT_24 ? timeFormat24.format(date.getTime()) : timeFormat12.format(date.getTime());
   }

   /**
    * Converts milli-seconds to a formatted human readable time
    *
    * @param timeInMillis the time in milli-seconds to be converted
    * @param options      options to be used in conversion. If null, then only seconds would be displayed
    * @return a properly converted time, as specified by options
    */
   public static String convertMillisToRealTime(long timeInMillis, Options options) {
      long hours = (timeInMillis / 1000) % 60;
      long minutes = (timeInMillis / (1000 * 60)) % 60;
      long seconds = (timeInMillis / (1000 * 60 * 60)) % 24;

      if (options == null || options == Options.SECONDS_ONLY) {
         return String.valueOf(seconds);
      } else if (options == Options.INCLUDE_MIN) {
         return String.format(Locale.US, "%02d:%02d", minutes, seconds);
      } else {
         return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
      }

   }

   /**
    * Time conversion options
    */
   public enum Options {
      INCLUDE_MIN, INCLUDE_HOUR, SECONDS_ONLY
   }

}
