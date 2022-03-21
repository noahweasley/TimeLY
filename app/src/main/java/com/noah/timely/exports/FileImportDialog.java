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
import com.noah.timely.core.DataModel;
import com.noah.timely.util.ThreadUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FileImportDialog extends DialogFragment {
   private static final String ARG_FILEPATH = "Import file path";
   private static boolean dismiss_flag;
   private OnResultReceivedListener listener;

   public FileImportDialog execute(Context context, String filePath) {
      Bundle bundle = new Bundle();
      bundle.putString(ARG_FILEPATH, filePath);
      setArguments(bundle);
      FragmentManager manager = ((FragmentActivity) context).getSupportFragmentManager();
      show(manager, FileImportDialog.class.getName());
      return this;
   }

   public void setOnResultReceived(OnResultReceivedListener listener) {
      this.listener = listener;
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
         ThreadUtils.runBackgroundTask(this::doFileImport);
      }

      private void doFileImport() {
         // generate data
         Bundle arguments = getArguments();
         Map<String, List<? extends DataModel>> results
                 = TMLYFileGenerator.importFromFile(getContext(), getArguments().getString(ARG_FILEPATH));
         // wierd, but I have to do it. I have to transform this map to a list of map entries.
         List<Map.Entry<String, List<? extends DataModel>>> list = new ArrayList<>();
         list.addAll(results.entrySet());
         // run in ui thread - required
         getActivity().runOnUiThread(() -> {
            listener.onResultReceived(list);
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

   public interface OnResultReceivedListener {
      void onResultReceived(List<Map.Entry<String, List<? extends DataModel>>> results);
   }
}