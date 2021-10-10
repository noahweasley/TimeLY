package com.noah.timely.util;

/**
 * Set of ready-to-use regex patterns mostly required in TimeLY
 */
public class PatternUtils {

   /**
    * Pattern to match 12 hours clock
    */
   public static final String _24_HoursClock = "^(?:[01][0-9]|2[0-3]):[0-5][0-9]$";

   /**
    * Pattern to match 24 hours clock
    */
   public static final String _12_HoursClock = "^(?:0?[0-9]|1[0-2]):[0-5][0-9] (?i)[ap]m$";

   /**
    * Pattern to match 24 hours clock
    */
   public static final String DATE_SHORT_24_HoursClock = "^[A-z]{3} \\d{2}, (?:[01][0-9]|2[0-3]):[0-5][0-9]$";

   /**
    * Pattern to match 24 hours clock
    */
   public static final String DATE_SHORT_12_HoursClock = "^[A-z]{3} \\d{2}, (?:0?[0-9]|1[0-2]):[0-5][0-9] (?i)[ap]m$";

   /**
    * Pattern to match all reasonable date formats
    * CAUTION: use wisely, because this pattern is slow compared to other means of date validation
    *
    * @see java.text.SimpleDateFormat
    */
   public static final String DATE_ALL = "^(?:(3[01]|[12][0-9]|0[1-9])[/._-](1[0-2]|0[1-9]))[/._-][0-9]{4}$";
}
