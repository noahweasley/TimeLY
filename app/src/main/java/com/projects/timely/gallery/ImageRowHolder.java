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

class ImageRowHolder extends RecyclerView.ViewHolder{
    private ImageView img;
    private TextView directoryName;
    private TextView directorySize;
    private String bucketDisplayName;
    private Activity parentActivity;
    private List<? extends List<Image>> imageCollection;
    private int position;
    private String action;

    @SuppressLint("ClickableViewAccessibility")
    ImageRowHolder(View view) {
        super(view);
        img = view.findViewById(R.id.rowImage);
        directoryName = view.findViewById(R.id.directoryName);
        directorySize = view.findViewById(R.id.directorySize);

        img.setOnClickListener(v -> {
            if (parentActivity instanceof ImageDirectory) {
                Intent intent = new Intent(parentActivity, ImageGallery.class);
                intent.setAction(action);
                intent.putExtra("folder", bucketDisplayName);
                parentActivity.startActivity(intent);
                parentActivity.finish();
            }
        });

        img.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    img.setColorFilter(
                            ContextCompat.getColor(img.getContext(), R.color.image_click_bg));
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    img.clearColorFilter();
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
        // set the directory's ImageView's thumbnail to the first image thumbnail.
        Image image = imageCollection.get(position).get(0);
        bucketDisplayName = image.getFolderName();
        directoryName.setText(bucketDisplayName);
        directorySize.setText(String.valueOf(imageCollection.get(position).size()));
        Picasso.get().load(image.getImageUri()).centerCrop().fit().into(img);
    }

    public ImageRowHolder setAction(String action) {
        this.action = action;
        return this;
    }
}
