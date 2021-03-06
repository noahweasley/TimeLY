package com.noah.timely.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.noah.timely.core.SchoolDatabase;

/**
 * <p>An alarm re-scheduler that re-schedules alarm from TimeLY's database.</p>
 * <p>
 * When user devices switches off, all alarms are terminated and no longer exists.
 * On re-boot, this receiver would re-set the alarms that was previously set.
 */
public class AlarmReScheduler extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SchoolDatabase database = new SchoolDatabase(context);

        String action = intent.getAction();
        boolean isValidBootAction
                = /* Normal device boot */ action.equals("android.intent.action.BOOT_COMPLETED")
                || /* on TimeLY upgrade */ action.equals("android.intent.action.PACKAGE_REPLACED")
                || /* Device locked */ action.equals("android.intent.action.LOCKED_BOOT_COMPLETED")
                || /* quick boot */ action.equals("android.intent.action.QUICKBOOT_POWERON")
                || /* htc devices */ action.equals("com.htc.intent.action.QUICKBOOT_POWERON");

        if (isValidBootAction) {
            context.startService(new Intent(context, AlarmReSchedulerService.class));
        }
    }
}