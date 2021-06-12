package com.noah.timely.core;

import androidx.annotation.NonNull;

/**
 * A time data model, that can store all data about time
 */
@SuppressWarnings("unused")
public class Time {
    //    fields
    private String date;
    private String hour;
    private String min;
    private boolean isMilitaryTime;
    private boolean isForenoon;
    private String dateFormat;

    public Time(String date, String hour, String min, boolean isMilitaryTime, boolean isForenoon) {
        this.date = date;
        this.hour = hour;
        this.min = min;
        this.isMilitaryTime = isMilitaryTime;
        this.isForenoon = isForenoon;
    }

    /**
     * Instantiate with all date params
     *
     * @param date           the date
     * @param hour           the hour
     * @param min            the minute
     * @param isMilitaryTime military time or not
     * @param isForenoon     day or night
     */
    public Time(String dateFormat, String date, String hour, String min,
                boolean isMilitaryTime, boolean isForenoon) {
        this(date, hour, min, isMilitaryTime, isForenoon);
        this.dateFormat = dateFormat;
    }

    /**
     * @return the current day part (Morning, Afternoon, Evening or Night)
     */
    public DayPart getCurrentDayPart() {
        int __hour = Integer.parseInt(hour);
        if (isForenoon && !isMilitaryTime) {
            return DayPart.MORNING;
        } else if (!isForenoon && !isMilitaryTime) {
            if (__hour == 12 || (__hour >= 1 && __hour <= 4)) {
                return DayPart.AFTERNOON;
            } else
                return DayPart.EVENING;
        } else if (isMilitaryTime) {
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

    public String getFullTime() {
        return getHour() + ":" + getMinutes();
    }

    public void setMin(String min) {
        this.min = min;
    }

    public boolean isMilitaryTime() {
        return isMilitaryTime;
    }

    public void setMilitaryTime(boolean militaryTime) {
        this.isMilitaryTime = militaryTime;
    }

    public boolean isForenoon() {
        return isForenoon;
    }

    public void setIsForeNoon(boolean isForenoon) {
        this.isForenoon = isForenoon;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Override
    @NonNull
    public String toString() {
        return "\n{\n" +
                "  \"date\": " + "\"" + getDate() + "\"" + ",\n" +
                "  \"time\": " + "\"" + getFullTime() + "\"" + ",\n" +
                "  \"isMilitary\": " + "\"" + isMilitaryTime() + "\"" + ",\n" +
                "  \"isForeNoon\": " + "\"" + isForenoon() + "\"" + ",\n" +
                "  \"dateFormat\": " + "\"" + getDateFormat() + "\"" + "\n" +
                "}";
    }

}
