package com.projects.timely.gallery;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.projects.timely.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

class ImageRowHolder extends RecyclerView.ViewHolder {
    private final ImageView img_image;
    private final TextView tv_directoryName;
    private final TextView tv_directorySize;
    private String bucketDisplayName;
    private Activity parentActivity;
    private List<? extends List<Image>> imageCollection;
    private int position;
    private String action;

    @SuppressLint("ClickableViewAccessibility")
    ImageRowHolder(View view) {
        super(view);
        img_image = view.findViewById(R.id.rowImage);
        tv_directoryName = view.findViewById(R.id.directoryName);
        tv_directorySize = view.findViewById(R.id.directorySize);

        img_image.setOnClickListener(v -> {
            if (parentActivity instanceof ImageDirectory) {
                Intent intent = new Intent(parentActivity, ImageGallery.class);
                intent.setAction(action);
                intent.putExtra("folder", bucketDisplayName);
                parentActivity.startActivity(intent);
                parentActivity.finish();
            }
        });

        img_image.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    img_image.setColorFilter(
                            ContextCompat.getColor(img_image.getContext(), R.color.image_click_bg));
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    img_image.clearColorFilter();
                }
            }
            return false;
        });
    }

    ImageRowHolder with(Activity activity, List<? extends List<Image>> list, int position) {
        this.parentActivity = activity;
        this.imageCollection = list;
        this.position = position;
        return this;
    }

    void loadThumbnail() {
        // set the directory's content image to the first image in the directory.
        Image image = imageCollection.get(position).get(0);
        bucketDisplayName = image.getFolderName();
        tv_directoryName.setText(bucketDisplayName);
        tv_directorySize.setText(String.valueOf(imageCollection.get(position).size()));
        Picasso.get().load(image.getImageUri()).centerCrop().fit().into(img_image);
    }

    public ImageRowHolder setAction(String action) {
        this.action = action;
        return this;
    }
}