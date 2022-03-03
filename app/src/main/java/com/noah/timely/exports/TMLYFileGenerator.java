package com.noah.timely.exports;

import static com.noah.timely.util.AppInfoUtils.getAppName;
import static com.noah.timely.util.AppInfoUtils.getAppVesionName;
import static com.noah.timely.util.AppInfoUtils.getDatabaseVerion;

import android.content.Context;
import android.text.TextUtils;

import com.noah.timely.core.DataModel;
import com.noah.timely.core.SchoolDatabase;
import com.noah.timely.io.Zipper;
import com.noah.timely.util.AppInfoUtils;
import com.noah.timely.util.Constants;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * TimeLY's native .tmly file generator
 */
public class TMLYFileGenerator {

   /**
    * Generates an XML-based file of database
    *
    * @param context                 the context used in accessing resources
    * @param dataModelIdentifierList the list of data to be tranformed
    * @return the exported file path if file was saved, null otherwise
    */
   public static String generate(Context context, List<String> dataModelIdentifierList) {
      Map<String, String> transformed = new HashMap<>();
      transformed.put("Metadata", Transformer.getXML(createMetadataMap(context)));

      for (int i = 0; i < dataModelIdentifierList.size(); i++) {
         String hashKey = dataModelIdentifierList.get(i);
         String xml_database = transformDatabaseToXML(context, hashKey);
         // don't export an empty table
         if (!TextUtils.isEmpty(xml_database))
            transformed.put(hashKey, xml_database);
      }

      return writeTransformedDatabaseToFile(context, transformed);
   }

   /**
    * Opens-up a valid .tmly file
    *
    * @param context  the context used in accessing resources
    * @param filePath the path to the file with the data to be imported
    * @return an hash map containing the datamodel constants as keys and corresponding datamodel list as values
    */
   public static Map<String, List<? extends DataModel>> importFromFile(Context context, String filePath) {
      Map<String, String> xmlStringMap = null;
      try {
         xmlStringMap = Zipper.unzipToXMLMap(context, filePath);
      } catch (IOException e) {
         e.printStackTrace();
      }
      return transformXMLMapToDatamodelMap(xmlStringMap);
   }

   private static Map<String, List<? extends DataModel>> transformXMLMapToDatamodelMap(Map<String, String> map) {
      if (map == null) return null;
      Map<String, List<? extends DataModel>> datamap = new HashMap<>();
      // key = data model constant equiv; value = xml as string to be transformed to a datamodel list
      for (Map.Entry<String, String> entry : map.entrySet()) {
         // ignore the metadata in the file for now, it's not useful yet
         if (entry.getKey().equals("Metadata")) continue;
         datamap.put(entry.getKey(), Transformer.getDataModel(entry.getKey(), entry.getValue()));
      }

      return datamap;
   }

   private static String writeTransformedDatabaseToFile(Context context, Map<String, String> transformed) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("HHMMddmmyyyy");
      Date date = new Date(System.currentTimeMillis()); // Set unique id for each file generated
      String time = dateFormat.format(date);
      String appName = AppInfoUtils.getAppName(context);
      String folderName = "exported";

      String output = String.format(Locale.US,
                                    "%1$s%2$s%3$s%2$s%4$s-Data%5$s%6$s", context.getExternalFilesDir(null),
                                    File.separator, folderName, appName, time, Zipper.FILE_EXTENSION);

      boolean isCompressed = false;
      try {
         isCompressed = Zipper.zipXMLMap(context, transformed, output);
      } catch (IOException e) {
         return null;
      }

      return isCompressed ? output : null;
   }

   private static Map<String, String> createMetadataMap(Context context) {
      Map<String, String> metadataMap = new HashMap<>();
      metadataMap.put("app_name", getAppName(context));
      metadataMap.put("app_version", getAppVesionName(context));
      metadataMap.put("database_version", String.valueOf(getDatabaseVerion(context)));
      return metadataMap;
   }

   private static String transformDatabaseToXML(Context context, String dataModelIdentifier) {
      List<DataModel> dataModelList;
      SchoolDatabase database = new SchoolDatabase(context);
      Map<String, String> dataModelListMap = new HashMap<>();

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

      // key - dataModelIdentifier, value - dataModelList
      return Transformer.getXML(new Object[]{ dataModelIdentifier, dataModelList });
   }

}
