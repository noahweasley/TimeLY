package com.noah.timely.alarms;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Process;

import androidx.core.os.ConfigurationCompat;
import androidx.preference.PreferenceManager;

import com.noah.timely.core.Time;

import org.greenrobot.eventbus.EventBus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.noah.timely.core.AppUtils.isUserPreferred24Hours;

/**
 * The thread responsible for blinking the colon in between the minute and second indicator
 */
public class TimeChangeDetector extends Thread {

    private static SharedPreferences mPreferences;
    private String min = "";
    private volatile boolean wantToStopOperation;
    private Context mContext;
    private static String prevDateFormat;

    /**
     * This won't spawn up a new thread. But just return the current OS time.
     *
     * @return the current OS time immediately
     */
    public static Time requestImmediateTime(Context mContext) {
        // mPreferences can be null when time or date changes but TimeLY is not in use
        if (mPreferences == null)
            mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        return getCalculatedTime(mContext);
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
                Time calculatedTime = getCalculatedTime(mContext);

                if (!wantToStopOperation && !this.min.equals(calculatedTime.getMinutes())) {
                    // system time is posted
                    EventBus eventBus = EventBus.getDefault();
                    if (eventBus.hasSubscriberForEvent(Time.class))
                        eventBus.post(calculatedTime);

                    this.min = calculatedTime.getMinutes();
                }
            }
        }
    }

    // retrieves calculated time
    private static Time getCalculatedTime(Context mContext) {

        boolean isMilitaryTime = isUserPreferred24Hours(mContext);

        Calendar calendar = Calendar.getInstance();

        Configuration config = mContext.getResources().getConfiguration();
        Locale currentLocale = ConfigurationCompat.getLocales(config).get(0);

        SimpleDateFormat timeFormat24 = new SimpleDateFormat("HH:mm:aa", currentLocale);
        SimpleDateFormat timeFormat12 = new SimpleDateFormat("hh:mm:aa", currentLocale);

        String formattedDate;
        String dateFormat = mPreferences.getString("date_format", "Medium");

        String timeView;
        Date calendarTime = calendar.getTime();
        timeView = isMilitaryTime ? timeFormat24.format(calendarTime)
                                  : timeFormat12.format(calendarTime);

        String[] splitTime = timeView.split(":");
        String hour = splitTime[0];
        String min = splitTime[1];

        boolean isForenoon = splitTime[2].equals("AM");

        switch (dateFormat) {
            case "Full":
                formattedDate
                        = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
                break;
            case "Short":
                formattedDate
                        = DateFormat.getDateInstance(DateFormat.SHORT).format(calendar.getTime());
                break;
            default:
                formattedDate
                        = DateFormat.getDateInstance(DateFormat.MEDIUM).format(calendar.getTime());
        }

        return new Time(dateFormat, formattedDate, hour, min, isMilitaryTime, isForenoon);
    }
}