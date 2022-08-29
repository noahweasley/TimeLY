package com.astrro.timely.main.chats;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.astrro.timely.R;
import com.astrro.timely.core.DataModel;
import com.squareup.picasso.Picasso;

import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class MessageRow extends RecyclerView.ViewHolder {
   private int position;
   private final ImageView img_senderImage;
   private final TextView tv_senderUsername;
   private final TextView tv_mainMessage;
   private final TextView tv_sendTime;
   private final TextView tv_messageCount;
   private Message message;

   public MessageRow(@NonNull View itemView) {
      super(itemView);
      img_senderImage = itemView.findViewById(R.id.sender_image);
      tv_senderUsername = itemView.findViewById(R.id.sender_username);
      tv_mainMessage = itemView.findViewById(R.id.main_message);
      tv_sendTime = itemView.findViewById(R.id.send_time);
      tv_messageCount = itemView.findViewById(R.id.message_count);
   }

   MessageRow with(int position, List<DataModel> messasgeList){
      this.message = (Message) messasgeList.get(getAbsoluteAdapterPosition());
      this.position = position;
      return this;
   }

   public void bindView() {
      if (message.getSenderImage() != null) {
         Picasso.get().load(message.getSenderImage()).fit().centerInside();
      }

      tv_senderUsername.setText(message.getSenderUserName());
      tv_sendTime.setText(message.getElapsedSentTime());
      tv_mainMessage.setText(message.getLastSentMessage());
      tv_messageCount.setText(String.valueOf(message.getMessageCount()));
   }
}
