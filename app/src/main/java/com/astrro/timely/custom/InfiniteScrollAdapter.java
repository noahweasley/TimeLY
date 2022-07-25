package com.astrro.timely.custom;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.astrro.timely.core.DataModel;

import java.util.List;

public abstract class InfiniteScrollAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {
   public static final int VIEW_TYPE_ITEM = 0;
   public static final int VIEW_TYPE_LOADING = 1;

   public abstract View getItemView(ViewGroup container);
   public abstract View getLoaderView(ViewGroup container);
   public abstract List<? extends DataModel> getList();

   public View getViewByViewType(ViewGroup container, int viewType) {
      if (viewType == VIEW_TYPE_ITEM) return getItemView(container);
      else return getLoaderView(container);
   }

   @NonNull
   @Override
   public T onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      return null;
   }

   @Override
   public void onBindViewHolder(@NonNull T holder, int position) {

   }

   @Override
   public int getItemCount() {
      return getList().size();
   }

   @Override
   public int getItemViewType(int position) {
      return getList().get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
   }
}
