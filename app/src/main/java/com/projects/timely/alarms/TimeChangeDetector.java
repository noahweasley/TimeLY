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

    public TimeChangeDetector with(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mContext = context.getApplicationContext();
        return this;
    }

    public void pauseOperation() {
        wantToStopOperation = true;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        // Get the preferences for date format
        while (!wantToStopOperation) {
            boolean is24 = isUserPreferred24Hours(mContext);

            if (!wantToStopOperation) {

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
                timeView = is24 ? timeFormat24.format(calendarTime)
                                : timeFormat12.format(calendarTime);

                String[] splitTime = timeView.split(":");
                String hour = splitTime[0];
                String min = splitTime[1];

                boolean isAM = false;
                if (!is24) isAM = splitTime[2].equals("AM");

                if (!wantToStopOperation) {
                    if (!this.min.equals(min)) {
                        // system time is posted
                        EventBus.getDefault()
                                .post(new Time(formattedDate, hour, min, is24, isAM));
                    }
                    this.min = min;
                }
            }
        }
    }
}