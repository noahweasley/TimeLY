package com.astrro.timely.util.test;

import com.astrro.timely.core.DataModel;

/**
 * A provider of fake followers details for testing purposes only
 */
public class DummyGenerator {
   // Dummies
   private static class Dummy {
      /**
       * @param dummyType the type of dummy to return
       * @return the required list of dummies
       */
      public static DataModel get(int dummyType) {
         throw new IllegalStateException("Unexpected value: " + dummyType);
      }

   }
}
