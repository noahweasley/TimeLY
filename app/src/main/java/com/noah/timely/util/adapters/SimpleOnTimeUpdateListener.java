package com.noah.timely.util.adapters;

import com.noah.timely.custom.CountdownTimer;

/**
 * A simple adapter class for {@link CountdownTimer#OnTimeUpdateListner}
 */
public class SimpleOnTimeUpdateListener implements CountdownTimer.OnTimerUpdateListener {
   @Override
   public void onTimerEnd() {

   }

   @Override
   public void onTimeDecrement(long counRemain) {

   }
}
