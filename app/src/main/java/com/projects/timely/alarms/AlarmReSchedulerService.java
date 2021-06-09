package com.projects.timely.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.projects.timely.R;
import com.projects.timely.assignment.AssignmentModel;
import com.projects.timely.assignment.Reminder;
import com.projects.timely.assignment.SubmissionNotifier;
import com.projects.timely.core.DataModel;
import com.projects.timely.core.SchoolDatabase;
import com.projects.timely.scheduled.AddScheduledDialog;
import com.projects.timely.scheduled.ScheduledTaskNotifier;
import com.projects.timely.timetable.DaysFragment;
import com.projects.timely.timetable.TimetableModel;
import com.projects.timely.timetable.TimetableNotifier;
import com.projects.timely.util.ThreadUtils;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.projects.timely.alarms.AlarmReceiver.ALARM_POS;
import static com.projects.timely.assignment.AddAssignmentActivity.NEXT_ALARM;
import static com.projects.timely.assignment.AddAssignmentActivity.POSITION;
import static com.projects.timely.assignment.AssignmentFragment.LECTURER_NAME;
import static com.projects.timely.assignment.AssignmentFragment.TITLE;
import static com.projects.timely.scheduled.AddScheduledDialog.ARG_COURSE;
import static com.projects.timely.timetable.DaysFragment.ARG_CLASS;
import static com.projects.timely.timetable.DaysFragment.ARG_DAY;
import static com.projects.timely.timetable.DaysFragment.ARG_PAGE_POSITION;
import static com.projects.timely.timetable.DaysFragment.ARG_POSITION;

/**
 * Service that would re-schedule all TimeLY alarms
 */
