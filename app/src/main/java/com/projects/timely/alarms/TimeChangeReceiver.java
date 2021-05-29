package com.projects.timely.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.projects.timely.core.Time;

import org.greenrobot.eventbus.EventBus;

public class TimeChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Retrieve time and post to any UI controller that is current in it's active state
        Time time = TimeChangeDetector.requestImmediateTime(context);
        EventBus.getDefault().post(time);
    }
}
