package com.astrro.timely.auth.data.api;

public class ApiError {
   public int statusCode;
   public String messaage;

   public static ApiError fromErrorMessage(String message) {
      ApiError apiError = new ApiError();
      apiError.setMessaage(message);
      return apiError;
   }

   public int getStatusCode() {
      return statusCode;
   }

   public void setStatusCode(int statusCode) {
      this.statusCode = statusCode;
   }

   public String getMessaage() {
      return messaage;
   }

   public void setMessaage(String messaage) {
      this.messaage = messaage;
   }
}
