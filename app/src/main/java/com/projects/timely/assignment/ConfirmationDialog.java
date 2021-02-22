package com.projects.timely.assignment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.projects.timely.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ConfirmationDialog extends DialogFragment {

    public void show(Context context) {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new CDialog(getContext());
    }

    private static class CDialog extends Dialog {
        public CDialog(Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.dialog_confirmation);

        }
    }

    public static class MessageBuilder {

    }
}