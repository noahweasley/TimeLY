package com.projects.timely.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.projects.timely.R;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

@SuppressWarnings({"ConstantConditions"})
public class EventFragment extends Fragment {

    static EventFragment newInstance() {
        return new EventFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent,
                             Bundle savedState) {
        return inflater.inflate(R.layout.home, parent, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle state) {
        view.findViewById(R.id.discover).setOnClickListener(
                (v) -> ((MainActivity) getActivity()).drawer.openDrawer(GravityCompat.START));
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Events");
    }
}
