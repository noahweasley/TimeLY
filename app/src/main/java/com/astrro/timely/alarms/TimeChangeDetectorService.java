package com.astrro.timely.alarms;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class TimeChangeDetectorService extends Service {
   private TimeChangeDetector timeChangeDetector;

   public TimeChangeDetectorService() {
   }

   @Override
   public IBinder onBind(Intent intent) {
      // TODO: Return the communication channel to the service.
      return null;
   }

   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
      tryActivateTimeChangeDetector();
      return START_STICKY;
   }

   @Override
   public void onLowMemory() {
      timeChangeDetector.pauseOperation();
      timeChangeDetector = null;
      super.onLowMemory();
   }

   @Override
   public void onDestroy() {
      tryActivateTimeChangeDetector();
      super.onDestroy();
   }

   private void tryActivateTimeChangeDetector() {
      if (timeChangeDetector == null) {
         (timeChangeDetector = new TimeChangeDetector().with(this)).start();
      } else {
         timeChangeDetector.pauseOperation();
         timeChangeDetector = null;
      }
   }

}