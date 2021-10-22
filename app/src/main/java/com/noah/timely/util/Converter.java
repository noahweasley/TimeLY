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
   public static String convertTime(String time, @MagicConstant(intValues = {UNIT_12, UNIT_24}) int to_unitTime) {
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

}
