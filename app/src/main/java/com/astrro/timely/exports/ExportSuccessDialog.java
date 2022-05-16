package com.astrro.timely.exports;

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
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.astrro.timely.BuildConfig;
import com.astrro.timely.R;

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

         Button btn_share = findViewById(R.id.share);

         String exportPath = getArguments().getString(ARG_EXPORT_PATH);
         // can't send a file:// uri, transform it into a content:// uri
         File file = new File(exportPath);
         Uri fileUri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", file);

         // opens up chooser, used to send send the Uri of the exported file
         btn_share.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);

            if (file.exists()) {
               shareIntent.setType("application/tmly");
               shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
               shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

               startActivity(Intent.createChooser(shareIntent, getString(R.string.share_text_subject_2)));
            } else {
               Toast.makeText(getActivity(), R.string.no_share_subject_text, Toast.LENGTH_SHORT).show();
            }
         });
      }
   }

}
