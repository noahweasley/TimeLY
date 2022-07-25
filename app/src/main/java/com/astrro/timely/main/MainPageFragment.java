package com.astrro.timely.main;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.astrro.timely.R;
import com.astrro.timely.alarms.TimeChangeDetector;
import com.astrro.timely.core.DayPart;
import com.astrro.timely.core.Time;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Random;

public class MainPageFragment extends Fragment {
   public static final String TAG = "com.astrro.timely.main.MainPageFragment";
   private TextView tv_gText;
   private DayPart lastDayPart;
   private Context context;
   private static final Fragment fragmentInstance = new MainPageFragment();
   private static final String TOOLBAR_TITLE = "Discover";

   public static Fragment getInstance() {
      return fragmentInstance;
   }

   public static String getToolbarTitle() {
      return TOOLBAR_TITLE;
   }

   @Subscribe(threadMode = ThreadMode.MAIN)
   public void doUpdateGreeting(Time time) {
      // prevent unnecessary UI update when day part is the same
      if (this.lastDayPart != time.getCurrentDayPart()) {
         switch (time.getCurrentDayPart()) {
            case MORNING:
               if (tv_gText != null)
                  tv_gText.setText(R.string.morning);
               break;
            case AFTERNOON:
               if (tv_gText != null)
                  tv_gText.setText(R.string.afternoon);
               break;
            case EVENING:
               if (tv_gText != null)
                  tv_gText.setText(R.string.evening);
               break;
            case NIGHT:
               if (tv_gText != null)
                  tv_gText.setText(R.string.night);
               break;
            case SLEEP_TIME:
               if (tv_gText != null)
                  tv_gText.setText(R.string.sleep);
               break;
            case DAY_START_ACTIVE_PERIOD:
               if (tv_gText != null)
                  tv_gText.setText(R.string.rise);
               break;
            case DEFAULT_INTERVAL_DAY:
               if (tv_gText != null) {
                  String[] gs = { "Hi there", "Good Day", "Hello" };
                  int r = new Random(System.currentTimeMillis()).nextInt(gs.length);
                  tv_gText.setText(gs[r]);
               }
               break;
            default:

               throw new IllegalStateException("Unexpected value: " + time.getCurrentDayPart());
         }
      }
      lastDayPart = time.getCurrentDayPart();
   }

   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedState) {
      return inflater.inflate(R.layout.fragment_main_page, parent, false);
   }

   @Override
   public void onViewCreated(@NonNull View view, Bundle state) {
      tv_gText = view.findViewById(R.id.greeting_text);

      EventBus.getDefault().register(this);
      doUpdateGreeting(TimeChangeDetector.requestImmediateTime(getContext()));
   }

   @Override
   public void onResume() {
      super.onResume();
      ((MainActivity) getActivity()).getSupportActionBar().setTitle(TOOLBAR_TITLE);
   }

   @Override
   public void onDetach() {
      super.onDetach();
      EventBus.getDefault().unregister(this);
   }

}