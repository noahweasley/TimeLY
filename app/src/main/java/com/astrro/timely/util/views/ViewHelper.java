package com.astrro.timely.util.views;

import android.widget.EditText;

import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;

import com.astrro.timely.R;

public class ViewHelper {

   public static void setupSearchView(SearchView searchView) {
      EditText edt_search = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
      edt_search.setHintTextColor(ContextCompat.getColor(searchView.getContext(), R.color.light_grey));
      edt_search.setTextColor(ContextCompat.getColor(searchView.getContext(), R.color.searchview_text_color));
   }

}
