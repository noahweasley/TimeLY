package com.astrro.timely.main.library;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.astrro.timely.R;
import com.astrro.timely.util.adapters.SimpleQueryTextListener;
import com.astrro.timely.util.views.ViewHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class StudentLibraryFragment extends Fragment {
   public static final String TAG = "com.astrro.timely.main.library.StudentLibraryFragment";
   private static final String EXTRA_SEARCH_QUERY = "Search_Query";
   private static Fragment fragmentInstance;
   private static final String TOOLBAR_TITLE = "Student Library";

   public static Fragment getInstance() {
      return fragmentInstance == null ? (fragmentInstance = new StudentLibraryFragment()) : fragmentInstance;
   }

   public static String getToolbarTitle() {
      return TOOLBAR_TITLE;
   }

   @Nullable
   @org.jetbrains.annotations.Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater,
                            @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                            @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
      return inflater.inflate(R.layout.fragment_student_library, container, false);
   }

   @Override
   public void onResume() {
      super.onResume();
      ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(TOOLBAR_TITLE);
   }

   @Override
   public void onViewCreated(@NonNull View view,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);

      FloatingActionButton fab_addDocument = view.findViewById(R.id.add_document);
      fab_addDocument.setOnClickListener(v -> AddDocumentActivity.start(getContext()));

      SearchView sv_search = view.findViewById(R.id.search);
      sv_search.setOnQueryTextListener(new QueryTextListener());
      ViewHelper.setupSearchView(sv_search);

      ViewPager2 pager = view.findViewById(R.id.pager);
      pager.setAdapter(new StudentLibraryAdapter(this));
      TabLayout tabs = view.findViewById(R.id.tabs);
      tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
      tabs.setTabGravity(TabLayout.GRAVITY_CENTER);

      new TabLayoutMediator(tabs, pager, (tab, position) -> {
         if (position == 0) {
            tab.setText(R.string.recent);
         } else if (position == 1) {
            tab.setText(R.string.recommended);
         } else {
            tab.setText(R.string.personal);
         }
      }).attach();
   }

   @Override
   public void onDetach() {
      super.onDetach();
      fragmentInstance = null;
   }

   @Override
   protected void finalize() throws Throwable {
      fragmentInstance = null;
      super.finalize();
   }

   private class QueryTextListener extends SimpleQueryTextListener {

      @Override
      public boolean onQueryTextSubmit(String query) {
         Intent intent = new Intent(getContext(), LibrarySearchActivity.class);
         intent.putExtra(EXTRA_SEARCH_QUERY, query);
         startActivity(intent);
         return true;
      }

   }

   private static class StudentLibraryAdapter extends FragmentStateAdapter {

      public StudentLibraryAdapter(@NonNull Fragment fragment) {
         super(fragment);
      }

      @NonNull
      @Override
      public Fragment createFragment(int position) {
         return LibraryFragment.newInstance(position);
      }

      @Override
      public int getItemCount() {
         return 3;
      }
   }
}
