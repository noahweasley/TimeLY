package com.astrro.timely.util.adapters;

import androidx.appcompat.widget.SearchView;

public class SimpleQueryTextListener implements SearchView.OnQueryTextListener {
   @Override
   public boolean onQueryTextSubmit(String query) {
      return false;
   }

   @Override
   public boolean onQueryTextChange(String newText) {
      return false;
   }
}
