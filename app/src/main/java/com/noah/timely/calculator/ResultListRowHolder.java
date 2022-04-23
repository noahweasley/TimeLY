package com.noah.timely.calculator;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.noah.timely.R;
import com.noah.timely.core.DataModel;
import com.noah.timely.courses.CourseModel;
import com.noah.timely.util.adapters.SimpleOnItemSelectedListener;

import java.util.Arrays;
import java.util.List;

public class ResultListRowHolder extends RecyclerView.ViewHolder {
   private int position;
   private List<DataModel> courseModelList;
   private final TextView tv_credits;
   private final TextView tv_courseName;
   private static final String[] GRADES = { "A", "B", "C", "E", "F" };
   public static String[] selectedGrades;

   public ResultListRowHolder(@NonNull View itemView) {
      super(itemView);
      Context context = itemView.getContext();

      Spinner spin_grades = itemView.findViewById(R.id.grades);
      tv_credits = itemView.findViewById(R.id.credits);
      tv_courseName = itemView.findViewById(R.id.course_name);

      ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(context, R.layout.simple_spinner_item, GRADES);
      courseAdapter.setDropDownViewResource(R.layout.simple_dropdown_item_1line);
      spin_grades.setAdapter(courseAdapter);

      spin_grades.setOnItemSelectedListener(new SimpleOnItemSelectedListener() {

         @Override
         public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            selectedGrades[position] = GRADES[i];
         }

      });

   }

   public ResultListRowHolder with(int position, List<DataModel> courseModelList) {
      // initialize and fill up selected grades to the defaults
      if (selectedGrades == null) {
         selectedGrades = new String[courseModelList.size()];
         Arrays.fill(selectedGrades, "A");
      }

      this.position = position;
      this.courseModelList = courseModelList;
      return this;
   }

   public void bindView() {
      CourseModel courseModel = (CourseModel) courseModelList.get(position);
      tv_courseName.setText(courseModel.getCourseName());
      tv_credits.setText(String.valueOf(courseModel.getCredits()));
   }

   public static String[] getSelectedGrades() {
      return selectedGrades;
   }

}
