package com.noah.timely.assignment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AssignmentSchedulerChecker extends BroadcastReceiver {

   @Override
   public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      boolean isValidBootAction
              = /* Normal device boot */ action.equals("android.intent.action.BOOT_COMPLETED")
              || /* Device locked */ action.equals("android.intent.action.LOCKED_BOOT_COMPLETED")
              || /* quick boot */ action.equals("android.intent.action.QUICKBOOT_POWERON")
              || /* htc devices */ action.equals("com.htc.intent.action.QUICKBOOT_POWERON");

      if (isValidBootAction) {
         context.startService(new Intent(context, AssignmentCheckerService.class));
      }
   }
}
