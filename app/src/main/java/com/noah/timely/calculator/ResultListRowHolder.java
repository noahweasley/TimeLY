package com.noah.timely.calculator;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.noah.timely.R;
import com.noah.timely.core.DataModel;
import com.noah.timely.util.adapters.SimpleOnItemSelectedListener;

import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class ResultListRowHolder extends RecyclerView.ViewHolder {
   private int position;
   private List<DataModel> courseModelList;
   private final Spinner spin_grades;
   private static final String[] GRADES = { "A", "B", "C", "E", "F" };
   private String selectedGrade = GRADES[0];

   public ResultListRowHolder(@NonNull View itemView) {
      super(itemView);
      Context context = itemView.getContext();
      spin_grades = itemView.findViewById(R.id.grades);

      ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(context, R.layout.simple_spinner_item, GRADES);
      courseAdapter.setDropDownViewResource(R.layout.simple_dropdown_item_1line);
      spin_grades.setAdapter(courseAdapter);

      spin_grades.setOnItemSelectedListener(new SimpleOnItemSelectedListener() {

         @Override
         public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
          selectedGrade = GRADES[position];
         }

      });


   }

   public ResultListRowHolder with(int position, List<DataModel> courseModelList) {
      this.position = position;
      this.courseModelList = courseModelList;
      return this;
   }

   public void bindView() {

   }

}
