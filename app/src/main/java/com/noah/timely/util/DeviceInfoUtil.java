package com.noah.timely.util;

import android.content.Context;
import android.util.DisplayMetrics;

public class DeviceInfoUtil {

   public static float getScreenDensity(Context context) {
      return context.getResources().getDisplayMetrics().density;
   }

   /**
    * @param context the user of this action
    * @return the device resolution in dp
    */
   public static float[] getDeviceResolutionDP(Context context) {
      DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
      float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
      float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
      return new float[]{dpWidth, dpHeight};
   }

}
