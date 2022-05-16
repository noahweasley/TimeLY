package com.astrro.timely.util;

/**
 * Set of constant fields required by TimeLY
 */
public class Constants {
   /**
    * Constant for assignment model
    */
   public static final String ASSIGNMENT_MODEL = "com.astrro.timely.assignment.AssignmentModel";

   /**
    * Constant for assignment
    */
   public static final String ASSIGNMENT = "com.astrro.timely.assignments";

   /**
    * Constant for exam model
    */
   public static final String EXAM_MODEL = "com.astrro.timely.exams.ExamModel";

   /**
    * Constant for course model
    */
   public static final String COURSE_MODEL = "com.astrro.timely.courses.CourseModel";

   /**
    * Constant for Timetable model
    */
   public static final String TIMETABLE_MODEL = "com.astrro.timely.timetable.TimetableModel";

   /**
    * Constants for TodoModel
    */
   public static final String TODO_MODEL = "com.astrro.timely.todo.TodoModel";

   /**
    * Constant for Scheduled Timetable
    */
   public static final String SCHEDULED_TIMETABLE = "com.astrro.timely.scheduled";

   /**
    * Constants for Timetable
    */
   public static final String TIMETABLE = "com.astrro.timely.timetable";

   /**
    * Constants for exams
    */
   public static final String EXAM = "com.astrro.timely.exams";

   /**
    * Constants for courses
    */
   public static final String COURSE = "com.astrro.timely.courses";

   /**
    * Constants for alarms
    */
   public static final String ALARM = "com.astrro.timely.alarms";

   /**
    * Constants for Work category
    */
   public static final String TODO_WORK = "Work_Todo";

   /**
    * Constants for Work category
    */
   public static final String TODO_GENERAL = "General_Todo";

   /**
    * Constants for Work category
    */
   public static final String TODO_CREATIVITY = "Creativity_Todo";

   /**
    * Constants for Work category
    */
   public static final String TODO_FUN = "Fun_Todo";

   /**
    * Constants for Work category
    */
   public static final String TODO_HOME = "Home_Todo";

   /**
    * Constants for Work category
    */
   public static final String TODO_MISCELLANEOUS = "Miscellaneous_Todo";

   /**
    * Constants for Work category
    */
   public static final String TODO_SHOPPING = "Shopping_Todo";

   /**
    * Constants for Work category
    */
   public static final String TODO_STUDY = "Study_Todo";

   /**
    * Constants for Work category
    */
   public static final String TODO_TRAVEL = "Travel_Todo";

   /**
    * Constants for Music category
    */
   public static final String TODO_MUSIC = "Music_Todo";

   /**
    * Custom intent actions used in TimeLY
    */
   public static class ACTION {

      /**
       * Intent action to show notifications
       */
      public static final String SHOW_NOTIFICATION = "com.astrro.timely.action.show-notification";

      public static final String SHOW_PICTURE = "com.astrro.timely.action.show-picutures";

   }

   public static class EXTRA {

      public static final String EXTRA_IMAGE = "extra image";
   }
}
