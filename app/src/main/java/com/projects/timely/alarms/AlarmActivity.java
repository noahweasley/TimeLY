package com.projects.timely.alarms;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.projects.timely.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import static com.projects.timely.alarms.AlarmReceiver.ALARM_POS;
import static com.projects.timely.alarms.AlarmReceiver.ID;
import static com.projects.timely.alarms.AlarmReceiver.NOTIFICATION_ID;
import static com.projects.timely.core.AppUtils.isUserPreferred24Hours;

@SuppressWarnings("ConstantConditions")
public class AlarmActivity extends AppCompatActivity {
    private Intent receiverSnooze;
    private Intent receiverDismiss;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.alarm_view);

        Intent starterIntent = getIntent();
        // Register this activity has a receiver of the MessageEvent posts
        EventBus.getDefault().register(this);

        TextView tv_time = findViewById(R.id.time);

        boolean is24 = isUserPreferred24Hours(this);
        TextView am_pm = findViewById(R.id.am_pm);
        ImageView img_alarmClock = findViewById(R.id.alarm_clock);
        img_alarmClock.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_animation));

        String time = starterIntent.getStringExtra("time");

        am_pm.setVisibility(is24 ? View.GONE : View.VISIBLE);
        if (!TextUtils.isEmpty(time) && !is24) {
            am_pm.setText(time.endsWith("PM") ? "PM" : "AM");
            time = time.replace(" PM", "");
        }

        if (time != null)
            tv_time.setText(time);

        // Start all the alarm animations
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setRepeatMode(Animation.REVERSE);
        alphaAnimation.setRepeatCount(Animation.INFINITE);
        alphaAnimation.setDuration(500);
        findViewById(R.id.alarm_text).startAnimation(alphaAnimation);

        Button btn_snooze, btn_dismiss;
        btn_snooze = findViewById(R.id.snooze);
        btn_dismiss = findViewById(R.id.dismiss);

        String action = starterIntent.getAction();

        boolean snoozeAction = false;
        if (action != null)
            snoozeAction = action.equals("Snooze");

        if (snoozeAction) findViewById(R.id.snooze_stat).setVisibility(View.VISIBLE);

        String label = starterIntent.getStringExtra("Label");
        TextView tv_Label = findViewById(R.id.label);

        if (label != null && !label.equals("Label"))
            tv_Label.setText(label);
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

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    // GreenRobot EventBuses was used here because it was impossible to finish this activity
    // because the action is from a service and the data that is needed is not supplied to this
    // activity when it start up.
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onExitEvent(EmptyMessageEvent event) {
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
}