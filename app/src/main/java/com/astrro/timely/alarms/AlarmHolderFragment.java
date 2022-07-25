package com.astrro.timely.alarms;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.astrro.timely.R;

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
      getChildFragmentManager()
              .beginTransaction()
              .replace(R.id.alarm_time_fragment, new AlarmTimeFragment())
              .replace(R.id.alarm_list_fragment, new AlarmListFragment())
              .commit();
   }

   @Override
   public void onResume() {
      super.onResume();
      ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Alarms");
   }
}
