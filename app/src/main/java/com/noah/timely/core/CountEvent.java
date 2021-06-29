package com.noah.timely.core;

public class CountEvent {
    private int size;

    public CountEvent(int size) {
        this.size = size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}
