package com.projects.timely.alarms;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.projects.timely.R;
import com.projects.timely.core.Time;
import com.projects.timely.core.TimeRefreshEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
        TextView blinkTarget = view.findViewById(R.id.blink);
        blinkTarget
                .startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.blink_animation));

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
        instantiateTime();
    }

    // avoid glitch by setting the icon before starting timer
    private void instantiateTime() {
        Time time = TimeChangeDetector.requestImmediateTime(getContext());
        this.is24 = time.isMilitaryTime();
        doTimeUpdate(time);
        setDayIcon(time);
        alarmDate.setText(time.getDate());
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
        am_pm.setText(time.isForenoon() ? "AM" : "PM");
        am_pm.setVisibility(is24 ? View.GONE : View.VISIBLE);
        setDayIcon(time);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doLayoutRefresh(TimeRefreshEvent refreshLayoutEvent) {
        instantiateTime();
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