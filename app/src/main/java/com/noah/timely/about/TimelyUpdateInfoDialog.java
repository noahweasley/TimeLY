package com.noah.timely.about;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.noah.timely.BuildConfig;
import com.noah.timely.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class TimelyUpdateInfoDialog extends DialogFragment implements View.OnClickListener {

   public void show(Context context) {
      FragmentManager manager = ((FragmentActivity) context).getSupportFragmentManager();
      show(manager, TimelyUpdateInfoDialog.class.getName());
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
         getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_edges_8);
         setContentView(R.layout.dialog_update_info);

         ImageButton btn_close = findViewById(R.id.close);
         btn_close.setOnClickListener(TimelyUpdateInfoDialog.this);

         TextView tv_version = findViewById(R.id.version);

         String version = BuildConfig.VERSION_NAME;
         String packageName = "com.noah.timely";

         try {
            PackageInfo packageInfo = getContext().getPackageManager().getPackageInfo(packageName, 0);
            version = packageInfo.versionName;
         } catch (PackageManager.NameNotFoundException ignored) {
         }

         tv_version.setText(String.format("v%s", version));

         // add the release notes
         ViewGroup vg_updateContainer = findViewById(R.id.update_container);
         XmlPullParser xpp = getResources().getXml(R.xml.release_note);

         try {
            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
               if (xpp.getEventType() == XmlPullParser.TEXT) {
                  vg_updateContainer.addView(createTextNode(xpp.getText()));
               }
               xpp.next();
            }
         } catch (XmlPullParserException | IOException exc) {
            Toast.makeText(getContext(), "Error retrieving release note", Toast.LENGTH_LONG).show();
         }

      }

      private View createTextNode(String text) {
         AppCompatTextView tv_text = new AppCompatTextView(getContext());
         MarginLayoutParams layoutParams = new MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                  ViewGroup.LayoutParams.WRAP_CONTENT);

         // Converts 8 dip into its equivalent px
         int mPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                   8.0f,
                                                   getResources().getDisplayMetrics());
         layoutParams.setMargins(mPx, mPx, mPx, mPx);
         tv_text.setLayoutParams(layoutParams);
         tv_text.setText(text);
         tv_text.setGravity(Gravity.START | Gravity.TOP);
         tv_text.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
         tv_text.setCompoundDrawablePadding(mPx);

         Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_circle_fill);
         tv_text.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
         return tv_text;
      }
   }
}