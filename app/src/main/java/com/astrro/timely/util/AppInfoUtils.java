package com.astrro.timely.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.astrro.timely.BuildConfig;
import com.astrro.timely.R;
import com.astrro.timely.core.SchoolDatabase;

/**
 * Utility class to retrieve app specific information
 */
public class AppInfoUtils {

   /**
    * @param context the context to access app resources
    * @return the version name of the app
    */
   public static String getAppVesionName(Context context) {
      String version = BuildConfig.VERSION_NAME;
      try {
         PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
         version = packageInfo.versionName;
      } catch (PackageManager.NameNotFoundException ignored) {
      }

      return version;
   }

   /**
    * @param context the context to access app resources
    * @return the app's current launcher label
    */
   public static String getAppName(Context context) {
      return context.getString(R.string.app_name);
   }

   /**
    * @param context the context to access app resources
    * @return the version of the database
    */
   public static int getDatabaseVerion(Context context) {
      return new SchoolDatabase(context).getDatabaseVersion();
   }

}
