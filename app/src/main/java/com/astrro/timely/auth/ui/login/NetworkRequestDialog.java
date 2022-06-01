package com.astrro.timely.auth.ui.login;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.astrro.timely.R;
import com.astrro.timely.util.ThreadUtils;
import com.astrro.timely.util.collections.ISupplier;

public class NetworkRequestDialog extends DialogFragment {
   public static final String ARG_LOADING_INFO = "Arg loading info";
   private ISupplier supplier;
   private boolean dismiss_flag;
   private OnActionProcessedListener listener;

   @NonNull
   @Override
   public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
      return new NetReqDialog(getContext());
   }

   public NetworkRequestDialog setLoadingInfo(@NonNull String loaderInfo) {
      Bundle bundle = new Bundle();
      bundle.putString(ARG_LOADING_INFO, loaderInfo);
      setArguments(bundle);
      return this;
   }

   public NetworkRequestDialog execute(Context context, ISupplier supplier) {
      this.supplier = supplier;
      FragmentManager manager = ((FragmentActivity) context).getSupportFragmentManager();
      show(manager, NetworkRequestDialog.class.getName());
      return this;
   }

   public void setOnActionProcessedListener(OnActionProcessedListener listener) {
      this.listener = listener;
   }

   public class NetReqDialog extends Dialog {

      public NetReqDialog(@NonNull Context context) {
         super(context, R.style.Dialog_No_Transition);
      }

      @Override
      protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         getWindow().requestFeature(Window.FEATURE_NO_TITLE);
         getWindow().setBackgroundDrawableResource(R.drawable.bg_rounded_edges);
         setContentView(R.layout.dialog_network_request);

         Bundle arguments = null;
         TextView tv_loaderText = findViewById(R.id.loader_text);
         if ((arguments = getArguments()) != null) {
            String loaderText = null;
            if (!TextUtils.isEmpty((loaderText = arguments.getString(ARG_LOADING_INFO)))) {
               tv_loaderText.setText(loaderText);
            }
         } else {
            tv_loaderText.setText(getString(R.string.processing));
         }

         ThreadUtils.runBackgroundTask(() -> {
            supplier.get();
            dismiss_flag = true;
            getActivity().runOnUiThread(() -> {
               if (listener != null) listener.onActionProcessed();
               dismiss();
            });
         });

      }

      @Override
      public void dismiss() {
         if (dismiss_flag)
            super.dismiss();
      }

   }

   public interface OnActionProcessedListener {
      void onActionProcessed();
   }

}
