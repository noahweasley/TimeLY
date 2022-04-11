package com.noah.timely.calculator;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.noah.timely.R;

import org.intellij.lang.annotations.MagicConstant;

public class ResultCalculatorInfoDialog extends DialogFragment {
   private static final String ARG_SEMESTER = "Semester";
   private OnActionReceivedListener listener;
   public static final int ACTION_DONT_SHOW = 0;
   public static final int ACTION_CANCELLED = 1;
   public static final int ACTION_PROCEED = 2;

   @NonNull
   @Override
   public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
      return new RCIDialog(getContext());
   }

   public ResultCalculatorInfoDialog show(Context context, String semester) {
      // to prevent the dialog from showing twice, when both first and second semester courses
      // haven't been registered yet.
      if (!isAdded()) {
         Bundle bundle = new Bundle();
         bundle.putString(ARG_SEMESTER, semester);
         setArguments(bundle);
         FragmentActivity activity = (AppCompatActivity) context;
         FragmentManager mgr = activity.getSupportFragmentManager();
         show(mgr, ResultCalculatorInfoDialog.class.getSimpleName());
      }
      return this;
   }

   public void setOnActionReceviedListener(OnActionReceivedListener listener) {
      this.listener = listener;
   }

   private class RCIDialog extends Dialog implements View.OnClickListener {

      public RCIDialog(@NonNull Context context) {
         super(context, R.style.Dialog_Closeable);
      }

      @Override
      protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         getWindow().requestFeature(Window.FEATURE_NO_TITLE);
         getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_edges);
         setContentView(R.layout.dialog_result_calculator);
         ImageButton btn_close = findViewById(R.id.close);
         Button btn_proceed = findViewById(R.id.proceed), btn_remove = findViewById(R.id.remove);

         btn_close.setOnClickListener(this);
         btn_remove.setOnClickListener(this);
         btn_proceed.setOnClickListener(this);

      }

      @Override
      @SuppressWarnings("all")
      public void onClick(View view) {
         switch (view.getId()) {
            case R.id.close: {
               if (listener != null) listener.onAction(ACTION_CANCELLED);
               ResultCalculatorInfoDialog.this.dismiss();
            }
            break;
            case R.id.proceed: {
               if (listener != null) listener.onAction(ACTION_PROCEED);
            }
            break;
            case R.id.remove: {
               if (listener != null) listener.onAction(ACTION_DONT_SHOW);
            }
            break;
         }

      }

   }

   @FunctionalInterface
   public interface OnActionReceivedListener {
      void onAction(@MagicConstant(intValues = { ACTION_CANCELLED, ACTION_PROCEED, ACTION_DONT_SHOW }) int action);
   }

}
