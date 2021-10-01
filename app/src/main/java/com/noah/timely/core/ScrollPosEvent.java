package com.noah.timely.core;

public class ScrollPosEvent {

   private int position;

   public ScrollPosEvent(int position) {
      this.position = position;
   }

   public int getPosition() {
      return position;
   }

   public void setPosition(int position) {
      this.position = position;
   }
}
