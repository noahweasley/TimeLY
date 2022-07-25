package com.astrro.timely.main.library;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.astrro.timely.R;
import com.astrro.timely.custom.InfiniteScrollAdapter;

@SuppressWarnings("FieldCanBeLocal")
public class MaterialRow extends RecyclerView.ViewHolder {
   private final int viewType;
   private int position;
   private TextView counter;

   public MaterialRow(@NonNull View itemView, int viewType) {
      super(itemView);
      this.viewType = viewType;

      if (viewType == InfiniteScrollAdapter.VIEW_TYPE_ITEM) {
         counter = itemView.findViewById(R.id.counter);
      }
   }

   public MaterialRow with(int position) {
      this.position = position;
      return this;
   }

   public void bindView() {
      if (viewType == InfiniteScrollAdapter.VIEW_TYPE_ITEM) {
         counter.setText(String.valueOf(position));
      }
   }

}
