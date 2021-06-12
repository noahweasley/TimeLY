package com.noah.timely.assignment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.noah.timely.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

@SuppressWarnings("ConstantConditions")
public class AssignmentViewDialog extends DialogFragment {
    static final String ARG_DATA = "Assignment";

    public void show(FragmentActivity context, AssignmentModel assignment) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_DATA, assignment);
        setArguments(bundle);
        show(context.getSupportFragmentManager(), AssignmentViewDialog.class.getName());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AVDialog(getContext());
    }

    private class AVDialog extends Dialog {

        public AVDialog(Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_edges);
            setContentView(R.layout.dialog_view_assignment);
            AssignmentModel assignment = (AssignmentModel) getArguments().getSerializable(ARG_DATA);
            TextView tv_title = findViewById(R.id.title);
            TextView tv_lecturer = findViewById(R.id.lecturer);
            TextView tv_description = findViewById(R.id.description);
            Button btn_viewImages = findViewById(R.id.view_images);

            String title = assignment.getTitle();
            int position = assignment.getPosition();

            tv_title.setText(title);
            tv_lecturer.setText(assignment.getLecturerName());
            tv_description.setText(assignment.getDescription());

            // Start image viewer
            btn_viewImages.setOnClickListener(
                    v -> ViewImagesActivity.start(getContext(), position, title));
        }
    }
}