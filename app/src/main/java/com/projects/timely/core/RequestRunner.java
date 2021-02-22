package com.projects.timely.core;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Process;
import android.util.Log;

import com.projects.timely.alarms.AlarmListFragment;
import com.projects.timely.alarms.AlarmModel;
import com.projects.timely.alarms.AlarmReceiver;
import com.projects.timely.assignment.AssignmentFragment;
import com.projects.timely.assignment.AssignmentModel;
import com.projects.timely.assignment.MultiUpdateMessage;
import com.projects.timely.assignment.Reminder;
import com.projects.timely.assignment.SubmissionNotifier;
import com.projects.timely.assignment.UpdateMessage;
import com.projects.timely.assignment.UpdateMessage.EventType;
import com.projects.timely.assignment.UriUpdateEvent;
import com.projects.timely.assignment.ViewImagesActivity;
import com.projects.timely.courses.CourseModel;
import com.projects.timely.courses.CourseRowHolder;
import com.projects.timely.exam.ExamModel;
import com.projects.timely.exam.ExamRowHolder;
import com.projects.timely.scheduled.ScheduledTaskNotifier;
import com.projects.timely.scheduled.ScheduledTimetableFragment;
import com.projects.timely.timetable.DaysFragment;
import com.projects.timely.timetable.TimetableModel;
import com.projects.timely.timetable.TimetableNotifier;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import static com.projects.timely.core.Globals.Alert;
import static com.projects.timely.core.Globals.deleteTaskRunning;
import static com.projects.timely.core.Globals.playAlertTone;

/**
 * Thread to handle all delete requests
 */
public class RequestRunner extends Thread {
    private static boolean deleteRequestDiscarded;
    private String request;
    private FragmentActivity mActivity;
    private int adapterPosition;
    private RecyclerView.Adapter<?> adapter;
    private List<DataModel> dList;
    private String alarmLabel;
    private String[] alarmTime;
    private AssignmentModel assignmentData;
    private SchoolDatabase database;
    private String timetable;
    private List<Uri> mediaUris;
    private int assignmentPosition;
    private Integer[] itemIndices;

    /**
     * Use {@link RequestRunner#getInstance()} instead, to get the instance of the
     * <strong>RequestRunner</strong>. This convenience method is just to improve readability.
     */
    private RequestRunner() {
    }

    /**
     * @return an instance of A <strong>RequestRunner</strong>
     */
    public static RequestRunner getInstance() {
        return new RequestRunner();
    }

    @Override
    public void run() {
        deleteTaskRunning = true;
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        performDeleteOperation();
        deleteRequestDiscarded = false;
        deleteTaskRunning = false;
    }

    private void performDeleteOperation() {
        switch (request) {
            case AssignmentFragment.DELETE_REQUEST:
                doAssignmentDelete();
                break;
            case DaysFragment.DELETE_REQUEST:
            case ScheduledTimetableFragment.DELETE_REQUEST:
                doTimeTableDelete();
                break;
            case AlarmListFragment.DELETE_REQUEST:
                doAlarmDelete();
                break;
            case CourseRowHolder.DELETE_REQUEST:
                doCourseDelete();
                break;
            case ExamRowHolder.DELETE_REQUEST:
                doExamDelete();
                break;
            case ViewImagesActivity.DELETE_REQUEST:
                doImageDelete();
                break;
            case ViewImagesActivity.MULTIPLE_DELETE_REQUEST:
                doImageMultiDelete();
                break;
        }
        database.close();
    }

    private void doImageMultiDelete() {
        List<Uri> uriCache = new ArrayList<>();

        Arrays.sort(itemIndices, Collections.reverseOrder());
        for (int i : itemIndices) {
            uriCache.add(mediaUris.remove(i));
        }
        EventBus.getDefault().post(new MultiUpdateMessage(MultiUpdateMessage.EventType.REMOVE));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException exc) {
            for (int x = 0; x < uriCache.size(); x++) {
                mediaUris.add(itemIndices[x], uriCache.get(x));
            }
            EventBus.getDefault().post(new MultiUpdateMessage(MultiUpdateMessage.EventType.INSERT));
        }

