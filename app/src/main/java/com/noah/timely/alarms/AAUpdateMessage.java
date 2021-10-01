package com.noah.timely.alarms;

public class AAUpdateMessage {
   private final EventType type;
   private int position;
   private AlarmModel data;
   private int pagePosition;

   public AAUpdateMessage(AlarmModel data, int position, EventType type) {
      this.data = data;
      this.type = type;
      this.position = position;
   }

   public int getPosition() {
      return position;
   }

   public void setPosition(int position) {
      this.position = position;
   }

   public AlarmModel getData() {
      return data;
   }

   public void setData(AlarmModel data) {
      this.data = data;
   }

   public EventType getType() {
      return type;
   }

   public enum EventType {
      NEW, UPDATE_CURRENT, INSERT, REMOVE
   }
}
