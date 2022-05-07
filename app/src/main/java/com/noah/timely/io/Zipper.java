package com.noah.timely.io;

import static com.noah.timely.util.AppInfoUtils.getAppName;
import static com.noah.timely.util.AppInfoUtils.getAppVesionName;

import android.content.Context;

import com.noah.timely.util.Constants;

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
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * TimeLY's native Zipper for .tmly generated files
 */
public class Zipper {

   /**
    * TimeLY's native export file extension
    */
   public static final String FILE_EXTENSION = ".tmly";

   /**
    * The file extension in which all exported data would have
    */
   public static final String DATA_FILE_EXTENSION = ".xml";

   /**
    * The maximum amount of data that can be transferred at once; 10MB
    */
   public static final int MAX_DATA_TRANSFER_BITS = 10_048_576;

   /**
    * Un-zips a .tmly file into one single folder
    *
    * @param context the context used in accessing application resources
    * @param output  the directory to output the zipped flle
    * @param input   the directory in which all it's contents would be zipped into one file
    * @return true if file was zipped successfully
    * @throws FileNotFoundException if file location specified was incorrect
    */
   public static Map<String, String> unzipToXMLMap(Context context, String finput) throws IOException {
      ZipInputStream zin = new ZipInputStream(new FileInputStream(finput));
      // timely is unable to unzip any file larger than MAX_DATA_TRANSFER_BITS
      if (zin.available() > MAX_DATA_TRANSFER_BITS)
         throw new ZipException("Unable to unzip large file of " + zin.available() + " bytes");

      Map<String, String> xmlmap = new HashMap<>();

      ZipEntry zipEntry = null;

      while ((zipEntry = zin.getNextEntry()) != null) {
         byte[] data = new byte[MAX_DATA_TRANSFER_BITS];
         if (zin.read(data) != -1) {
            String entryName = zipEntry.getName();
            xmlmap.put(getDatamodelName(entryName), new String(data, Charset.forName("UTF-8")));
         }

      }

      zin.close();
      return xmlmap;
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
   public static boolean zipXMLMap(Context context, Map<String, String> transf, String foutput) throws IOException {
      File exportFile = new File(foutput);
      File exportDirectory = exportFile.getParentFile();
      boolean created = true;

      if (!exportDirectory.exists()) {
         created = exportDirectory.mkdirs();
      }

      if (!created) return false;
      else {
         ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(foutput));
         zout.setComment("Archive created by " + String.format("%s v%s", getAppName(context), getAppVesionName(context)));

         Set<Map.Entry<String, String>> entries = transf.entrySet();

         int zippedCount = 0;

         for (Map.Entry<String, String> entry : entries) {
            String filename = getProperFilename(entry.getKey());
            ZipEntry zipEntry = new ZipEntry(filename);
            zout.putNextEntry(zipEntry);

            zout.write(entry.getValue().getBytes(Charset.forName("UTF-8")));
            zout.closeEntry();
            zippedCount++;
         }

         zout.finish();
         zout.close();
         return entries.size() == zippedCount;
      }
   }

   private static String getProperFilename(String dataModelIdentifier) {
      switch (dataModelIdentifier) {
         case Constants.ASSIGNMENT:
            return "Assignments.xml";
         case Constants.COURSE:
            return "Courses.xml";
         case Constants.EXAM:
            return "Exams.xml";
         case Constants.TIMETABLE:
            return "Timetable.xml";
         case Constants.SCHEDULED_TIMETABLE:
            return "Scheduled Timetable.xml";
         case "Metadata":
            return dataModelIdentifier + ".xml";
         default:
            throw new IllegalArgumentException("The identifier " + dataModelIdentifier + " doesn't exists in database");
      }

   }

   private static String getDatamodelName(String properName) {
      switch (properName) {
         case "Assignments.xml":
            return Constants.ASSIGNMENT;
         case "Courses.xml":
            return Constants.COURSE;
         case "Exams.xml":
            return Constants.EXAM;
         case "Timetable.xml":
            return Constants.TIMETABLE;
         case "Scheduled Timetable.xml":
            return Constants.SCHEDULED_TIMETABLE;
         case "Metadata.xml":
            return properName;
         default:
            throw new IllegalArgumentException("A datamodel with name: " + properName + " doesn't exist in database");
      }
   }

}
