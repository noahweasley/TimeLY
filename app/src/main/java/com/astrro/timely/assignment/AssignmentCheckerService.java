package com.astrro.timely.assignment;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.astrro.timely.core.DataModel;
import com.astrro.timely.core.SchoolDatabase;
import com.astrro.timely.util.ThreadUtils;

import java.util.Calendar;
import java.util.List;

/**
 * Service that is responsible for checking the app's database on device start-up, for any assignment that has passed
 * its submission date. If any assignment was found that matches, it updates that assignment to reflect it's current
 * submission status.
 */
public class AssignmentCheckerService extends Service {
   private SchoolDatabase database;

   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
      database = new SchoolDatabase(this);

      ThreadUtils.runBackgroundTask(() -> {
         List<DataModel> pendingAssignments = database.getPendingAssignments();
         if (!pendingAssignments.isEmpty()) {
            for (DataModel rawData : pendingAssignments) {
               AssignmentModel pendingAssignment = (AssignmentModel) rawData;
               performCheck(pendingAssignment);
            }
         }
      });

      return START_STICKY;
   }

   @Nullable
   @Override
   public IBinder onBind(Intent intent) {
      return null;
   }

   private void performCheck(AssignmentModel assignment) {
      Calendar azzCalendar = Calendar.getInstance();

      String submissionDate = assignment.getSubmissionDate();
      String[] sArr = submissionDate.split("[/._-]");
      int day = Integer.parseInt(sArr[0]);
      int month = Integer.parseInt(sArr[1]);
      int year = Integer.parseInt(sArr[2]);
      // Time set to 07:00 am
      azzCalendar.set(year, month, day);
      azzCalendar.set(Calendar.HOUR_OF_DAY, 7);
      azzCalendar.set(Calendar.MINUTE, 0);
      azzCalendar.set(Calendar.SECOND, 0);
      azzCalendar.set(Calendar.MILLISECOND, 0);

      long AZZ_TIME = azzCalendar.getTimeInMillis();
      long NOW = System.currentTimeMillis();

      if (NOW > AZZ_TIME) database.updateAssignmentStatus(assignment.getId(), true);
   }
}