package com.noah.timely.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.noah.timely.core.Time;

import org.greenrobot.eventbus.EventBus;

public class TimeChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Retrieve time and post to any UI controller that is current in it's active state

        // also check the action that was sent to this receiver
        String action = intent.getAction();
        boolean isTimeChangeEvent =
                TextUtils.equals(Intent.ACTION_TIME_CHANGED, action)
                        || TextUtils.equals(Intent.ACTION_TIMEZONE_CHANGED, action)
                        || TextUtils.equals(Intent.ACTION_DATE_CHANGED, action);
        if (isTimeChangeEvent) {
            Time time = TimeChangeDetector.requestImmediateTime(context);
            EventBus.getDefault().post(time);
        }
    }
}
