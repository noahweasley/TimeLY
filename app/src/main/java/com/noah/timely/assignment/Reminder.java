package com.noah.timely.assignment;

/**
 * A {@link AssignmentNotifier} that sends notification reminding user that assignment's are
 * to be submitted the next day
 */
public class Reminder extends AssignmentNotifier {

    @Override
    public String getDay() {
        return "tomorrow";
    }
}
