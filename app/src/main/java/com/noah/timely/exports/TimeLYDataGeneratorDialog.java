package com.noah.timely.exports;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.noah.timely.R;
import com.noah.timely.error.ErrorDialog;
import com.noah.timely.util.Constants;
import com.noah.timely.util.ThreadUtils;

import java.util.ArrayList;
import java.util.List;

public class TimeLYDataGeneratorDialog extends DialogFragment {
   @SuppressWarnings("FieldCanBeLocal")
   public static final String TAG = "com.noah.timely.exports.TimeLYDataGeneratorDialog";
   private final List<String> dataModelList = new ArrayList<>();

   public void show(Context context) {
      FragmentManager manager = ((FragmentActivity) context).getSupportFragmentManager();
      show(manager, TAG);
   }

   @NonNull
   @Override
   public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
      return new DataGeneratorDialog(getContext());
   }

   private class DataGeneratorDialog extends Dialog implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
      private Button btn_export;
      private ProgressBar progress;

      public DataGeneratorDialog(@NonNull Context context) {
         super(context);
      }

      @Override
      protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         getWindow().requestFeature(Window.FEATURE_NO_TITLE);
         getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_edges_8);
         setContentView(R.layout.dialog_generate);

         btn_export = findViewById(R.id.export);
         progress = findViewById(R.id.progress);
         btn_export.setOnClickListener(this);

         ViewGroup vg_dataParent = findViewById(R.id.data_parent);
         CheckBox cbx_courses, cbx_assignments, cbx_timetable, cbx_scheduled, cbx_exams;

         // Avoiding too much findViewById()'s, for quick load time
         CheckBox[] checkBoxes = { (CheckBox) vg_dataParent.getChildAt(0), (CheckBox) vg_dataParent.getChildAt(1),
                                   (CheckBox) vg_dataParent.getChildAt(2), (CheckBox) vg_dataParent.getChildAt(3),
                                   (CheckBox) vg_dataParent.getChildAt(4) };
         // This is much more cleaner, than copy and pasting the same thing over again :)
         for (int i = 0; i <= checkBoxes.length; i++) checkBoxes[i].setOnCheckedChangeListener(this);

      }

      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
         int id = buttonView.getId();

         if (id == R.id.courses) {
            // course check-bok
            if (isChecked) dataModelList.add(Constants.COURSE);
            else dataModelList.remove(Constants.COURSE);

         } else if (id == R.id.assignment) {
            // assignments check-box
            if (isChecked) dataModelList.add(Constants.ASSIGNMENT);
            else dataModelList.remove(Constants.ASSIGNMENT);

         } else if (id == R.id.timetable) {
            // timetable check-box
            if (isChecked) dataModelList.add(Constants.TIMETABLE);
            else dataModelList.remove(Constants.TIMETABLE);

         } else if (id == R.id.scheduled_classes) {
            // scheduled classes check-box
            if (isChecked) dataModelList.add(Constants.SCHEDULED_TIMETABLE);
            else dataModelList.remove(Constants.SCHEDULED_TIMETABLE);

         } else if (id == R.id.exam_timetable) {
            // exams check-box
            if (isChecked) dataModelList.add(Constants.EXAM);
            else dataModelList.remove(Constants.EXAM);

         }
      }

      @Override
      public void onClick(View v) {
         btn_export.setText(null);
         btn_export.setEnabled(false);
         progress.setVisibility(View.VISIBLE);
         // generate data
         ThreadUtils.runBackgroundTask(() -> {
            // run parallel in background
            boolean isGenerated = TMLFileGenerator.generate(getContext(), dataModelList);
            // run in ui thread - required
            getActivity().runOnUiThread(() -> {
               // reset to defaults
               btn_export.setText(R.string.generate_export);
               btn_export.setEnabled(true);
               progress.setVisibility(View.GONE);

               if (isGenerated) {
                  // Export successful, show result dialog
                  new ExportSuccessDialog().show(getContext(), R.string.export_success_message);

               } else {
                  // Export unsuccessful. Error occurred
                  ErrorDialog.Builder errorBuilder = new ErrorDialog.Builder();
                  errorBuilder.setShowSuggestions(true)
                              .setDialogMessage("An Error occurred while generating your file")
                              .setSuggestionCount(1)
                              .setSuggestion1("Check that you have enough memory");

                  new ErrorDialog().showErrorMessage(getContext(), errorBuilder.build());
               }

            });

         });

      }

   }

}
