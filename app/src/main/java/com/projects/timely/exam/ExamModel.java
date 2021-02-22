package com.projects.timely.exam;

import com.projects.timely.core.DataModel;

public class ExamModel extends DataModel {
    private String courseCode;
    private String courseName;
    private String start;
    private String end;
    private int id;
    private int chronologicalOrder;
    private String week;
    private String day;

    public ExamModel(String courseCode, String courseName, String start, String end) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.start = start;
        this.end = end;
    }

    public ExamModel() {
    }

    public ExamModel(String day, String week, int id, String courseCode, String courseName,
                     String start, String end) {
        this.day = day;
        this.week = week;
        this.id = id;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.start = start;
        this.end = end;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public int getChronologicalOrder() {
        return chronologicalOrder;
    }

    public void setChronologicalOrder(int chronologicalOrder) {
        this.chronologicalOrder = chronologicalOrder;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getTime() {
        return start + " - " + end;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public int getDayIndex() {
        switch (getDay()) {
            case "Monday":
                return 0;
            case "Tuesday":
                return 1;
            case "Wednesday":
                return 2;
            case "Thursday":
                return 3;
            case "Friday":
                return 4;
            default:
                throw new UnsupportedOperationException("getDayIndex() can't be used here");
        }
    }

    public int getStartAsInt() {
        String[] ss = start.split(":");
        return Integer.parseInt(ss[0] + ss[1]);
    }
}
