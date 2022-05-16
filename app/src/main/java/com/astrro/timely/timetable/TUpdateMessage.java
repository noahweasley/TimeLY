package com.astrro.timely.timetable;

public class TUpdateMessage {
   private final EventType type;
   private int position;
   private TimetableModel data;
   private int pagePosition;

   public TUpdateMessage(TimetableModel data, int pagePosition, EventType type) {
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

   public TimetableModel getData() {
      return data;
   }

   public void setData(TimetableModel data) {
      this.data = data;
   }

   public EventType getType() {
      return type;
   }

   public enum EventType {
      NEW, UPDATE_CURRENT, INSERT, REMOVE
   }
}
