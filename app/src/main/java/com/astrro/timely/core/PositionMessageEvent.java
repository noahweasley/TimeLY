package com.astrro.timely.core;

public class PositionMessageEvent {
   private int position;
   private int pagePosition;

   public PositionMessageEvent(int position) {
      this.position = position;
   }

   public PositionMessageEvent(int position, int pagePosition) {
      this(position);
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

}
