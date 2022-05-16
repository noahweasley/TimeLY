package com.astrro.timely.calculator;

import android.content.Context;

import com.astrro.timely.core.DataModel;
import com.astrro.timely.courses.CourseModel;
import com.astrro.timely.util.PreferenceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The G.P.A Calculator
 */
public class Calculator {

   /**
    * Calculates G.P.A, returning a future with results. A custom  {@link java.util.concurrent.Future} and
    * {@link java.util.concurrent.CompletableFuture} was used to support pre Android API Level 26 devices.
    *
    * @param context  the contexts that would be used to get user settings
    * @param scoreMap the score map of the user
    * @return a future result of the calculation and resolves to a floating point value
    * @see java.util.concurrent.Future
    * @see java.util.concurrent.CompletableFuture
    */
   public static float calculateGPA(Context context, Map<Integer, String[]> scoreMap) {
      Set<Map.Entry<Integer, String[]>> entries = scoreMap.entrySet();
      double totalCredits = 0.0f;
      double totalScore = 0.0f;

      // key = credits; value = score
      for (Map.Entry<Integer, String[]> entry : entries) {
         try {
            String[] scoreArray = entry.getValue();
            int credit = Integer.parseInt(scoreArray[0]);
            String score = scoreArray[1];

            totalCredits += credit;
            totalScore += getGradeScore(context, scoreArray);
         } catch (NumberFormatException exc) {
            return 0.0f;
         }
      }

      // then do final gpa calculations
      float value = (float) totalScore / (float) totalCredits;
      // just in case the user miraclously is offering only one course that semester and that course is a zero credit
      // unit course. ;=)
      if (Float.isNaN(value)) {
         return 0.0f;
      } else return value;
   }

   // easily get grade scores by multiplying grades with credits
   private static int getGradeScore(Context context, String[] grade) throws NumberFormatException {
      int gradeScore = 0;
      int multiplier = Integer.parseInt(grade[0]);
      int maxGPAScale = Integer.parseInt(PreferenceUtils.getStringValue(context, "max_gpa_scale", "5"));

      switch (grade[1]) {
         case "A": {
            gradeScore = (maxGPAScale) * multiplier;
         }
         break;
         case "B": {
            gradeScore = (maxGPAScale - 1) * multiplier;
         }
         break;
         case "C": {
            gradeScore = (maxGPAScale - 2) * multiplier;
         }
         break;
         case "D": {
            gradeScore = (maxGPAScale - 3) * multiplier;
         }
         break;
         case "E": {
            gradeScore = (maxGPAScale - 4) * multiplier;
         }
         break;
         default: {
            gradeScore = (maxGPAScale - 5) * multiplier;
         }
      }

      return gradeScore;
   }

   /**
    * @param context         the context required to access resources
    * @param courseModelList the course mode list in which data would be retrieved from
    * @return the total credit units of a semester from the course model list used
    */
   public static double getTotalCredits(Context context, List<DataModel> courseModelList) {
      // couldn't find a better way to do this because, Collection.stream() isn't backported and I couldn't find out
      // a way to reproduce Stream.reduce()
      int maxGPAScale = Integer.parseInt(PreferenceUtils.getStringValue(context, "max_gpa_scale", "5"));
      List<Integer> creditsList = new ArrayList<>();
      for (DataModel courseModel : courseModelList) {
         CourseModel courseModel1 = (CourseModel) courseModel;
         creditsList.add(courseModel1.getCredits() * maxGPAScale);
      }

      int counter = 0;
      for (int val : creditsList) {
         counter += val;
      }

      return Double.valueOf(counter);
   }

   /**
    * Simple function that just calculates the average semester of both first and second semester
    *
    * @param firstSemesterGPA  the first G.P.A
    * @param secondSemesterGPA the second semester's G.P.A
    * @return the calculated average G.P.A
    */
   public static float calulateAverageGPA(Context context, Map<Integer, String[]>[] scoreMaps) {
      float gpa1 = calculateGPA(context, scoreMaps[0]);
      float gpa2 = calculateGPA(context, scoreMaps[1]);
      return (gpa1 + gpa2) / 2;
   }
}
