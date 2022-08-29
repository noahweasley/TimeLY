package com.astrro.timely.custom;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.astrro.timely.core.DataModel;

import java.util.List;

/**
 * Subclass of {@link RecyclerView.Adapter} to be used with all infinte scrolling adapters
 *
 * @param <T> a subclass of {@link RecyclerView.ViewHolder} to be used in the infinit scroll list
 */
public abstract class InfiniteScrollAdapter <T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {
   public static final int VIEW_TYPE_ITEM = 0;
   public static final int VIEW_TYPE_LOADING = 1;

   /**
    * gets the main item view used in the list
    *
    * @param container the container or parent view to attach the view's layout
    * @return the view that was attached
    */
   public abstract View getItemView(ViewGroup container);

   /**
    * gets the view showing that more items are loading
    *
    * @param container the container or parent view to attach the view's layout
    * @return the view that was attached
    */
   public abstract View getLoaderView(ViewGroup container);

   /**
    * @return the list to be used to retrieve data
    */
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
