package com.astrro.timely.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

/**
 * Utility class to send bug report.
 * Used to send user bug report or can be used while debugging hard to detect bugs
 */
public class ReportActionUtil {

   public static void reportBug(Context context, String message) {
      // start WhatsApp
      PackageManager packageManager = context.getPackageManager();
      String whatsappPkgName = "com.whatsapp", gbwhatsappPkgName = "com.gbwhatsapp", w4bPkgName = "com.whatsapp.w4b",
              devCon = "+2347065478947";

      String dataString = String.format("https://api.whatsapp.com/send?phone=%s&text=%s", devCon, message);

      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(dataString));
      try {
         PackageInfo packageInfo = packageManager.getPackageInfo(whatsappPkgName, PackageManager.GET_META_DATA);

         intent.setPackage(whatsappPkgName);
         context.startActivity(intent);

      } catch (PackageManager.NameNotFoundException e) {
         // if whatsapp not installed, try to open W4B
         try {
            PackageInfo packageInfo = packageManager.getPackageInfo(gbwhatsappPkgName, PackageManager.GET_META_DATA);

            intent.setPackage(gbwhatsappPkgName);
            context.startActivity(intent);

         } catch (PackageManager.NameNotFoundException e1) {
            // if W4B not installed, try to open GBWhatsapp
            try {
               PackageInfo packageInfo = packageManager.getPackageInfo(w4bPkgName, PackageManager.GET_META_DATA);

               intent.setPackage(w4bPkgName);
               context.startActivity(intent);

            } catch (PackageManager.NameNotFoundException e2) {
               Toast.makeText(context, "Whatsapp not installed", Toast.LENGTH_LONG).show();
            }
         }
      }
   }

}
