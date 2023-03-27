package com.astrro.timely.calculator;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.astrro.timely.R;

import java.util.Locale;

public class GPAAveragerDialog extends DialogFragment implements Runnable {
   private static final String ARG_GPA = "User average GPA";
   private TextView tv_gpa;
   private Handler handler;

   @NonNull
   @Override
   public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
      return new DGPAAverager(getContext());
   }

   public GPAAveragerDialog show(Context context, float gpa) {
      FragmentManager manager = ((FragmentActivity) context).getSupportFragmentManager();
      show(manager, GPAAveragerDialog.class.getName());
      Bundle bundle = new Bundle();
      bundle.putFloat(ARG_GPA, gpa);
      setArguments(bundle);
      return this;
   }

   private class DGPAAverager extends Dialog {

      public DGPAAverager(@NonNull Context context) {
         super(context, R.style.Dialog_Closeable);
      }

      @Override
      protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         getWindow().requestFeature(Window.FEATURE_NO_TITLE);
         getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_edges);
         setContentView(R.layout.dialog_gpa_averager);

         tv_gpa = findViewById(R.id.gpa);

         Handler handler = new Handler(Looper.getMainLooper());
         handler.postDelayed(GPAAveragerDialog.this, getResources().getInteger(android.R.integer.config_longAnimTime));
      }

   }

   @Override
   public void run() {
      if (getArguments() != null) {
         ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.0f, getArguments().getFloat(ARG_GPA));
         valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
         valueAnimator.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
         // add value animation listeners for when the value updates
         valueAnimator.addUpdateListener(valueAnimator1 -> {
            float value = (float) valueAnimator1.getAnimatedValue();
            tv_gpa.setText(String.format(Locale.US, "%.2f", value));
         });

         valueAnimator.start();
      }
   }

   @Override
   public void onDetach() {
      super.onDetach();
      handler.removeCallbacks(GPAAveragerDialog.this);
      handler = null;
   }
}
