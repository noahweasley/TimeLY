package com.astrro.timely.main.library;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.astrro.timely.R;
import com.astrro.timely.core.DataModel;
import com.astrro.timely.custom.InfiniteScrollAdapter;
import com.astrro.timely.util.test.DummyGenerator;
import com.google.android.material.divider.MaterialDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {
   public static final String ARG_POSITION = "Page Position";
   private List<DataModel> libraryList = new ArrayList<>();
   private boolean isLoading;
   private LibraryAdapter libraryAdapter;

   public static LibraryFragment newInstance(int position) {
      Bundle args = new Bundle();
      args.putInt(ARG_POSITION, position);
      LibraryFragment fragment = new LibraryFragment();
      fragment.setArguments(args);
      return fragment;
   }

   @Nullable
   @org.jetbrains.annotations.Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater,
                            @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                            @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
      return inflater.inflate(R.layout.fragment_library, container, false);
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);

      // testing
       libraryList = DummyGenerator.getDummyDocument(10);
      // testing

      LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
      MaterialDividerItemDecoration itemDecoration
              = new MaterialDividerItemDecoration(getContext(), layoutManager.getOrientation());

      RecyclerView rv_materialList = view.findViewById(R.id.materials);
      rv_materialList.setLayoutManager(layoutManager);
      rv_materialList.setHasFixedSize(true);
      rv_materialList.addItemDecoration(itemDecoration);
      rv_materialList.setAdapter(libraryAdapter = new LibraryAdapter());
      initScrollDetectionFor(rv_materialList);
   }

   // starts end of scroll detection
   private void initScrollDetectionFor(RecyclerView rV_followersList) {
      rV_followersList.addOnScrollListener(new RecyclerView.OnScrollListener() {

         @Override
         public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            // get more materials when second-to-last material is visible
            LinearLayoutManager lmgr = (LinearLayoutManager) recyclerView.getLayoutManager();
            int lastVisibleItemPos = lmgr.findLastCompletelyVisibleItemPosition();
            int listSize = libraryList.size() - 1;
            if (!isLoading && lmgr != null && lastVisibleItemPos == listSize) {
               isLoading = true;
               getMoreMaterials();
            }
         }

         // debugging mode: gets more rows
         private void getMoreMaterials() {
            libraryList.add(null);
            int lastItem = libraryList.size() - 1;
            libraryAdapter.notifyItemInserted(lastItem);

            Handler handler = new Handler();
            handler.postDelayed(() -> {
               int scrollPos = libraryList.size();
               libraryList.remove(lastItem);
               libraryAdapter.notifyItemRemoved(scrollPos);
               libraryList.addAll(DummyGenerator.getDummyDocument(5));
               libraryAdapter.notifyDataSetChanged();
               isLoading = false;
            }, 5000);

         }

      });
   }

   private class LibraryAdapter extends InfiniteScrollAdapter<MaterialRow> {

      @NonNull
      @Override
      public MaterialRow onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         super.onCreateViewHolder(parent, viewType);
         return new MaterialRow(getViewByViewType(parent, viewType), viewType);
      }

      @Override
      public void onBindViewHolder(@NonNull MaterialRow holder, int position) {
         super.onBindViewHolder(holder, position);
         holder.with(position).bindView();
      }

      @Override
      public View getItemView(ViewGroup container) {
         return getLayoutInflater().inflate(R.layout.library_row, container, false);
      }

      @Override
      public View getLoaderView(ViewGroup container) {
         return getLayoutInflater().inflate(R.layout.row_items_loading, container, false);
      }

      @Override
      public List<? extends DataModel> getList() {
         return libraryList;
      }

   }

}
