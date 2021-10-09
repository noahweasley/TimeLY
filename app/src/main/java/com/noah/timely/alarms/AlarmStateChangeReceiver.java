package com.noah.timely.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

@SuppressWarnings("all")
public class AlarmStateChangeReceiver extends BroadcastReceiver {

   @Override
   public void onReceive(Context context, Intent intent) {

      if (intent.getAction().equals("android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED")) {
         // do something
      }

   }

}
