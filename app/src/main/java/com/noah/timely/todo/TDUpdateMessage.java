package com.noah.timely.todo;

public class TDUpdateMessage {
   private final EventType type;
   private TodoModel data;
   private int changePosition;
   private int pagePosition;

   public TDUpdateMessage(TodoModel data, int pagePosition, EventType type) {
      this.data = data;
      this.type = type;
      this.pagePosition = pagePosition;
   }

   public TDUpdateMessage(int position, int pagePosition, EventType type) {
      this.changePosition = position;
      this.pagePosition = pagePosition;
      this.type = type;
   }

   public TDUpdateMessage(TodoModel model, int changePosition, int pagePosition, EventType eventType) {
      this(model, pagePosition, eventType);
      this.changePosition = changePosition;
   }

   public int getPagePosition() {
      return pagePosition;
   }

   public void setPagePosition(int pagePositionn) {
      this.pagePosition = pagePositionn;
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

