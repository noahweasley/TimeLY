package com.noah.timely.gallery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.noah.timely.R;
import com.noah.timely.assignment.AddAssignmentActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

class ImageGalleryRowHolder extends RecyclerView.ViewHolder {
    private final ImageView img_image;
    private final TextView tv_fileName;
    private final ViewGroup v_selectionOverlay;
    private ImageGallery.ImageAdapter imageAdapter;
    private String fileName;
    private Uri imageContentUri;
    private List<? extends Image> imageList;
    private boolean isChecked;

    @SuppressLint("ClickableViewAccessibility")
    ImageGalleryRowHolder(View rootView) {
        super(rootView);
        img_image = rootView.findViewById(R.id.rowImage);
        tv_fileName = rootView.findViewById(R.id.filename);
        v_selectionOverlay = rootView.findViewById(R.id.checked_overlay);
        Context context = img_image.getContext();

        rootView.setOnClickListener(c -> {
            if (imageAdapter.isMultiSelectionEnabled()) {
                trySelectImage();
                if (imageAdapter.getCheckedImageCount() == 0) {
                    imageAdapter.setMultiSelectionEnabled(false);
                }
            } else ImageSlideActivity.start(context, getAbsoluteAdapterPosition(), imageList);

        });

        rootView.setOnLongClickListener(l -> {
            trySelectImage();
            imageAdapter
                    .setMultiSelectionEnabled(!imageAdapter.isMultiSelectionEnabled()
                                                      || imageAdapter.getCheckedImageCount() != 0);
            return true;
        });

        rootView.setOnTouchListener((t, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!isChecked)
                        img_image.setColorFilter(
                                ContextCompat
                                        .getColor(img_image.getContext(), R.color.image_click_bg));
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    img_image.clearColorFilter();
            }
            return false;
        });
    }

    ImageGalleryRowHolder with(ImageGallery.ImageAdapter imageAdapter,
                               List<? extends Image> imageList) {
        this.imageList = imageList;
        Image image = imageList.get(getAbsoluteAdapterPosition());
        this.imageContentUri = image.getImageUri();
        this.fileName = image.getFileName();
        this.imageAdapter = imageAdapter;
        return this;
    }

    private void trySelectImage() {
        isChecked = !isChecked;
        v_selectionOverlay.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        imageAdapter.onChecked(getAbsoluteAdapterPosition(), isChecked, imageContentUri);
        if (isChecked) AddAssignmentActivity.mediaUris.add(imageContentUri);
        else AddAssignmentActivity.mediaUris.remove(imageContentUri);
    }

    public void bindView() {
        tv_fileName.setText(fileName);
        Picasso.get().load(imageContentUri).centerCrop().fit().into(img_image);
        isChecked = imageAdapter.isChecked(getAbsoluteAdapterPosition());
        v_selectionOverlay.setVisibility(isChecked ? View.VISIBLE : View.GONE);
    }
}