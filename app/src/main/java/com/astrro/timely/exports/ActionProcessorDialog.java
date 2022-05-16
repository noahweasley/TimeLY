package com.astrro.timely.exports;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.astrro.timely.R;
import com.astrro.timely.util.collections.ISupplier;
import com.astrro.timely.util.ThreadUtils;

public class ActionProcessorDialog extends DialogFragment {
   private static final String ARG_LIST = "list";
   private static boolean dismiss_flag;
   private ISupplier supplier;
   private OnActionProcessedListener listener;

   public ActionProcessorDialog execute(Context context, ISupplier supplier) {
      this.supplier = supplier;
      FragmentManager manager = ((FragmentActivity) context).getSupportFragmentManager();
      show(manager, ActionProcessorDialog.class.getName());
      return this;
   }

   public void setOnActionProcessedListener(OnActionProcessedListener listener) {
      this.listener = listener;
   }

   @NonNull
   @Override
   public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
      return new ProcessDialog(getContext());
   }

   private class ProcessDialog extends Dialog {

      public ProcessDialog(@NonNull Context context) {
         super(context, R.style.Dialog_No_Transition);
      }

      @Override
      protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         getWindow().requestFeature(Window.FEATURE_NO_TITLE);
         getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_edges);
         setContentView(R.layout.dialog_processing);
         ThreadUtils.runBackgroundTask(() -> {
            supplier.get();
            dismiss_flag = true;
            getActivity().runOnUiThread(() -> {
               if (listener != null) listener.onActionProcessed();
               dismiss();
            });
         });
      }

      @Override
      public void dismiss() {
         if (dismiss_flag)
            super.dismiss();
      }
   }

   public interface OnActionProcessedListener {
      void onActionProcessed();
   }
}