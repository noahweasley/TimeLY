package com.astrro.timely.util.test;

import com.astrro.timely.core.DataModel;
import com.astrro.timely.main.chats.Message;
import com.astrro.timely.main.library.IDocument;
import com.astrro.timely.main.notification.Notification;

import java.util.ArrayList;
import java.util.List;

/**
 * A provider of fake followers details for testing purposes only
 */
public class DummyGenerator {
   private static final int FOLLOWERS = 1;
   private static final int NOTIFICATIONS = 3;
   private static final int DOCUMENTS = 4;
   private static final int MESSAGES = 5;

   public static List<DataModel> getNotifications(int size) {
      List<DataModel> notifications = new ArrayList<>();
      for (int i = 0; i < size; i++) {
         notifications.add((Notification) Dummy.get(DummyGenerator.NOTIFICATIONS));
      }
      return notifications;
   }

   public static List<DataModel> getDummyDocument(int size) {
      List<DataModel> documents = new ArrayList<>();
      for (int i = 0; i < size; i++) {
         documents.add((IDocument) Dummy.get(DummyGenerator.DOCUMENTS));
      }
      return documents;
   }

   public static List<DataModel> getDummyMessages(int size) {
      List<DataModel> messages = new ArrayList<>();
      for (int i = 0; i < size; i++) {
         messages.add((Message) Dummy.get(DummyGenerator.MESSAGES));
      }
      return messages;
   }

   // Dummies
   private static class Dummy {
      private static final String[] names = { "Beti Ayah", "Charies Myles", "Lynux Noah", "Charles Dhenn", "Augustus Lab" };
      private static final String[] messages = { "liked your comment", "replied to your comment", "liked your post",
                                                 "mentioned you in a post", "replied to your post", "commented on your video" };
      private static final String[] uTime = { "1 hour ago", "4 hours ago", "2 seconds ago", "5 minutes ago" };
      private static final String[] placeHolders = { "PDF", "DOC", "DOCX", "ZIP", "TXT", "BIN" };
      private static final String[] docTitles = { "Lorem Ipsum", "Dolor Lorem Ipsum", "Lorem Ipsum Amet Dolor", "Lorem " +
              "Ipsum Dolor Ipsum Amet", "Dolor Amet", "A title" };
      private static final String[] authors = { "Noah Weasley (Admin)", "Franklin (Admin)",
                                                "Chinedu (Admin)", "Astrro (Admin)" };

      private static final String[] dates = { "09.10.2003", "10.22.2230", "01.01.2022" };
      private static final String[] mm = { "Hi there, hope you've been well", "It's been nice meeting you", "I hope we " +
              "can chat another time", "Good evening sir", "I don't want to see you anymore", "We are happy to break " +
                                                   "the news to you that you got the job"};

      // array index
      private static int index = 0;

      /**
       * @param dummyType the type of dummy to return
       * @return the required list of dummies
       */
      public static DataModel get(int dummyType) {
         if (dummyType == NOTIFICATIONS) {
            int nIndex = index >= names.length ? index = 0 : index;    // return 0 or inc
            int mIndex = index >= messages.length ? index = 0 : index; // return 0 or inc
            int tIndex = index >= uTime.length ? index = 0 : index++;    // return 0 or inc
            return new Notification(null, names[nIndex], messages[mIndex], uTime[tIndex]);
         } else if (dummyType == DOCUMENTS) {
            int randomBytes = (int) (Math.random() * 123432423);
            String placeHolder = index >= placeHolders.length ? placeHolders[index = 0] : placeHolders[index];
            String docTitle = index >= docTitles.length ? docTitles[index = 0] : docTitles[index];
            String author = index >= authors.length ? authors[index = 0] : authors[index];
            String uploadDate = index >= dates.length ? dates[index = 0] : dates[index++];
            boolean isLiked = Math.random() >= 0.3;
            boolean isFlagged = Math.random() < 0.5;

            return new IDocument(null, placeHolder, docTitle, author, randomBytes, uploadDate, isFlagged, isLiked);
         } else if (dummyType == MESSAGES) {
            Message message = new Message();
            String author = index >= authors.length ? authors[index = 0] : authors[index];
            String docTitle = index >= docTitles.length ? docTitles[index = 0] : docTitles[index];
            message.setMessageCount((int) (Math.random() * 99));
            message.setSenderUserName(author);
            message.setElapsedSentTime((long) (Math.random() * 1966199));
            message.setElapsedSentTime((int) (Math.random() * 198398));
            message.setLastSentMessage(docTitle);
            index++;
            return message;
         } else {
            throw new IllegalStateException("Unexpected value: " + dummyType);
         }
      }

   }
}
