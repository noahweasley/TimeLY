package com.noah.timely.gallery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import com.noah.timely.auth.ui.login.CompleteRegistrationActivity;
import com.noah.timely.util.Constants;
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
   private String requestAction;
   private Image image;

   @SuppressLint("ClickableViewAccessibility")
   ImageGalleryRowHolder(View rootView) {
      super(rootView);
      img_image = rootView.findViewById(R.id.rowImage);
      tv_fileName = rootView.findViewById(R.id.filename);
      v_selectionOverlay = rootView.findViewById(R.id.checked_overlay);
      Context context = img_image.getContext();

      rootView.setOnClickListener(c -> {
         // only when the multi-select action is requested should IT, save uris
         if (requestAction.equals(ImageGallery.ACTION_MULTI_SELECT)) {
            if (imageAdapter.isMultiSelectionEnabled()) {
               trySelectImage();
               if (imageAdapter.getCheckedImageCount() == 0) {
                  imageAdapter.setMultiSelectionEnabled(false);
               }
            } else ImageSlideActivity.start(context, getAbsoluteAdapterPosition(), imageList);

         } else if (requestAction.equals(ImageGallery.ACTION_SINGLE_SELECT)) {
            Intent intent = new Intent(context, CompleteRegistrationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(Constants.EXTRA.EXTRA_IMAGE, image);
            intent.setAction(Constants.ACTION.SHOW_PICTURE);
            context.startActivity(intent);
         }
      });

      rootView.setOnLongClickListener(l -> {
         // only when the multi-select action is requested should IT, save uris
         if (requestAction.equals(ImageGallery.ACTION_MULTI_SELECT)) {
            trySelectImage();
            imageAdapter.setMultiSelectionEnabled(!imageAdapter.isMultiSelectionEnabled()
                                                          || imageAdapter.getCheckedImageCount() != 0);
         }

         return true;
      });

      rootView.setOnTouchListener((t, event) -> {
         switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
               if (!isChecked)
                  img_image.setColorFilter(ContextCompat.getColor(img_image.getContext(), R.color.image_click_bg));
               break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
               img_image.clearColorFilter();
         }
         return false;
      });
   }

   ImageGalleryRowHolder with(ImageGallery.ImageAdapter imageAdapter, List<? extends Image> imageList) {
      this.imageList = imageList;
      this.image = imageList.get(getAbsoluteAdapterPosition());
      this.imageContentUri = image.getImageUri();
      this.fileName = image.getFileName();
      this.imageAdapter = imageAdapter;
      return this;
   }

   public ImageGalleryRowHolder setRequestAction(String requestAction) {
      this.requestAction = requestAction;
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