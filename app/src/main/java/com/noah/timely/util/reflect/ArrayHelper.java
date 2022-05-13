package com.noah.timely.util.reflect;

import java.lang.reflect.Array;

public class ArrayHelper {

   public static <E> E[] getArray(Class<E> clazz, int capacity) {
      return (E[]) Array.newInstance(clazz, capacity);
   }
}
