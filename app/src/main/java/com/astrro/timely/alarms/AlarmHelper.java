package com.astrro.timely.alarms;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.astrro.timely.R;

public class AlarmHelper {

   @RequiresApi(api = Build.VERSION_CODES.S)
   public static void showInContextUI(Context context) {
      String noticeTitle = context.getString(R.string.noticeTitle);
      String noticeMessage = context.getString(R.string.exact_alarm_request_info);
      String goText = context.getString(R.string.go);
      String cancelText = context.getString(R.string.deny);

      DialogInterface.OnClickListener listener = (dialog, which) -> {
         if (which == DialogInterface.BUTTON_POSITIVE) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            context.startActivity(intent);
         } else {
            Toast.makeText(context, "Can't set alarm at this moment", Toast.LENGTH_LONG).show();
         }

         dialog.cancel();

      };

      new AlertDialog.Builder(context)
              .setTitle(noticeTitle)
              .setMessage(noticeMessage)
              .setIcon(R.drawable.ic_baseline_info_24)
              .setNegativeButton(cancelText, listener)
              .setPositiveButton(goText, listener)
              .create()
              .show();
   }
}
