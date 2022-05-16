package com.astrro.timely.todo;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.google.gson.Gson;
import com.astrro.timely.R;

public class TodoViewDialog extends DialogFragment {
   static final String ARG_DATA = "todo";

   public void show(FragmentActivity context, TodoModel todo) {
      Bundle bundle = new Bundle();
      Gson gson = new Gson();
      bundle.putString(ARG_DATA, gson.toJson(todo));
      setArguments(bundle);
      show(context.getSupportFragmentManager(), TodoViewDialog.class.getName());
   }

   @NonNull
   @Override
   public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
      return new TVDialog(getContext());
   }

   private class TVDialog extends Dialog {

      public TVDialog(Context context) {
         super(context, R.style.Dialog_Closeable);
      }

      @Override
      protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         getWindow().requestFeature(Window.FEATURE_NO_TITLE);
         getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_edges);
         setContentView(R.layout.dialog_view_todo);

         Gson gson = new Gson();
         TodoModel todoModel = (TodoModel) gson.fromJson(getArguments().getString(ARG_DATA), TodoModel.class);

         TextView tv_title, tv_completionTime, tv_description, tv_from;

         tv_title = findViewById(R.id.title);
         tv_completionTime = findViewById(R.id.completioon_time);
         tv_description = findViewById(R.id.description);
         tv_from = findViewById(R.id.from);

         tv_title.setText(todoModel.getTaskTitle());

         if (TextUtils.isEmpty(todoModel.getStartTime()) && TextUtils.isEmpty(todoModel.getEndTime())) {
            tv_from.setVisibility(View.GONE);
            tv_completionTime.setVisibility(View.GONE);
         } else {
            tv_completionTime.setText(todoModel.getCompletionTime());
         }

         String description = todoModel.getTaskDescription();
         if (TextUtils.isEmpty(description)) {
            tv_description.setText("Description not available");
            tv_description.setTextColor(ContextCompat.getColor(getContext(), R.color.light_grey));
         } else {
            tv_description.setText(description);
         }
      }
   }
}