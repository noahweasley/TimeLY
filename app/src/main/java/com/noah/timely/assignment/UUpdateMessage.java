package com.noah.timely.assignment;

import android.net.Uri;

import com.noah.timely.gallery.Image;

public class UUpdateMessage {
    private int position;
    private Uri data;
    private final Image image;
    private final EventType type;

    public UUpdateMessage(Uri data, Image image, int position, EventType type) {
        this.data = data;
        this.image = image;
        this.position = position;
        this.type = type;
    }

    public Image getImage() {
        return image;
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
