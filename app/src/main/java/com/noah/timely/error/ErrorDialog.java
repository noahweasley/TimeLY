package com.noah.timely.error;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.noah.timely.R;

public class ErrorDialog extends DialogFragment implements View.OnClickListener {
    private ErrorMessage err;

    public void showErrorMessage(Context context, ErrorMessage errorMessage) {
        this.err = errorMessage;
        FragmentManager mgr = ((FragmentActivity) context).getSupportFragmentManager();
        final String TAG = ErrorDialog.class.getName();
        show(mgr, TAG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new Error1Dialog(getContext());
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }

    /**
     * Convenience Builder Class implementation to set the <code>ErrorMessage</code> to be used in the
     * <code>ErrorDialog</code>
     */
    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        private final ErrorMessage e = new ErrorMessage();

        public Builder setDialogMessage(String dialogMessage) {
            e.setDialogMessage(dialogMessage);
            return this;
        }

        public Builder setShowSuggestions(boolean showSuggestions) {
            e.setShowSuggestions(showSuggestions);
            return this;
        }

        public Builder setSuggestion1(String suggestion1) {
            e.setSuggestion1(suggestion1);
            return this;
        }

        public Builder setSuggestion2(String suggestion2) {
            e.setSuggestion2(suggestion2);
            return this;
        }

        public ErrorMessage build() {
            return e;
        }

        public Builder setSuggestionCount(int suggestionCount) {
            e.setSuggestionCount(suggestionCount);
            return this;
        }
    }

    // The dialog UI
    private class Error1Dialog extends Dialog {

        public Error1Dialog(@NonNull Context context) {
            super(context, R.style.Dialog);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_edges);
            setContentView(R.layout.dialog_operation_failed);

            TextView suggestion1 = findViewById(R.id.suggestion_1);
            TextView suggestion2 = findViewById(R.id.suggestion_2);
            TextView suggestionTitle = findViewById(R.id.suggestion_title);

            ((TextView) findViewById(R.id.message)).setText(err.getDialogMessage());
            ImageButton img_dismiss = findViewById(R.id.dismiss2);
            img_dismiss.setOnClickListener(ErrorDialog.this);

            if (err.getSuggestionCount() < 1 && err.isShowSuggestions())
                throw new IllegalArgumentException("Suggestion count must be >= 1 if suggestions" +
                                                           " are" + " enabled");
            if (err.getSuggestionCount() > 2)
                Log.w(getClass().getSimpleName(), "Ignoring suggestionCount of: "
                        + err.getSuggestionCount() + ", 2 suggestions will be shown");

            if (err.isShowSuggestions()) {
                suggestion1.setVisibility(View.VISIBLE);
                suggestionTitle.setVisibility(View.VISIBLE);
                suggestion1.setText(err.getSuggestion1());

                if (err.getSuggestionCount() == 1) suggestion2.setVisibility(View.GONE);
                else {
                    suggestion2.setVisibility(View.VISIBLE);
                    suggestion2.setText(err.getSuggestion2());
                }
            } else {
                suggestionTitle.setVisibility(View.GONE);
                suggestion1.setVisibility(View.GONE);
                suggestion2.setVisibility(View.GONE);
            }
        }
    }
}