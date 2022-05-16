package com.astrro.timely.about;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.astrro.timely.R;
import com.astrro.timely.util.DeviceInfoUtil;

public class ContactActivity extends AppCompatActivity {

   @Override
   protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_contact);
      setSupportActionBar(findViewById(R.id.toolbar));
      getSupportActionBar().setTitle("Contact us");
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      Button btn_send = findViewById(R.id.send);
      EditText edt_description = findViewById(R.id.description);
      CheckBox cbx_checkBox = findViewById(R.id.checkbox);

      btn_send.setOnClickListener(v -> {
         // user might want to send empty message
         if (TextUtils.isEmpty(edt_description.getText())) {
            // because TextInputLayout has child, FrameLayout, that is the TextInputEditText's parent
            // view, calling getParent() directly on the TextInputEditText will return the FrameLayout
            // and not the TextInputLayout
            ViewParent containerParent = edt_description.getParent();
            TextInputLayout til_descriptionParent = ((TextInputLayout) containerParent.getParent());
            til_descriptionParent.setError("Messaage can't be empty");
            return;
         }

         Intent intent = new Intent();
         intent.setType("message/rfc822");
         // add device info if checkbox was checked
         String userMessage = edt_description.getText().toString();
         intent.putExtra(Intent.EXTRA_TEXT, cbx_checkBox.isChecked() ? userMessage + "\n" + getDeviceInfo() : userMessage);
         intent.putExtra(Intent.EXTRA_SUBJECT, "Bug Report");
         intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ getString(R.string.company_email) });

         try {
            startActivity(Intent.createChooser(intent, getString(R.string.send_email_title)));
         } catch (ActivityNotFoundException exc) {
            Toast.makeText(this, "No E-mail client intalled", Toast.LENGTH_LONG).show();
         }

      });
   }

   private String getDeviceInfo() {
      float[] deviceRes = DeviceInfoUtil.getDeviceResolutionDP(this);

      return "Device specs "
              + "\n\n"
              + "Api Level: " + Build.VERSION.SDK_INT
              + "\n"
              + "Device Screen Density: " + DeviceInfoUtil.getScreenDensity(this)
              + "\n"
              + "Screen Resolution(dp) : " + deviceRes[0] + " x " + deviceRes[1];
   }

   @Override
   public boolean onSupportNavigateUp() {
      super.onBackPressed();
      return super.onSupportNavigateUp();
   }

}
