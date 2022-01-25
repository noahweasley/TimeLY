package com.tools.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("all")
public class FunctionalityTest {

   private static void setError(String error) {
      System.out.println("Error: " + error);
   }

   public static void main(String... args) {
      File file = new File("C:\\Users\\Noah\\Desktop\\Brittany Chiang_files");
      if (file.isDirectory()) {
         System.out.println("Zipping this directory");
      } else if (file.isFile()) {
         System.out.println("Zipping this file");
      }

      try {
         FileOutputStream fout = new FileOutputStream("C:\\Users\\Noah\\Desktop\\" + file.getName() + ".zip");
         ZipOutputStream zout = new ZipOutputStream(fout);
         File[] files = file.listFiles();

         for (int i = 0; i < files.length; i++) {
            zout.putNextEntry(new ZipEntry(files[i].getName()));
            FileInputStream finput = new FileInputStream(files[i].getAbsolutePath());
            byte[] data = new byte[finput.available()];
            finput.read(data);
            zout.write(data);
            zout.closeEntry();
         }

         zout.finish();
         zout.close();
         System.out.println("Finished processing files");
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}