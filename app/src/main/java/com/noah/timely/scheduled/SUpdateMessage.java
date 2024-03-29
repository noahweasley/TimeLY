package com.noah.timely.scheduled;

import com.noah.timely.timetable.TimetableModel;

public class SUpdateMessage {
   private final EventType type;
   private int position;
   private TimetableModel data;

   public SUpdateMessage(TimetableModel data, EventType type) {
      this.data = data;
      this.type = type;
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
