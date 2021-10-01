package com.noah.timely.exam;

public class EUpdateMessage {
   private final EventType type;
   private int position;
   private ExamModel data;
   private int pagePosition;

   public EUpdateMessage(ExamModel data, EventType type, int pagePosition) {
      this.data = data;
      this.type = type;
      this.pagePosition = pagePosition;
   }

   public int getPagePosition() {
      return pagePosition;
   }

   public void setPagePosition(int pagePosition) {
      this.pagePosition = pagePosition;
   }

   public int getPosition() {
      return position;
   }

   public void setPosition(int position) {
      this.position = position;
   }

   public ExamModel getData() {
      return data;
   }

   public void setData(ExamModel data) {
      this.data = data;
   }

   public EventType getType() {
      return type;
   }

   public enum EventType {
      NEW, UPDATE_CURRENT, REMOVE, INSERT
   }
}
