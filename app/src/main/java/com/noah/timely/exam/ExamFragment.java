package com.noah.timely.exam;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.noah.timely.R;
import com.noah.timely.main.MainActivity;

@SuppressWarnings("ConstantConditions")
public class ExamFragment extends Fragment {
    private int weekCount;

    public static Fragment newInstance() {
        return new ExamFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_horizontal_pagers, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String countValue = null;
        try {
            countValue = prefs.getString("exam weeks", "8");
            weekCount = Integer.parseInt(countValue);
        } catch (NumberFormatException exc) {
            Log.w(getClass().getSimpleName(),
                  "Week count of: " + countValue + " is ignored, using" + " 8 weeks instead ");
            weekCount = 8;
        }

        ViewPager2 pager = view.findViewById(R.id.pager);
        pager.setAdapter(new ExamPageAdapter(this));
        TabLayout tabs = view.findViewById(R.id.tabs);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabs.setTabGravity(TabLayout.GRAVITY_CENTER);
        new TabLayoutMediator(tabs, pager,
                              (tab, position) -> tab.setText("WEEK " + (position + 1))).attach();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Exam Timetable");
    }

    private class ExamPageAdapter extends FragmentStateAdapter {

        public ExamPageAdapter(ExamFragment examFragment) {
            super(examFragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return ExamTimetableFragment.newInstance(position);
        }

        @Override
        public int getItemCount() {
            return weekCount;
        }

    }
}
