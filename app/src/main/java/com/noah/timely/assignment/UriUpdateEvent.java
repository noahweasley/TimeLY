package com.noah.timely.assignment;

public class UriUpdateEvent {
    private int position;
    private String uris;

    public UriUpdateEvent(int position, String uris) {
        this.position = position;
        this.uris = uris;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getUris() {
        return uris;
    }

    public void setUris(String uris) {
        this.uris = uris;
    }
}
