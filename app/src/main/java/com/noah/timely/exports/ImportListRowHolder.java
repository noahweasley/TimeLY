package com.noah.timely.exports;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.noah.timely.R;
import com.noah.timely.core.DataModel;
import com.noah.timely.util.Constants;

import java.util.List;
import java.util.Map;

@SuppressWarnings("FieldCanBeLocal")
public class ImportListRowHolder extends RecyclerView.ViewHolder {
   private int position;
   private Context context;
   private ImportResultsActivity.ImportListRowAdapter adapter;
   private List<Map.Entry<String, List<? extends DataModel>>> datamap;
   private final TextView tv_contentText, tv_contentCount;
   private final View headerTop, headerBottom;
   private final CheckBox cbx_state;

   private static final int[] COLOR = {
           android.R.color.holo_purple,
           android.R.color.holo_green_light,
           android.R.color.holo_blue_dark,
           android.R.color.holo_orange_dark,
           android.R.color.holo_red_light
   };

   private static final int[] DRAWABLE = {
           R.drawable.bg_holo_purple_5,
           R.drawable.bg_holo_green_dark_5,
           R.drawable.bg_holo_blue_dark_5,
           R.drawable.bg_holo_orange_dark_5,
           R.drawable.bg_holo_red_light_5
   };

   public ImportListRowHolder(@NonNull View itemView) {
      super(itemView);
      tv_contentText = itemView.findViewById(R.id.content_text);
      tv_contentCount = itemView.findViewById(R.id.content_count);
      headerBottom = itemView.findViewById(R.id.header_bottom);
      headerTop = itemView.findViewById(R.id.header_top);
      cbx_state = itemView.findViewById(R.id.checkbox);

      cbx_state.setOnCheckedChangeListener((v, isChecked) -> adapter.onChecked(position, isChecked));

   }

   public ImportListRowHolder with(int position, Context context,
                                   ImportResultsActivity.ImportListRowAdapter adapter,
                                   List<Map.Entry<String, List<? extends DataModel>>> datamap) {
      this.position = position;
      this.context = context;
      this.adapter = adapter;
      adapter.onChecked(position, true); // at the default, all the data would be imported if user doesn't toggle states
      this.datamap = datamap;
      return this;
   }

   public void bindView() {
      Map.Entry<String, List<? extends DataModel>> entry = datamap.get(position);
      switch (entry.getKey() /* The data model constant */) {
         case Constants.ASSIGNMENT:
            tv_contentText.setText("Assignments");
            headerTop.setBackgroundColor(ContextCompat.getColor(context, COLOR[0]));
            headerBottom.setBackgroundColor(ContextCompat.getColor(context, COLOR[0]));
            tv_contentCount.setBackground(ContextCompat.getDrawable(context, DRAWABLE[0]));
            break;
         case Constants.COURSE:
            tv_contentText.setText("Registered Courses");
            headerTop.setBackgroundColor(ContextCompat.getColor(context, COLOR[1]));
            headerBottom.setBackgroundColor(ContextCompat.getColor(context, COLOR[1]));
            tv_contentCount.setBackground(ContextCompat.getDrawable(context, DRAWABLE[1]));
            break;
         case Constants.EXAM:
            tv_contentText.setText("Exams");
            headerTop.setBackgroundColor(ContextCompat.getColor(context, COLOR[2]));
            headerBottom.setBackgroundColor(ContextCompat.getColor(context, COLOR[2]));
            tv_contentCount.setBackground(ContextCompat.getDrawable(context, DRAWABLE[2]));
            break;
         case Constants.TIMETABLE:
            tv_contentText.setText("Timetable");
            headerTop.setBackgroundColor(ContextCompat.getColor(context, COLOR[3]));
            headerBottom.setBackgroundColor(ContextCompat.getColor(context, COLOR[3]));
            tv_contentCount.setBackground(ContextCompat.getDrawable(context, DRAWABLE[3]));
            break;
         case Constants.SCHEDULED_TIMETABLE:
            tv_contentText.setText("Scheduled Classes");
            headerTop.setBackgroundColor(ContextCompat.getColor(context, COLOR[4]));
            headerBottom.setBackgroundColor(ContextCompat.getColor(context, COLOR[4]));
            tv_contentCount.setBackground(ContextCompat.getDrawable(context, DRAWABLE[4]));
            break;
      }

      tv_contentCount.setText(String.valueOf(entry.getValue().size())); // list.size()
   }

}
