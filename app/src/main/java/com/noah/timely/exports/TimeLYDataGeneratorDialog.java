package com.noah.timely.exports;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.noah.timely.R;
import com.noah.timely.core.DataModel;

import java.util.ArrayList;
import java.util.List;

public class TimeLYDataGeneratorDialog extends DialogFragment implements View.OnClickListener {
   @SuppressWarnings("FieldCanBeLocal")
   public static final String TAG = "com.noah.timely.exports.TimeLYDataGeneratorDialog";
   @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")  // fixme: remove this
   // use classes to simplify the process
   private final List<Class<? extends DataModel>> dataModelList = new ArrayList<>();

   public void show(Context context) {
      FragmentManager manager = ((FragmentActivity) context).getSupportFragmentManager();
      show(manager, TAG);
   }

   @Override
   public void onClick(View v) {
      boolean isGenerated = TMLFileGenerator.generate(getContext(), dataModelList);
      if (isGenerated) Toast.makeText(getContext(), "Error occurred", Toast.LENGTH_SHORT).show();
   }

   @NonNull
   @Override
   public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
      return new DataGeneratorDialog(getContext());
   }

   private static class DataGeneratorDialog extends Dialog {

      public DataGeneratorDialog(@NonNull Context context) {
         super(context, R.style.Dialog_Closeable);
      }

      @Override
      protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         getWindow().requestFeature(Window.FEATURE_NO_TITLE);
         getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_edges_8);
         setContentView(R.layout.dialog_generate);

      }

   }
}
