package com.noah.timely.assignment;

public class UpdateMessage {
    private AssignmentModel data;
    private EventType type;
    private int position;

    public UpdateMessage(AssignmentModel data, EventType type) {
        this.data = data;
        this.type = type;
    }

    public UpdateMessage(int position, EventType type) {
        this.position = position;
        this.type = type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public AssignmentModel getData() {
        return data;
    }

    public void setData(AssignmentModel data) {
        this.data = data;
    }

    public EventType getType() {
        return type;
    }

    public enum EventType {
        NEW, UPDATE_CURRENT, INSERT, REMOVE
    }
}

