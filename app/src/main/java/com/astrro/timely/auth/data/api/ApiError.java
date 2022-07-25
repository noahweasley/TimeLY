package com.astrro.timely.auth.data.api;

public class ApiError extends IResponse {
   public String messaage;

   public static ApiError fromErrorMessage(String message) {
      ApiError apiError = new ApiError();
      apiError.setMessaage(message);
      return apiError;
   }

   public String getMessaage() {
      return messaage;
   }

   public void setMessaage(String messaage) {
      this.messaage = messaage;
   }
}
