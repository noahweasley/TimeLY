package com.noah.timely.exports;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.noah.timely.R;

@SuppressWarnings("FieldCanBeLocal")
public class ExportSuccessDialog extends DialogFragment {
   public static final String TAG = "com.noah.timely.exports.ExportSuccessDialog";
   private static final String MESSAGE = "MESSAGE";

   public void show(Context context, @StringRes int message) {
      Bundle bundle = new Bundle();
      bundle.putString(MESSAGE, context.getString(message));
      setArguments(bundle);
      FragmentManager manager = ((FragmentActivity) context).getSupportFragmentManager();
      show(manager, TAG);
   }

   @NonNull
   @Override
   public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
      return new DExportSuccessDialog(getContext());
   }

   private class DExportSuccessDialog extends Dialog {

      public DExportSuccessDialog(@NonNull Context context) {
         super(context, R.style.Dialog_Closeable);
      }

      @Override
      protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         getWindow().requestFeature(Window.FEATURE_NO_TITLE);
         getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_edges_8);
         setContentView(R.layout.dialog_export_success);
         TextView tv_message = findViewById(R.id.message);
         Bundle arguments = getArguments();
         tv_message.setText(arguments.getString(MESSAGE));
      }
   }

}
