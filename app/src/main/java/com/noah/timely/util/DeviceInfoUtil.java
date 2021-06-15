package com.noah.timely.util;

import android.content.Context;
import android.util.DisplayMetrics;

import com.noah.timely.R;

public class DeviceInfoUtil {

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

    /**
     * @param context the user of this action
     * @return the required dialog resolution in dp
     */
    public static float getRequiredDialogWidth(Context context) {
        float w = context.getResources().getDimension(R.dimen.normal_dialog_width);
        return getDeviceResolutionDP(context)[0] + w;
    }

}
