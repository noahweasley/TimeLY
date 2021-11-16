package com.noah.timely.exports;

import static com.noah.timely.util.AppInfoUtils.getAppVesionName;
import static com.noah.timely.util.AppInfoUtils.getDatabaseVerion;

import android.content.Context;

import java.util.List;

/**
 * TimeLY's native .tmly file generator
 */
public class TMLFileGenerator {

   /**
    * Generates an XML-based file of database
    *
    * @param context       the context used in accessing resources
    * @param dataModelList the list of data to be tranformed
    * @return true if file was saved, false otherwise
    */
   public static boolean generate(Context context, List<String> dataModelIdentifierList) {
      String[] transformed = new String[dataModelIdentifierList.size()];

      for (int i = 0; i <= transformed.length; i++) {
         transformed[i] = transformDatabaseToXML(context, dataModelIdentifierList.get(i));
      }

      return writeTransformedDatabaseToFile(context, transformed);
   }

   private static boolean writeTransformedDatabaseToFile(Context context, String[] transformed) {
      String app_version = getAppVesionName(context);
      int database_version = getDatabaseVerion(context);

      return false;
   }

   private static String transformDatabaseToXML(Context context, String dataModelIdentifier) {
      return null;
   }
}
