package com.noah.timely.gallery;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.noah.timely.R;

public class FullScreenImageActivity extends AppCompatActivity {
   public static final String ARG_IMAGE = "Fullscreen image";

   public static void start(Context context, Image image) {
       Intent starter = new Intent(context, FullScreenImageActivity.class);
       starter.putExtra(ARG_IMAGE, image);
       context.startActivity(starter);
   }

   @Override
   protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_fullscreeen_image);
   }
}
