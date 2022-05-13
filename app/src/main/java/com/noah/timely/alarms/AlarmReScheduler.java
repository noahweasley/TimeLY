package com.noah.timely.alarms;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.noah.timely.util.Constants;

/**
 * <p>An alarm re-scheduler that re-schedules alarm from TimeLY's database.</p>
 * <p>
 * When user devices switches off, all alarms are terminated and no longer exists.
 * On re-boot, this receiver would re-set the alarms that was previously set.
 */
public class AlarmReScheduler extends BroadcastReceiver {

   @Override
   public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      boolean isValidReBootAction
              = isScheduleExactAlarmPermissionStateChanged(action)
              || /* Normal device boot */action.equals("android.intent.action.BOOT_COMPLETED")
              || /* on TimeLY upgrade */ action.equals("android.intent.action.PACKAGE_REPLACED")
              || /* Device locked */ action.equals("android.intent.action.LOCKED_BOOT_COMPLETED")
              || /* quick boot */ action.equals("android.intent.action.QUICKBOOT_POWERON")
              || /* htc devices */ action.equals("com.htc.intent.action.QUICKBOOT_POWERON");

      if (isValidReBootAction) {
         Intent serviceIntent = new Intent(context, AlarmReSchedulerService.class);
         serviceIntent.setAction(Constants.ACTION.SHOW_NOTIFICATION);
         context.startService(serviceIntent);
      }
   }

   private boolean isScheduleExactAlarmPermissionStateChanged(String action) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
         return action.equals(AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED);
      } else return false;
   }
}