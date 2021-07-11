package com.noah.timely.about;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.noah.timely.BuildConfig;
import com.noah.timely.R;

public class TimelyUpdateInfoDialog extends DialogFragment implements View.OnClickListener {
    @SuppressWarnings("FieldCanBeLocal")
    public static final String TAG = "com.noah.timely.about.UpdateInfoDialog";

    public void show(Context context) {
        FragmentManager manager = ((FragmentActivity) context).getSupportFragmentManager();
        show(manager, TAG);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.close)
            dismiss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new InfoDialog(getContext());
    }

    private class InfoDialog extends Dialog {

        public InfoDialog(@NonNull Context context) {
            super(context, R.style.Dialog_Closeable);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_edges_8);
            setContentView(R.layout.dialog_update_info);

            ImageButton btn_close = findViewById(R.id.close);
            btn_close.setOnClickListener(TimelyUpdateInfoDialog.this);

            TextView tv_version = findViewById(R.id.version);

            String version = BuildConfig.VERSION_NAME;
            String packageName = "com.noah.timely";

            try {
                PackageInfo packageInfo = getContext().getPackageManager().getPackageInfo(packageName, 0);
                version = packageInfo.versionName;
            } catch (PackageManager.NameNotFoundException ignored) {}

            tv_version.setText(String.format("V%s", version));
        }
    }
}