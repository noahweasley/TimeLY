package com.noah.timely.todo;


import androidx.annotation.NonNull;

import com.noah.timely.core.DataModel;
import com.noah.timely.util.CollectionUtils;

public class TodoModel extends DataModel {
   public static final String[] CATEGORIES = { "General_Todo", "Work_Todo", "Music_Todo", "Travel_Todo",
                                               "Study_Todo", "Home_Todo", "Creativity_Todo", "Shopping_Todo",
                                               "Fun_Todo", "Miscellaneous_Todo" };
   public static final String[] CATEGORIES_2 = { "Miscellaneous_Todo", "Work_Todo", "Music_Todo", "Travel_Todo",
                                                 "Study_Todo", "Home_Todo", "Creativity_Todo", "Shopping_Todo",
                                                 "Fun_Todo" };
   public static final String[] CATEGORIES_3 = { "Work_Todo", "Music_Todo", "Travel_Todo",
                                                 "Study_Todo", "Home_Todo", "Creativity_Todo", "Shopping_Todo",
                                                 "Fun_Todo", "Miscellaneous_Todo" };
   private boolean isTaskCompleted;
   private String taskTitle;
   private String taskDescription;
   private String DBcategory;
   private String completionDate;
   private String completionTime;
   private String startTime;
   private String endTime;

   public TodoModel() {
      // required default constructor
   }

   public TodoModel(String taskTitle, String taskDescription, boolean isTaskCompleted, String DBcategory,
                    String completionDate, String startTime, String endTime, String completionTime) {

      this.taskTitle = taskTitle;
      this.taskDescription = taskDescription;
      this.startTime = startTime;
      this.endTime = endTime;
      this.isTaskCompleted = isTaskCompleted;
      this.DBcategory = DBcategory;
      this.completionDate = completionDate;
      this.completionTime = completionTime;
   }

   public TodoModel(int id, String taskTitle, String taskDescription, boolean isTaskCompleted, String DBcategory,
                    String completionDate, String startTime, String endTime, String completionTime) {

      this(taskTitle, taskDescription, isTaskCompleted, DBcategory,
           completionDate, startTime, endTime, completionTime);

      this.id = id;
   }

   public String getTaskTitle() {
      return taskTitle;
   }

   public void setTaskTitle(String taskTitle) {
      this.taskTitle = taskTitle;
   }

   public String getDBcategory() {
      return DBcategory;
   }

   public void setDBcategory(String DBcategory) {
      this.DBcategory = DBcategory;
   }

   public String getCategory() {
      String todoCategory = getDBcategory().replace("_Todo", "");
      if (todoCategory.equals("Fun")) {
         todoCategory = todoCategory + " & Leisure";
      }
      return todoCategory;
   }

   public boolean isTaskCompleted() {
      return isTaskCompleted;
   }

   public void setTaskCompleted(boolean taskCompleted) {
      isTaskCompleted = taskCompleted;
   }

   public String getTaskDescription() {
      return taskDescription;
   }

   public void setTaskDescription(String taskDescription) {
      this.taskDescription = taskDescription;
   }

   public String getCompletionDate() {
      return completionDate;
   }

   public void setCompletionDate(String completionDate) {
      this.completionDate = completionDate;
   }

   public String getCompletionTime() {
      return completionTime;
   }

   public void setCompletionTime(String completionTime) {
      this.completionTime = completionTime;
   }

   public String getStartTime() {
      return startTime;
   }

   public void setStartTime(String startTime) {
      this.startTime = startTime;
   }

   public String getEndTime() {
      return endTime;
   }

   public void setEndTime(String endTime) {
      this.endTime = endTime;
   }

   public int getCategoryOrder() {
      int searchIndex = CollectionUtils.linearSearch(TodoModel.CATEGORIES_3, this.DBcategory);
      if (searchIndex == -1) throw new IllegalArgumentException("order of " + this.DBcategory + " not found");
      return searchIndex;
   }

   @NonNull
   @Override
   public String toString() {
      return "TodoModel {" +
              "id = " + id +
              ", position = " + position +
              ", isTaskCompleted = " + isTaskCompleted +
              ", taskTitle = '" + taskTitle + '\'' +
              ", taskDescription = '" + taskDescription + '\'' +
              ", DBcategory = '" + DBcategory + '\'' +
              ", completionDate = '" + completionDate + '\'' +
              ", completionTime = '" + completionTime + '\'' +
              ", startTime = '" + startTime + '\'' +
              ", endTime = '" + endTime + '\'' +
              '}';
   }
}
