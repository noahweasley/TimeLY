package com.projects.timely.gallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.projects.timely.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ImageSlideActivity extends AppCompatActivity {
    public static final String ARG_INIT_URI = "Initial Uri from list";

    public static void start(Context context, Uri initial) {
        Intent starter = new Intent(context, ImageSlideActivity.class);
        starter.putExtra(ARG_INIT_URI, initial.toString());
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
        setContentView(R.layout.image_silder);
    }
}
