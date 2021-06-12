package com.noah.timely.courses;

import com.noah.timely.core.DataModel;

@SuppressWarnings("unused")
public class CourseModel extends DataModel {
    private String semester;
    private int credits;
    private String courseCode;
    private String courseName;
    private int chronologicalOrder;

    public CourseModel(String semester, int credits, String courseCode, String courseName) {
        this.semester = semester;
        this.credits = credits;
        this.courseCode = courseCode;
        this.courseName = courseName;
    }

    public CourseModel() {
    }

    public int getChronologicalOrder() {
        return chronologicalOrder;
    }

    public void setChronologicalOrder(int chronologicalOrder) {
        this.chronologicalOrder = chronologicalOrder;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }
}
