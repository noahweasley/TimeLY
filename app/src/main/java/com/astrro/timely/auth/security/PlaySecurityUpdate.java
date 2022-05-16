package com.astrro.timely.auth.security;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PlaySecurityUpdate {
   private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

   public static Future<Boolean> updateNetworkSecurity(@NonNull Context context) {

      return executorService.submit(() -> {
         try {
            ProviderInstaller.installIfNeeded(context);
         } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
            return false;
         }

         return true;

      });
   }

}
