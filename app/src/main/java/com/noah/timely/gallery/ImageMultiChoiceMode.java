package com.noah.timely.gallery;

import android.net.Uri;

import com.noah.timely.core.MultiChoiceMode;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link MultiChoiceMode} subclass that can get access to the uris of the images selected
 */
public class ImageMultiChoiceMode extends MultiChoiceMode {
   private final List<String> uriList = new ArrayList<>();

   /**
    * @return the uris of the items that was previously selected
    */
   public String[] getUriList() {
      String[] uris = uriList.toArray(new String[0]);
      uriList.clear();
      return uris;
   }

   /**
    * Add to list of selected items
    *
    * @param uri the uri of the item to be added
    */
   public void addImageUri(Uri uri) {
      uriList.add(uri.toString());
   }

   /**
    * Add list of uris as currently selected items
    *
    * @param uris the uri list of items to be added
    */
   public void addAllImageUri(List<Uri> uris) {
      for (Uri uri : uris) addImageUri(uri);
   }

   /**
    * Removes an item from the list of selected items
    *
    * @param uri the uri to be removed or deleted
    */
   public void removeImageUri(Uri uri) {
      uriList.remove(uri.toString());
   }

   @Override
   public void clearChoices() {
      super.clearChoices();
      uriList.clear();
   }

   /**
    * used this to persist the state of the checked items. If <code>checked</code> is false, then
    * any previous entry (when <code>checked</code> was true) in the choice mode, would be erased,
    * as it is not useful.
    *
    * @param position the position in which the its checked value is to be inserted
    * @param check    the status of the checked item
    */
   public void setChecked(int position, boolean checked) {
      if (!checked) {
         sbarr.delete(position);
         indices.remove(Integer.valueOf(position));
      } else {
         sbarr.put(position, true);
         indices.add(position);
      }
   }

   /**
    * selects all items
    *
    * @param itemSize the size of the list
    */
   public void selectAll(int itemSize) {
      clearChoices();
      // reverse entry, start from back
      for (int position = itemSize - 1; position >= 0; position--) {
         setChecked(position, true);
      }

   }
}