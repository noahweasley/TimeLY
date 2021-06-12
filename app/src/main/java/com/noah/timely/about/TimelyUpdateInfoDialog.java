package com.noah.timely.about;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.noah.timely.R;

public class TimelyUpdateInfoDialog extends DialogFragment {
    @SuppressWarnings("FieldCanBeLocal")
    public static final String TAG = "com.noah.timely.about.UpdateInfoDialog";

    public void show(Context context) {
        FragmentManager manager = ((FragmentActivity) context).getSupportFragmentManager();
        show(manager, TAG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new InfoDialog(getContext());
    }

    private static class InfoDialog extends Dialog {

        public InfoDialog(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_edges);
            setContentView(R.layout.dialog_update_info);
        }
    }
}
