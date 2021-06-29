package com.noah.timely.timetable;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.noah.timely.R;
import com.noah.timely.main.MainActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import static com.noah.timely.util.Utility.DAYS;

@SuppressWarnings("ConstantConditions")
public class TimetableFragment extends Fragment {
    public static final String ARG_POSITION = "page position";

    public static TimetableFragment newInstance() {
        return new TimetableFragment();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_horizontal_pagers, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ViewPager2 pager = view.findViewById(R.id.pager);
        pager.setAdapter(new DaysAdapter(this));
        pager.setOffscreenPageLimit(6);
        TabLayout tabs = view.findViewById(R.id.tabs);
        tabs.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabs.setTabGravity(TabLayout.GRAVITY_CENTER);
        new TabLayoutMediator(tabs, pager,
                              ((tab, position) -> tab.setText(DAYS[position]))).attach();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Timetable");
    }

    // The horizontal paging adapter of the viewpager
    private static class DaysAdapter extends FragmentStateAdapter {

        public DaysAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return DaysFragment.newInstance(position);
        }

        @Override
        public int getItemCount() {
            return 6;
        }
    }
}