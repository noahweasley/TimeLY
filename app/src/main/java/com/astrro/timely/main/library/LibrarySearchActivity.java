package com.astrro.timely.main.library;

import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.astrro.timely.R;
import com.astrro.timely.util.views.ViewHelper;

public class LibrarySearchActivity extends AppCompatActivity {
   private static final String EXTRA_SEARCH_QUERY = "Search_Query";

   @Override
   protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_library_search);

      SearchView sv_search = findViewById(R.id.search);
      ViewHelper.setupSearchView(sv_search);
      EditText edt_search = sv_search.findViewById(androidx.appcompat.R.id.search_src_text);
      String searchQueryExtra = getIntent().getStringExtra(EXTRA_SEARCH_QUERY);
      edt_search.setText(searchQueryExtra);
      edt_search.setSelection(searchQueryExtra.length());
   }
}
