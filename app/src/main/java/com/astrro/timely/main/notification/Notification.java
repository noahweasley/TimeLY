package com.astrro.timely.main.notification;

import android.net.Uri;

import com.astrro.timely.core.DataModel;

public class Notification extends DataModel {
    private Uri profileImage;
    private String username;
    private String message;
    private String updateTime;

    public Notification() {
    }

    public Notification(Uri profileImage, String username, String message, String updateTime) {
        this.profileImage = profileImage;
        this.username = username;
        this.message = message;
        this.updateTime = updateTime;
    }

    public Uri getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(Uri profileImage) {
        this.profileImage = profileImage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
