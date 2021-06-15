package com.noah.timely.main;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        TextView tv_appName, tv_version;
        tv_appName = findViewById(R.id.app_name);
        tv_version = findViewById(R.id.version);
        findViewById(R.id.skip).setOnClickListener(this::skipSplashScreen);

        String version = BuildConfig.VERSION_NAME;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(getClass().getSimpleName(), "Using build version: " + version);
        }

        tv_version.setText(String.format("Version %s", version));
        // Load all animations
        tv_version.startAnimation(AnimationUtils.loadAnimation(this, R.anim.cn_anim));
        tv_appName.startAnimation(AnimationUtils.loadAnimation(this, R.anim.an_anim));
        // Display main screen after the splash screen
        (handler = new Handler(getMainLooper()))
                .postDelayed(startMain = this::launch, TIMEOUT);

    }

    private void skipSplashScreen(View v) {
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
        Intent launchIntent = new Intent(this, isFirstLaunch ?
                                               IntroPageActivity.class : MainActivity.class);
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
