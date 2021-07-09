package com.noah.timely.core;

public class MultiUpdateMessage {
    private final EventType type;

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

