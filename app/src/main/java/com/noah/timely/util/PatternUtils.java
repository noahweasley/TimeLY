package com.noah.timely.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Set of ready-to-use regex patterns mostly required in TimeLY
 */
public class PatternUtils {

   /**
    * Pattern to match 12 hours clock
    */
   public static final String _24_HoursClock = "(?:[01][0-9]|2[0-3]):[0-5][0-9]";

   /**
    * Pattern to match 24 hours clock
    */
   public static final String _12_HoursClock = "(?:0?[0-9]|1[0-2]):[0-5][0-9] (?i)[ap]m";

   /**
    * Pattern to match both 12 and 24 hours clock
    */
   public static final String TIME_ALL = "(?:(?:0?[0-9]|1[0-2]):[0-5][0-9] (?i:[ap]m)|(?:[01][0-9]|2[0-3]):[0-5][0-9])";

   /**
    * Pattern to match 24 hours clock
    */
   public static final String DATE_SHORT_24_HoursClock = "[A-z]{3} \\d{2}, (?:[01][0-9]|2[0-3]):[0-5][0-9]";

   /**
    * Pattern to match 24 hours clock
    */
   public static final String DATE_SHORT_12_HoursClock = "[A-z]{3} \\d{2}, (?:0?[0-9]|1[0-2]):[0-5][0-9] (?i)[ap]m";

   /**
    * Pattern to match all reasonable dates in format (dd-mm-yyyy or dd/mm/yyyy or dd.mm.yyyy or dd_mm_yyyy)
    * CAUTION: use wisely, because this pattern is slow compared to other means of date validation
    *
    * @see java.text.SimpleDateFormat
    */
   public static final String DATE_ALL = "(?:(3[01]|[12][0-9]|0[1-9])[/._-](1[0-2]|0[1-9]))[/._-][0-9]{4}";

   /**
    * Pattern to match short dates in format, <code>MMM dd</code> for example, May 16
    */
   public static final String DATE_SHORT = "[A-z]{3} \\d{2}";

   /**
    * Pattern to match both 12 and 24 hours clock with short date in front, e.g May 6, 00:00 or May 6, 12:00 am
    */
   public static final String DATE_SHORT_12_24_HoursClock = DATE_SHORT + ", " + TIME_ALL;

   /**
    * Validates a regex pattern against an input
    *
    * @param regex the pattern to use to validate the input against
    * @param input the input to be validated
    * @return true if the input matches the pattern
    */
   public static boolean test(String regex, CharSequence input) {
      return Pattern.matches(regex, input);
   }

   /**
    * Finds the nearest match in the input string and then returns it
    *
    * @param regex the pattern to use to validate the input against
    * @param input the input to be validated
    * @return the match or an empty string if no match was foudn
    */
   public static String findMatch(String regex, CharSequence input) {
      Pattern pattern = Pattern.compile(regex);
      Matcher matcher = pattern.matcher(input);
      String match = "";
      if (matcher.find()) match = matcher.group();
      return match;
   }

}
