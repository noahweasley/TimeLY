package com.projects.timely.assignment;

public class MultiUpdateMessage {
    private EventType type;

    public MultiUpdateMessage(EventType type) {
        this.type = type;
    }

    public EventType getType() {
        return type;
    }

    public enum EventType {
       INSERT, REMOVE
    }
}

