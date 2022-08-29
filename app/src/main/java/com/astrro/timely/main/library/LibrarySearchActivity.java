package com.astrro.timely.main.library;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.astrro.timely.R;
import com.astrro.timely.core.SchoolDatabase;
import com.astrro.timely.util.views.ViewHelper;
import com.google.android.material.divider.MaterialDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class LibrarySearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
   private static final String EXTRA_SEARCH_QUERY = "Search_Query";
   private List<String> suggestions = new ArrayList<>();
   private SuggestionsRowAdapter adapter;
   private SchoolDatabase database;

   @Override
   protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_library_search);

      database = new SchoolDatabase(this);
      SearchView sv_search = findViewById(R.id.search);
      RecyclerView rv_suggestionList = findViewById(R.id.suggestions);

      ViewHelper.setupSearchView(sv_search);
      EditText edt_search = sv_search.findViewById(androidx.appcompat.R.id.search_src_text);
      String searchQueryExtra = getIntent().getStringExtra(EXTRA_SEARCH_QUERY);
      edt_search.setText(searchQueryExtra);
      edt_search.setSelection(searchQueryExtra.length());

      sv_search.setOnQueryTextListener(this);
      rv_suggestionList.setHasFixedSize(true);
      rv_suggestionList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));
      rv_suggestionList
              .addItemDecoration(new MaterialDividerItemDecoration(this, MaterialDividerItemDecoration.VERTICAL));
      rv_suggestionList.setAdapter(adapter = new SuggestionsRowAdapter());

      edt_search.setOnFocusChangeListener((v, hasFocus) -> {
         if (hasFocus) {
            suggestions = database.getAllLibrarySearchQueries();
            adapter.notifyDataSetChanged();
            rv_suggestionList.setVisibility(View.VISIBLE);
         }
      });

   }

   @Override
   public boolean onQueryTextSubmit(String query) {
      database.addLibrarySearchQuery(query);
      return true;
   }

   @Override
   public boolean onQueryTextChange(String newText) {
      return false;
   }

   @Override
   protected void onDestroy() {
      database.close();
      super.onDestroy();
   }

   private class SuggestionsRowAdapter extends RecyclerView.Adapter<SearchRowHolder> {

      @NonNull
      @Override
      public SearchRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         View itemView = getLayoutInflater().inflate(R.layout.search_query_row, parent, false);
         return new SearchRowHolder(itemView);
      }

      @Override
      public void onBindViewHolder(@NonNull SearchRowHolder holder, int position) {
         holder.with(suggestions).bindView();
      }

      @Override
      public int getItemCount() {
         return suggestions.size();
      }

   }
}
