package com.projects.timely.alarms;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Process;

import com.projects.timely.core.Time;

import org.greenrobot.eventbus.EventBus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.core.os.ConfigurationCompat;
import androidx.preference.PreferenceManager;

import static com.projects.timely.core.Globals.isUserPreferred24Hours;

/**
 * The thread responsible for blinking the colon in between the minute and second indicator
 */
public class TimeChangeDetector extends Thread {

    private static SharedPreferences mPreferences;
    private String min = "";
    private volatile boolean wantToStopOperation;
    private Context mContext;

    /**
     * @return the current OS time immediately
     */
    public Time requestImmediateTime() {
        return getCalculatedTime();
    }

    /**
     * Main initializations useful for TimeChangeDetector
     *
     * @param context the user
     * @return the same instance of the TimeChangeDetector for chain calls
     */
    public TimeChangeDetector with(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mContext = context.getApplicationContext();
        return this;
    }

    /**
     * Temporarily pauses the operation of this background task, but thread is still active
     */
    public void pauseOperation() {
        wantToStopOperation = true;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while (!wantToStopOperation) {

            if (!wantToStopOperation) {
                Time calculatedTime = getCalculatedTime();

                if (!wantToStopOperation && !this.min.equals(calculatedTime.getMinutes())) {
                    // system time is posted
                    EventBus.getDefault().post(calculatedTime);
                    this.min = calculatedTime.getMinutes();
                }
            }
        }
    }

    // retrieves calculated time
    private Time getCalculatedTime() {
        boolean is24 = isUserPreferred24Hours(mContext);

        Calendar calendar = Calendar.getInstance();

        Configuration config = mContext.getResources().getConfiguration();
        Locale currentLocale = ConfigurationCompat.getLocales(config).get(0);

        SimpleDateFormat timeFormat24 = new SimpleDateFormat("HH:mm", currentLocale);
        SimpleDateFormat timeFormat12 = new SimpleDateFormat("hh:mm:aa", currentLocale);

        String formattedDate;

        switch (mPreferences.getString("date_format", "Medium")) {
            case "Full":
                formattedDate = DateFormat.getDateInstance(DateFormat.FULL)
                        .format(calendar.getTime());
                break;
            case "Short":
                formattedDate = DateFormat.getDateInstance(DateFormat.SHORT)
                        .format(calendar.getTime());
                break;
            default:
                formattedDate = DateFormat.getDateInstance(DateFormat.MEDIUM)
                        .format(calendar.getTime());
        }

        String timeView;
        Date calendarTime = calendar.getTime();
        timeView = is24 ? timeFormat24.format(calendarTime) : timeFormat12.format(calendarTime);

        String[] splitTime = timeView.split(":");
        String hour = splitTime[0];
        String min = splitTime[1];

        boolean isAM = false;
        if (!is24) isAM = splitTime[2].equals("AM");

        return new Time(formattedDate, hour, min, is24, isAM);
    }
}