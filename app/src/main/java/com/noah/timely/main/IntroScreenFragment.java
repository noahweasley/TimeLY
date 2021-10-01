package com.noah.timely.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.noah.timely.R;

public class IntroScreenFragment extends Fragment {
   private static final String ARG_POSITION = "page position";

   public static IntroScreenFragment newInstance(int position) {
      Bundle args = new Bundle();
      args.putInt(ARG_POSITION, position);
      IntroScreenFragment fragment = new IntroScreenFragment();
      fragment.setArguments(args);
      return fragment;
   }

   @Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {

      return inflater.inflate(R.layout.intro_page, container, false);
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      int position = getArguments().getInt(ARG_POSITION);

      LottieAnimationView lottieAnimationView = view.findViewById(R.id.animationView);
      TextView tv_title = view.findViewById(R.id.title);

      switch (position) {
         case 0:
            tv_title.setText(R.string.manage_time);
            lottieAnimationView.setAnimation(R.raw.time_management);
            break;
         case 1:
            tv_title.setText(R.string.track_progress);
            lottieAnimationView.setAnimation(R.raw.track_your_progress);
            break;
         case 2:
            tv_title.setText(R.string.manage_assignments);
            lottieAnimationView.setAnimation(R.raw.paper_and_document);
            break;
         case 3:
            tv_title.setText(R.string.manage_exams);
            lottieAnimationView.setAnimation(R.raw.get_things_done);
            break;
      }

   }

   @Override
   public void onDestroyView() {
      super.onDestroyView();
   }
}
