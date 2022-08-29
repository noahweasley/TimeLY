package com.astrro.timely.main.chats;

import android.net.Uri;

import com.astrro.timely.core.DataModel;

public class Message extends DataModel {
   private Uri senderImage;
   private int messageCount;
   private long elapsedSentTime;
   private String senderUserName;
   private String lastSentMessage;
   private String sentTime;

   public Uri getSenderImage() {
      return senderImage;
   }

   public void setSenderImage(Uri senderImage) {
      this.senderImage = senderImage;
   }

   public int getMessageCount() {
      return messageCount;
   }

   public void setMessageCount(int messageCount) {
      this.messageCount = messageCount;
   }

   public String getSenderUserName() {
      return senderUserName;
   }

   public void setSenderUserName(String senderUserName) {
      this.senderUserName = senderUserName;
   }

   public String getLastSentMessage() {
      return lastSentMessage;
   }

   public void setLastSentMessage(String lastSentMessage) {
      this.lastSentMessage = lastSentMessage;
   }

   public String getSentTime() {
      return sentTime;
   }

   public void setSentTime(String sentTime) {
      this.sentTime = sentTime;
   }

   public String getElapsedSentTime() {
      long now = System.currentTimeMillis();
      long then = elapsedSentTime;
      return "24m";
   }

   public void setElapsedSentTime(long elapsedSentTime) {
      this.elapsedSentTime = elapsedSentTime;
   }
}
