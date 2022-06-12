package com.astrro.timely.main.notification;

import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.astrro.timely.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NotificationRowHolder extends RecyclerView.ViewHolder {
   private final ImageView img_profileImage;
   private final TextView tv_message, tv_updateTime;
   private Notification notification;

   public NotificationRowHolder(@NonNull View itemView) {
      super(itemView);
      tv_message = itemView.findViewById(R.id.message);
      tv_updateTime = itemView.findViewById(R.id.update_time);
      img_profileImage = itemView.findViewById(R.id.profile_image);
   }

   public void bindView() {
      String completeMessage = "<b>" + notification.getUsername() + "</b> " + notification.getMessage();
      tv_updateTime.setText(notification.getUpdateTime());

      tv_message.setText(HtmlCompat.fromHtml(completeMessage, HtmlCompat.FROM_HTML_MODE_LEGACY));

      DisplayMetrics metrics = img_profileImage.getContext().getResources().getDisplayMetrics();
      int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, metrics);

      Uri profileImage = notification.getProfileImage();

      if (profileImage != null)
         Picasso.get().load(profileImage).centerCrop().resize(px, px).into(img_profileImage);
   }

   public NotificationRowHolder with(List<Notification> notificationList) {
      this.notification = notificationList.get(getAbsoluteAdapterPosition());
      return this;
   }
}
