package com.projects.timely.alarms;

import android.text.TextUtils;

import com.projects.timely.core.DataModel;

@SuppressWarnings("unused")
public class AlarmModel extends DataModel {
    private String time;
    private boolean isOn;
    private boolean isRepeated;
    private String ringTone;
    private String[] repeatDays;
    private int position;
    private boolean vibrate;
    private String label;
    private int initialPosition;
    private boolean snoozed;
    private String snoozedTime;

    public AlarmModel(int position, String time, boolean isOn, boolean isRepeated, String ringTone,
                      String[] repeatDays, boolean vibrate, String label) {
        this.position = position;
        this.time = time;
        this.isOn = isOn;
        this.isRepeated = isRepeated;
        this.ringTone = ringTone;
        this.repeatDays = repeatDays;
        this.vibrate = vibrate;
        this.label = label;
    }

    public AlarmModel() {

    }

    public boolean isSnoozed() {
        return snoozed;
    }

    public void setSnoozed(boolean snoozed) {
        this.snoozed = snoozed;
    }

    public String getSnoozedTime() {
        return snoozedTime;
    }

    public void setSnoozedTime(String snoozedTime) {
        this.snoozedTime = snoozedTime;
    }

    public int getInitialPosition() {
        return initialPosition;
    }

    public void setInitialPosition(int initialPosition) {
        this.initialPosition = initialPosition;
    }

    public String getLabel() {
        return TextUtils.isEmpty(label) ? "Label" : label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isVibrate() {
        return vibrate;
    }

    public void setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
    }

    public boolean isRepeated() {
        return isRepeated;
    }

    public void setRepeated(boolean repeated) {
        isRepeated = repeated;
    }

    String getRingTone() {
        return ringTone != null ? ringTone : "Choose ringtone";
    }

    public void setRingTone(String ringTone) {
        this.ringTone = ringTone;
    }

    public Boolean[] getRepeatDays() {
        boolean _1 = Boolean.parseBoolean(repeatDays[0]);
        boolean _2 = Boolean.parseBoolean(repeatDays[1]);
        boolean _3 = Boolean.parseBoolean(repeatDays[2]);
        boolean _4 = Boolean.parseBoolean(repeatDays[3]);
        boolean _5 = Boolean.parseBoolean(repeatDays[4]);
        boolean _6 = Boolean.parseBoolean(repeatDays[5]);
        boolean _7 = Boolean.parseBoolean(repeatDays[6]);

        return new Boolean[]{_1, _2, _3, _4, _5, _6, _7};
    }

    public void setRepeatDays(Boolean[] repeatDays) {
        String _1 = String.valueOf(repeatDays[0]);
        String _2 = String.valueOf(repeatDays[1]);
        String _3 = String.valueOf(repeatDays[2]);
        String _4 = String.valueOf(repeatDays[3]);
        String _5 = String.valueOf(repeatDays[4]);
        String _6 = String.valueOf(repeatDays[5]);
        String _7 = String.valueOf(repeatDays[6]);

        this.repeatDays = new String[]{_1, _2, _3, _4, _5, _6, _7};
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
