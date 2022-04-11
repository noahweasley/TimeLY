package com.noah.timely.auth.data.model;

public class UserAccount {
   private String userId;
   private String displayName;

   public UserAccount(String userId, String displayName) {
      this.userId = userId;
      this.displayName = displayName;
   }

   public void setUserId(String userId) {
      this.userId = userId;
   }

   public void setDisplayName(String displayName) {
      this.displayName = displayName;
   }

   public String getUserId() {
      return userId;
   }

   public String getDisplayName() {
      return displayName;
   }
}
