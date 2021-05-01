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
import com.projects.timely.assignment.MultiUpdateMessage2;
import com.projects.timely.assignment.Reminder;
import com.projects.timely.assignment.SubmissionNotifier;
import com.projects.timely.assignment.UpdateMessage;
import com.projects.timely.assignment.UpdateMessage.EventType;
import com.projects.timely.assignment.UriUpdateEvent;
import com.projects.timely.assignment.ViewImagesActivity;
import com.projects.timely.courses.CourseModel;
import com.projects.timely.courses.CourseRowHolder;
import com.projects.timely.courses.SemesterFragment;
import com.projects.timely.exam.ExamModel;
import com.projects.timely.exam.ExamRowHolder;
import com.projects.timely.exam.ExamTimetableFragment;
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

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import static com.projects.timely.core.AppUtils.Alert;
import static com.projects.timely.core.AppUtils.deleteTaskRunning;
import static com.projects.timely.core.AppUtils.playAlertTone;

/**
 * Thread to handle all delete requests
 */
public class RequestRunner extends Thread {
    public static final int WAIT_TIME = 3000;
    public static final String TAG = "SchoolDatabase";
    private static boolean deleteRequestDiscarded;
    private String request;
    private SchoolDatabase database;
    private RequestParams params;
    private Context appContext;

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
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        database = new SchoolDatabase(params.getActivity());
        deleteTaskRunning = true;
        try {
            performDeleteOperation();
        } finally {
            database.close();
        }
        deleteRequestDiscarded = false;
        deleteTaskRunning = false;
    }

    private void performDeleteOperation() throws IllegalArgumentException {
        switch (request) {
            case AssignmentFragment.DELETE_REQUEST:
                doAssignmentDelete();
                break;
            case AssignmentFragment.MULTIPLE_DELETE_REQUEST:
            case SemesterFragment.MULTIPLE_DELETE_REQUEST:
            case ScheduledTimetableFragment.MULTIPLE_DELETE_REQUEST:
            case DaysFragment.MULTIPLE_DELETE_REQUEST:
            case ExamTimetableFragment.MULTIPLE_DELETE_REQUEST:
                doDataModelMultiDelete();
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
            default:
                throw new IllegalArgumentException(request + " is invalid");

        }
    }

    private void doDataModelMultiDelete() {
        List<DataModel> dataCache = new ArrayList<>();

        Integer[] itemIndices = params.getItemIndices();
        // Reverse array of indices in reversed order, because if the indices are not reversed,
        // an error occurs.
        Arrays.sort(itemIndices, Collections.reverseOrder());

        for (int i : itemIndices) {
            dataCache.add(params.getModelList().remove(i));
        }

        // Update UI
        EventBus.getDefault().post(new CountEvent(params.getModelList().size()));
        EventBus.getDefault().post(new MultiUpdateMessage(MultiUpdateMessage.EventType.REMOVE));

        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException exc) {
            for (int x = 0; x < dataCache.size(); x++) {
                params.getModelList().add(itemIndices[x], dataCache.get(x));
            }

            // Update UI
            EventBus.getDefault().post(new CountEvent(params.getModelList().size()));
            EventBus.getDefault().post(new MultiUpdateMessage(MultiUpdateMessage.EventType.INSERT));
        }

        if (!deleteRequestDiscarded) {
            // Delete the data model from SchoolDatabase using their positions
            String[] metadata;

            switch (params.getMetadataType()) {
                case NO_DATA:
                    metadata = new String[]{null, null, null, null};
                    break;
                case COURSE:
                    metadata = new String[]{params.getSemester(), null, null, null};
                    break;
                case EXAM:
                    ExamModel exam = (ExamModel) params.getModelList();
                    metadata = new String[]{null, exam.getWeek(), null, null};
                    break;
                case TIMETABLE:
                    metadata = new String[]{null, null, params.getTimetable(), null};
                    break;
                default:
                    throw new IllegalArgumentException("Specified metadata is invalid");
            }

            boolean isDeleted = database.deleteDataModels(params.getDataClass(),
                                                          metadata,
                                                          params.getPositionIndices(),
                                                          dataCache);

            if (isDeleted) {
                playAlertTone(appContext, Alert.DELETE);
                if (params.getModelList().isEmpty())
                    EventBus.getDefault().post(new EmptyListEvent());
            } else {
                Log.d(TAG, "Couldn't delete data models");
            }
        }
    }

    private void doImageMultiDelete() {
        List<Uri> uriCache = new ArrayList<>();

        Integer[] itemIndices = params.getItemIndices();
        List<Uri> mediaUris = params.getMediaUris();

        // Reverse array of indices in reversed order, because if the indices are not reversed,
        // an error occurs.
        Arrays.sort(itemIndices, Collections.reverseOrder());

        for (int i : itemIndices) {
            uriCache.add(mediaUris.remove(i));
        }

        // Update UI
        EventBus.getDefault().post(new MultiUpdateMessage2(MultiUpdateMessage2.EventType.REMOVE));

        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException exc) {
            for (int x = 0; x < uriCache.size(); x++) {
                mediaUris.add(itemIndices[x], uriCache.get(x));
            }

            // Update UI
            EventBus.getDefault()
                    .post(new MultiUpdateMessage2(MultiUpdateMessage2.EventType.INSERT));
        }

        if (!deleteRequestDiscarded) {
            // delete attached images from database
            boolean isDeleted
                    = database.deleteMultipleImages(params.getAssignmentPosition(), itemIndices);
            if (isDeleted) {
                playAlertTone(appContext, Alert.DELETE);
                if (mediaUris.isEmpty())
                    EventBus.getDefault().post(new EmptyListEvent());
            }
        }
    }

    private void doImageDelete() {
        List<Uri> mediaUris = params.getMediaUris();

        Uri uri = mediaUris.remove(params.getAdapterPosition());
        params.getAdapter().notifyItemRemoved(params.getAdapterPosition());
        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException exc) {
            mediaUris.add(params.getAdapterPosition(), uri);
            params.getAdapter().notifyItemInserted(params.getAdapterPosition());
        }

        if (!deleteRequestDiscarded) {
            String uris = database.deleteImage(params.getAdapterPosition(), uri);
            if (uris != null) {
                playAlertTone(appContext, Alert.DELETE);
                if (mediaUris.isEmpty())
                    EventBus.getDefault().post(new EmptyListEvent());
            }
            EventBus.getDefault().post(new UriUpdateEvent(params.getAdapterPosition(), uris));
        }
    }

    private void doExamDelete() {
        DataModel model = params.getModelList().get(params.getAdapterPosition());
        params.getModelList().remove(params.getAdapterPosition());
        params.getAdapter().notifyItemRemoved(params.getAdapterPosition());
        params.getAdapter().notifyDataSetChanged();
        EventBus.getDefault().post(new CountEvent(params.getModelList().size()));
            /*
            wait 3 seconds to perform actual delete request, because an undo request
            might also be issued, which delete request would have to be cancelled.
            The sleep timer is also synchronized   with the undo Snackbar's display timeout,
            which also is 3 seconds.
            */
        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            /*
            will be executed when the deleteRequestDiscarded property has been set,
            meaning an undo request
            */
            params.getModelList().add(params.getAdapterPosition(), model);
            params.getAdapter().notifyItemInserted(params.getAdapterPosition());
            params.getAdapter().notifyDataSetChanged();
            EventBus.getDefault().post(new CountEvent(params.getModelList().size()));
        }
        if (!deleteRequestDiscarded) {
            ExamModel examModel = (ExamModel) model;
            boolean isDeleted = database.deleteExamEntry(examModel, examModel.getWeek());
            if (isDeleted) {
                playAlertTone(appContext, Alert.DELETE);
                if (params.getModelList().isEmpty())
                    EventBus.getDefault().post(new EmptyListEvent());
            }
        }
    }

    private void doCourseDelete() {
        DataModel model = params.getModelList().get(params.getAdapterPosition());
        params.getModelList().remove(params.getAdapterPosition());
        params.getAdapter().notifyItemRemoved(params.getAdapterPosition());
        params.getAdapter().notifyDataSetChanged();
        EventBus.getDefault().post(new CountEvent(params.getModelList().size()));
            /*
            wait 3 seconds to perform actual delete request, because an undo request
            might also be issued, which delete request would have to be cancelled.
            The sleep timer is also synchronized with the undo Snackbar's display timeout,
            which also is 3 seconds.
            */
        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            /*
            will be executed when the deleteRequestDiscarded property has been set,
            meaning an undo request
            */
            params.getModelList().add(params.getAdapterPosition(), model);
            params.getAdapter().notifyItemInserted(params.getAdapterPosition());
            params.getAdapter().notifyDataSetChanged();
            EventBus.getDefault().post(new CountEvent(params.getModelList().size()));
        }
        if (!deleteRequestDiscarded) {
            CourseModel courseModel = (CourseModel) model;
            boolean isDeleted = database.deleteCourseEntry(courseModel, courseModel.getSemester());
            if (isDeleted) {
                playAlertTone(appContext, Alert.DELETE);
                if (params.getModelList().isEmpty())
                    EventBus.getDefault().post(new EmptyListEvent());
            }
        }
    }

    private void doAssignmentDelete() {
        DataModel model = params.getModelList().get(params.getAdapterPosition());
        AssignmentModel aModel = (AssignmentModel) model;
        aModel.setId(params.getAdapterPosition());
        EventBus.getDefault().post(new UpdateMessage(aModel, EventType.REMOVE));
            /*
            wait 3 seconds to perform actual delete request, because an undo request
            might also be issued, which delete request would have to be cancelled.
            The sleep timer is also synchronized with the undo Snackbar's display timeout,
            which also is 3 seconds.
            */
        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            EventBus.getDefault()
                    .post(new UpdateMessage((AssignmentModel) model, EventType.INSERT));
        }
        if (!deleteRequestDiscarded) {
            boolean isDeleted = database.deleteAssignmentEntry((AssignmentModel) model);
            if (isDeleted) {
                cancelAssignmentNotifier(params.getAssignmentData());
                playAlertTone(appContext, Alert.DELETE);
            }
        }
    }

    private void doTimeTableDelete() {
        DataModel model = params.getModelList().get(params.getAdapterPosition());
        params.getModelList().remove(params.getAdapterPosition());
        params.getAdapter().notifyItemRemoved(params.getAdapterPosition());
        params.getAdapter().notifyDataSetChanged();
        EventBus.getDefault().post(new CountEvent(params.getModelList().size()));
        // wait 3 seconds to perform actual delete request, because an undo request
        // might also be issued, which delete request would have to be cancelled.
        // The sleep timer is also synchronized with the undo Snackbar's display timeout,
        // which also is 3 seconds.
        // We don't want someone to change view of current timetable (if the RequestRunner is
        // being used in the TableFragment) when delete operation is still in progress,
        // as that would halt the delete operation !!!
        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            // will be executed when the deleteRequestDiscarded property has been set,
            // meaning an undo request
            params.getModelList().add(params.getAdapterPosition(), model);
            params.getAdapter().notifyItemInserted(params.getAdapterPosition());
            params.getAdapter().notifyDataSetChanged();
            EventBus.getDefault().post(new CountEvent(params.getModelList().size()));
        }
        if (!deleteRequestDiscarded) {
            boolean isDeleted;
            if (request.equals(ScheduledTimetableFragment.DELETE_REQUEST))
                isDeleted = database.deleteTimetableEntry((TimetableModel) model,
                                                          SchoolDatabase.SCHEDULED_TIMETABLE);
            else isDeleted
                    = database.deleteTimetableEntry((TimetableModel) model, params.getTimetable());
            if (isDeleted) {
                if (params.getModelList().isEmpty())
                    EventBus.getDefault().post(new EmptyListEvent());
                if (request.equals(ScheduledTimetableFragment.DELETE_REQUEST))
                    cancelScheduledTimetableNotifier((TimetableModel) model);
                else cancelTimetableNotifier((TimetableModel) model);
                playAlertTone(appContext, Alert.DELETE);
            }
        }
    }

    private void cancelTimetableNotifier(TimetableModel timetable) {
        String[] t = timetable.getStartTime().split(":");
        timetable.setDay(params.getTimetable());

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

        AlarmManager manager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        Intent timetableIntent = new Intent(appContext, TimetableNotifier.class);
        timetableIntent.addCategory("com.projects.timely.timetable")
                .setAction("com.projects.timely.timetable.addAction")
                .setDataAndType(Uri.parse("content://com.projects.timely.add." + timeInMillis),
                                "com.projects.timely.dataType");

        PendingIntent pi = PendingIntent.getBroadcast(appContext, 555, timetableIntent,
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
        DataModel model = database.getAlarmAt(params.getAdapterPosition());
        params.getModelList().remove(params.getAdapterPosition());
        params.getAdapter().notifyItemRemoved(params.getAdapterPosition());
        // now notify list of item change so that it can invalidate current views.
        // This line fixed a bug for me and help me change the indicators :)
        params.getAdapter().notifyDataSetChanged();
            /*
            wait 3 seconds to perform actual delete request, because an undo request
            might also be issued, which delete request would have to be cancelled.
            The sleep timer is also synchronized with the undo Snackbar's display timeout,
            which also is 3 seconds.
            */
        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            /*
            will be executed when the deleteRequestDiscarded property has been set,
            meaning an undo request
            */
            params.getModelList().add(params.getAdapterPosition(), model);
            params.getAdapter().notifyItemInserted(params.getAdapterPosition());
            // now notify list of item change so that it can invalidate current views.
            // This line fixed a bug for me and help me change the indicators :)
            params.getAdapter().notifyDataSetChanged();
        }
        if (!deleteRequestDiscarded) {
            boolean isDeleted = database.deleteAlarmEntry((AlarmModel) model);
            if (isDeleted) {
                cancelAlarm(); // if alarm was deleted, cancel alarm first, then ...
                playAlertTone(appContext, Alert.DELETE);
                if (params.getModelList().isEmpty())
                    EventBus.getDefault().post(new EmptyListEvent());
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
        playAlertTone(appContext, Alert.UNDO);
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

    private void cancelScheduledTimetableNotifier(TimetableModel timetable) {
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

        AlarmManager manager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        Intent timetableIntent = new Intent(appContext, ScheduledTaskNotifier.class);
        timetableIntent.addCategory("com.projects.timely.scheduled")
                .setAction("com.projects.timely.scheduled.addAction")
                .setDataAndType(
                        Uri.parse("content://com.projects.timely.scheduled.add." + timeInMillis),
                        "com.projects.timely.scheduled.dataType");

        PendingIntent pi = PendingIntent.getBroadcast(appContext, 1156, timetableIntent,
                                                      PendingIntent.FLAG_CANCEL_CURRENT);
        pi.cancel();
        manager.cancel(pi);
    }

    private void cancelAssignmentNotifier(AssignmentModel data) {
        Intent notifyIntentCurrent = new Intent(appContext, SubmissionNotifier.class);
        notifyIntentCurrent
                .addCategory(appContext.getPackageName() + ".category")
                .setAction(appContext.getPackageName() + ".update")
                .setDataAndType(Uri.parse("content://" + appContext.getPackageName()),
                                data.toString());

        Intent notifyIntentPrevious = new Intent(appContext, Reminder.class);
        notifyIntentPrevious
                .addCategory(appContext.getPackageName() + ".category")
                .setAction(appContext.getPackageName() + ".update")
                .setDataAndType(Uri.parse("content://" + appContext.getPackageName()),
                                data.toString());

        PendingIntent assignmentPiPrevious
                = PendingIntent.getBroadcast(appContext, 147, notifyIntentPrevious,
                                             PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent assignmentPiCurrent
                = PendingIntent.getBroadcast(appContext, 141, notifyIntentCurrent,
                                             PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager manager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        assignmentPiCurrent.cancel();
        manager.cancel(assignmentPiCurrent);
        assignmentPiPrevious.cancel();
        manager.cancel(assignmentPiPrevious);
    }

    public void cancelAlarm() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(params.getAlarmTime()[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(params.getAlarmTime()[1]));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        boolean isNextDay = System.currentTimeMillis() > calendar.getTimeInMillis();
        long alarmMillis = isNextDay ? calendar.getTimeInMillis() + TimeUnit.DAYS.toMillis(1)
                                     : calendar.getTimeInMillis();

        Intent alarmReceiverIntent = new Intent(appContext, AlarmReceiver.class);
        alarmReceiverIntent.putExtra("Label", params.getAlarmLabel());
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
                = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmPI = PendingIntent.getBroadcast(appContext,
                                                           1189765,
                                                           alarmReceiverIntent,
                                                           PendingIntent.FLAG_CANCEL_CURRENT);
        alarmPI.cancel();
        alarmManager.cancel(alarmPI);
    }

    public RequestRunner setRequestParams(RequestParams params) {
        this.params = params;
        // Use the application's context to prevent app crash
        this.appContext = this.params.getActivity().getApplicationContext();
        return this;
    }

    /**
     * Special Builder class implementation to make code readable
     */
    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        private RequestParams requestParams = new RequestParams();

        public RequestParams getParams() {
            return requestParams;
        }

        public Builder setOwnerContext(FragmentActivity mActivity) {
            requestParams.setActivity(mActivity);
            return this;
        }

        public Builder setAdapterPosition(int adapterPosition) {
            requestParams.setAdapterPosition(adapterPosition);
            return this;
        }

        public Builder setAdapter(RecyclerView.Adapter<?> adapter) {
            requestParams.setAdapter(adapter);
            return this;
        }

        public Builder setModelList(List<DataModel> dList) {
            requestParams.setModelList(dList);
            return this;
        }

        public Builder setAlarmLabel(String alarmLabel) {
            requestParams.setAlarmLabel(alarmLabel);
            return this;
        }

        public Builder setAlarmTime(String[] alarmTime) {
            requestParams.setAlarmTime(alarmTime);
            return this;
        }

        public Builder setAssignmentData(AssignmentModel assignmentData) {
            requestParams.setAssignmentData(assignmentData);
            return this;
        }

        public Builder setTimetable(String timetable) {
            requestParams.setTimetable(timetable);
            return this;
        }

        public Builder setMediaUris(List<Uri> mediaUris) {
            requestParams.setMediaUris(mediaUris);
            return this;
        }


        public Builder setAssignmentPosition(int assignmentPosition) {
            requestParams.setAssignmentPosition(assignmentPosition);
            return this;
        }

        public Builder setDataProvider(Class<? extends DataModel> dataClass) {
            requestParams.setDataClass(dataClass);
            return this;
        }

        public Builder setItemIndices(Integer[] itemIndices) {
            requestParams.setItemIndices(itemIndices);
            return this;
        }

        public Builder setPositionIndices(Integer[] positionIndices) {
            requestParams.setPositionIndices(positionIndices);
            return this;
        }

        public Builder setCourseSemester(String semester) {
            requestParams.setSemester(semester);
            return this;
        }

        public Builder setMetadataType(RequestParams.MetaDataType metadataType) {
            requestParams.setMetadataType(metadataType);
            return this;
        }
    }
}