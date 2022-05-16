package com.astrro.timely.calculator;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.astrro.timely.R;
import com.astrro.timely.core.DataModel;
import com.astrro.timely.courses.CourseModel;
import com.astrro.timely.util.PreferenceUtils;
import com.astrro.timely.util.adapters.SimpleOnItemSelectedListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ResultListRowHolder extends RecyclerView.ViewHolder {
   private int position;
   private final TextView tv_credits;
   private final TextView tv_courseName;
   private static final String[] GRADES = { "A", "B", "C", "D", "E", "F" };
   private static final String[] GRADES_2 = { "A", "B", "C", "D", "E" };
   public static String[] selectedGrades;
   public static String[][] selectedGradesArr;
   public static int totalUnits;
   private CourseModel courseModel;
   private int semesterIndex;
   private int listSize;
   private final int[] listSizes = new int[2];

   public ResultListRowHolder(@NonNull View itemView) {
      super(itemView);
      Context context = itemView.getContext();

      Spinner spin_grades = itemView.findViewById(R.id.grades);
      tv_credits = itemView.findViewById(R.id.credits);
      tv_courseName = itemView.findViewById(R.id.course_name);

      // select which grade array that is to be used
      int maxGPAScale = Integer.parseInt(PreferenceUtils.getStringValue(context, "max_gpa_scale", "5"));
      String[] GRADESS = maxGPAScale == 5 ? GRADES : GRADES_2;

      ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(context, R.layout.simple_spinner_item, GRADESS);
      courseAdapter.setDropDownViewResource(R.layout.simple_dropdown_item_1line);
      spin_grades.setAdapter(courseAdapter);

      spin_grades.setOnItemSelectedListener(new SimpleOnItemSelectedListener() {

         @Override
         public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            selectedGrades[position] = GRADESS[i];
            selectedGradesArr[semesterIndex][position] = GRADESS[i];
         }

      });

   }

   public ResultListRowHolder with(int position, int semesterIndex, List<DataModel> courseModelList) {
      this.semesterIndex = semesterIndex;
      listSize = courseModelList.size();
      listSizes[semesterIndex] = courseModelList.size();
      initializeGrades();
      this.position = position;
      this.courseModel = (CourseModel) courseModelList.get(position);
      return this;
   }

   public void bindView() {
      tv_courseName.setText(courseModel.getCourseName());
      int credits = courseModel.getCredits();
      String append = credits > 1 || credits == 0 ? "S" : "";
      tv_credits.setText(String.format(Locale.US, "%d CREDIT%s", credits, append));
   }

   public static String[] getSelectedGrades() {
      return selectedGrades;
   }

   public static void doCleanUp() {
      selectedGrades = null;
      selectedGradesArr = null;
      totalUnits = 0;
   }

   public static int getTotalUnits() {
      return totalUnits;
   }

   /**
    * @return the user's score map
    */
   public Map<Integer, String[]> getScoreMap() {
      initializeGrades();
      Map<Integer, String[]> scoreMap = new HashMap<>();
      for (int i = 0; i < selectedGrades.length; i++) {
         String selectedGrade = selectedGrades[i];
         String credits = String.valueOf(courseModel.getCredits());
         scoreMap.put(i, new String[]{ credits, selectedGrade });
      }

      return scoreMap;
   }

   /**
    * @return both the user's score map; first and second semester
    */
   @SuppressWarnings("unchecked")
   public Map<Integer, String[]>[] getScoreMaps() {
      initializeGrades();
      HashMap<Integer, String[]> scoreMap = new HashMap<>();
      HashMap<Integer, String[]> scoreMap2 = new HashMap<>();
      Map<Integer, String[]>[] scoreMaps = new HashMap[]{ scoreMap, scoreMap2 };

      for (int row = 0; row < selectedGradesArr.length; row++) {
         for (int column = 0; column < selectedGradesArr[row].length; column++) {
            String selectedGrade = selectedGradesArr[row][column];
            String credits = String.valueOf(courseModel.getCredits());
            scoreMaps[row].put(row, new String[]{ credits, selectedGrade });
         }
      }

      return scoreMaps;
   }

   private void initializeGrades() {
      // initialize and fill up selected grades to the defaults
      if (selectedGrades == null) {
         selectedGrades = new String[listSize];
         Arrays.fill(selectedGrades, "A");
      }

      if (selectedGradesArr == null) {
         selectedGradesArr = new String[2][listSizes[semesterIndex]]; // 2 = semester count
         for (String[] row : selectedGradesArr) {
            Arrays.fill(row, "A");
         }
      }
   }

}
