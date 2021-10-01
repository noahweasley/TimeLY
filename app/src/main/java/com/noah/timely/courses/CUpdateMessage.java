package com.noah.timely.courses;

public class CUpdateMessage {
   private final EventType type;
   private int position;
   private CourseModel data;
   private int pagePosition;

   public CUpdateMessage(CourseModel data, EventType type, int pagePosition) {
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

   public CourseModel getData() {
      return data;
   }

   public void setData(CourseModel data) {
      this.data = data;
   }

   public EventType getType() {
      return type;
   }

   public enum EventType {
      NEW, UPDATE_CURRENT, INSERT, REMOVE
   }
}
