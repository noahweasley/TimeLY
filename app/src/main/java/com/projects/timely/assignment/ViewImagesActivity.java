package com.projects.timely.assignment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.projects.timely.R;
import com.projects.timely.core.EmptyListEvent;
import com.projects.timely.core.RequestRunner;
import com.projects.timely.core.SchoolDatabase;
import com.projects.timely.core.ThreadUtils;
import com.projects.timely.gallery.ChoiceMode;
import com.projects.timely.gallery.ImageDirectory;
import com.projects.timely.gallery.ImageListRowHolder;
import com.projects.timely.gallery.ImageMultiChoiceMode;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ViewImagesActivity extends AppCompatActivity implements ActionMode.Callback {
    public static final String ARG_POSITION = "com.projects.timely.viewImagesActivity.position";
    public static final String DELETE_REQUEST = "Delete Image";
    public static final String MULTIPLE_DELETE_REQUEST = "Delete Multiple Images";
    public static final String ARG_TITLE = "com.projects.timely.viewImagesActivity.title";
    public static final String ARG_URI_LIST = "com.projects.timely.viewImagesActivity.imageUris";
    public static final String ADD_NEW = "com.projects.timely.viewImagesActivity.add_new";
    private final ChoiceMode choiceMode = ChoiceMode.IMAGE_MULTI_SELECT;
    private ImageAdapter imageAdapter;
    private ProgressBar indeterminateProgress, indeterminateProgress2;
    private RecyclerView rv_imageList;
    private ViewGroup no_media;
    private CoordinatorLayout coordinator;
    private ActionMode actionMode;
    private List<Uri> mediaUris = new ArrayList<>();
    private int position;

    public static void start(Context context, int position, String title) {
        Intent starter = new Intent(context, ViewImagesActivity.class);
        starter.putExtra(ARG_POSITION, position);
        starter.putExtra(ARG_TITLE, title);
        context.startActivity(starter);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doViewUpdate(EmptyListEvent event) {
        indeterminateProgress.setVisibility(View.GONE);
        no_media.setVisibility(mediaUris.isEmpty() ? View.VISIBLE : View.GONE);
        rv_imageList.setVisibility(mediaUris.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doListUpdate(MultiUpdateMessage mUpdate) {
        if (mUpdate.getType() == MultiUpdateMessage.EventType.REMOVE
                || mUpdate.getType() == MultiUpdateMessage.EventType.INSERT) {
            actionMode.finish(); // require onDestroyActionMode() callback
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        imageAdapter.getChoiceMode().onRestoreInstanceState(savedInstanceState);
    }

    @Override
    @SuppressLint("DefaultLocale")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.image_gallery2);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        position = getIntent().getIntExtra(ARG_POSITION, -1);
        getSupportActionBar().setTitle(String.format("Assignment #%d Images", position + 1));

        indeterminateProgress = findViewById(R.id.indeterminateProgress);
        indeterminateProgress2 = findViewById(R.id.content_loading);

        coordinator = findViewById(R.id.coordinator);
        rv_imageList = findViewById(R.id.imageList);
        no_media = findViewById(R.id.no_media);
        rv_imageList.setHasFixedSize(true);
        rv_imageList.setLayoutManager(new LinearLayoutManager(this));
        rv_imageList.setAdapter(imageAdapter = new ImageAdapter(choiceMode));

        findViewById(R.id.add_new).setOnClickListener(
                v -> startActivity(new Intent(this, ImageDirectory.class).setAction(ADD_NEW)));

        ItemTouchHelper swipeHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                        @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                RequestRunner runner = RequestRunner.getInstance();
                runner.with(ViewImagesActivity.this, viewHolder, imageAdapter, null)
                        .setUriList(mediaUris)
                        .setAssignmentPosition(position)
                        .runRequest(DELETE_REQUEST);

                Snackbar snackbar
                        = Snackbar.make(coordinator, "Image Deleted", Snackbar.LENGTH_LONG);
                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.setAction("UNDO", v -> runner.undoRequest());
                snackbar.show();
            }
        });

        swipeHelper.attachToRecyclerView(rv_imageList);

        ThreadUtils.runBackgroundTask(() -> {
            SchoolDatabase database = new SchoolDatabase(this);
            mediaUris = database.getAttachedImagesAsUriList(position);

            runOnUiThread(() -> {
                imageAdapter.notifyDataSetChanged();
                doViewUpdate(null);
            });
            database.close();
        });
    }

    @Override

    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        choiceMode.onSaveInstanceState(outState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        indeterminateProgress2.setVisibility(View.VISIBLE);
        String[] uriList = intent.getStringArrayExtra(ARG_URI_LIST);

        SchoolDatabase database = new SchoolDatabase(this);
        ThreadUtils.runBackgroundTask(() -> {
            boolean isUpdated /* use the intent that started this activity */
                    = database.updateUris(getIntent().getIntExtra(ARG_POSITION, -1), uriList);

            if (isUpdated) {
                for (String uri : uriList) mediaUris.add(Uri.parse(uri));
                runOnUiThread(() -> {
                    imageAdapter.notifyDataSetChanged();
                    doViewUpdate(null);
                });
            } else Toast.makeText(this, "An Error Occurred", Toast.LENGTH_SHORT).show();

            runOnUiThread(() -> indeterminateProgress2.setVisibility(View.GONE));
            database.close();
        });
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        getMenuInflater().inflate(R.menu.deleted_items, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        imageAdapter.deleteMultiple();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;
        imageAdapter.getChoiceMode().clearChoices();
        imageAdapter.notifyDataSetChanged();
    }

    public class ImageAdapter extends RecyclerView.Adapter<ImageListRowHolder> {
        private final ChoiceMode choiceMode;
        private ImageListRowHolder rowHolder;
        private boolean multiSelectionEnabled;

        public ImageAdapter(ChoiceMode choiceMode) {
            this.choiceMode = choiceMode;
        }

        @NonNull
        @Override
        public ImageListRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View rootView = getLayoutInflater().inflate(R.layout.file_list_row, parent, false);
            return (rowHolder = new ImageListRowHolder(rootView));
        }

        @Override
        public void onBindViewHolder(@NonNull ImageListRowHolder holder, int position) {
            holder.with(mediaUris, imageAdapter).bindView();
        }

        @Override
        public int getItemCount() {
            return mediaUris.size();
        }

        /**
         * @return the choice-mode that was set
         */
        public ChoiceMode getChoiceMode() {
            return choiceMode;
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
            this.multiSelectionEnabled = status;
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
         * @return an array of the checked indices
         */
        private Integer[] getCheckedImagesIndices() {
            return choiceMode.getCheckedChoicesIndices();
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
                actionMode = startSupportActionMode(ViewImagesActivity.this);
            } else if (choiceCount == 1) {
                actionMode.finish();
                isFinished = true;
            }

            if (!isFinished) actionMode.setTitle(String.format("%d %s", choiceCount, "selected"));
        }

        /**
         * Deletes multiple images from the list of selected items
         */
        public void deleteMultiple() {
            RequestRunner runner = RequestRunner.getInstance();
            runner.with(ViewImagesActivity.this, rowHolder, imageAdapter, null)
                    .setUriList(mediaUris)
                    .setAssignmentPosition(position)
                    .setItemsIndices(getCheckedImagesIndices())
                    .runRequest(MULTIPLE_DELETE_REQUEST);

            final int count = getCheckedImageCount();
            Snackbar snackbar
                    = Snackbar.make(coordinator,
                                    count + " Image" + (count > 1 ? "s" : "") + " Deleted",
                                    Snackbar.LENGTH_LONG);

            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.setAction("UNDO", v -> runner.undoRequest());
            snackbar.show();
        }
    }
}