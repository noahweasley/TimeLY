package com.noah.timely.gallery;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.noah.timely.R;
import com.squareup.picasso.Picasso;

import java.util.List;

class ImageDirectoryRowHolder extends RecyclerView.ViewHolder {
   private final ImageView img_image;
   private final TextView tv_directoryName;
   private final TextView tv_directorySize;
   private String bucketDisplayName;
   private Activity parentActivity;
   private List<? extends List<Image>> imageCollection;
   private int position;
   private String accessedStorage;
   private String reqAction;

   @SuppressLint("ClickableViewAccessibility")
   ImageDirectoryRowHolder(View view) {
      super(view);
      img_image = view.findViewById(R.id.rowImage);
      tv_directoryName = view.findViewById(R.id.directoryName);
      tv_directorySize = view.findViewById(R.id.directorySize);

      img_image.setOnClickListener(v -> {
         if (parentActivity instanceof ImageDirectory) {
            Intent intent = new Intent(parentActivity, ImageGallery.class);
            intent.setAction(reqAction);
            intent.putExtra("folder", bucketDisplayName);
            intent.putExtra(ImageDirectory.STORAGE_ACCESS_ROOT, accessedStorage);
            parentActivity.startActivity(intent);
            parentActivity.finish();
         }
      });

      img_image.setOnTouchListener((v, event) -> {
         switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
               img_image.setColorFilter(ContextCompat.getColor(img_image.getContext(), R.color.image_click_bg));
               break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
               img_image.clearColorFilter();
            }
         }
         return false;
      });
   }

   ImageDirectoryRowHolder with(Activity activity, List<? extends List<Image>> list, int position,
                                String accessedStorage) {
      this.parentActivity = activity;
      this.imageCollection = list;
      this.position = position;
      this.accessedStorage = accessedStorage;
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

   public ImageDirectoryRowHolder setRequestAction(String action) {
      this.reqAction = action;
      return this;
   }
}