package com.projects.timely.core;

/**
 * A time data model, that can store all data about time
 */
public class Time {
    private DayPart dayPart;

    public DayPart getDayPart() {
        return dayPart;
    }

    public enum DayPart{
        MORNING, AFTERNOON, EVENING
    }

}
