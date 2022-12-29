package com.astrro.timely.main;

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

import com.astrro.timely.R;
import com.astrro.timely.util.PreferenceUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class IntroPageActivity extends AppCompatActivity implements View.OnClickListener {
   public static final int MAX_PAGE_INDEX = 3;
   private IntroPagerAdapter adapter;
   private ViewPager2 pager_intro;

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.intro);

      Button start = findViewById(R.id.start);
      Button skip = findViewById(R.id.skip);
      FloatingActionButton btn_next = findViewById(R.id.next);
      FloatingActionButton btn_prev = findViewById(R.id.prev);

      adapter = new IntroPagerAdapter(this);
      pager_intro = findViewById(R.id.intro_pager);
      pager_intro.setAdapter(adapter);
      // set up page position indicator to react to page scroll
      TabLayout tab = findViewById(R.id.indicator);
      new TabLayoutMediator(tab, pager_intro, (tab1, position) -> {
      }).attach();

      btn_prev.setOnClickListener(this);
      btn_next.setOnClickListener(this);

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
            start.setVisibility(position == MAX_PAGE_INDEX ? View.VISIBLE : View.GONE);
            skip.setVisibility(position == MAX_PAGE_INDEX ? View.GONE : View.VISIBLE);

         }

      });

      // navigate to landing page
      start.setOnClickListener(this::navigateToLandingPage);
      skip.setOnClickListener(this::navigateToLandingPage);
   }

   private void navigateToLandingPage(View view) {
      PreferenceUtils.setFirstLaunchKey(getApplicationContext(), false);
      Intent nav_main = new Intent(this, MainActivity.class);
      nav_main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
      startActivity(nav_main);      // TimeLY is being exited, set the first launch key to false
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
   }

   @Override
   public void onClick(View v) {
      int shift = 0;
      int viewId = v.getId();
      if (viewId == R.id.next) shift = +1;
      else if (viewId == R.id.prev) shift = -1;

      int selectedItem = pager_intro.getCurrentItem();
      if (selectedItem >= 0 && selectedItem < adapter.getItemCount())
         pager_intro.setCurrentItem(pager_intro.getCurrentItem() + shift);
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
