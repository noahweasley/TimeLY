package com.astrro.timely.main.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.astrro.timely.R;
import com.astrro.timely.core.DataModel;
import com.astrro.timely.util.ThreadUtils;
import com.astrro.timely.util.collections.CollectionUtils;
import com.astrro.timely.util.test.DummyGenerator;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {
   private List<DataModel> notificationList = new ArrayList<>();
   private static Fragment fragmentInstance;
   public static final String TOOLBAR_TITLE = "Notifications";

   public static Fragment getInstance() {
      return fragmentInstance == null ? (fragmentInstance = new NotificationsFragment()) : fragmentInstance;
   }

   public static String getToolbarTitle() {
      return TOOLBAR_TITLE;
   }

   @Nullable
   @org.jetbrains.annotations.Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater,
                            @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                            @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
      return inflater.inflate(R.layout.fragment_notification, container, false);
   }

   @Override
   public void onViewCreated(@NonNull View view,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      RecyclerView rv_notificationList = view.findViewById(R.id.notification_list);
      TextView tv_notificationText = view.findViewById(R.id.text);
      ViewGroup vg_loaderView = view.findViewById(R.id.loader_view);

      ThreadUtils.runBackgroundTask(() -> {
         notificationList = DummyGenerator.getNotifications(20);
         requireActivity().runOnUiThread(() -> {
            if (CollectionUtils.isEmpty(notificationList)) {
               rv_notificationList.setVisibility(View.GONE);
               tv_notificationText.setVisibility(View.VISIBLE);
            } else {
               rv_notificationList.setVisibility(View.VISIBLE);
               tv_notificationText.setVisibility(View.GONE);
            }
            vg_loaderView.setVisibility(View.GONE);
         });
      });

      rv_notificationList.setAdapter(new NotificationsAdapter());
      rv_notificationList.setLayoutManager(new LinearLayoutManager(getContext()));

      rv_notificationList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
   }

   @Override
   protected void finalize() throws Throwable {
      fragmentInstance = null;
      super.finalize();
   }

   @Override
   public void onDetach() {
      fragmentInstance = null;
      super.onDetach();
   }

   private class NotificationsAdapter extends RecyclerView.Adapter<NotificationRowHolder> {

      @NonNull
      @Override
      public NotificationRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         View view = getLayoutInflater().inflate(R.layout.notification_row, parent, false);
         return new NotificationRowHolder(view);
      }

      @Override
      public void onBindViewHolder(@NonNull NotificationRowHolder holder, int position) {
         holder.with(notificationList).bindView();
      }

      @Override
      public int getItemCount() {
         return notificationList.size();
      }
   }
}
