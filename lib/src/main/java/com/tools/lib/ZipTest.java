package com.tools.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipTest {

   public static void main(String... args) {
      Map<String, String> map = new HashMap<>();

      map.put("key1", "entry1");
      map.put("key2", "entry2");
      map.put("key3", "entry3");
      map.put("key4", "entry4");
      map.put("key5", "entry5");
      map.put("key6", "entry6");

      boolean isZipped = false;
      try {
         isZipped = zipXMLArray(map, "C:\\Users\\Noah\\Desktop\\exported\\test.zip");
      } catch (IOException e) {
         System.out.println("File zip failed, file not zipped: " + e.getMessage());
      }

      if (isZipped) System.out.println("File zipped succesfully");
      else System.out.println("File Zip failed, file not zipped 2");

      /////////////////////////////////////////////////////////////////////////////////
      Map<String, String> stringMap = null;
      try {
         stringMap = unzipToXMLArray("C:\\Users\\Noah\\Desktop\\exported\\test.zip");
      } catch (IOException e) {
         System.out.println("File unzip failed, file not unipped: " + e.getMessage());
      }

      if (stringMap != null) System.out.println("File unzipped succesfully\n\n" + stringMap);
      else System.out.println("File unip failed, file not unipped 2");
   }

   /**
    * Zips a folder into one single file with the .tmly file extension
    *
    * @param context the context used in accessing application resources
    * @param output  the directory to output the zipped flle
    * @param input   the directory in which all it's contents would be zipped into one file
    * @return true if file was zipped successfully
    * @throws FileNotFoundException if file location specified was incorrect
    */
   public static boolean zipXMLArray(Map<String, String> transf, String foutput) throws IOException {
      File exportFile = new File(foutput);
      File exportDirectory = exportFile.getParentFile();
      if (!exportDirectory.exists()) {
         boolean created = exportDirectory.mkdirs();
      }

      ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(foutput));
      zout.setComment("Archive created by " + String.format("%s v%s", "TimeLY", "1.2.0"));

      Set<Map.Entry<String, String>> entries = transf.entrySet();

      int zippedCount = 0;

      for (Map.Entry<String, String> entry : entries) {
         String filename = entry.getKey();
         ZipEntry zipEntry = new ZipEntry(filename + ".txt");
         zipEntry.setSize(entry.getValue().length());
         zout.putNextEntry(zipEntry);

         zout.write(entry.getValue().getBytes());
         zout.closeEntry();
         zippedCount++;
      }

      zout.finish();
      zout.close();

      return entries.size() == zippedCount;
   }

   /**
    * Un-zips a .tmly file into one single folder
    *
    * @param context the context used in accessing application resources
    * @param output  the directory to output the zipped flle
    * @param input   the directory in which all it's contents would be zipped into one file
    * @return true if file was zipped successfully
    * @throws FileNotFoundException if file location specified was incorrect
    */
   public static Map<String, String> unzipToXMLArray(String finput) throws IOException {
      ZipInputStream zin = new ZipInputStream(new FileInputStream(finput));
      Map<String, String> xmlmap = new HashMap<>();

      ZipEntry zipEntry = null;

      while ((zipEntry = zin.getNextEntry()) != null) {
         byte[] data = new byte[Byte.MAX_VALUE];
         if (zin.read(data) != -1) {
            xmlmap.put(zipEntry.getName(), new String(data, Charset.forName("UTF-8")));
         }

      }

      zin.close();
      return xmlmap;
   }

}
