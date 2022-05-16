package com.astrro.timely.custom;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
 * Just a regular progress bar that has backward compatibility to Android Jelly bean MR2
 */
public class MaterialProgressbar extends MaterialProgressBar {

   public MaterialProgressbar(Context context) {
      super(context);
   }

   public MaterialProgressbar(Context context, AttributeSet attrs) {
      super(context, attrs);
   }

   public MaterialProgressbar(Context context, AttributeSet attrs, int defStyleAttr) {
      super(context, attrs, defStyleAttr);
   }

   public MaterialProgressbar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
      super(context, attrs, defStyleAttr, defStyleRes);
   }

   @Override
   protected void onAttachedToWindow() {
      super.onAttachedToWindow();
      // isHardwareAccelerated() only works when attached to a window.
      fixCanvasScalingWhenHardwareAccelerated();
   }

   // enable compatibility mode for API < Jelly Bean MR2 devices (Android 4.1)
   private void fixCanvasScalingWhenHardwareAccelerated() {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
         // Canvas scaling when hardware accelerated results in artifacts on older API levels, so
         // we need to use software rendering
         if (isHardwareAccelerated() && getLayerType() != LAYER_TYPE_SOFTWARE) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
         }
      }
   }
}