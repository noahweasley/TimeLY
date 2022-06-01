package com.astrro.timely.util;

public class Primitives {

   // convert to primitive boolean array
   public static boolean[] convertToBooleanPrimitive(Boolean[] source) {
      boolean[] dest = new boolean[source.length];
      for (int i = 0; i < source.length; i++) {
         dest[i] = source[i];
      }
      return dest;
   }

   // Convert database string array into the required boolean values.
   public static Boolean[] convertToBooleanArray(String[] repeatDays) {
      boolean _1 = Boolean.parseBoolean(repeatDays[0]);
      boolean _2 = Boolean.parseBoolean(repeatDays[1]);
      boolean _3 = Boolean.parseBoolean(repeatDays[2]);
      boolean _4 = Boolean.parseBoolean(repeatDays[3]);
      boolean _5 = Boolean.parseBoolean(repeatDays[4]);
      boolean _6 = Boolean.parseBoolean(repeatDays[5]);
      boolean _7 = Boolean.parseBoolean(repeatDays[6]);
      return new Boolean[]{ _1, _2, _3, _4, _5, _6, _7 };
   }

}
