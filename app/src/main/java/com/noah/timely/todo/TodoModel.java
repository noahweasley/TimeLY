package com.noah.timely.todo;

import com.noah.timely.core.DataModel;

public class TodoModel extends DataModel {
    public static final String[] CATEGORIES = {"General_Todo", "Work_Todo", "Music_Todo", "Travel_Todo",
                                               "Study_Todo", "Home_Todo", "Creativity_Todo", "Shopping_Todo",
                                               "Fun_Todo", "Miscellaneous_Todo"};
    private String taskTitle;
    private String taskDescription;
    private String category;
    private String completionDate;
    private String completionTime;

    public TodoModel() {
        // required default constructor
    }

    public TodoModel(String taskTitle, String taskDescription, String category, String completionDate,
                     String completionTime) {
        this.taskTitle = taskTitle;
        this.taskDescription = taskDescription;
        this.category = category;
        this.completionDate = completionDate;
        this.completionTime = completionTime;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
}
