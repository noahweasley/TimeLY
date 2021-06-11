package com.projects.timely.gallery;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.provider.MediaStore;
import android.util.Log;
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

import com.projects.timely.R;
import com.projects.timely.assignment.AddAssignmentActivity;
import com.projects.timely.assignment.ViewImagesActivity;
import com.projects.timely.core.ChoiceMode;

import java.util.ArrayList;
import java.util.List;

import static com.projects.timely.assignment.ViewImagesActivity.ARG_URI_LIST;
import static com.projects.timely.gallery.ImageDirectory.EXTERNAL;
import static com.projects.timely.gallery.ImageDirectory.STORAGE_ACCESS_ROOT;

@SuppressWarnings("ConstantConditions")
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
        startActivity(new Intent(this, ImageDirectory.class)
                .putExtra(STORAGE_ACCESS_ROOT, getIntent().getStringExtra(STORAGE_ACCESS_ROOT)));
        finish();
    }

    // will be replaced with a cursor loader
    @Override
    public void run() {
        String root_extra = getIntent().getStringExtra(STORAGE_ACCESS_ROOT);
        Uri storageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        if (root_extra != null) {
            storageUri = root_extra.equals(EXTERNAL) ? MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    : MediaStore.Images.Media.INTERNAL_CONTENT_URI;
        }

        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DISPLAY_NAME};
        String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ?";
        String[] selectionArgs = {folder};

        Cursor imgCursor = getApplicationContext()
                .getContentResolver()
                .query(storageUri, projection, selection, selectionArgs, null);

        int idColumn = imgCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        int sizeColumn = imgCursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
        int displayNameColumn
                = imgCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
        int bucketDisplayNameColumn
                = imgCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

        while (imgCursor.moveToNext()) {
            long id = imgCursor.getLong(idColumn);
            int size = imgCursor.getInt(sizeColumn);
            String fileName = imgCursor.getString(displayNameColumn);
            String folderName = imgCursor.getString(bucketDisplayNameColumn);

            Uri contentUri = ContentUris.withAppendedId(storageUri, id);
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
    protected void onDestroy() {
        super.onDestroy();
        imageAdapter.getChoiceMode().clearChoices();
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        Log.d(getClass().getSimpleName(), "Action: " + getIntent().getAction());

        if (getIntent().getAction().equals(ViewImagesActivity.ADD_NEW)) {
            ImageMultiChoiceMode imageMultiChoiceMode
                    = (ImageMultiChoiceMode) imageAdapter.getChoiceMode();

            // FIXME: 2/23/2021 Find the cause of the additional images
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
        imageAdapter.reset();
    }

    class ImageAdapter extends RecyclerView.Adapter<ImageGalleryRowHolder> {
        private final ChoiceMode choiceMode;
        private boolean multiSelectionEnabled;

        public ImageAdapter(ChoiceMode choiceMode) {
            this.choiceMode = choiceMode;
        }

        @NonNull
        @Override
        public ImageGalleryRowHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int pos) {
            View view =
                    getLayoutInflater()
                            .inflate(R.layout.layout_image_gallery_row, viewGroup, false);
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
         * Reset this image adapter to initial state
         */
        public void reset() {
            choiceMode.clearChoices();
            setMultiSelectionEnabled(false);
            notifyDataSetChanged();
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
            } else if (choiceCount == 0 && actionMode != null) {
                actionMode.finish();
                isFinished = true;
            }

            if (!isFinished && actionMode != null)
                actionMode.setTitle(String.format("%d %s", choiceCount, "selected"));
        }
    }
}
