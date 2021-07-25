package com.noah.timely.gallery;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.noah.timely.R;
import com.noah.timely.util.ThreadUtils;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

public class ImageDirectory extends AppCompatActivity implements Runnable {
    public static final int requestCode = 112;
    public static final String STORAGE_ACCESS_ROOT = "Storage access";
    public static final String EXTERNAL = "External Storage";
    public static final String INTERNAL = "Internal Storage";
    private final List<List<Image>> imageDirectoryList = new ArrayList<>();
    private final ImageAdapter imageAdapter = new ImageAdapter();
    private ProgressBar indeterminateProgress;
    private RecyclerView imageList;
    private ViewGroup v_noMedia;
    private String accessedStorage;

    private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (SDK_INT >= Build.VERSION_CODES.R) {
                        if (Environment.isExternalStorageManager()) {
                            // perform action when allow permission success
                            new Thread(this).start();
                        } else {
                            Toast.makeText(this, "Allow permission for storage access", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.image_gallery);
        Toolbar toolbar = findViewById(R.id.toolbar);
        imageList = findViewById(R.id.imageList);
        indeterminateProgress = findViewById(R.id.indeterminateProgress);
        v_noMedia = findViewById(R.id.no_media);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Select Images");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageList.setHasFixedSize(true);
        imageList.setAdapter(imageAdapter);
        imageList.setLayoutManager(new GridLayoutManager(this, 2));
        imageList.setClickable(true);

        if (checkPermission()) {
            ThreadUtils.runBackgroundTask(this);
        } else {
            requestPermission();
        }
    }

    private void requestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getPackageName())));
                resultLauncher.launch(intent);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                resultLauncher.launch(intent);
            }
        } else {
            // below android 11
            ActivityCompat.requestPermissions(this,
                                              new String[]{READ_EXTERNAL_STORAGE},
                                              requestCode);
        }
    }

    private boolean checkPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int result = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int _requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(_requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            new Thread(this).start();
        } else {
            Toast.makeText(this, "Allow permission for storage access", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    @SuppressLint("InlinedApi")
    public void run() {
        Uri storageUri;
        if (SDK_INT >= Build.VERSION_CODES.Q) {
            storageUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            storageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DISPLAY_NAME};

        Cursor imgCursor = getApplicationContext().getContentResolver().query(storageUri, projection, null, null, null);

        int bucketId = imgCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        int imgSize = imgCursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
        int name = imgCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
        int bucketName = imgCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

        List<String> directoryDictionary = new ArrayList<>();
        List<Image> generalList = new ArrayList<>();
        while (imgCursor.moveToNext()) {
            long id = imgCursor.getLong(bucketId);
            int size = imgCursor.getInt(imgSize);
            String fileName = imgCursor.getString(name);
            String folderName = imgCursor.getString(bucketName);

            Uri contentUri = ContentUris.withAppendedId(storageUri, id);
            Image currentImage = new Image(contentUri, size, fileName, folderName);
            // add all images to the general image list, but modifying the directory name
            Image genImage = new Image(contentUri, size, fileName, "All Media");
            generalList.add(genImage);

            int directoryIndex = linearSearch(directoryDictionary, folderName);
            // if search result (directoryIndex) passes this test, then it means that there is
            // no such directory in list of directory names
            if (directoryIndex < 0) {
                imageDirectoryList.add(new ArrayList<>());
                directoryDictionary.add(folderName);
                directoryIndex = linearSearch(directoryDictionary, folderName);
                if (directoryIndex >= 0)
                    imageDirectoryList.get(directoryIndex).add(currentImage);
            } else {
                imageDirectoryList.get(directoryIndex).add(currentImage);
            }
        }

        //...then add it if the image list of folder is > 2
        if (imageDirectoryList.size() > 2) imageDirectoryList.add(0, generalList);


        imgCursor.close();
        runOnUiThread(() -> {
            imageAdapter.notifyDataSetChanged();
            doViewUpdate();
        });
    }

    private void doViewUpdate() {
        indeterminateProgress.setVisibility(View.GONE);
        imageList.setVisibility(imageDirectoryList.isEmpty() ? View.GONE : View.VISIBLE);
        v_noMedia.setVisibility(imageDirectoryList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /*
     Collection.binarySearch() (which uses the binary search algorithm) would have been a
     better choice because of it's faster search time, but because I would need to sort the
     list to use Collection.binarySearch(), and sorting is not favoured in this program,
     I just had to use the linear search algorithm.
     */
    private int linearSearch(List<String> list, String dir) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(dir)) return i; // dir found, return immediately
        }
        return -1; // dir was not found
    }

    // Image List Adapter
    private class ImageAdapter extends RecyclerView.Adapter<ImageDirectoryRowHolder> {

        @NonNull
        @Override
        public ImageDirectoryRowHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int pos) {
            View view = getLayoutInflater().inflate(R.layout.layout_image_directory_row, viewGroup, false);
            return new ImageDirectoryRowHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageDirectoryRowHolder viewHolder, int pos) {
            viewHolder.with(ImageDirectory.this, imageDirectoryList, pos, accessedStorage)
                      .setRequestAction(getIntent().getAction())
                      .loadThumbnail();
        }

        @Override
        public int getItemCount() {
            return imageDirectoryList.size();
        }
    }
}