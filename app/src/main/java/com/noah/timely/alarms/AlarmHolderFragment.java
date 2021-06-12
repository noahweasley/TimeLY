package com.noah.timely.alarms;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.noah.timely.main.MainActivity;
import com.noah.timely.R;

@SuppressWarnings("ConstantConditions")
public class AlarmHolderFragment extends Fragment {

    public static AlarmHolderFragment newInstance() {
        return new AlarmHolderFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_holder_2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Duplicate code from TimetableHolderFragment
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.alarm_time_fragment, new AlarmTimeFragment())
                .replace(R.id.alarm_list_fragment, new AlarmListFragment())
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Alarms");
    }
}
