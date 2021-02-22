package com.projects.timely.alarms;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.projects.timely.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.os.ConfigurationCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import static com.projects.timely.core.Globals.isUserPreferred24Hours;

@SuppressWarnings("ConstantConditions")
public class AlarmTimeFragment extends Fragment {
    private SharedPreferences preferences;
    private TimeChangeDetector timeChangeDetector;
    private ImageView img_dayAndNight;
    private TextView alarmDate, alarmMin, alarmHour, am_pm;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        return inflater.inflate(R.layout.fragment_alarm_time, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle state) {
        // start blinking the colon in between the min and sec
        // Start all the alarm animations
        TextView blinkTarget = view.findViewById(R.id.blink);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setRepeatMode(Animation.REVERSE);
        alphaAnimation.setRepeatCount(Animation.INFINITE);
        alphaAnimation.setDuration(500);
        alphaAnimation.setInterpolator(new AccelerateInterpolator());
        blinkTarget.startAnimation(alphaAnimation);

        alarmDate = view.findViewById(R.id.alarm_date);
        alarmMin = view.findViewById(R.id.alarm_min);
        alarmHour = view.findViewById(R.id.alarm_hour);
        am_pm = view.findViewById(R.id.am_pm);
        ImageButton btn_setTime = view.findViewById(R.id.set_time);
        img_dayAndNight = view.findViewById(R.id.day_and_night);

        btn_setTime.setOnClickListener((v) -> {
            // when settings button is clicked, show the system date settings
            startActivity(new Intent(Settings.ACTION_DATE_SETTINGS));
        });

        /////////////////////////////      Code duplication    //////////////////////////////////

        Calendar date = Calendar.getInstance();

        Configuration config = getResources().getConfiguration();
        Locale currentLocale = ConfigurationCompat.getLocales(config).get(0);

        SimpleDateFormat timeFormat24 = new SimpleDateFormat("HH:mm", currentLocale);
        SimpleDateFormat timeFormat12 = new SimpleDateFormat("hh:mm:aa", currentLocale);

        String timeView;
        Date calendarTime = date.getTime();

        boolean is24 = isUserPreferred24Hours(getContext());
        timeView = is24 ? timeFormat24.format(calendarTime)
                        : timeFormat12.format(calendarTime);

        String[] splitTime = timeView.split(":");
        String hour = splitTime[0];
        alarmHour.setText(hour);
        alarmMin.setText(splitTime[1]);
        boolean isAM = false;
        if (is24) {
            am_pm.setVisibility(View.GONE);
        } else {
            am_pm.setText(splitTime[2]);
            isAM = splitTime[2].equals("AM");
        }

        // avoid glitch by setting the icon before starting timer
        setDayIcon(is24, isAM, Short.parseShort(hour));

        // Get the preferences for date format

        Context appContext = getContext().getApplicationContext();
        preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        String dateFormat = preferences.getString("date_format", "");
        String format;

        switch (dateFormat) {
            case "Full":
                format = DateFormat.getDateInstance(DateFormat.FULL)
                        .format(date.getTime());
                break;
            case "Short":
                format = DateFormat.getDateInstance(DateFormat.SHORT)
                        .format(date.getTime());
                break;
            default:
                format = DateFormat.getDateInstance(DateFormat.MEDIUM)
                        .format(date.getTime());

        }

        alarmDate.setText(format);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (timeChangeDetector == null) {
            (timeChangeDetector
                    = TimeChangeDetector.getInstance().with(this.getContext(),
                                                            preferences))
                    .start();

        }
        if (timeChangeDetector != null)
            timeChangeDetector.setOnTimeChangedListener(
                    (date, hour, min, v, is24, isAM) -> {
                        alarmDate.setText(date);
                        alarmHour.setText(hour);
                        alarmMin.setText(min);
                        am_pm.setVisibility(v);
                        setDayIcon(is24, isAM, Short.parseShort(hour));
                    });
    }


    @Override
    public void onDestroyView() {
        if (timeChangeDetector != null) {
            timeChangeDetector.pauseOperation();
            timeChangeDetector = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        if (timeChangeDetector != null) {
            timeChangeDetector.pauseOperation();
            timeChangeDetector = null;
        }
        super.onDetach();
    }

    private void setDayIcon(boolean is24, boolean isAM, short hourNum) {
        if (is24) {
            if (hourNum <= 18) {
                img_dayAndNight.setImageResource(R.drawable.ic_day_full);
            } else {
                img_dayAndNight.setImageResource(R.drawable.ic_night_icon);
                img_dayAndNight.setBackgroundResource(R.drawable.night);
            }
        } else {
            if (isAM) {
                img_dayAndNight.setImageResource(R.drawable.ic_day_full);
            } else {
                img_dayAndNight.setImageResource(R.drawable.ic_night_icon);
                img_dayAndNight.setBackgroundResource(R.drawable.night);
            }
        }
    }
}