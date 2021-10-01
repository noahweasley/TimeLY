package com.noah.timely.core;

/**
 * This class simplified the RequestRunner thread operations and make it remove code duplication.
 * The class itself is somewhat like a marker and all objects that provide data to the app's
 * database should extend this naturally. It provides some of the basic data that is required of
 * a DATA MODEL.
 */
public class DataModel {
   protected int id;
   protected int position;

   public int getId() {
      return id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public int getPosition() {
      return position;
   }

   public void setPosition(int position) {
      this.position = position;
   }
}
