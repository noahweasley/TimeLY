package com.noah.timely.timetable;

import com.noah.timely.core.DataModel;

import java.io.Serializable;

public class TimetableModel extends DataModel implements Serializable {

    private int id;
    private String lecturerName;
    private String fullCourseName;
    private String startTime;
    private int chronology;
    private String endTime;
    private String courseCode;
    private String day;
    private String importance;
    private boolean isClassOver;

    public TimetableModel(String lecturerName, String fullCourseName, String startTime,
                          String endTime, String courseCode) {
        this.lecturerName = lecturerName;
        this.fullCourseName = fullCourseName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.courseCode = courseCode;
    }

    public TimetableModel(String fullCourseName, String startTime, String endTime, String courseCode) {
        this(null, fullCourseName, startTime, endTime, courseCode);
    }

    public TimetableModel() {

    }

    public TimetableModel(String lecturer, String f_cn, String start, String end, String courseCode, String importance,
                          String day) {
        this(lecturer, f_cn, start, end, courseCode);
        this.importance = importance;
        this.day = day;
    }

    public TimetableModel(String course, String start, String end, String code, boolean classOver) {
        this(course, start, end, code);
        this.isClassOver = classOver;
    }

    public TimetableModel(String course, String start, String end, String code, boolean classOver, String day) {
        this(course, start, end, code, classOver);
        this.day = day;
    }

    public int getChronologicalOrder() {
        return chronology;
    }

    public void setChronologicalOrder(int chronology) {
        this.chronology = chronology;
    }

    public void setClassOver(boolean classOver) {
        isClassOver = classOver;
    }

    public String getImportance() {
        return importance;
    }

    public void setImportance(String importance) {
        this.importance = importance;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public int getStartTimeAsInt() {
        String[] ss = startTime.split(":");
        return Integer.parseInt(ss[0] + ss[1]);
    }

    public int getCalendarDay() {
        switch (getDay()) {
            case "Monday":
                return 2;
            case "Tuesday":
                return 3;
            case "Wednesday":
                return 4;
            case "Thursday":
                return 5;
            case "Friday":
                return 6;
            case "Saturday":
                return 7;
            default:
                throw new UnsupportedOperationException("Invalid day");
        }
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
            case "Saturday":
                return 5;
            default:
                throw new UnsupportedOperationException("getDayIndex() can't be used here");
        }
    }

    public String getLecturerName() {
        return lecturerName;
    }

    public void setLecturerName(String lecturerName) {
        this.lecturerName = lecturerName;
    }

    public String getFullCourseName() {
        return fullCourseName;
    }

    public void setFullCourseName(String fullCourseName) {
        this.fullCourseName = fullCourseName;
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

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        return "TimetableModel{" +
                "id=" + id +
                ", lecturerName='" + lecturerName + '\'' +
                ", fullCourseName='" + fullCourseName + '\'' +
                ", startTime='" + startTime + '\'' +
                ", chronology=" + chronology +
                ", endTime='" + endTime + '\'' +
                ", courseCode='" + courseCode + '\'' +
                ", day='" + day + '\'' +
                ", importance='" + importance + '\'' +
                ", isClassOver=" + isClassOver +
                '}';
    }
}

