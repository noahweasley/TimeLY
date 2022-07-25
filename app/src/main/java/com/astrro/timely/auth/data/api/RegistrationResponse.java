package com.astrro.timely.auth.data.api;

import com.google.gson.annotations.SerializedName;

public class RegistrationResponse extends IResponse {

   @SerializedName("registered")
   private boolean isUserRegistered;

   public boolean isUserRegistered() {
      return isUserRegistered;
   }

   public void setUserRegistered(boolean userRegistered) {
      isUserRegistered = userRegistered;
   }
}