public class AlarmReSchedulerService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;  // unused, had to implement
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // make re-scheduler return immediately to avoid blocking main thread
        ThreadUtils.runBackgroundTask(() -> {

            Context context = getApplicationContext();
            SchoolDatabase database = new SchoolDatabase(context);

            // Reset the alarm here.
            List<DataModel> activeAlarms = database.getActiveAlarms();
            if (!activeAlarms.isEmpty()) {
                // re-schedule alarms; get alarm data from app's database
                for (DataModel rawData : activeAlarms) {
                    AlarmModel alarm = (AlarmModel) rawData;
                    registerAlarm(context, alarm);
                }
            }

            // Reset the assignment notification alarms here.
            List<DataModel> pendingAssignments = database.getPendingAssignments();
            if (!pendingAssignments.isEmpty()) {
                // re-schedule alarms; get alarm data from app's database
                for (DataModel rawData : pendingAssignments) {
                    AssignmentModel pendingAssignment = (AssignmentModel) rawData;
                    registerPendingAssignments(context, pendingAssignment);
                }
            }

            // Reset the scheduled timetable notification alarms here.
            List<DataModel> timetables1
                    = database.getTimeTableData(SchoolDatabase.SCHEDULED_TIMETABLE);
            if (!timetables1.isEmpty()) {
                // re-schedule alarms; get alarm data from app's database
                for (DataModel rawData : timetables1) {
                    TimetableModel timetable = (TimetableModel) rawData;
                    registerPendingScheduledTimetables(context, timetable);
                }
            }

            // Reset the scheduled timetable notification alarms here.
            List<DataModel> timetables2 = database.getAllNormalSchoolTimetable();
            if (!timetables2.isEmpty()) {
                // re-schedule alarms; get alarm data from app's database
                for (DataModel rawData : timetables2) {
                    TimetableModel timetable = (TimetableModel) rawData;
                    registerPendingTimetables(context, timetable);
                }
            }

        }); // end re-schedule task

        return START_STICKY;
    }

    // register pending scheduled timetables
    private void registerPendingScheduledTimetables(Context context, TimetableModel timetable) {
        String time = timetable.getStartTime();
        String[] sTime = time.split(":");
        String course = timetable.getFullCourseName() + " (" + timetable
                .getCourseCode() + ")";
        int day = timetable.getCalendarDay();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(sTime[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(sTime[1]));
        calendar.set(Calendar.DAY_OF_WEEK, day);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.setTimeInMillis(
                calendar.getTimeInMillis() - TimeUnit.MINUTES.toMillis(10));

        long NOW = System.currentTimeMillis();
        long CURRENT = calendar.getTimeInMillis();
        long triggerTime = CURRENT < NOW ? CURRENT + TimeUnit.DAYS.toMillis(7) : CURRENT;

        AlarmManager manager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        Intent scheduleIntent = new Intent(context, ScheduledTaskNotifier.class)
                .putExtra(ARG_COURSE, course)
                .putExtra(AddScheduledDialog.ARG_TIME, time)
                .putExtra(ARG_DAY, day)
                .addCategory("com.projects.timely.scheduled")
                .setAction("com.projects.timely.scheduled.addAction")
                .setDataAndType(
                        Uri.parse(
                                "content://com.projects.timely.scheduled.add." + triggerTime),
                        "com.projects.timely.scheduled.dataType");

        PendingIntent pi = PendingIntent.getBroadcast(context, 1156, scheduleIntent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            manager.setExact(AlarmManager.RTC, triggerTime, pi);
        manager.set(AlarmManager.RTC, triggerTime, pi);
    }

    // register previous timetable
    private void registerPendingTimetables(Context context, TimetableModel timetable) {
        int position = timetable.getChronologicalOrder();
        String time = timetable.getStartTime();
        String course = timetable.getFullCourseName() + " (" + timetable
                .getCourseCode() + ")";
        String[] t = timetable.getStartTime().split(":");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, timetable.getCalendarDay());
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(t[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(t[1]));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.setTimeInMillis(
                calendar.getTimeInMillis() - TimeUnit.MINUTES.toMillis(10));

        long NOW = System.currentTimeMillis();
        long CURRENT = calendar.getTimeInMillis();
        long timeInMillis = CURRENT < NOW ? CURRENT + TimeUnit.DAYS.toMillis(7) : CURRENT;

        AlarmManager manager = (AlarmManager) context.getSystemService(
                Context.ALARM_SERVICE);

        Intent timetableIntent = new Intent(context, TimetableNotifier.class);
        timetableIntent.putExtra(DaysFragment.ARG_TIME, time)
                       .putExtra(ARG_CLASS, course)
                       .putExtra(ARG_DAY, timetable.getCalendarDay())
                       .putExtra(ARG_POSITION, position)
                       .putExtra(ARG_PAGE_POSITION, position)
                       .addCategory("com.projects.timely.timetable")
                       .setAction("com.projects.timely.timetable.addAction")
                       .setDataAndType(
                               Uri.parse(
                                       "content://com.projects.timely.add." + timeInMillis),
                               "com.projects.timely.dataType");

        PendingIntent pi = PendingIntent.getBroadcast(context, 555, timetableIntent,
                                                      PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            manager.setExact(AlarmManager.RTC, timeInMillis, pi);
        else manager.set(AlarmManager.RTC, timeInMillis, pi);
    }

    // register pending assignments
    private void registerPendingAssignments(Context context, AssignmentModel assignment) {
        Calendar calendar = Calendar.getInstance();

        String submissionDate = assignment.getSubmissionDate();
        String[] sArr = submissionDate.split("/");
        int day = Integer.parseInt(sArr[0]);
        int month = Integer.parseInt(sArr[1]);
        int year = Integer.parseInt(sArr[2]);
        // Time set to 07:00 am
        calendar.set(year, month, day);
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long CURRENT = calendar.getTimeInMillis();
        long NOW = System.currentTimeMillis();
        final int ONE_DAY = 1000 * 60 * 60 * 24;
        final long PREVIOUS = CURRENT - ONE_DAY;
        // Get the alarm manager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        // Now set the next alarm
        String ln = truncateLecturerName(context, assignment.getLecturerName());
        Intent notifyIntentCurrent = new Intent(context, SubmissionNotifier.class);
        notifyIntentCurrent.putExtra(LECTURER_NAME, ln)
                           .putExtra(TITLE, assignment.getTitle())
                           .putExtra(POSITION, assignment.getPosition())
                           .addCategory(context.getPackageName() + ".category")
                           .setAction(context.getPackageName() + ".update")
                           .setDataAndType(
                                   Uri.parse("content://" + context.getPackageName()),
                                   assignment.toString());

        Intent notifyIntentPrevious = new Intent(context, Reminder.class);
        notifyIntentPrevious.putExtra(LECTURER_NAME, ln)
                            .putExtra(TITLE, assignment.getTitle())
                            .putExtra(NEXT_ALARM, CURRENT)
                            .addCategory(context.getPackageName() + ".category")
                            .setAction(context.getPackageName() + ".update")
                            .setDataAndType(
                                    Uri.parse("content://" + context.getPackageName()),
                                    assignment.toString());

        PendingIntent assignmentPiPrevious
                = PendingIntent.getBroadcast(context, 147, notifyIntentPrevious,
                                             PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent assignmentPiCurrent
                = PendingIntent.getBroadcast(context, 141, notifyIntentCurrent,
                                             PendingIntent.FLAG_UPDATE_CURRENT);
        // Exact alarms not used here, so that android can perform its normal operation
        // on devices
        // >= 4.4 (KITKAT) to prevent unnecessary battery drain by alarms.
        //
        // Also set alarms for both that day, and a day before the assignment's deadline, to
        // act as a reminder to the deadline
        alarmManager.set(AlarmManager.RTC, CURRENT, assignmentPiCurrent);
        if (PREVIOUS > NOW) alarmManager.set(AlarmManager.RTC, PREVIOUS,
                                             assignmentPiPrevious);
    }

    // Truncate lecturer's name to enable user view more of the name because
    // if the name is too long, the system will add ellipses at the end of the name
    // thereby removing some important parts of the name.
    private String truncateLecturerName(Context context, String fullName) {
        String[] nameTokens = fullName.split(" ");

        String[] titles = {"Barr", "Barrister", "Doc", "Doctor", "Dr", "Engineer", "Engr",
                           "Mr",
                           "Mister", "Mrs", "Ms", "Prof", "Professor"};

        StringBuilder nameBuilder = new StringBuilder();
        String shortenedName = "";

        int iMax = nameTokens.length - 1;

        int nameLimit = context.getResources().getInteger(R.integer.name_limit);
        if (fullName.length() > nameLimit && nameTokens.length > 2) {
            if (startsWithAny(titles, fullName)) {
                // Append the title if there is one
                switch (nameTokens[0]) {
                    case "Barrister":
                        nameBuilder.append(titles[0] /* Barr */).append(" ");
                        break;
                    case "Doctor":
                    case "Doc":
                        nameBuilder.append(titles[4] /* Dr */).append(" ");
                        break;
                    case "Engineer":
                        nameBuilder.append(titles[6] /* Engr */).append(" ");
                        break;
                    case "Mister":
                        nameBuilder.append(titles[7] /* Mr */).append(" ");
                        break;
                    case "Mrs":
                        nameBuilder.append(titles[10] /* Mr */).append(" ");
                        break;
                    case "Professor":
                        nameBuilder.append(titles[11] /* Prof */).append(" ");
                        break;
                    default:
                        nameBuilder.append(nameTokens[0]).append(" ");
                }

                // Shorten the names to their first characters in uppercase
                for (int i = 1; i <= iMax; i++) {
                    nameBuilder.append(Character.toUpperCase(nameTokens[i].charAt(0)));
                    if (i == iMax) break;
                    nameBuilder.append(" ");
                }
            } else {
                for (int i = 0; i <= iMax; i++) {
                    nameBuilder.append(Character.toUpperCase(nameTokens[i].charAt(0)));
                    if (i == iMax) break;
                    nameBuilder.append(" ");
                }
            }

            int bMaxLen = nameBuilder.length();

            return nameBuilder.replace(bMaxLen - 1, bMaxLen, nameTokens[iMax]).toString();

        } else {
            return fullName;
        }
    }

    // Determines if there was an added title in the lecturer's name
    private boolean startsWithAny(String[] titles, String s) {
        for (String title : titles)
            if (s.startsWith(title))
                return true;
        return false;
    }

    // register individual alarms
    private void registerAlarm(@NonNull Context context, @NonNull AlarmModel alarm) {
        // This gets the current day of the week as of TODAY / NOW
        // int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // This calculates the days between now and the day needed which is represented
        // by whichDay.
        // int numberOfDays = Calendar.SATURDAY + whichDay - dayOfWeek;

        // Now add NOT set the difference of the days to your Calendar object
        // calendar`.add(Calendar.DATE, numberOfDays);

        Calendar calendar = Calendar.getInstance();
        // The amount of milliseconds to the next day
        final int NEXT_DAY = 1000 * 60 * 60 * 24;

        String alarmTime = alarm.getTime();

        String[] time = alarmTime.split(":");

        int hh = Integer.parseInt(time[0]);
        int mm = Integer.parseInt(time[1]);

        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hh);
        calendar.set(Calendar.MINUTE, mm);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        boolean isNextDay = System.currentTimeMillis() > calendar.getTimeInMillis();

        boolean isForenoon = hh >= 0 && hh < 12;

        String formattedHrAM = String.format(Locale.US, "%02d", (hh == 0 ? 12 : hh));
        String formattedHrPM = String.format(Locale.US, "%02d",
                                             (hh % 12 == 0 ? 12 : hh % 12));
        String formattedMinAM = String.format(Locale.US, "%02d", mm) + " AM";
        String formattedMinPM = String.format(Locale.US, "%02d", mm) + " PM";

        String _12H = isForenoon ? formattedHrAM + ":" + formattedMinAM
                                 : formattedHrPM + ":" + formattedMinPM;

        long alarmMillis = isNextDay ? calendar.getTimeInMillis() + NEXT_DAY
                                     : calendar.getTimeInMillis();

        Intent alarmReceiverIntent = new Intent(context, AlarmReceiver.class);
        alarmReceiverIntent.putExtra(ALARM_POS, alarm.getInitialPosition());
        alarmReceiverIntent.putExtra("Time", alarmTime);

        alarmReceiverIntent.addCategory("com.projects.timely.alarm.category");
        alarmReceiverIntent.setAction("com.projects.timely.alarm.cancel");
        alarmReceiverIntent.setDataAndType(
                Uri.parse("content://com.projects.timely/Alarms/alarm" + alarmMillis),
                "com.projects.timely.alarm.dataType");

        AlarmManager alarmManager
                = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmPI = PendingIntent.getBroadcast(context,
                                                           1189765,
                                                           alarmReceiverIntent,
                                                           PendingIntent.FLAG_CANCEL_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // alarm has to be triggered even when device is in idle or doze mode.
            // This alarm is very important
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                                                       alarmMillis, alarmPI);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmMillis, alarmPI);
            }
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmMillis, alarmPI);
        }
    }
}