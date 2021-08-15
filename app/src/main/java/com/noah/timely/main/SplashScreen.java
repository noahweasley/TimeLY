package com.noah.timely.main;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.noah.timely.BuildConfig;
import com.noah.timely.R;
import com.noah.timely.util.PreferenceUtils;

public class SplashScreen extends AppCompatActivity {
    private static final int TIMEOUT = 5000;
    private Handler handler;
    private Runnable startMain;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                skipSplashScreen();
                return true;
            }
        });

        TextView tv_version, tv_appName;
        tv_version = findViewById(R.id.version);
        tv_appName = findViewById(R.id.app_name);

//        Spannable wordSpan = new SpannableString(getString(R.string.app_name));
//        ForegroundColorSpan foregroundColorSpan
//                = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.colorAccent));
//        wordSpan.setSpan(foregroundColorSpan, 4 /* L and Y */, wordSpan.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//
//        tv_appName.setText(wordSpan);

        String version = BuildConfig.VERSION_NAME;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException ignored) {}

        tv_version.setText(String.format("v%s", version));
        // Load all animations
        tv_appName.startAnimation(AnimationUtils.loadAnimation(this, R.anim.an_anim));
        // Display main screen after the splash screen
        (handler = new Handler(getMainLooper()))
                .postDelayed(startMain = this::launch, TIMEOUT);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    private void skipSplashScreen() {
        removeSystemCallback();
        launch();
    }

    @Override
    protected void onStop() {
        super.onStop();
        removeSystemCallback();
    }

    private void launch() {
        boolean isFirstLaunch = PreferenceUtils.getFirstLaunchKey(getApplicationContext());
        // start next screen based on the app's first time launch saved preference
        Intent launchIntent = new Intent(this, isFirstLaunch ? IntroPageActivity.class : MainActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(launchIntent);
    }

    @Override
    public void onBackPressed() {
        removeSystemCallback();
        super.onBackPressed();
    }

    private void removeSystemCallback() {
        if (handler != null)
            handler.removeCallbacks(startMain);
        handler = null;
    }

}
