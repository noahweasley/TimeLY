package com.astrro.timely.assignment;

public class MultiUpdateMessage2 {
   private final EventType type;

   public MultiUpdateMessage2(EventType type) {
      this.type = type;
   }

   public EventType getType() {
      return type;
   }

   public enum EventType {
      INSERT, REMOVE
   }
}

