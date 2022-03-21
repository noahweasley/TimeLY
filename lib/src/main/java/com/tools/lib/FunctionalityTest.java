package com.tools.lib;

import java.util.Arrays;

@SuppressWarnings("all")
public class FunctionalityTest {

   public static void main(String... args) {
      System.out.println(haveGotDataElement("Registered-Course"));
   }

   private static boolean haveGotDataElement(String tagName) {
      String[] dataTags = { "Assignment", "Exam", "Registered-Course", "Scheduled-Timetable", "Timetable" };
      return Arrays.binarySearch(dataTags, tagName) >= 0;
   }
}