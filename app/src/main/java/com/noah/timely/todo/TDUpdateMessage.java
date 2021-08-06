package com.noah.timely.todo;

public class TDUpdateMessage {
    private TodoModel data;
    private final EventType type;
    private int position;

    public TDUpdateMessage(TodoModel data, EventType type) {
        this.data = data;
        this.type = type;
    }

    public TDUpdateMessage(int position, EventType type) {
        this.position = position;
        this.type = type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public TodoModel getData() {
        return data;
    }

    public void setData(TodoModel data) {
        this.data = data;
    }

    public EventType getType() {
        return type;
    }

    public enum EventType {
        NEW, UPDATE_CURRENT, INSERT, REMOVE
    }
}

