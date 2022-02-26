package com.noah.timely.exports;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.noah.timely.R;

import java.io.File;

public class ExportSuccessDialog extends DialogFragment {
   private static final String MESSAGE = "MESSAGE";
   private static final String ARG_EXPORT_PATH = "EXPORT_PATH";

   public void show(Context context, @StringRes int message, String exportPath) {
      Bundle bundle = new Bundle();
      bundle.putString(MESSAGE, context.getString(message));
      bundle.putString(ARG_EXPORT_PATH, exportPath);
      setArguments(bundle);
      FragmentManager manager = ((FragmentActivity) context).getSupportFragmentManager();
      show(manager, ExportSuccessDialog.class.getName());
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

         Button btn_locate, btn_share;
         btn_locate = findViewById(R.id.locate);
         btn_share = findViewById(R.id.share);

         // navigate to file explorer if installed
         btn_locate.setOnClickListener(v -> {
            File file = new File(getActivity().getExternalFilesDir(null) + File.separator + "exported" + File.separator);
            Uri selectedUri = Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(selectedUri, "resource/folder");

            if (intent.resolveActivityInfo(getActivity().getPackageManager(), 0) != null) {
               startActivity(Intent.createChooser(intent, "Open folder"));
            } else {
               Toast.makeText(getContext(), "Unable to navigate to file", Toast.LENGTH_LONG).show();
            }
         });

         // opens up chooser, used to send send the Uri of the exported file
         btn_share.setOnClickListener(v -> {
            String exportPath = getArguments().getString(ARG_EXPORT_PATH);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            File file = new File(exportPath);

            if (file.exists()) {
               shareIntent.setType("*/*");
               shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Sharing file using ...");
               shareIntent.putExtra(Intent.EXTRA_TEXT, "Sharing file using ...");
               shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
               startActivity(Intent.createChooser(shareIntent, "Share using"));
            } else {
               Toast.makeText(getActivity(), "Nothing to send", Toast.LENGTH_SHORT).show();
            }
         });
      }
   }

}
