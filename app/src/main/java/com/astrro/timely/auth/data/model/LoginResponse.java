package com.astrro.timely.auth.data.model;

import com.astrro.timely.auth.data.api.IResponse;
import com.google.gson.annotations.SerializedName;

public class LoginResponse extends IResponse {
   private String time;

   @SerializedName("user")
   private UserAccount userAccount;

   @SerializedName("token")
   String jsonWebToken;

   public static LoginResponse fromStatusCode(int code) {
      LoginResponse loginResponse = new LoginResponse();
      loginResponse.setStatusCode(code);
      return loginResponse;
   }

   public UserAccount getUserAccount() {
      return userAccount;
   }

   public void setUserAccount(UserAccount userAccount) {
      this.userAccount = userAccount;
   }

   public String getJsonWebToken() {
      return jsonWebToken;
   }

   public void setJsonWebToken(String jsonWebToken) {
      this.jsonWebToken = jsonWebToken;
   }

   public String getTime() {
      return time;
   }

   public void setTime(String time) {
      this.time = time;

   }
}
