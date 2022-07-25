package com.astrro.timely.util.test;

import com.astrro.timely.core.DataModel;
import com.astrro.timely.main.library.IDocument;
import com.astrro.timely.main.notification.Notification;

import java.util.ArrayList;
import java.util.List;

/**
 * A provider of fake followers details for testing purposes only
 */
public class DummyGenerator {
   private static final int FOLLOWERS = 1;
   private static final int STORIES = 2;
   private static final int NOTIFICATIONS = 3;
   private static final int DOCUMENTS = 4;

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

   // Dummies
   private static class Dummy {
      private static final String[] names = { "Beti Ayah", "Charies Myles", "Lynux Noah", "Charles Dhenn", "Augustus Lab" };

      private static final String[] messages = { "liked your comment", "replied to your comment", "liked your post",
                                                 "mentioned you in a post", "replied to your post", "commented on your video" };

      private static final String[] uTime = { "1 hour ago", "4 hours ago", "2 seconds ago", "5 minutes ago" };

      // array indices
      private static int dn = 0;
      private static int dm = 0;
      private static int dt = 0;
      private static final int dummyNum = 0;

      /**
       * @param dummyType the type of dummy to return
       * @return the required list of dummies
       */
      public static DataModel get(int dummyType) {
         if (dummyType == NOTIFICATIONS) {
            int nIndex = dn == names.length ? dn = 0 : dn++;    // return 0 or inc
            int mIndex = dm == messages.length ? dm = 0 : dm++; // return 0 or inc
            int tIndex = dt == uTime.length ? dt = 0 : dt++;    // return 0 or inc
            return new Notification(null, names[nIndex], messages[mIndex], uTime[tIndex]);
         } else if (dummyType == DOCUMENTS) {
            return new IDocument();
         }
         throw new IllegalStateException("Unexpected value: " + dummyType);
      }

   }
}
