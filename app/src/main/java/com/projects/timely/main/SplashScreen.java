package com.projects.timely.main;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.projects.timely.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {
    private static final int TIMEOUT = 5000;
    private Handler handler;
    private Runnable startMain;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        TextView tv_appName, tv_company;
        tv_appName = findViewById(R.id.app_name);
        tv_company = findViewById(R.id.company);
        findViewById(R.id.skip).setOnClickListener(this::skipSplashScreen);

        // Used the non-italic version here, even when VISIONPHIX font is the italics version
        tv_company.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Modern Machine.ttf"));
        // Load all animations
        tv_company.startAnimation(AnimationUtils.loadAnimation(this, R.anim.cn_anim));
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
        startActivity(new Intent(this, MainActivity.class));
        finish();
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
