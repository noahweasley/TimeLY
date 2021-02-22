package com.projects.timely.alarms;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Process;
import android.view.View;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.core.os.ConfigurationCompat;

import static com.projects.timely.core.Globals.isUserPreferred24Hours;

/**
 * The thread responsible for blinking the colon in between the minute and second indicator
 */
class TimeChangeDetector extends Thread {

    private static SharedPreferences mPreferences;
    private String min = "";
    private volatile boolean wantToStopOperation;
    private Context mContext;
    private Activity caller;
    private OnTimeChangeListener listener;

    static TimeChangeDetector getInstance() {
        return new TimeChangeDetector();
    }

    public TimeChangeDetector with(Context context, SharedPreferences preferences) {
        mContext = context.getApplicationContext();
        mPreferences = preferences;
        caller = (Activity) context;
        return this;
    }

    void pauseOperation() {
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

                String dateFormat = "Medium";
                if (mPreferences != null)
                    dateFormat = mPreferences.getString("date_format", "");
                String format;

                switch (dateFormat) {
                    case "Full":
                        format = DateFormat.getDateInstance(DateFormat.FULL)
                                .format(calendar.getTime());
                        break;
                    case "Short":
                        format = DateFormat.getDateInstance(DateFormat.SHORT)
                                .format(calendar.getTime());
                        break;
                    default:
                        format = DateFormat.getDateInstance(DateFormat.MEDIUM)
                                .format(calendar.getTime());

                }

                String timeView;
                Date calendarTime = calendar.getTime();
                timeView = is24 ? timeFormat24.format(calendarTime)
                                : timeFormat12.format(calendarTime);

                String[] splitTime = timeView.split(":");
                String hour = splitTime[0];
                String min = splitTime[1];

                final String date = format;

                boolean isAM = false;
                if (!is24)
                    isAM = splitTime[2].equals("AM");

                if (!wantToStopOperation) {
                    int visibility = is24 ? View.GONE : View.VISIBLE;
                    if (listener != null) {
                        boolean _isAM = isAM;
                        if (!this.min.equals(min))
                            caller.runOnUiThread(() -> listener.onTimeChanged(date,
                                                                              hour,
                                                                              min,
                                                                              visibility,
                                                                              is24,
                                                                              _isAM));
                        this.min = min;
                    }
                }
            }
        }
    }

    public void setOnTimeChangedListener(OnTimeChangeListener listener) {
        this.listener = listener;
    }


    public interface OnTimeChangeListener {
        void onTimeChanged(String date,
                           String hour,
                           String min,
                           int visibility,
                           boolean is24,
                           boolean isAM);
    }
}