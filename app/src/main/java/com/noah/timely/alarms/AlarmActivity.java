package com.noah.timely.alarms;

import static com.noah.timely.alarms.AlarmReceiver.ALARM_POS;
import static com.noah.timely.alarms.AlarmReceiver.ID;
import static com.noah.timely.alarms.AlarmReceiver.NOTIFICATION_ID;
import static com.noah.timely.util.MiscUtil.isUserPreferred24Hours;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.noah.timely.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AlarmActivity extends AppCompatActivity {
   private Intent receiverSnooze;
   private Intent receiverDismiss;

   @Override
   @SuppressWarnings("deprecation")
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.alarm_view);
      // keep screen on
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
      // show activity even when device is locked
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
         setShowWhenLocked(true);
      } else {
         // noinspection deprecation
         getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
      }

      Intent starterIntent = getIntent();
      // Register this activity as a receiver of the MessageEvent posts
      EventBus.getDefault().register(this);

      TextView tv_time = findViewById(R.id.time);

      boolean is24 = isUserPreferred24Hours(this);
      TextView am_pm = findViewById(R.id.am_pm);
      ImageView img_alarmClock = findViewById(R.id.alarm_clock);
      img_alarmClock.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_animation));

      String time = starterIntent.getStringExtra("time");

      am_pm.setVisibility(is24 ? View.GONE : View.VISIBLE);
      if (!TextUtils.isEmpty(time) && !is24) {
         if (time.endsWith("PM")) {
            am_pm.setText(R.string.pm);
            time = time.replace(" PM", "");
         } else {
            am_pm.setText(R.string.am);
            time = time.replace(" AM", "");
         }
      }

      if (time != null) tv_time.setText(time);

      Animation alphaAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_reverse);
      findViewById(R.id.alarm_text).startAnimation(alphaAnimation);

      Button btn_snooze, btn_dismiss;
      btn_snooze = findViewById(R.id.snooze);
      btn_dismiss = findViewById(R.id.dismiss);

      String action = starterIntent.getAction();

      boolean snoozeAction = false;
      if (action != null) snoozeAction = action.equals("Snooze");

      if (snoozeAction) findViewById(R.id.snooze_stat).setVisibility(View.VISIBLE);

      String label = starterIntent.getStringExtra("Label");
      TextView tv_Label = findViewById(R.id.label);

      if (label != null && !label.equals("Label")) tv_Label.setText(label);
      else tv_Label.setVisibility(View.GONE);

      receiverSnooze = new Intent(this, NotificationActionReceiver.class);
      receiverSnooze.putExtra("action", "Snooze")
                    .putExtra(ID, NOTIFICATION_ID)
                    .putExtra(ALARM_POS, getIntent().getIntExtra(ALARM_POS, -1));

      receiverDismiss = new Intent(this, NotificationActionReceiver.class);
      receiverDismiss.putExtra("action", "Dismiss")
                     .putExtra(ID, NOTIFICATION_ID)
                     .putExtra(ALARM_POS, getIntent().getIntExtra(ALARM_POS, -1));

      btn_snooze.setOnClickListener(v -> {
         sendBroadcast(receiverSnooze);
         finish();
      });

      btn_dismiss.setOnClickListener(v -> {
         sendBroadcast(receiverDismiss);
         finish();
      });
   }

   // GreenRobot EventBuses was used here because it was impossible to finish this activity
   // because the action is from a service and the data that is needed is not supplied to this
   // activity when it start up.
   @Subscribe(threadMode = ThreadMode.MAIN)
   public void onExitEvent(MessageEvent event) {
      finish();
   }

   @Override
   public void onBackPressed() {
      // Dismiss the alarm or snooze, based on user preference
      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
      boolean backIsDismiss = preferences.getBoolean("dismiss", true);
      sendBroadcast(backIsDismiss ? receiverDismiss : receiverSnooze);
      super.onBackPressed();
      overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
   }

   @Override
   protected void onDestroy() {
      EventBus.getDefault().unregister(this);
      receiverDismiss = null;
      receiverSnooze = null;
      super.onDestroy();
   }
}