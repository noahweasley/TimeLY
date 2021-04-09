package com.projects.timely.alarms;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.projects.timely.R;
import com.projects.timely.core.Time;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.util.Calendar;

@SuppressWarnings("ConstantConditions")
public class AlarmTimeFragment extends Fragment {
    private ImageView img_dayAndNight;
    private TextView alarmDate, alarmMin, alarmHour, am_pm;
    private boolean is24;

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

        // can't register event bus in onCreate() callback, because of possible null exceptions that
        // would have been thrown
        EventBus.getDefault().register(this);

        btn_setTime.setOnClickListener((v) -> {
            // when settings button is clicked, show the system date settings
            startActivity(new Intent(Settings.ACTION_DATE_SETTINGS));
        });

        // avoid glitch by setting the icon before starting timer
        Time time = new TimeChangeDetector().with(getActivity()).requestImmediateTime();
        this.is24 = time.getIs24();
        doTimeUpdate(time);
        setDayIcon(time);

        // Get the preferences for date format
        Calendar date = Calendar.getInstance();
        Context appContext = getContext().getApplicationContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        EventBus.getDefault().unregister(this);
        super.onDetach();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doTimeUpdate(Time time) {
        alarmDate.setText(time.getDate());
        alarmHour.setText(time.getHour());
        alarmMin.setText(time.getMinutes());
        am_pm.setVisibility(is24 ? View.GONE : View.VISIBLE);
        setDayIcon(time);
    }

    private void setDayIcon(Time time) {

        boolean isForeNoon = time.isForenoon();
        int hourNum = Integer.parseInt(time.getHour());

        /////////////////////   CODE DUPLICATION, TO BE UPDATED  ///////////////////////////////

        if (isForeNoon && !is24) {
            img_dayAndNight.setImageResource(R.drawable.ic_day_full);
        } else if (!isForeNoon && !is24) {
            if (hourNum == 12 || (hourNum >= 1 && hourNum <= 4)) {
                img_dayAndNight.setImageResource(R.drawable.ic_day_full);
            } else {
                img_dayAndNight.setImageResource(R.drawable.ic_night_icon);
                img_dayAndNight.setBackgroundResource(R.drawable.night);
            }
        } else if (is24) {
            if (hourNum >= 0 && hourNum < 12) {
                img_dayAndNight.setImageResource(R.drawable.ic_day_full);
            } else if (hourNum >= 12 && hourNum <= 16) {
                img_dayAndNight.setImageResource(R.drawable.ic_day_full);
            } else {
                img_dayAndNight.setImageResource(R.drawable.ic_night_icon);
                img_dayAndNight.setBackgroundResource(R.drawable.night);
            }
        }
    }
}