package com.astrro.timely.auth.data.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
   int statusCode;
   String time;

   @SerializedName("user")
   UserAccount userAccount;

   @SerializedName("token")
   String jsonWebToken;

   public static LoginResponse fromStatusCode(int code) {
      LoginResponse loginResponse = new LoginResponse();
      loginResponse.setStatusCode(code);
      return loginResponse;
   }

   public int getStatusCode() {
      return statusCode;
   }

   public void setStatusCode(int statusCode) {
      this.statusCode = statusCode;
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
