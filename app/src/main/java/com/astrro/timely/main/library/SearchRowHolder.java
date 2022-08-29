package com.astrro.timely.main.library;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.astrro.timely.R;

import java.util.List;

public class SearchRowHolder extends RecyclerView.ViewHolder {
   private String suggestion;
   private final TextView tv_suggestion;

   public SearchRowHolder(@NonNull View itemView) {
      super(itemView);
      tv_suggestion = itemView.findViewById(R.id.text);
   }

   public SearchRowHolder with(List<String> suggestions) {
      this.suggestion = suggestions.get(getAbsoluteAdapterPosition());
      return this;
   }

   public void bindView() {
      tv_suggestion.setText(this.suggestion);
   }
}
