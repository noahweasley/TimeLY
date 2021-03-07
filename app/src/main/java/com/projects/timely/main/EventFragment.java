package com.projects.timely.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.projects.timely.R;
import com.projects.timely.core.Time;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

@SuppressWarnings({"ConstantConditions"})
public class EventFragment extends Fragment {
    private TextView text;

    static EventFragment newInstance() {
        return new EventFragment();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doUpdateGreeting(Time time) {
        switch (time.getDayPart()) {
            case MORNING:
                text.setText(R.string.morning);
                break;
            case AFTERNOON:
                text.setText(R.string.afternoon);
                break;
            case EVENING:
                text.setText(R.string.evening);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + time.getDayPart());
        }
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
        text = view.findViewById(R.id.no_task_text);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
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
