package com.noah.timely.todo;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.google.gson.Gson;
import com.noah.timely.R;

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
         TodoModel assignment = (TodoModel) gson.fromJson(getArguments().getString(ARG_DATA), TodoModel.class);
      }
   }
}