package com.noah.timely.courses;

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

@SuppressWarnings("ConstantConditions")
public class CoursesFragment extends Fragment {

    public static Fragment newInstance() {
        return new CoursesFragment();
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
        pager.setAdapter(new SemesterCoursesAdapter(this));
        TabLayout tabs = view.findViewById(R.id.tabs);
        new TabLayoutMediator(tabs, pager,
                              (tab, position) -> tab.setText(position == 0 ? "First Semester"
                                                                           : "Second Semester")
        ).attach();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Registered Courses");
    }

    // The horizontal paging adapter of the viewpager
    private static class SemesterCoursesAdapter extends FragmentStateAdapter {

        public SemesterCoursesAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return SemesterFragment.newInstance(position);
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

}
