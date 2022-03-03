package com.noah.timely.exports;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.noah.timely.R;
import com.noah.timely.error.ErrorDialog;
import com.noah.timely.util.ThreadUtils;

import java.util.ArrayList;
import java.util.List;

public class ActionProcessorDialog extends DialogFragment {
   private static final String ARG_LIST = "list";
   private static boolean dismiss_flag;

   public void execute(Context context, List<String> dataModelList) {
      Bundle bundle = new Bundle();
      bundle.putStringArrayList(ARG_LIST, (ArrayList<String>) dataModelList);
      setArguments(bundle);
      FragmentManager manager = ((FragmentActivity) context).getSupportFragmentManager();
      show(manager, ActionProcessorDialog.class.getName());
   }

   @NonNull
   @Override
   public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
      return new ProcessDialog(getContext());
   }

   private class ProcessDialog extends Dialog {

      public ProcessDialog(@NonNull Context context) {
         super(context, R.style.Dialog_No_Transition);
      }

      @Override
      protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         getWindow().requestFeature(Window.FEATURE_NO_TITLE);
         getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_edges);
         setContentView(R.layout.dialog_processing);
         ThreadUtils.runBackgroundTask(this::doExport);
      }

      private void doExport() {
         // generate data
         Bundle arguments = getArguments();
         String exportPath = TMLYFileGenerator.generate(getContext(), arguments.getStringArrayList(ARG_LIST));
         // run in ui thread - required
         getActivity().runOnUiThread(() -> {
            if (exportPath != null) {
               // Export successful, show result dialog
               new ExportSuccessDialog().show(getActivity(), R.string.export_success_message, exportPath);

            } else {
               // Export unsuccessful. Error occurred
               ErrorDialog.Builder errorBuilder = new ErrorDialog.Builder();
               errorBuilder.setShowSuggestions(true)
                           .setDialogMessage("An Error occurred while generating data")
                           .setSuggestionCount(1)
                           .setSuggestion1("Check that you have enough memory");

               new ErrorDialog().showErrorMessage(getActivity(), errorBuilder.build());
            }

            dismiss_flag = true;
            dismiss();  // dismiss dialog if data was generated or not

         });
      }

      @Override
      public void dismiss() {
         if (dismiss_flag)
            super.dismiss();
      }
   }
}