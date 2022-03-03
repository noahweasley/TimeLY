package com.tools.lib;

import java.io.File;

@SuppressWarnings("all")
public class FunctionalityTest {

   public static void main(String... args) {
      File file = new File("C:\\Users\\Noah\\StudioProjects\\TimeLY\\app\\src\\main\\java\\com" +
                                   "\\noah\\timely\\settings\\SettingsActivity.java");
      System.out.println(file.getAbsolutePath());
   }

}