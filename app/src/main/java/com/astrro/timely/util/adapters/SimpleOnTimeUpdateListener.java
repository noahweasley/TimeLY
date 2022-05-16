package com.astrro.timely.util.adapters;

import com.astrro.timely.custom.CountdownTimer;

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
