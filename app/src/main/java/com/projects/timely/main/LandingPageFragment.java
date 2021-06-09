package com.projects.timely.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.projects.timely.R;
import com.projects.timely.alarms.TimeChangeDetector;
import com.projects.timely.core.DayPart;
import com.projects.timely.core.Time;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

@SuppressWarnings({"ConstantConditions"})
public class LandingPageFragment extends Fragment {
    private TextView tv_gText;
    private DayPart lastDayPart;

    static LandingPageFragment newInstance() {
        return new LandingPageFragment();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doUpdateGreeting(Time time) {
        // prevent unnecessary UI update when day part is the same
        if (this.lastDayPart != time.getCurrentDayPart()) {
            switch (time.getCurrentDayPart()) {
                case MORNING:
                    if (tv_gText != null)
                        tv_gText.setText(R.string.morning);
                    Log.d(getClass().getSimpleName(), "Morning");
                    break;
                case AFTERNOON:
                    if (tv_gText != null)
                        tv_gText.setText(R.string.afternoon);
                    Log.d(getClass().getSimpleName(), "Afternoon");
                    break;
                case EVENING:
                    if (tv_gText != null)
                        tv_gText.setText(R.string.evening);
                    Log.d(getClass().getSimpleName(), "Evening");
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + time.getCurrentDayPart());
            }
        }
        lastDayPart = time.getCurrentDayPart();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent,
                             Bundle savedState) {
        return inflater.inflate(R.layout.landing_page, parent, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle state) {
        view.findViewById(R.id.discover).setOnClickListener(
                (v) -> ((MainActivity) getActivity()).drawer.openDrawer(GravityCompat.START));
        tv_gText = view.findViewById(R.id.greeting_text);
        EventBus.getDefault().register(this);
        doUpdateGreeting(TimeChangeDetector.requestImmediateTime(getContext()));
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Events");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

}
