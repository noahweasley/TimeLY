package com.noah.timely.exports;

import android.content.Context;

import com.noah.timely.core.DataModel;
import com.noah.timely.core.SchoolDatabase;
import com.noah.timely.io.Zipper;
import com.noah.timely.util.AppInfoUtils;
import com.noah.timely.util.Constants;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * TimeLY's native .tmly file generator
 */
public class TMLFileGenerator {

   /**
    * Generates an XML-based file of database
    *
    * @param context                 the context used in accessing resources
    * @param dataModelIdentifierList the list of data to be tranformed
    * @return true if file was saved, false otherwise
    */
   public static boolean generate(Context context, List<String> dataModelIdentifierList) {
      Map<String, String> transformed = new HashMap<>();

      for (int i = 0; i <= dataModelIdentifierList.size(); i++) {
         String hashKey = dataModelIdentifierList.get(i);
         transformed.put(hashKey, transformDatabaseToXML(context, hashKey));
      }

      return writeTransformedDatabaseToFile(context, transformed);
   }

   private static boolean writeTransformedDatabaseToFile(Context context, Map<String, String> transformed) {
      long time = System.currentTimeMillis(); // Set unique id for each file generated
      String appName = AppInfoUtils.getAppName(context);
      String output = String.format(Locale.US, "%s%s-Data%d.tmly", context.getExternalFilesDir(null), appName, time);

      Map<String, String> metadata = createMetadataFiles(context);
      for (Map.Entry<String, String> entry : metadata.entrySet()) {
         transformed.put(entry.getKey(), entry.getValue());
      }

      boolean isCompressed = false;
      try {
         isCompressed = Zipper.zipXMLArray(context, transformed, output);
      } catch (IOException e) {
         return false;
      }
      return isCompressed;
   }

   private static Map<String, String> createMetadataFiles(Context context) {
      return null;
   }

   private static String transformDatabaseToXML(Context context, String dataModelIdentifier) {
      List<DataModel> dataModelList;
      SchoolDatabase database = new SchoolDatabase(context);

      switch (dataModelIdentifier) {
         case Constants.ASSIGNMENT:
            dataModelList = database.getAssignmentData();
            break;
         case Constants.COURSE:
            dataModelList = database.getCoursesData(null);
            break;
         case Constants.EXAM:
            dataModelList = database.getExamTimetableDataForWeek(-1);
            break;
         case Constants.TIMETABLE:
            dataModelList = database.getAllNormalSchoolTimetable();
            break;
         case Constants.SCHEDULED_TIMETABLE:
            dataModelList = database.getTimeTableData(SchoolDatabase.SCHEDULED_TIMETABLE);
            break;
         default:
            throw new IllegalArgumentException("The identifier " + dataModelIdentifier + " doesn't exists in database");
      }


      return null;
   }
}
