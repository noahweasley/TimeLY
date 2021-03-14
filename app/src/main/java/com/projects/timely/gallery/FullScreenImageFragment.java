package com.projects.timely.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.projects.timely.R;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FullScreenImageFragment extends Fragment {
    public static final String ARG_IMAGE = "Fullscreen image";

    public static FullScreenImageFragment newInstance(Image image) {

        Bundle args = new Bundle();
        args.putSerializable(ARG_IMAGE, image);
        FullScreenImageFragment fragment = new FullScreenImageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fullscreen_images, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView img_fullScreenImage = view.findViewById(R.id.fullscreen_image);
        Image image = (Image) getArguments().getSerializable(ARG_IMAGE);

        Picasso.get().load(image.getImageUri()).fit().centerInside().into(img_fullScreenImage);
    }
}