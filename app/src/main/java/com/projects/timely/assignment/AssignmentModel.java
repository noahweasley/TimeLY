package com.projects.timely.assignment;

import com.projects.timely.core.DataModel;

import java.io.Serializable;

@SuppressWarnings("unused")
public
class AssignmentModel extends DataModel implements Serializable {

    private int position;
    private int chronologicalOrder;
    private String courseCode;
    private String lecturerName;
    private String title;
    private String description;
    private String date;
    private String submissionDate;
    private String attachedPDF;
    private String attachedImage;
    private boolean isSubmitted;
    public AssignmentModel() {
    }
    AssignmentModel(int id, String lecturerName, String title, String description, String date,
                    String courseCode, String submissionDate, String attachedPDF,
                    String attachedImage, boolean isSubmitted) {

        this.lecturerName = lecturerName;
        this.title = title;
        this.description = description;
        this.date = date;
        this.position = id;
        this.courseCode = courseCode;
        this.submissionDate = submissionDate;
        this.attachedPDF = attachedPDF;
        this.attachedImage = attachedImage;
        this.isSubmitted = isSubmitted;
    }

    public int getChronologicalOrder() {
        return chronologicalOrder;
    }

    public void setChronologicalOrder(int chronologicalOrder) {
        this.chronologicalOrder = chronologicalOrder;
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        return "AssignmentModel{" +
                "id=" + position +
                ", courseCode='" + courseCode + '\'' +
                ", lecturerName='" + lecturerName + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", date='" + date + '\'' +
                ", submissionDate='" + submissionDate + '\'' +
                ", attachedPDF='" + attachedPDF + '\'' +
                ", attachedImage='" + attachedImage + '\'' +
                ", isSubmitted=" + isSubmitted +
                '}';
    }

    public boolean isSubmitted() {
        return isSubmitted;
    }

    public void setSubmitted(boolean submitted) {
        isSubmitted = submitted;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(String submissionDate) {
        this.submissionDate = submissionDate;
    }

    public String getAttachedPDF() {
        return attachedPDF;
    }

    public void setAttachedPDF(String attachedPDF) {
        this.attachedPDF = attachedPDF;
    }

    public String getAttachedImages() {
        return attachedImage;
    }

    public void setAttachedImage(String attachedImage) {
        this.attachedImage = attachedImage;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getLecturerName() {
        return lecturerName;
    }

    public void setLecturerName(String lecturerName) {
        this.lecturerName = lecturerName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
