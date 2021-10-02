package com.noah.timely.todo;

public class TDUpdateMessage {
   private final EventType type;
   private TodoModel data;
   private int changePosition;

   public TDUpdateMessage(TodoModel data, EventType type) {
      this.data = data;
      this.type = type;
   }

   public TDUpdateMessage(int position, EventType type) {
      this.changePosition = position;
      this.type = type;
   }

   public TDUpdateMessage(TodoModel model, int changePosition, EventType eventType) {
      this(model, eventType);
      this.changePosition = changePosition;
   }

   public int getChangePosition() {
      return changePosition;
   }

   public void setChangePosition(int changePosition) {
      this.changePosition = changePosition;
   }

   public TodoModel getData() {
      return data;
   }

   public void setData(TodoModel data) {
      this.data = data;
   }

   public EventType getType() {
      return type;
   }

   public enum EventType {
      NEW, UPDATE_CURRENT, INSERT, REMOVE
   }
}

