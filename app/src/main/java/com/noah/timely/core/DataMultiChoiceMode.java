package com.noah.timely.core;

import java.util.ArrayList;
import java.util.List;

public class DataMultiChoiceMode extends MultiChoiceMode {
   private final List<DataModel> dataModels = new ArrayList<>();

   /**
    * @return the items that was previously selected
    */
   public DataModel[] getSelectedItems() {
      return dataModels.toArray(new DataModel[0]);
   }

   /**
    * Add to list of selected items
    *
    * @param item the to be added
    */
   public void addSelectedItem(DataModel item) {
      dataModels.add(item);
   }

   /**
    * Removes an item from the list of selected items
    *
    * @param item the data model to be removed or deleted
    */
   public void removeSelectedItem(DataModel item) {
      dataModels.remove(item);
   }

   /**
    * Use this instead of {@link MultiChoiceMode#selectAll(int)} for Data Models.
    *
    * @param dataPositions the list of positions as seen by the database
    * @param itemSize      the size of the list
    */
   public void selectAll(int itemSize, List<Integer> dataPositions) {
      clearChoices();
      // reverse entry, start from back
      for (int position = itemSize - 1; position >= 0; position--) {
         setChecked(position, true, dataPositions.get(position));
      }

   }

   /**
    * Use this instead of {@link MultiChoiceMode#setChecked(int, boolean)} for Data Models.
    *
    * @param position the position in which the its checked value is to be inserted
    * @param checked  the status of the checked item
    * @param dataPos  the original data position in database
    */
   public void setChecked(int position, boolean checked, int dataPos) {
      if (!checked) {
         sbarr.delete(position);
         indices.remove(Integer.valueOf(position));
         indices2.remove(Integer.valueOf(dataPos));
      } else {
         sbarr.put(position, true);
         indices.add(position);
         indices2.add(dataPos);
      }
   }

}
