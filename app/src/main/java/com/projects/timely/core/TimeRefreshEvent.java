package com.projects.timely.core;

/**
 * Empty refresh layout class, indicating a subscriber for an Event, it's layout be refreshed
 */
public class TimeRefreshEvent {
    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    private Object data;
}
