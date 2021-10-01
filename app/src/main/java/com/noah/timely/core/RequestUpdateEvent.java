package com.noah.timely.core;

public class RequestUpdateEvent {

   private UpdateType updateType;
   private int changePosition;

   public RequestUpdateEvent(UpdateType updateType, int changePosition) {
      this.updateType = updateType;
      this.changePosition = changePosition;
   }

   public UpdateType getUpdateType() {
      return updateType;
   }

   public void setUpdateType(UpdateType updateType) {
      this.updateType = updateType;
   }

   public int getChangePosition() {
      return changePosition;
   }

   public void setChangePosition(int changePosition) {
      this.changePosition = changePosition;
   }

   public enum UpdateType {INSERT, REMOVE}
}
