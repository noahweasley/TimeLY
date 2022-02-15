package com.noah.timely.exports;

import static android.os.Looper.getMainLooper;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import java.util.Arrays;
import java.util.List;

public class TimeLYDataGeneratorDialog extends DialogFragment {
   private static final String ARG_IDENTIFIER = "default_identifier";
   private final List<String> dataModelList = new ArrayList<>();
   private boolean dismissable;

   public void show(Context context) {
      Bundle bundle = new Bundle();
      setArguments(bundle);
      FragmentManager manager = ((FragmentActivity) context).getSupportFragmentManager();
      show(manager, TimeLYDataGeneratorDialog.class.getName());
   }

   public void show(Context context, String defaultIdentifier) {
      Bundle bundle = new Bundle();
      bundle.putString(ARG_IDENTIFIER, defaultIdentifier);
      setArguments(bundle);
      FragmentManager manager = ((FragmentActivity) context).getSupportFragmentManager();
      show(manager, TimeLYDataGeneratorDialog.class.getName());
   }

   @NonNull
   @Override
   public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
      return new DataGeneratorDialog(getContext());
   }

   public void onCloseDialog(View view) {
      if (dismissable) {
         dismiss();
      } else {
         Toast.makeText(getContext(), "Press again to stop operation", Toast.LENGTH_SHORT).show();
      }
      dismissable = true;
      new Handler(getMainLooper()).postDelayed(() -> dismissable = false, 2000);
   }

   private class DataGeneratorDialog extends Dialog implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
      private Button btn_export;
      private ProgressBar progress;

      public DataGeneratorDialog(@NonNull Context context) {
         super(context, R.style.Dialog);
      }

      @Override
      protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         getWindow().requestFeature(Window.FEATURE_NO_TITLE);
         getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_edges_8);
         setContentView(R.layout.dialog_generate);

         findViewById(R.id.cancel).setOnClickListener(TimeLYDataGeneratorDialog.this::onCloseDialog);

         btn_export = findViewById(R.id.export);
         progress = findViewById(R.id.progress);
         btn_export.setOnClickListener(this);

         ViewGroup vg_dataParent = findViewById(R.id.data_parent);
         // Avoiding too much findViewById()'s, for quick load time
         CheckBox cbx_courses, cbx_assignments, cbx_timetable, cbx_scheduled, cbx_exams;
         cbx_courses = (CheckBox) vg_dataParent.getChildAt(0);
         cbx_assignments = (CheckBox) vg_dataParent.getChildAt(1);
         cbx_timetable = (CheckBox) vg_dataParent.getChildAt(2);
         cbx_scheduled = (CheckBox) vg_dataParent.getChildAt(3);
         cbx_exams = (CheckBox) vg_dataParent.getChildAt(4);
         CheckBox[] checkBoxes = { cbx_courses, cbx_assignments, cbx_timetable, cbx_scheduled, cbx_exams };
         // This is much more cleaner, than copy and pasting the same thing over again :)
         for (int i = 0; i < checkBoxes.length; i++) checkBoxes[i].setOnCheckedChangeListener(this);

         String defaultIdentifier = null;
         if (getArguments() != null) {
            defaultIdentifier = getArguments().getString(ARG_IDENTIFIER);
         }
         if (!TextUtils.isEmpty(defaultIdentifier)) {
            // select default checked checkbox
            switch (defaultIdentifier) {
               case Constants.COURSE:
                  cbx_courses.setChecked(true);
                  break;
               case Constants.ASSIGNMENT:
                  cbx_assignments.setChecked(true);
                  break;
               case Constants.TIMETABLE:
                  cbx_timetable.setChecked(true);
                  break;
               case Constants.SCHEDULED_TIMETABLE:
                  cbx_scheduled.setChecked(true);
                  break;
               case Constants.EXAM:
                  cbx_exams.setChecked(true);
                  break;
               default:
                  throw new IllegalStateException("Unexpected value: " + defaultIdentifier);
            }
         } else {
            // intially all checkboxes are checked
            String[] datamodels = new String[]{ Constants.COURSE, Constants.ASSIGNMENT, Constants.TIMETABLE,
                                                Constants.SCHEDULED_TIMETABLE, Constants.EXAM };
            dataModelList.addAll(Arrays.asList(datamodels));
         }
      }

      @Override
      public void dismiss() {
         if (dismissable) super.dismiss();
         else onCloseDialog(null);
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
            if (isAdded()) {
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
            }

         });

      }

   }
}