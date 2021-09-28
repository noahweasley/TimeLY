package com.noah.timely.todo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.noah.timely.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TodoContainerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TodoContainerFragment extends Fragment {
    private static final String ARG_TODO_CATEGORY = "Todo category";
    private String category;

    public TodoContainerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment
     *
     * @return A new instance of fragment TodoListSubFragment.
     */
    public static TodoContainerFragment newInstance(String category) {
        TodoContainerFragment fragment = new TodoContainerFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_TODO_CATEGORY, category);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = getArguments().getString(ARG_TODO_CATEGORY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_horizontal_pagers, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPager2 pager = view.findViewById(R.id.pager);
        pager.setOffscreenPageLimit(2);
        pager.setAdapter(new TodoSubListAdapter(this));
        TabLayout tabs = view.findViewById(R.id.tabs);
        tabs.setTabMode(TabLayout.MODE_FIXED);
        tabs.setTabGravity(TabLayout.GRAVITY_FILL);

        new TabLayoutMediator(tabs, pager,
                              (tab, position) -> tab.setText(position == 0 ? "ACTIVE" : "FINISHED")).attach();
    }

    private class TodoSubListAdapter extends FragmentStateAdapter {

        public TodoSubListAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return TodoListFragment.newInstance(position, category);
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

}