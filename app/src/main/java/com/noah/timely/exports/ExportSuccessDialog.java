package com.noah.timely.exports;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.noah.timely.R;

public class ExportSuccessDialog extends DialogFragment {
   @SuppressWarnings("FieldCanBeLocal")
   public static final String TAG = "com.noah.timely.exports.ExportSuccessDialog";
   @SuppressWarnings("FieldCanBeLocal")
   private String message;

   public void show(Context context, @StringRes int message) {
      this.message = context.getString(message);
      FragmentManager manager = ((FragmentActivity) context).getSupportFragmentManager();
      show(manager, TAG);
   }

   @NonNull
   @Override
   public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
      return null;
   }

   private static class DExportSuccessDialog extends Dialog {

      public DExportSuccessDialog(@NonNull Context context) {
         super(context, R.style.Dialog_Closeable);
      }

      @Override
      protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         getWindow().requestFeature(Window.FEATURE_NO_TITLE);
         getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_edges_8);
         setContentView(R.layout.dialog_export_success);

      }
   }

}
