package com.projects.timely.gallery;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.projects.timely.R;
import com.projects.timely.assignment.AddAssignmentActivity;
import com.projects.timely.assignment.ViewImagesActivity;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.projects.timely.assignment.ViewImagesActivity.ARG_URI_LIST;

@SuppressLint("InlinedApi")
public class ImageGallery extends AppCompatActivity implements Runnable, ActionMode.Callback {
    public static final String ARG_FILES_COUNT = "Attached files count";
    private final List<Image> images = new ArrayList<>();
    private ImageAdapter imageAdapter;
    private String folder;
    private ProgressBar indeterminateProgress;
    private RecyclerView imageList;
    private ActionMode actionMode;
    private final ChoiceMode choiceMode = ChoiceMode.IMAGE_MULTI_SELECT;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        folder = getIntent().getStringExtra("folder");
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
        new Thread(this).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        imageAdapter.getChoiceMode().onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        choiceMode.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, ImageDirectory.class));
        finish();
    }

    // will be replaced with a cursor loader
    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.MIME_TYPE};
        String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ?";
        String[] selectionArgs = {folder};

        Cursor imgCursor = getApplicationContext()
                .getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                       projection,
                       selection,
                       selectionArgs,
                       null);

        int idColumn = imgCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        int sizeColumn = imgCursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
        int displayNameColumn
                = imgCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
        int bucketDisplayNameColumn
                = imgCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        int mimeTypeColumn = imgCursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE);

        while (imgCursor.moveToNext()) {
            long id = imgCursor.getLong(idColumn);
            int size = imgCursor.getInt(sizeColumn);
            String fileName = imgCursor.getString(displayNameColumn);
            String folderName = imgCursor.getString(bucketDisplayNameColumn);
            String mimeType = imgCursor.getString(mimeTypeColumn);

            Uri contentUri
                    = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            images.add(new Image(contentUri, size, fileName, folderName, mimeType));
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
        if (getIntent().getAction().equals(ViewImagesActivity.ADD_NEW)) {
            ImageMultiChoiceMode imageMultiChoiceMode
                    = (ImageMultiChoiceMode) imageAdapter.getChoiceMode();
            startActivity(new Intent(this, ViewImagesActivity.class)
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
        imageAdapter.getChoiceMode().clearChoices();
        imageAdapter.notifyDataSetChanged();
    }

    // Image Adapter
    class ImageAdapter extends RecyclerView.Adapter<ImageGalleryRowHolder> {
        private final ChoiceMode choiceMode;
        private boolean multiSelectionEnabled;

        public ImageAdapter(ChoiceMode choiceMode) {
            this.choiceMode = choiceMode;
        }

        @NonNull
        @Override
        public ImageGalleryRowHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int pos) {
            View view = getLayoutInflater().inflate(R.layout.layout_image_gallery_row, viewGroup,
                                                    false);
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
         * @return the choice-mode that was set
         */
        public ChoiceMode getChoiceMode() {
            return choiceMode;
        }

        /**
         * @param adapterPosition the position of the view holder
         * @return the checked status of a particular image int he list
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
         * @param position the position where the change occurred
         * @param state    the new state of the change
         * @param uri      the uri of the image where the change occurred
         */
        @SuppressLint("DefaultLocale")
        public void onChecked(int position, boolean state, Uri uri) {
            boolean isFinished = false;

            ImageMultiChoiceMode imcm = (ImageMultiChoiceMode) choiceMode;
            imcm.setChecked(position, state);

            if (state) imcm.addImageUri(uri);
            else imcm.removeImageUri(uri);

            int choiceCount = imcm.getCheckedChoiceCount();

            if (actionMode == null && choiceCount == 1) {
                actionMode = startSupportActionMode(ImageGallery.this);
            } else if (choiceCount < 1) {
                actionMode.finish();
                isFinished = true;
            }

            if (!isFinished) actionMode.setTitle(String.format("%d %s", choiceCount, "selected"));
        }
    }
}