package com.noah.timely.calculator;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.noah.timely.R;
import com.noah.timely.util.PreferenceUtils;

import org.intellij.lang.annotations.MagicConstant;

public class ResultCalculatorInfoDialog extends DialogFragment {
   private OnActionReceivedListener listener;
   public static final int ACTION_DONT_SHOW = 0;
   public static final int CANCELLED = 1;
   public static final int ACTION_PROCEED = 2;

   @NonNull
   @Override
   public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
      return new RCIDialog(getContext());
   }

   public ResultCalculatorInfoDialog show(Context context) {
      // to prevent the dialog from showing twice, when both first and second semester courses
      // haven't been registered yet.
      if (!isAdded()) {
         FragmentActivity activity = (AppCompatActivity) context;
         FragmentManager mgr = activity.getSupportFragmentManager();
         show(mgr, ResultCalculatorInfoDialog.class.getSimpleName());
      }
      return this;
   }

   public void setOnActionReceviedListener(OnActionReceivedListener listener) {
      this.listener = listener;
   }

   @Override
   public void onDetach() {
      super.onDetach();
      PreferenceUtils.setBooleanValue(getContext(), PreferenceUtils.GPA_INFO_SHOWN, true);
   }

   private class RCIDialog extends Dialog {

      public RCIDialog(@NonNull Context context) {
         super(context, R.style.Dialog_Closeable);
      }

      @Override
      protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         getWindow().requestFeature(Window.FEATURE_NO_TITLE);
         getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_edges);
         setContentView(R.layout.dialog_result_calculator);
         Button btn_close = findViewById(R.id.close);

         btn_close.setOnClickListener(v -> {
            if (listener != null) listener.onAction(CANCELLED);
            ResultCalculatorInfoDialog.this.dismiss();
         });

      }

   }

   @FunctionalInterface
   public interface OnActionReceivedListener {
      void onAction(@MagicConstant(intValues = { CANCELLED, ACTION_PROCEED, ACTION_DONT_SHOW }) int action);
   }

}
