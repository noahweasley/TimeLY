package com.projects.timely.gallery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.projects.timely.R;
import com.projects.timely.assignment.ViewImagesActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class ImageListRowHolder extends RecyclerView.ViewHolder {
    private final ImageView img_image;
    private final View v_selectionOverlay;
    private boolean isChecked;
    private Uri imageContentUri;
    private ViewImagesActivity.ImageAdapter imageAdapter;
    private List<Image> imageList;

    @SuppressLint("ClickableViewAccessibility")
    public ImageListRowHolder(@NonNull View rootView) {
        super(rootView);
        img_image = rootView.findViewById(R.id.image);
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
                    if (!isChecked) img_image.setColorFilter(
                            ContextCompat.getColor(context, R.color.image_click_bg));
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    img_image.clearColorFilter();
                    break;
            }
            return false;
        });
    }

    public ImageListRowHolder with(List<? extends Uri> uris,
                                   List<Image> imageList,
                                   ViewImagesActivity.ImageAdapter imageAdapter) {
        this.imageList = imageList;
        this.imageContentUri = uris.get(getAbsoluteAdapterPosition());
        this.imageAdapter = imageAdapter;
        return this;
    }

    private void trySelectImage() {
        isChecked = !isChecked;
        v_selectionOverlay.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        imageAdapter.onChecked(getAbsoluteAdapterPosition(), isChecked, imageContentUri);
    }

    public void bindView() {
        Picasso.get().load(imageContentUri).fit().centerCrop().into(this.img_image);
        isChecked = imageAdapter.isChecked(getAbsoluteAdapterPosition());
        v_selectionOverlay.setVisibility(isChecked ? View.VISIBLE : View.GONE);
    }
}