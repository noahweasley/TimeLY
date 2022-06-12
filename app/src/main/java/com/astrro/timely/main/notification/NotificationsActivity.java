package com.astrro.timely.main.notification;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.astrro.timely.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
   private final List<Notification> notificationList = new ArrayList<>();

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_notification);

      setSupportActionBar(findViewById(R.id.toolbar));
      getSupportActionBar().setTitle("Notification");
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_keyboard_arrow_down_24);
      getSupportActionBar().setHomeActionContentDescription(R.string.pull_down);

      RecyclerView rv_notificationList = findViewById(R.id.notification_list);
      rv_notificationList.setAdapter(new NotificationsAdapter());
      rv_notificationList.setLayoutManager(new LinearLayoutManager(this));
      rv_notificationList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
   }

   @Override
   public boolean onSupportNavigateUp() {
      onBackPressed();
      return true;
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
