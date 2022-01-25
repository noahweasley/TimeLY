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
    * Un-zips a .tmly file into one single folder
    *
    * @param context the context used in accessing application resources
    * @param output  the directory to output the zipped flle
    * @param input   the directory in which all it's contents would be zipped into one file
    * @return true if file was zipped successfully
    * @throws FileNotFoundException if file location specified was incorrect
    */
   public static boolean unzipToXMLArray(Context context, String input, String output) throws IOException {
      File finput = new File(input);
      File foutput = new File(output);

      FileOutputStream fout = new FileOutputStream(foutput);
      FileInputStream fin = new FileInputStream(finput);
      ZipInputStream zin = new ZipInputStream(fin);

      while ((zin.getNextEntry()) != null) {
         byte[] data = new byte[zin.available()];
         if (zin.read(data) != -1) {
            fout.write(data);
         }

      }

      zin.close();
      return false;
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
      ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(foutput));
      zout.setComment("Archive created by " + String.format("%s v%s", getAppName(context), getAppVesionName(context)));

      Set<Map.Entry<String, String>> entries = transf.entrySet();

      for (Map.Entry<String, String> entry : entries) {
         String filename = getProperFilename(entry.getKey());
         ZipEntry zipEntry = new ZipEntry(filename);
         zout.putNextEntry(zipEntry);

         zout.write(entry.getValue().getBytes());
         zout.closeEntry();
      }

      zout.finish();
      zout.close();
      return false;
   }

   private static String getProperFilename(String dataModelIdentifier) {
      if (Constants.ASSIGNMENT.equals(dataModelIdentifier)) {
         return "Assignments";
      } else if (Constants.COURSE.equals(dataModelIdentifier)) {
         return "Courses";
      } else if (Constants.EXAM.equals(dataModelIdentifier)) {
         return "Exams";
      } else if (Constants.TIMETABLE.equals(dataModelIdentifier)) {
         return "Timetable";
      } else if (Constants.SCHEDULED_TIMETABLE.equals(dataModelIdentifier)) {
         return "Scheduled Timetable";
      }
      throw new IllegalArgumentException("The identifier " + dataModelIdentifier + " doesn't exists in database");

   }


}
