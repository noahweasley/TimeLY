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

   /**
    * TimeLY's native export file extension
    */
   public static final String FILE_EXTENSION = ".tmly";

   /**
    * The file extension in which all exported data would have
    */
   public static final String DATA_FILE_EXTENSION = ".txt";

   public static void main(String... args) {
      /////////////////////////////////////////////////////////////////////////////////
      Map<String, String> stringMap = null;
      try {
         stringMap = unzipToXMLArray("C:\\Users\\Noah\\Desktop\\test.zip");
      } catch (IOException e) {
         System.out.println("File unzip failed, file not unipped: " + e.getMessage());
      }

      if (stringMap != null) {
         System.out.println("File unzipped succesfully\n\n");
         for (Map.Entry<String, String> entry : stringMap.entrySet()) {
            System.out.println(entry.getKey().trim() + "=" + entry.getValue().trim());
         }
      } else System.out.println("File unzip failed, file not unipped 2");
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
         byte[] data = new byte[10_048_576];
         if (zin.read(data) != -1) {
            String entryName = zipEntry.getName();
            entryName = entryName.substring(0, entryName.indexOf(DATA_FILE_EXTENSION));
            System.out.println("Unzipped: " + entryName);
            xmlmap.put(entryName, new String(data, Charset.forName("UTF-8")));
         }

      }

      zin.close();
      return xmlmap;
   }

}
