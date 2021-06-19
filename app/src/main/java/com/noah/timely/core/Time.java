package com.noah.timely.core;

import androidx.annotation.NonNull;

/**
 * A time data model, that can store all data about time
 */
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
    public Time(String dateFormat, String date, String hour, String min, boolean isMilitaryTime, boolean isForenoon) {
        this(date, hour, min, isMilitaryTime, isForenoon);
        this.dateFormat = dateFormat;
    }

    /**
     * @return the current day part (Morning, Afternoon, Evening, Night...)
     */
    public DayPart getCurrentDayPart() {
        int __hour = Integer.parseInt(hour);
        int __min = Integer.parseInt(min);

        if (isMilitaryTime) /* for Military Time */ {
            boolean isForenoon = __hour >= 0 && __hour < 12; // 00:00 to 11:59

            if (isForenoon) /* AM */ {
                // 00:00 to 04:59
                if (__hour >= 0 && __hour < 5) return DayPart.SLEEP_TIME;
                    // 05:00 to 05:59
                else if (__hour == 5) return DayPart.DEFAULT_INTERVAL_DAY;
                    // 06:00 to 06:59
                else if (__hour == 6) return DayPart.DAY_START_ACTIVE_PERIOD;
                    // if any time period exists that is not within the other condition ranges i.e 07:00 to 11:59
                else return DayPart.MORNING;

            } else /* PM */ {
                // 12:00 to 16:59
                if (__hour >= 12 && __hour < 17) return DayPart.AFTERNOON;
                    // 17:00 to 22:59
                else if (__hour >= 17 && __hour < 23) return DayPart.EVENING;
                    // only time interval left is 23:00 to 23:59, which is night
                else return DayPart.NIGHT;
            }

        } else  /* for Civilian Time */ {

            if (this.isForenoon) /* AM */ {
                // 12:00 am to 04:59 am
                if (__hour == 12 || (__hour >= 1 && __hour < 5)) return DayPart.SLEEP_TIME;
                    // 05:00 am to 05:59 am
                else if (__hour == 5) return DayPart.DEFAULT_INTERVAL_DAY;
                    // 06:00 am to 06:59 am
                else if (__hour == 6) return DayPart.DAY_START_ACTIVE_PERIOD;
                    // if any time period exists that is not within the other condition ranges i.e 07:00 am o 11:59 am
                else return DayPart.MORNING;

            } else /* PM */ {
                // 12:00 pm to 04:59 pm
                if (__hour == 12 || (__hour >= 1 && __hour < 5)) return DayPart.AFTERNOON;
                // 05:00 pm to 11:00 pm
                if (__hour >= 5 && __hour < 11) return DayPart.EVENING;
                    // only time interval left is 11:00 am to 11:59, which is night
                else return DayPart.NIGHT;
            }
        }
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
