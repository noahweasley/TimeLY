package com.noah.timely.gallery;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.noah.timely.R;

import java.util.List;

public class ImageSlideActivity extends AppCompatActivity {
    public static final String ARG_INITIAL_POSITION = "Initial Uri from list";
    private static List<? extends Image> images;

    /**
     * Static utility method to simplify starting the ImageSlideActivity with the arguments
     * needed for adequate response
     *
     * @param context      the originator of this event
     * @param fromPosition the position at which the slide begins image display
     * @param images1      the list of images to display
     */
    public static void start(Context context, int fromPosition, List<? extends Image> images1) {
        images = images1;
        Intent starter = new Intent(context, ImageSlideActivity.class);
        starter.putExtra(ARG_INITIAL_POSITION, fromPosition);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_silder);
        ViewPager2 pager = findViewById(R.id.pager);
        pager.setAdapter(new ImageAdapter(this));

        // Converts 8 dip into its equivalent px
        float mPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8.0f,
                getResources().getDisplayMetrics());

        pager.setPageTransformer(new MarginPageTransformer((int) mPx));
        pager.setOffscreenPageLimit(images.size());
        pager.setCurrentItem(getIntent().getIntExtra(ARG_INITIAL_POSITION, 0), false);
    }

    private static class ImageAdapter extends FragmentStateAdapter {

        public ImageAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return FullScreenImageFragment.newInstance(images.get(position));
        }

        @Override
        public int getItemCount() {
            return images.size();
        }
    }
}
