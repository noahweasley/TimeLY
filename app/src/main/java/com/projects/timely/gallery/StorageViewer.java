package com.projects.timely.gallery;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.projects.timely.R;

@SuppressWarnings("ConstantConditions")
public class StorageViewer extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_view);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Select Storage");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewGroup v_internalStorage = findViewById(R.id.internal_storage);
        v_internalStorage.setOnClickListener(this);

        ViewGroup v_externalStorage = findViewById(R.id.external_storage);
        v_externalStorage.setOnClickListener(this);

        boolean ext_str_mounted
                = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        v_externalStorage.setVisibility(ext_str_mounted ? View.VISIBLE : View.GONE);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.internal_storage) {
            startActivity(new Intent(this, ImageDirectory.class)
                    .putExtra(ImageDirectory.STORAGE_ACCESS, ImageDirectory.INTERNAL));
        } else {
            startActivity(new Intent(this, ImageDirectory.class)
                    .putExtra(ImageDirectory.STORAGE_ACCESS, ImageDirectory.EXTERNAL));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
//        return super.onSupportNavigateUp();
    }
}

