package com.noah.timely.about;

import static com.noah.timely.util.AppInfoUtils.getAppVesionName;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.noah.timely.R;

public class TimelyBasicInfoDialog extends DialogFragment implements View.OnClickListener {
   public static final String TAG = "com.noah.timely.about.TimelyInfoDialog";

   public void show(Context context) {
      FragmentManager manager = ((FragmentActivity) context).getSupportFragmentManager();
      show(manager, TAG);
   }

   @Override
   public void onClick(View v) {
      if (v.getId() == R.id.close)
         dismiss();
   }

   @NonNull
   @Override
   public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
      return new InfoDialog(getContext());
   }

   private class InfoDialog extends Dialog {

      public InfoDialog(@NonNull Context context) {
         super(context, R.style.Dialog_Closeable);
      }

      @Override
      protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         getWindow().requestFeature(Window.FEATURE_NO_TITLE);
         getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_edges);
         setContentView(R.layout.dialog_about);

         ImageButton btn_close = findViewById(R.id.close);
         btn_close.setOnClickListener(TimelyBasicInfoDialog.this);

         findViewById(R.id.bmc).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.buymeacoffee.com/noahweasley"));
            getActivity().startActivity(Intent.createChooser(intent, getString(R.string.link_open_text)));
         });

         TextView tv_version = findViewById(R.id.version);
         tv_version.setText(String.format("V%s", getAppVesionName(getContext())));
      }
   }
}