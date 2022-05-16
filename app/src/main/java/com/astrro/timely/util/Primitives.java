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

}
