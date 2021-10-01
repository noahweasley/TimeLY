package com.noah.timely.alarms;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.noah.timely.R;
import com.noah.timely.error.ErrorDialog;

public class EditTextDialog extends DialogFragment implements View.OnClickListener {
   private OnActionListener listener;
   private EditText alarm_label;

   public void prepareAndShow(FragmentActivity context) {
      FragmentManager mgr = ((FragmentActivity) context).getSupportFragmentManager();
      final String TAG = ErrorDialog.class.getName();
      show(mgr, TAG);
   }

   @NonNull
   @Override
   public Dialog onCreateDialog(Bundle savedInstanceState) {
      return new ETDialog(getContext());
   }

   @Override
   public void onClick(View v) {
      if (v.getId() == R.id.ok_button) {
         String label = alarm_label.getText().toString();
         if (listener != null && !TextUtils.isEmpty(label)) listener.onAction(label);
      }
      dismiss();
   }

   public EditTextDialog setActionListener(OnActionListener listener) {
      this.listener = listener;
      return this;
   }

   @FunctionalInterface
   public interface OnActionListener {
      void onAction(String value);
   }

   private class ETDialog extends Dialog {

      ETDialog(@NonNull Context context) {
         super(context, R.style.Dialog_Closeable);
      }

      @Override
      protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.dialog));
         setContentView(R.layout.layout_label_editor);
         alarm_label = findViewById(R.id.alarm_label);
         findViewById(R.id.ok_button).setOnClickListener(EditTextDialog.this);
         findViewById(R.id.cancel_button).setOnClickListener(EditTextDialog.this);
      }
   }
}