package com.astrro.timely.assignment;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.astrro.timely.R;
import com.astrro.timely.core.ChoiceMode;
import com.astrro.timely.core.EmptyListEvent;
import com.astrro.timely.core.RequestRunner;
import com.astrro.timely.core.SchoolDatabase;
import com.astrro.timely.gallery.Image;
import com.astrro.timely.gallery.ImageDirectory;
import com.astrro.timely.gallery.ImageGallery;
import com.astrro.timely.gallery.ImageListRowHolder;
import com.astrro.timely.gallery.ImageMultiChoiceMode;
import com.astrro.timely.util.ThreadUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ImageViewerActivity extends AppCompatActivity implements ActionMode.Callback {
   public static final String ARG_POSITION = "com.astrro.timely.viewImagesActivity.position";
   public static final String DELETE_REQUEST = "Delete Image";
   public static final String MULTIPLE_DELETE_REQUEST = "Delete Multiple Images";
   public static final String ARG_TITLE = "com.astrro.timely.viewImagesActivity.title";
   public static final String ARG_URI_LIST = "com.astrro.timely.viewImagesActivity.imageUris";
   public static final String ADD_NEW = "com.astrro.timely.viewImagesActivity.add_new";
   private final ChoiceMode choiceMode = ChoiceMode.IMAGE_MULTI_SELECT;
   private ImageAdapter imageAdapter;
   private ProgressBar indeterminateProgress, indeterminateProgress2;
   private RecyclerView rv_imageList;
   private ViewGroup no_media;
   private CoordinatorLayout coordinator;
   private ActionMode actionMode;
   private List<Uri> mediaUris = new ArrayList<>();
   private List<Image> imageList = new ArrayList<>();
   private int position;
   private SchoolDatabase database;

   public static void start(Context context, int position, String title) {
      Intent starter = new Intent(context, ImageViewerActivity.class);
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
   public void doListUpdate(MultiUpdateMessage2 mUpdate) {
      if (mUpdate.getType() == MultiUpdateMessage2.EventType.REMOVE
              || mUpdate.getType() == MultiUpdateMessage2.EventType.INSERT) {
         if (actionMode != null)
            actionMode.finish(); // require onDestroyActionMode() callback
      }
   }

   @Override
   protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      imageAdapter.getChoiceMode().onRestoreInstanceState(savedInstanceState);
   }

   @Override
   @SuppressWarnings("unchecked")
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      database = new SchoolDatabase(this);
      EventBus.getDefault().register(this);
      setContentView(R.layout.image_gallery2);
      setSupportActionBar(findViewById(R.id.toolbar));
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      position = getIntent().getIntExtra(ARG_POSITION, -1);
      getSupportActionBar().setTitle(String.format(Locale.US, "Assignment #%d Images", position + 1));

      indeterminateProgress = findViewById(R.id.indeterminateProgress);
      indeterminateProgress2 = findViewById(R.id.content_loading);

      coordinator = findViewById(R.id.coordinator);
      rv_imageList = findViewById(R.id.imageList);
      no_media = findViewById(R.id.no_media);

      rv_imageList.setHasFixedSize(true);
      rv_imageList.setLayoutManager(new LinearLayoutManager(this));
      rv_imageList.setAdapter(imageAdapter = new ImageAdapter(choiceMode));

      findViewById(R.id.add_new).setOnClickListener(
              v -> startActivity(new Intent(this, ImageDirectory.class)
                                         .putExtra(ImageDirectory.STORAGE_ACCESS_ROOT, ImageDirectory.EXTERNAL)
                                         .setAction(ImageGallery.ACTION_MULTI_SELECT)));

      setupSwipeHelper(rv_imageList);

      ThreadUtils.runBackgroundTask(() -> {
         List<?>[] genericList = database.getAttachedImagesAsUriList(position);
         mediaUris = (List<Uri>) genericList[0];
         imageList = (List<Image>) genericList[1];
         runOnUiThread(() -> {
            imageAdapter.notifyDataSetChanged();
            doViewUpdate(null);
            // invalidate options menu if list is empty
            if (mediaUris.isEmpty()) invalidateOptionsMenu();
         });
      });
   }

   private void setupSwipeHelper(RecyclerView recyclerView) {
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
            RequestRunner runner = RequestRunner.createInstance();
            RequestRunner.Builder builder = new RequestRunner.Builder();
            builder.setOwnerContext(ImageViewerActivity.this)
                   .setMediaUris(mediaUris)
                   .setImageList(imageList)
                   .setAssignmentPosition(position)
                   .setAdapterPosition(viewHolder.getAbsoluteAdapterPosition());

            runner.setRequestParams(builder.getParams())
                  .runRequest(DELETE_REQUEST);

            Snackbar snackbar = Snackbar.make(coordinator, "Image Deleted", Snackbar.LENGTH_LONG);
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.setAction("UNDO", v -> runner.undoRequest());
            snackbar.show();
         }
      });

      swipeHelper.attachToRecyclerView(recyclerView);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.list_menu_image, menu);
      menu.findItem(R.id.select_all).setEnabled(mediaUris.isEmpty() ? false : true);

      return super.onCreateOptionsMenu(menu);
   }

   @Override
   public boolean onPrepareOptionsMenu(@NonNull Menu menu) {
      menu.findItem(R.id.select_all).setEnabled(mediaUris.isEmpty() ? false : true);
      return super.onPrepareOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(@NonNull MenuItem item) {
      if (item.getItemId() == R.id.select_all) {
         imageAdapter.selectAllItems();
      }
      return super.onOptionsItemSelected(item);
   }

   @Override
   protected void onNewIntent(Intent intent) {
      super.onNewIntent(intent);

      // FIXME: 2/23/2021 Find the reason for the checked images, then remove next line.
      imageAdapter.getChoiceMode().clearChoices();

      indeterminateProgress2.setVisibility(View.VISIBLE);
      String[] uriList = intent.getStringArrayExtra(ARG_URI_LIST);

      ThreadUtils.runBackgroundTask(() -> {
         /* use the intent that started this activity */
         boolean isUpdated = database.updateUris(getIntent().getIntExtra(ARG_POSITION, -1), uriList);

         if (isUpdated) {
            for (String uri : uriList) {
               mediaUris.add(Uri.parse(uri));
               imageList.add(Image.createImageFromUri(Uri.parse(uri)));
            }

            runOnUiThread(() -> {
               imageAdapter.notifyDataSetChanged();
               doViewUpdate(null);
               indeterminateProgress2.setVisibility(View.GONE);
            });

         } else Toast.makeText(this, "An Error Occurred", Toast.LENGTH_SHORT).show();

      });
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
      database.close();
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
   public void onBackPressed() {
      super.onBackPressed();
      overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
   }

   @Subscribe(threadMode = ThreadMode.MAIN)
   public void doImageUpdate(UUpdateMessage update) {
      int changePos = update.getPosition();
      switch (update.getType()) {
         case INSERT:
            imageAdapter.notifyItemInserted(changePos);
            imageAdapter.notifyDataSetChanged();
            break;
         case REMOVE:
            imageAdapter.notifyItemRemoved(changePos);
            imageAdapter.notifyDataSetChanged();
            break;
      }
      // hide or reveal select-all menu itemn
      invalidateOptionsMenu();
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
      if (item.getItemId() == R.id.delete_multiple_action) {
         imageAdapter.deleteMultiple();
      } else {
         imageAdapter.selectAllItems();
      }

      return true;
   }

   @Override
   public void onDestroyActionMode(ActionMode mode) {
      actionMode = null;
      imageAdapter.reset();
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
         holder.with(mediaUris, imageList, imageAdapter).bindView();
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
       * Reset this image adapter to initial state
       */
      public void reset() {
         choiceMode.clearChoices();
         setMultiSelectionEnabled(false);
         notifyDataSetChanged();
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
      public void onChecked(int position, boolean state, Uri uri) {
         boolean isFinished = false;

         ImageMultiChoiceMode imcm = (ImageMultiChoiceMode) choiceMode;
         imcm.setChecked(position, state);

         if (state) imcm.addImageUri(uri);
         else imcm.removeImageUri(uri);

         int choiceCount = imcm.getCheckedChoiceCount();

         if (actionMode == null && choiceCount == 1) {
            actionMode = startSupportActionMode(ImageViewerActivity.this);
         } else if (actionMode != null && choiceCount == 0) {
            actionMode.finish();
            isFinished = true;
            choiceMode.clearChoices(); // added this, might be solution to my problem
         }

         if (!isFinished && actionMode != null)
            actionMode.setTitle(String.format(Locale.US, "%d %s", choiceCount, "selected"));
      }

      /**
       * Selects all items on the list
       */
      public void selectAllItems() {
         ImageMultiChoiceMode imcm = (ImageMultiChoiceMode) choiceMode;
         imcm.selectAll(mediaUris.size());
         imcm.addAllImageUri(mediaUris);
         notifyDataSetChanged();
         setMultiSelectionEnabled(true);
         // also start action mode
         if (actionMode == null) {
            // select all action peformed, create ation mode, because it wasn't already created
            actionMode = startSupportActionMode(ImageViewerActivity.this);
            actionMode.setTitle(String.format(Locale.US, "%d %s", choiceMode.getCheckedChoiceCount(), "selected"));
         } else if (actionMode != null) {
            // select all action performed, but action mode is activated, only set title to length of list
            actionMode.setTitle(String.format(Locale.US, "%d %s", choiceMode.getCheckedChoiceCount(), "selected"));
         }
      }

      /**
       * Deletes multiple images from the list of selected items
       */
      public void deleteMultiple() {
         RequestRunner runner = RequestRunner.createInstance();
         RequestRunner.Builder builder = new RequestRunner.Builder();
         builder.setOwnerContext(ImageViewerActivity.this)
                .setAdapterPosition(rowHolder.getAbsoluteAdapterPosition())
                .setAssignmentPosition(position)
                .setMediaUris(mediaUris)
                .setImageList(imageList)
                .setItemIndices(getCheckedImagesIndices());

         runner.setRequestParams(builder.getParams())
               .runRequest(MULTIPLE_DELETE_REQUEST);

         final int count = getCheckedImageCount();
         Snackbar snackbar = Snackbar.make(coordinator,
                                           count + " Image" + (count > 1 ? "s" : "") + " Deleted",
                                           Snackbar.LENGTH_LONG);

         snackbar.setActionTextColor(Color.YELLOW);
         snackbar.setAction("UNDO", v -> runner.undoRequest());
         snackbar.show();
      }
   }
}