        if (!deleteRequestDiscarded) {
            boolean isDeleted = database.deleteMultipleImages(assignmentPosition, itemIndices);
            if (isDeleted) {
                playAlertTone(mActivity.getApplicationContext(), Alert.DELETE);
                if(mediaUris.isEmpty())
                    EventBus.getDefault().post(new EmptyListEvent());
            }
        }
    }

    private void doImageDelete() {
        Uri uri = mediaUris.remove(adapterPosition);
        adapter.notifyItemRemoved(adapterPosition);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException exc) {
            mediaUris.add(adapterPosition, uri);
            adapter.notifyItemInserted(adapterPosition);
        }

        if (!deleteRequestDiscarded) {
            String uris = database.deleteImage(assignmentPosition, uri);
            if (uris != null) {
                playAlertTone(mActivity.getApplicationContext(), Alert.DELETE);
                if (mediaUris.isEmpty())
                    EventBus.getDefault().post(new EmptyListEvent());
            }
            EventBus.getDefault().post(new UriUpdateEvent(assignmentPosition, uris));
        }
    }

    private void doExamDelete() {
        DataModel model = dList.get(adapterPosition);
        dList.remove(adapterPosition);
        adapter.notifyItemRemoved(adapterPosition);
        adapter.notifyDataSetChanged();
        EventBus.getDefault().post(new CountEvent(dList.size()));
            /*
            wait 3 seconds to perform actual delete request, because an undo request
            might also be issued, which delete request would have to be cancelled.
            The sleep timer is also synchronized   with the undo Snackbar's display timeout,
            which also is 3 seconds.
            */
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            /*
            will be executed when the deleteRequestDiscarded property has been set,
            meaning an undo request
            */
            dList.add(adapterPosition, model);
            adapter.notifyItemInserted(adapterPosition);
            adapter.notifyDataSetChanged();
            EventBus.getDefault().post(new CountEvent(dList.size()));
        }
        if (!deleteRequestDiscarded) {
            ExamModel examModel = (ExamModel) model;
            boolean isDeleted = database.deleteExamEntry(examModel, examModel.getWeek());
            if (isDeleted) {
                playAlertTone(mActivity.getApplicationContext(), Alert.DELETE);
                if (dList.isEmpty())
                    EventBus.getDefault().post(new EmptyListEvent());
            }
        }
    }

    private void doCourseDelete() {
        DataModel model = dList.get(adapterPosition);
        dList.remove(adapterPosition);
        adapter.notifyItemRemoved(adapterPosition);
        adapter.notifyDataSetChanged();
        EventBus.getDefault().post(new CountEvent(dList.size()));
            /*
            wait 3 seconds to perform actual delete request, because an undo request
            might also be issued, which delete request would have to be cancelled.
            The sleep timer is also synchronized with the undo Snackbar's display timeout,
            which also is 3 seconds.
            */
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            /*
            will be executed when the deleteRequestDiscarded property has been set,
            meaning an undo request
            */
            dList.add(adapterPosition, model);
            adapter.notifyItemInserted(adapterPosition);
            adapter.notifyDataSetChanged();
            EventBus.getDefault().post(new CountEvent(dList.size()));
        }
        if (!deleteRequestDiscarded) {
            CourseModel courseModel = (CourseModel) model;
            boolean isDeleted = database.deleteCourseEntry(courseModel, courseModel.getSemester());
            if (isDeleted) {
                playAlertTone(mActivity.getApplicationContext(), Alert.DELETE);
                if (dList.isEmpty())
                    EventBus.getDefault().post(new EmptyListEvent());
            }
        }
    }

    private void doAssignmentDelete() {
        DataModel model = dList.get(adapterPosition);
        EventBus.getDefault().post(new UpdateMessage((AssignmentModel) model, EventType.REMOVE));
            /*
            wait 3 seconds to perform actual delete request, because an undo request
            might also be issued, which delete request would have to be cancelled.
            The sleep timer is also synchronized with the undo Snackbar's display timeout,
            which also is 3 seconds.
            */
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            EventBus.getDefault()
                    .post(new UpdateMessage((AssignmentModel) model, EventType.INSERT));
        }
        if (!deleteRequestDiscarded) {
            boolean isDeleted = database.deleteAssignmentEntry((AssignmentModel) model);
            if (isDeleted) {
                cancelAssignmentNotifier(assignmentData);
                playAlertTone(mActivity.getApplicationContext(), Alert.DELETE);
            }
        }
    }

    private void doTimeTableDelete() {
        DataModel model = dList.get(adapterPosition);
        dList.remove(adapterPosition);
        adapter.notifyItemRemoved(adapterPosition);
        adapter.notifyDataSetChanged();
        EventBus.getDefault().post(new CountEvent(dList.size()));
        // wait 3 seconds to perform actual delete request, because an undo request
        // might also be issued, which delete request would have to be cancelled.
        // The sleep timer is also synchronized with the undo Snackbar's display timeout,
        // which also is 3 seconds.
        // We don't want someone to change view of current timetable (if the RequestRunner is
        // being used in the TableFragment) when delete operation is still in progress,
        // as that would halt the delete operation !!!
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // will be executed when the deleteRequestDiscarded property has been set,
            // meaning an undo request
            dList.add(adapterPosition, model);
            adapter.notifyItemInserted(adapterPosition);
            adapter.notifyDataSetChanged();
            EventBus.getDefault().post(new CountEvent(dList.size()));
        }
        if (!deleteRequestDiscarded) {
            boolean isDeleted;
            if (request.equals(ScheduledTimetableFragment.DELETE_REQUEST))
                isDeleted = database.deleteTimetableEntry((TimetableModel) model,
                                                          SchoolDatabase.SCHEDULED_TIMETABLE);
            else isDeleted = database.deleteTimetableEntry((TimetableModel) model, timetable);
            if (isDeleted) {
                if (dList.isEmpty())
                    EventBus.getDefault().post(new EmptyListEvent());
                if (request.equals(ScheduledTimetableFragment.DELETE_REQUEST))
                    cancelScheduledTimetableNotifier((TimetableModel) model);
                else cancelTimetableNotifier((TimetableModel) model);
                playAlertTone(mActivity.getApplicationContext(), Alert.DELETE);
            }
        }
    }

    private void cancelTimetableNotifier(TimetableModel timetable) {
        Context context = mActivity.getApplicationContext();
        String[] t = timetable.getStartTime().split(":");
        timetable.setDay(this.timetable);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, timetable.getCalendarDay());
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(t[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(t[1]));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.setTimeInMillis(calendar.getTimeInMillis() - TimeUnit.MINUTES.toMillis(10));

        long NOW = System.currentTimeMillis();
        long CURRENT = calendar.getTimeInMillis();
        long timeInMillis = CURRENT < NOW ? CURRENT + TimeUnit.DAYS.toMillis(7) : CURRENT;

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent timetableIntent = new Intent(context, TimetableNotifier.class);
        timetableIntent.addCategory("com.projects.timely.timetable")
                .setAction("com.projects.timely.timetable.addAction")
                .setDataAndType(Uri.parse("content://com.projects.timely.add." + timeInMillis),
                                "com.projects.timely.dataType");

        PendingIntent pi = PendingIntent.getBroadcast(context, 555, timetableIntent,
                                                      PendingIntent.FLAG_CANCEL_CURRENT);
        pi.cancel();
        manager.cancel(pi);
    }

    private void doAlarmDelete() {
        // I experienced a bug at the next line here.
        // I had to use getAlarmAt() instead of using the data supplied from the
        // modelList variable. Why ? Because I ran a background task that always changed the
        // data the alarm list adapter was holding, so the modelList variable became invalid.
        // So why did I still use it ? Because I wanted to play the animation of alarm row
        // delete task, and using the modelList, even though it is invalid, had an important role
        // to play, because it still represent the alarm row
        DataModel model = database.getAlarmAt(adapterPosition);
        dList.remove(adapterPosition);
        adapter.notifyItemRemoved(adapterPosition);
        // now notify list of item change so that it can invalidate current views.
        // This line fixed a bug for me and help me change the indicators :)
        adapter.notifyDataSetChanged();
            /*
            wait 3 seconds to perform actual delete request, because an undo request
            might also be issued, which delete request would have to be cancelled.
            The sleep timer is also synchronized with the undo Snackbar's display timeout,
            which also is 3 seconds.
            */
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            /*
            will be executed when the deleteRequestDiscarded property has been set,
            meaning an undo request
            */
            dList.add(adapterPosition, model);
            adapter.notifyItemInserted(adapterPosition);
            // now notify list of item change so that it can invalidate current views.
            // This line fixed a bug for me and help me change the indicators :)
            adapter.notifyDataSetChanged();
        }
        if (!deleteRequestDiscarded) {
            boolean isDeleted = database.deleteAlarmEntry((AlarmModel) model);
            if (isDeleted) {
                cancelAlarm(); // if alarm was deleted, cancel alarm first, then ...
                playAlertTone(mActivity.getApplicationContext(), Alert.DELETE);
                if (dList.isEmpty()) EventBus.getDefault().post(new EmptyListEvent());
            }
        }
    }

    /**
     * Undo the current delete request
     */
    public void undoRequest() {
        deleteRequestDiscarded = true;
        // wake RequestRunner thread immediately from sleep, for immediate undo response
        interrupt();
        playAlertTone(mActivity.getApplicationContext(), Alert.UNDO);
    }

    /**
     * Start the thread, and set the request
     *
     * @param request the request to be run by the <strong>RequestRunner</strong>
     */
    public void runRequest(String request) {
        this.request = request;
        this.start();
    }

    /**
     * Use this to set the <strong>RequestRunner</strong>'s properties
     *
     * @param activity   the container activity for the current fragment executing the request
     * @param viewHolder the viewHolder to get the position in the list to be deleted
     * @param adapter    the adapter to be notified about the changes
     * @param modelList  the {@link List} which it's data is displayed in the list
     * @return same instance of this class to be used for chain calls
     */
    public RequestRunner with(@Nullable FragmentActivity activity,
                              ViewHolder viewHolder,
                              RecyclerView.Adapter<?> adapter,
                              List<DataModel> modelList) {
        this.mActivity = activity;
        this.adapterPosition = viewHolder.getAdapterPosition();
        this.adapter = adapter;
        this.dList = modelList;
        database = new SchoolDatabase(mActivity);
        return this;
    }

    /**
     * Use this to set the fields to be used by {@link RequestRunner#cancelAlarm}
     *
     * @param alarmLabel the label of the alarm
     * @param alarmTime  the time to be cancelled by alarm
     * @return same instance of this class to be used for chain calls
     */
    public RequestRunner setAlarmData(String alarmLabel, String[] alarmTime) {
        this.alarmLabel = alarmLabel;
        this.alarmTime = alarmTime;
        return this;
    }

    /**
     * Use this to set the field to be used with
     * {@link SchoolDatabase#deleteTimetableEntry(TimetableModel, String)}, if the user of
     * this <code>RequestRunner</code> is an <strong>ordinary</strong> timetable, and
     * <strong>not</strong> a scheduled timetable
     *
     * @param timetable the timetable to perform delete operation on
     * @return same instance of this class to be used form chaining
     */
    public RequestRunner setTimetableData(String timetable) {
        this.timetable = timetable;
        return this;
    }

    public RequestRunner setAssignmentData(AssignmentModel assignmentData) {
        this.assignmentData = assignmentData;
        return this;
    }

    private void cancelScheduledTimetableNotifier(TimetableModel timetable) {
        Context context = mActivity.getApplicationContext();
        String[] t = timetable.getStartTime().split(":");

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, timetable.getCalendarDay());
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(t[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(t[1]));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.setTimeInMillis(calendar.getTimeInMillis() - TimeUnit.MINUTES.toMillis(10));

        long NOW = System.currentTimeMillis();
        long CURRENT = calendar.getTimeInMillis();
        long timeInMillis = CURRENT < NOW ? CURRENT + TimeUnit.DAYS.toMillis(7) : CURRENT;

        Log.d(getClass().getSimpleName(), "Cancelling alarm: " + new Date(timeInMillis));

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent timetableIntent = new Intent(context, ScheduledTaskNotifier.class);
        timetableIntent.addCategory("com.projects.timely.scheduled")
                .setAction("com.projects.timely.scheduled.addAction")
                .setDataAndType(
                        Uri.parse("content://com.projects.timely.scheduled.add." + timeInMillis),
                        "com.projects.timely.scheduled.dataType");

        PendingIntent pi = PendingIntent.getBroadcast(context, 1156, timetableIntent,
                                                      PendingIntent.FLAG_CANCEL_CURRENT);
        pi.cancel();
        manager.cancel(pi);
    }

    private void cancelAssignmentNotifier(AssignmentModel data) {
        Context aCtxt = mActivity.getApplicationContext();
        Intent notifyIntentCurrent = new Intent(aCtxt, SubmissionNotifier.class);
        notifyIntentCurrent
                .addCategory(aCtxt.getPackageName() + ".category")
                .setAction(aCtxt.getPackageName() + ".update")
                .setDataAndType(Uri.parse("content://" + aCtxt.getPackageName()),
                                data.toString());

        Intent notifyIntentPrevious = new Intent(aCtxt, Reminder.class);
        notifyIntentPrevious
                .addCategory(aCtxt.getPackageName() + ".category")
                .setAction(aCtxt.getPackageName() + ".update")
                .setDataAndType(Uri.parse("content://" + aCtxt.getPackageName()),
                                data.toString());

        PendingIntent assignmentPiPrevious
                = PendingIntent.getBroadcast(aCtxt, 147, notifyIntentPrevious,
                                             PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent assignmentPiCurrent
                = PendingIntent.getBroadcast(aCtxt, 141, notifyIntentCurrent,
                                             PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager manager = (AlarmManager) aCtxt.getSystemService(Context.ALARM_SERVICE);
        assignmentPiCurrent.cancel();
        manager.cancel(assignmentPiCurrent);
        assignmentPiPrevious.cancel();
        manager.cancel(assignmentPiPrevious);
    }

    public void cancelAlarm() {
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(alarmTime[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(alarmTime[1]));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        boolean isNextDay = System.currentTimeMillis() > calendar.getTimeInMillis();
        long alarmMillis = isNextDay ? calendar.getTimeInMillis() + TimeUnit.DAYS.toMillis(1)
                                     : calendar.getTimeInMillis();

        Intent alarmReceiverIntent = new Intent(mActivity, AlarmReceiver.class);
        alarmReceiverIntent.putExtra("Label", alarmLabel);
        // This is just used to prevent cancelling all pending intent, because when
        // PendingIntent#cancel is called, all pending intent that matches the intent supplied to
        // Intent#filterEquals (and it returns true), will be cancelled because there was no
        // difference between the intents. So this code segment was used to provide a distinguishing
        // effect.
        alarmReceiverIntent.addCategory("com.projects.timely.alarm.category");
        alarmReceiverIntent.setAction("com.projects.timely.alarm.cancel");
        alarmReceiverIntent.setDataAndType(
                Uri.parse("content://com.projects.timely/Alarms/alarm" + alarmMillis),
                "com.projects.timely.alarm.dataType");

        AlarmManager alarmManager
                = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmPI = PendingIntent.getBroadcast(mActivity,
                                                           1189765,
                                                           alarmReceiverIntent,
                                                           PendingIntent.FLAG_CANCEL_CURRENT);
        alarmPI.cancel();
        alarmManager.cancel(alarmPI);
    }

    public RequestRunner setUriList(List<Uri> mediaUris) {
        this.mediaUris = mediaUris;
        return this;
    }

    public RequestRunner setAssignmentPosition(int position) {
        this.assignmentPosition = position;
        return this;
    }

    public RequestRunner setItemsIndices(Integer[] itemsIndices) {
        this.itemIndices = itemsIndices;
        return this;
    }
}