package com.noah.timely.gallery;

import static com.noah.timely.assignment.ImageViewerActivity.ARG_URI_LIST;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.noah.timely.R;
import com.noah.timely.assignment.AddAssignmentActivity;
import com.noah.timely.assignment.ImageViewerActivity;
import com.noah.timely.core.ChoiceMode;
import com.noah.timely.util.ThreadUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressLint("InlinedApi")
public class ImageGallery extends AppCompatActivity implements Runnable, ActionMode.Callback {
   public static final String ARG_FILES_COUNT = "Attached files count";
   private final List<Image> images = new ArrayList<>();
   private final ChoiceMode choiceMode = ChoiceMode.IMAGE_MULTI_SELECT;
   private ImageAdapter imageAdapter;
   private String folder;
   private ProgressBar indeterminateProgress;
   private RecyclerView imageList;
   private ActionMode actionMode;

   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      folder = getIntent().getStringExtra("folder");
      setContentView(R.layout.image_gallery);

      Toolbar toolbar = findViewById(R.id.toolbar);
      imageList = findViewById(R.id.imageList);
      indeterminateProgress = findViewById(R.id.indeterminateProgress);
      setSupportActionBar(toolbar);
      getSupportActionBar().setTitle(folder);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      imageAdapter = new ImageAdapter(choiceMode);

      imageList.setHasFixedSize(true);
      imageList.setAdapter(imageAdapter);
      imageList.setLayoutManager(new GridLayoutManager(this, 2));
      imageList.setClickable(true);
      doViewUpdate();
      ThreadUtils.runBackgroundTask(this);
   }

   @Override
   protected void onSaveInstanceState(@NonNull Bundle outState) {
      super.onSaveInstanceState(outState);
      imageAdapter.getChoiceMode().onSaveInstanceState(outState);
   }

   @Override
   public void onBackPressed() {
      startActivity(new Intent(this, ImageDirectory.class));
      super.onBackPressed();
      overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
   }

   @Override
   protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      choiceMode.onRestoreInstanceState(savedInstanceState);
   }

   // will be replaced with a cursor loader
   @Override
   public void run() {
      Uri storageUri;
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
         storageUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
      } else {
         storageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
      }

      Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

      boolean queryAll = folder.equals("All Media");
      String[] projection = {
              MediaStore.Images.Media._ID,
              MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
              MediaStore.Images.Media.SIZE,
              MediaStore.Images.Media.DISPLAY_NAME};
      String selection = queryAll ? null : MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ?";
      String[] selectionArgs = queryAll ? null : new String[]{folder};
      String sortOrder = MediaStore.Images.Media.DATE_ADDED;

      Cursor imgCursor
              = getApplicationContext()
              .getContentResolver().query(storageUri, projection, selection, selectionArgs, sortOrder);

      int idColumn = imgCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
      int sizeColumn = imgCursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
      int displayNameColumn = imgCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
      int bucketDisplayNameColumn = imgCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

      while (imgCursor.moveToNext()) {
         long id = imgCursor.getLong(idColumn);
         int size = imgCursor.getInt(sizeColumn);
         String fileName = imgCursor.getString(displayNameColumn);
         String folderName = imgCursor.getString(bucketDisplayNameColumn);

         Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
         images.add(new Image(contentUri, size, fileName, folderName));
      }

      imgCursor.close();
      runOnUiThread(() -> {
         imageAdapter.notifyDataSetChanged();
         doViewUpdate();
      });
   }

   private void doViewUpdate() {
      indeterminateProgress.setVisibility(images.isEmpty() ? View.VISIBLE : View.GONE);
      imageList.setVisibility(images.isEmpty() ? View.GONE : View.VISIBLE);
   }

   @Override
   public boolean onCreateActionMode(ActionMode mode, Menu menu) {
      getMenuInflater().inflate(R.menu.checked_items, menu);
      return true;
   }

   @Override
   public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
      return false;
   }

   @Override
   public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

      if (getIntent().getAction().equals(ImageViewerActivity.ADD_NEW)) {
         ImageMultiChoiceMode imageMultiChoiceMode = (ImageMultiChoiceMode) imageAdapter.getChoiceMode();

         // FIXME: 2/23/2021 Find the cause of the additional images
         startActivity(new Intent(this, ImageViewerActivity.class)
                 .putExtra(ARG_URI_LIST, imageMultiChoiceMode.getUriList()));
      } else {
         startActivity(new Intent(this, AddAssignmentActivity.class)
                 .putExtra(ARG_FILES_COUNT, imageAdapter.getCheckedImageCount()));
      }
      finish();
      return true;
   }

   @Override
   public void onDestroyActionMode(ActionMode mode) {
      actionMode = null;
      imageAdapter.reset();
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
      imageAdapter.getChoiceMode().clearChoices();
   }

   @Override
   public boolean onSupportNavigateUp() {
      onBackPressed();
      return true;
   }

   class ImageAdapter extends RecyclerView.Adapter<ImageGalleryRowHolder> {
      private final ChoiceMode choiceMode;
      private boolean multiSelectionEnabled;

      public ImageAdapter(ChoiceMode choiceMode) {
         super();
         this.choiceMode = choiceMode;
      }

      @NonNull
      @Override
      public ImageGalleryRowHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int pos) {
         View view = getLayoutInflater().inflate(R.layout.layout_image_gallery_row, viewGroup, false);
         return new ImageGalleryRowHolder(view);
      }

      @Override
      public void onBindViewHolder(@NonNull ImageGalleryRowHolder viewHolder, int pos) {
         viewHolder.with(this, images).bindView();
      }

      @Override
      public int getItemCount() {
         return images.size();
      }

      /**
       * @return the choice-mode that was set.
       */
      public ChoiceMode getChoiceMode() {
         return choiceMode;
      }

      /**
       * @param adapterPosition the position of the view holder
       * @return the checked status of a particular image in the list
       */
      public boolean isChecked(int adapterPosition) {
         return choiceMode.isChecked(adapterPosition);
      }

      /**
       * @return the number of images that was selected
       */
      public int getCheckedImageCount() {
         return choiceMode.getCheckedChoiceCount();
      }

      /**
       * @return the status of the multi-selection mode
       */
      public boolean isMultiSelectionEnabled() {
         return multiSelectionEnabled;
      }

      /**
       * Sets the multi-selection mode status
       *
       * @param status the status of the multi-selection mode
       */
      public void setMultiSelectionEnabled(boolean status) {
         multiSelectionEnabled = status;
      }

      /**
       * Reset this image adapter to initial state
       */
      public void reset() {
         choiceMode.clearChoices();
         setMultiSelectionEnabled(false);
         notifyDataSetChanged();
      }

      /**
       * @param position the position where the change occurred
       * @param state    the new state of the change
       * @param uri      the uri of the image where the change occurred
       */
      public void onChecked(int position, boolean state, Uri uri) {
         boolean isFinished = false;

         ImageMultiChoiceMode imcm = (ImageMultiChoiceMode) choiceMode;
         imcm.setChecked(position, state);

         if (state) imcm.addImageUri(uri);
         else imcm.removeImageUri(uri);

         int choiceCount = imcm.getCheckedChoiceCount();

         if (actionMode == null && choiceCount == 1) {
            actionMode = startSupportActionMode(ImageGallery.this);
         } else if (choiceCount == 0 && actionMode != null) {
            actionMode.finish();
            isFinished = true;
         }

         if (!isFinished && actionMode != null)
            actionMode.setTitle(String.format(Locale.US, "%d %s", choiceCount, "selected"));
      }
   }
}
