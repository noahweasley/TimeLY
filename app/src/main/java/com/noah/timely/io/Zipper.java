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
    * Un-zips a .tmly file into one single folder
    *
    * @param context the context used in accessing application resources
    * @param output  the directory to output the zipped flle
    * @param input   the directory in which all it's contents would be zipped into one file
    * @return true if file was zipped successfully
    * @throws FileNotFoundException if file location specified was incorrect
    */
   public static Map<String, String> unzipToXMLArray(Context context, String finput) throws IOException {
      ZipInputStream zin = new ZipInputStream(new FileInputStream(finput));
      Map<String, String> xmlmap = new HashMap<>();

      ZipEntry zipEntry = null;

      while ((zipEntry = zin.getNextEntry()) != null) {
         byte[] data = new byte[Integer.MAX_VALUE];
         if (zin.read(data) != -1) {
            String entryName = zipEntry.getName();
            String ssEntryName = entryName.substring(0, entryName.indexOf(FILE_EXTENSION));
            xmlmap.put(ssEntryName, new String(data, Charset.forName("UTF-8")));
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
   public static boolean zipXMLArray(Context context, Map<String, String> transf, String foutput) throws IOException {
      File exportFile = new File(foutput);
      File exportDirectory = exportFile.getParentFile();
      boolean created = true;

      if (!exportDirectory.exists()) {
         created = exportDirectory.mkdirs();
      }

      if (!created) return false;
      else {
         ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(foutput));
         zout.setComment(
                 "Archive created by " + String.format("%s v%s", getAppName(context), getAppVesionName(context)));

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
      if (Constants.ASSIGNMENT.equals(dataModelIdentifier)) {
         return "Assignments.xml";
      } else if (Constants.COURSE.equals(dataModelIdentifier)) {
         return "Courses.xml";
      } else if (Constants.EXAM.equals(dataModelIdentifier)) {
         return "Exams.xml";
      } else if (Constants.TIMETABLE.equals(dataModelIdentifier)) {
         return "Timetable.xml";
      } else if (Constants.SCHEDULED_TIMETABLE.equals(dataModelIdentifier)) {
         return "Scheduled Timetable.xml";
      } else if (dataModelIdentifier.equals("Metadata")) {
         return dataModelIdentifier + ".xml";
      }
      throw new IllegalArgumentException("The identifier " + dataModelIdentifier + " doesn't exists in database");

   }

}
