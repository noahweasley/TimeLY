package com.astrro.timely.auth.data.model;

import com.astrro.timely.auth.data.api.IResponse;
import com.google.gson.annotations.SerializedName;

public class RegistrationResponse extends IResponse {

   @SerializedName("registered")
   private boolean isUserRegistered;

   public static RegistrationResponse fromStatusCode(int code) {
      RegistrationResponse registrationResponse = new RegistrationResponse();
      registrationResponse.setStatusCode(code);
      return registrationResponse;
   }

   public boolean isUserRegistered() {
      return isUserRegistered;
   }

   public void setUserRegistered(boolean userRegistered) {
      isUserRegistered = userRegistered;
   }

}
