package com.projects.timely.core;

/**
 * A time data model, that can store all data about time
 */
@SuppressWarnings("unused")
public class Time {
    private String date;
    private String hour;
    private String min;
    private boolean is24;
    private boolean isAM;

    public Time(String date, String hour, String min, boolean is24, boolean isAM) {
        this.date = date;
        this.hour = hour;
        this.min = min;
        this.is24 = is24;
        this.isAM = isAM;
    }

    public DayPart getCurrentDayPart() {
        int __hour = Integer.parseInt(hour);
        if (isAM && !is24) {
            return DayPart.MORNING;
        } else if (!isAM && !is24) {
            if (__hour == 12 || (__hour >= 1 && __hour <= 4)) {
                return DayPart.AFTERNOON;
            } else
                return DayPart.EVENING;
        } else if (is24) {
            if (__hour >= 0 && __hour < 12) {
                return DayPart.MORNING;
            } else if (__hour >= 12 && __hour <= 16) {
                return DayPart.AFTERNOON;
            } else
                return DayPart.EVENING;
        }
        return null;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getMinutes() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public boolean getIs24() {
        return is24;
    }

    public void setIs24(boolean is24) {
        this.is24 = is24;
    }

    public boolean isAM() {
        return isAM;
    }

    public void setAM(boolean AM) {
        isAM = AM;
    }

}
