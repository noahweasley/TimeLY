package com.projects.timely.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.projects.timely.R;

public class IntroPageActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.intro);

        Button start = findViewById(R.id.start);

        ViewPager2 pager_intro = findViewById(R.id.intro_pager);
        pager_intro.setOffscreenPageLimit(3);
        pager_intro.setAdapter(new IntroPagerAdapter(this));
        // set up page position indicator to react to page scroll
        TabLayout tab = findViewById(R.id.indicator);
        new TabLayoutMediator(tab, pager_intro, (tab1, position) -> {
        }).attach();
        // add scroll listener
        pager_intro.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // disable over scroll glow effect at page edge
                View s_RecyclerView = pager_intro.getChildAt(0);
                if (s_RecyclerView instanceof RecyclerView)
                    s_RecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
                // hide or show corresponding views according to page position
                start.setVisibility(position == 3 ? View.VISIBLE : View.GONE);

            }

        });
        // navigate to landing page
        start.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(this, MainActivity.class)
                              .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                                | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    private static class IntroPagerAdapter extends FragmentStateAdapter {

        public IntroPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return IntroScreenFragment.newInstance(position);
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }
}