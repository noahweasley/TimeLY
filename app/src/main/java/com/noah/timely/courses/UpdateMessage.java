package com.noah.timely.courses;

public class UpdateMessage {
    private int position;
    private CourseModel data;
    private EventType type;
    private int pagePosition;

    public UpdateMessage(CourseModel data, EventType type, int pagePosition) {
        this.data = data;
        this.type = type;
        this.pagePosition = pagePosition;
    }

    public int getPagePosition() {
        return pagePosition;
    }

    public void setPagePosition(int pagePosition) {
        this.pagePosition = pagePosition;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public CourseModel getData() {
        return data;
    }

    public void setData(CourseModel data) {
        this.data = data;
    }

    public EventType getType() {
        return type;
    }

    @SuppressWarnings("unused")
    public enum EventType {
        NEW, UPDATE_CURRENT
    }
}
