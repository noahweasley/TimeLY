package com.astrro.timely.calculator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.astrro.timely.R;

public class ResultCalculatorContainerFragment extends Fragment {

   public static Fragment newInstance() {
      ResultCalculatorContainerFragment fragment = new ResultCalculatorContainerFragment();
      Bundle args = new Bundle();
      fragment.setArguments(args);
      return fragment;
   }

   @Nullable
   @org.jetbrains.annotations.Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater,
                            @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                            @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
      // Inflate the layout for this fragment
      return inflater.inflate(R.layout.fragment_horizontal_pagers, container, false);
   }

   @Override
   public void onViewCreated(@NonNull View view,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      ViewPager2 pager = view.findViewById(R.id.pager);
      pager.setAdapter(new ContainerAdapter(this));
      TabLayout tabs = view.findViewById(R.id.tabs);
      tabs.setTabMode(TabLayout.MODE_FIXED);
      tabs.setTabGravity(TabLayout.GRAVITY_FILL);

      new TabLayoutMediator(tabs, pager, (tab, position) -> tab.setText(position == 0 ? "1st SEM" : "2nd SEM")).attach();

   }

   @Override
   public void onResume() {
      super.onResume();
      ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.gpa_calculator);
   }

   private static class ContainerAdapter extends FragmentStateAdapter {

      public ContainerAdapter(Fragment fragment) {
         super(fragment);
      }

      @NonNull
      @Override
      public Fragment createFragment(int position) {
         return ResultCalculatorFragment.newInstance(position);
      }

      @Override
      public int getItemCount() {
         return 2;
      }
   }

}
