package com.noah.timely.assignment;

import android.net.Uri;

public class UUpdateMessage {
    private int position;
    private Uri data;
    private final EventType type;

    public UUpdateMessage(Uri data, int position, EventType type) {
        this.data = data;
        this.type = type;
    }

    public int getPagePosition() {
        return position;
    }

    public void setPagePosition(int pagePosition) {
        this.position = pagePosition;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Uri getData() {
        return data;
    }

    public void setData(Uri data) {
        this.data = data;
    }

    public EventType getType() {
        return type;
    }

    public enum EventType {
        NEW, UPDATE_CURRENT, INSERT, REMOVE
    }
}
