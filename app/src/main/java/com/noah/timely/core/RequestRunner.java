package com.noah.timely.core;

import static com.noah.timely.util.Utility.Alert;
import static com.noah.timely.util.Utility.deleteTaskRunning;
import static com.noah.timely.util.Utility.playAlertTone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Process;

import androidx.fragment.app.FragmentActivity;

import com.noah.timely.alarms.AAUpdateMessage;
import com.noah.timely.alarms.AlarmListFragment;
import com.noah.timely.alarms.AlarmModel;
import com.noah.timely.alarms.AlarmReceiver;
import com.noah.timely.assignment.AUpdateMessage;
import com.noah.timely.assignment.AssignmentFragment;
import com.noah.timely.assignment.AssignmentModel;
import com.noah.timely.assignment.ImageViewerActivity;
import com.noah.timely.assignment.MultiUpdateMessage2;
import com.noah.timely.assignment.Reminder;
import com.noah.timely.assignment.SubmissionNotifier;
import com.noah.timely.assignment.UUpdateMessage;
import com.noah.timely.assignment.UriUpdateEvent;
import com.noah.timely.courses.CUpdateMessage;
import com.noah.timely.courses.CourseModel;
import com.noah.timely.courses.CourseRowHolder;
import com.noah.timely.courses.SemesterFragment;
import com.noah.timely.exam.EUpdateMessage;
import com.noah.timely.exam.ExamModel;
import com.noah.timely.exam.ExamRowHolder;
import com.noah.timely.exam.ExamTimetableFragment;
import com.noah.timely.gallery.Image;
import com.noah.timely.scheduled.SUpdateMessage;
import com.noah.timely.scheduled.ScheduledTaskNotifier;
import com.noah.timely.scheduled.ScheduledTimetableFragment;
import com.noah.timely.timetable.DaysFragment;
import com.noah.timely.timetable.TUpdateMessage;
import com.noah.timely.timetable.TimetableModel;
import com.noah.timely.timetable.TimetableNotifier;
import com.noah.timely.todo.TDUpdateMessage;
import com.noah.timely.todo.TodoListFragment;
import com.noah.timely.todo.TodoListRowHolder;
import com.noah.timely.todo.TodoModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Thread to handle all delete requests
 */
public class RequestRunner extends Thread {
    public static final int WAIT_TIME = 3000;
    private boolean deleteRequestDiscarded;
    private String request;
    private SchoolDatabase database;
    private RequestParams params;
    private Context appContext;

    /**
     * Use {@link RequestRunner#createInstance()} instead, to get the instance of the
     * <strong>RequestRunner</strong>. This convenience method is just to improve readability.
     */
    private RequestRunner() {
    }

    /**
     * @return an instance of A <strong>RequestRunner</strong>
     */
    public static RequestRunner createInstance() {
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
            case AssignmentFragment.MULTIPLE_DELETE_REQUEST:
            case SemesterFragment.MULTIPLE_DELETE_REQUEST:
            case ScheduledTimetableFragment.MULTIPLE_DELETE_REQUEST:
            case DaysFragment.MULTIPLE_DELETE_REQUEST:
            case ExamTimetableFragment.MULTIPLE_DELETE_REQUEST:
            case TodoListFragment.MULTIPLE_DELETE_REQUEST:
                doDataModelMultiDelete();
                break;
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
            case ImageViewerActivity.DELETE_REQUEST:
                doImageDelete();
                break;
            case ImageViewerActivity.MULTIPLE_DELETE_REQUEST:
                doImageMultiDelete();
                break;
            case TodoListRowHolder.DELETE_REQUEST:
                doTodoDelete();
                break;
            default:
                throw new IllegalArgumentException(request + " is invalid");
        }
    }

    private void doDataModelMultiDelete() {
        List<DataModel> dataCache = new ArrayList<>();
        Integer[] itemIndices = params.getItemIndices();
        // Reverse order of items in array of indices, because if the indices are not reversed, an error occurs.
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
            // metadata: [0] = semester; [1] = exam; [2] = timetable; ALL_NULL = Assignment
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
                if (params.getModelList().isEmpty()) EventBus.getDefault().post(new EmptyListEvent());
            }
        }
    }

    private void doTodoDelete() {
        DataModel model = params.getModelList().get(params.getAdapterPosition());
        params.getModelList().remove(params.getAdapterPosition());
        int pos = params.getPagePosition();
        EventBus.getDefault().post(new TDUpdateMessage((TodoModel) model, TDUpdateMessage.EventType.REMOVE));
            /*
            wait 3 seconds to perform actual delete request, because an undo request might also be issued, which
            delete request would have to be cancelled. The sleep timer is also synchronized   with the undo
            Snackbar's display timeout, which also is 3 seconds.
            */
        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            /*
            will be executed when the deleteRequestDiscarded property has been set,
            meaning an undo request
            */
            params.getModelList().add(params.getAdapterPosition(), model);
            EventBus.getDefault().post(new TDUpdateMessage((TodoModel) model, TDUpdateMessage.EventType.INSERT));
        }
        if (!deleteRequestDiscarded) {
            TodoModel examModel = (TodoModel) model;
            if (database.deleteTodo(model)) {
                playAlertTone(appContext, Alert.DELETE);
                if (params.getModelList().isEmpty()) EventBus.getDefault().post(new EmptyListEvent());
            }
        }
    }

    private void doImageMultiDelete() {
        // data cache
        List<Uri> uriCache = new ArrayList<>();
        List<Image> imgCache = new ArrayList<>();

        Integer[] itemIndices = params.getItemIndices();
        List<Uri> mediaUris = params.getMediaUris();
        List<Image> images = params.getImageList();
        // Reverse array of indices in reversed order, because if the indices are not reversed, an error occurs.
        Arrays.sort(itemIndices, Collections.reverseOrder());

        for (int i : itemIndices) {
            uriCache.add(mediaUris.remove(i));
            imgCache.add(images.remove(i));
        }

        // Update UI
        EventBus.getDefault().post(new MultiUpdateMessage2(MultiUpdateMessage2.EventType.REMOVE));

        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException exc) {
            for (int x = 0; x < uriCache.size(); x++) {
                mediaUris.add(itemIndices[x], uriCache.get(x));
                images.add(itemIndices[x], imgCache.get(x));
            }
            // Update UI
            EventBus.getDefault().post(new MultiUpdateMessage2(MultiUpdateMessage2.EventType.INSERT));
        }

        if (!deleteRequestDiscarded) {
            // delete attached images from database
            boolean isDeleted = database.deleteMultipleImages(params.getAssignmentPosition(), itemIndices);
            if (isDeleted) {
                playAlertTone(appContext, Alert.DELETE);
                if (mediaUris.isEmpty()) EventBus.getDefault().post(new EmptyListEvent());
            }
        }
    }

    private void doImageDelete() {
        List<Uri> mediaUris = params.getMediaUris();
        List<Image> images = params.getImageList();

        Uri uri = mediaUris.remove(params.getAdapterPosition());
        Image image = images.remove(params.getAdapterPosition());

        int changePos = params.getAdapterPosition();
        EventBus.getDefault().post(new UUpdateMessage(uri, image, changePos, UUpdateMessage.EventType.REMOVE));

        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException exc) {
            mediaUris.add(params.getAdapterPosition(), uri);
            images.add(params.getAdapterPosition(), image);
            EventBus.getDefault().post(new UUpdateMessage(uri, image, changePos, UUpdateMessage.EventType.INSERT));
        }

        if (!deleteRequestDiscarded) {
            String uris = database.deleteImage(params.getAdapterPosition(), uri);
            if (uris != null) {
                playAlertTone(appContext, Alert.DELETE);
                if (mediaUris.isEmpty()) EventBus.getDefault().post(new EmptyListEvent());
            }
            EventBus.getDefault().post(new UriUpdateEvent(params.getAdapterPosition(), uris));
        }
    }

    private void doExamDelete() {
        DataModel model = params.getModelList().get(params.getAdapterPosition());
        params.getModelList().remove(params.getAdapterPosition());
        int pos = params.getPagePosition();
        EventBus.getDefault().post(new EUpdateMessage((ExamModel) model, EUpdateMessage.EventType.REMOVE, pos));
            /*
            wait 3 seconds to perform actual delete request, because an undo request might also be issued, which
            delete request would have to be cancelled. The sleep timer is also synchronized   with the undo
            Snackbar's display timeout, which also is 3 seconds.
            */
        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            /*
            will be executed when the deleteRequestDiscarded property has been set,
            meaning an undo request
            */
            params.getModelList().add(params.getAdapterPosition(), model);
            EventBus.getDefault().post(new EUpdateMessage((ExamModel) model, EUpdateMessage.EventType.INSERT, pos));
        }
        if (!deleteRequestDiscarded) {
            ExamModel examModel = (ExamModel) model;
            boolean isDeleted = database.deleteExamEntry(examModel, examModel.getWeek());
            if (isDeleted) {
                playAlertTone(appContext, Alert.DELETE);
                if (params.getModelList().isEmpty()) EventBus.getDefault().post(new EmptyListEvent());
            }
        }
    }

    private void doCourseDelete() {
        DataModel model = params.getModelList().get(params.getAdapterPosition());
        params.getModelList().remove(params.getAdapterPosition());
        int pos = params.getPagePosition();

        EventBus.getDefault().post(new CUpdateMessage((CourseModel) model, CUpdateMessage.EventType.REMOVE, pos));
         /*
            wait 3 seconds to perform actual delete request, because an undo request might also be issued, which
            delete request would have to be cancelled. The sleep timer is also synchronized with the undo Snackbar's
            display timeout, which also is 3 seconds.
         */
        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            // will be executed when the deleteRequestDiscarded property has been set, meaning an undo request
            params.getModelList().add(params.getAdapterPosition(), model);
            EventBus.getDefault().post(new CUpdateMessage((CourseModel) model, CUpdateMessage.EventType.REMOVE, pos));
        }
        if (!deleteRequestDiscarded) {
            CourseModel courseModel = (CourseModel) model;
            boolean isDeleted = database.deleteCourseEntry(courseModel, courseModel.getSemester());
            if (isDeleted) {
                playAlertTone(appContext, Alert.DELETE);
                if (params.getModelList().isEmpty()) EventBus.getDefault().post(new EmptyListEvent());
            }
        }
    }

    private void doAssignmentDelete() {
        DataModel model = params.getModelList().get(params.getAdapterPosition());
        AssignmentModel aModel = (AssignmentModel) model;
        aModel.setId(params.getAdapterPosition());
        EventBus.getDefault().post(new AUpdateMessage(aModel, AUpdateMessage.EventType.REMOVE));
          /*
            wait 3 seconds to perform actual delete request, because an undo request might also be issued, which
            delete request would have to be cancelled. The sleep timer is also synchronized with the undo Snackbar's
            display timeout, which also is 3 seconds.
         */
        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            EventBus.getDefault().post(new AUpdateMessage(aModel, AUpdateMessage.EventType.INSERT));
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
        int pagePosition = params.getPagePosition();
        if (request.equals(ScheduledTimetableFragment.DELETE_REQUEST))
            EventBus.getDefault().post(new SUpdateMessage((TimetableModel) model, SUpdateMessage.EventType.REMOVE));
        else
            EventBus.getDefault().post(new TUpdateMessage((TimetableModel) model, pagePosition,
                                                          TUpdateMessage.EventType.REMOVE));
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
            if (request.equals(ScheduledTimetableFragment.DELETE_REQUEST))
                EventBus.getDefault().post(new SUpdateMessage((TimetableModel) model, SUpdateMessage.EventType.INSERT));
            else
                EventBus.getDefault().post(new TUpdateMessage((TimetableModel) model, pagePosition,
                                                              TUpdateMessage.EventType.INSERT));
        }
        if (!deleteRequestDiscarded) {
            boolean isDeleted;
            if (request.equals(ScheduledTimetableFragment.DELETE_REQUEST))
                isDeleted = database.deleteTimetableEntry((TimetableModel) model, SchoolDatabase.SCHEDULED_TIMETABLE);

            else isDeleted = database.deleteTimetableEntry((TimetableModel) model, params.getTimetable());

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
        timetableIntent.addCategory("com.noah.timely.timetable")
                       .setAction("com.noah.timely.timetable.addAction")
                       .setDataAndType(Uri.parse("content://com.noah.timely.add." + timeInMillis),
                                       "com.noah.timely.dataType");

        PendingIntent pi = PendingIntent.getBroadcast(appContext, 555, timetableIntent,
                                                      PendingIntent.FLAG_CANCEL_CURRENT);
        pi.cancel();
        manager.cancel(pi);
    }

    private void doAlarmDelete() {
        int changePos = params.getAdapterPosition();
        DataModel model = database.getAlarmAt(changePos);
        AlarmModel alarm = (AlarmModel) model;
        params.getModelList().remove(changePos);
        // now notify list of item change so that it can invalidate current views.
        // This line fixed a bug for me and help me change the indicators :)
        EventBus.getDefault().post(new AAUpdateMessage(alarm, changePos, AAUpdateMessage.EventType.REMOVE));
            /*
            wait 3 seconds to perform actual delete request, because an undo request
            might also be issued, which delete request would have to be cancelled.
            The sleep timer is also synchronized with the undo Snackbar's display timeout,
            which also is 3 seconds.
            */
        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            // will be executed when the deleteRequestDiscarded property has been set,  meaning an undo request
            params.getModelList().add(changePos, alarm);
            // now notify list of item change so that it can invalidate current views.
            // This line fixed a bug for me and help me change the indicators :)
            EventBus.getDefault().post(new AAUpdateMessage(alarm, changePos, AAUpdateMessage.EventType.INSERT));
        }
        if (!deleteRequestDiscarded) {
            boolean isDeleted = database.deleteAlarmEntry(alarm);
            if (isDeleted) {
                if (!alarm.isRepeated()) cancelNonRepeatingAlarm();
                else cancelRepeatingAlarm();
                playAlertTone(appContext, Alert.DELETE);
                if (params.getModelList().isEmpty()) EventBus.getDefault().post(new EmptyListEvent());
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

        AlarmManager manager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        Intent timetableIntent = new Intent(appContext, ScheduledTaskNotifier.class);
        timetableIntent.addCategory("com.noah.timely.scheduled")
                       .setAction("com.noah.timely.scheduled.addAction")
                       .setDataAndType(Uri.parse("content://com.noah.timely.scheduled.add." + timeInMillis),
                                       "com.noah.timely.scheduled.dataType");

        PendingIntent pi = PendingIntent.getBroadcast(appContext, 1156, timetableIntent,
                                                      PendingIntent.FLAG_CANCEL_CURRENT);
        pi.cancel();
        manager.cancel(pi);
    }

    private void cancelAssignmentNotifier(AssignmentModel data) {
        Intent notifyIntentCurrent = new Intent(appContext, SubmissionNotifier.class);
        notifyIntentCurrent.addCategory(appContext.getPackageName() + ".category")
                           .setAction(appContext.getPackageName() + ".update")
                           .setDataAndType(Uri.parse("content://" + appContext.getPackageName()), data.toString());

        Intent notifyIntentPrevious = new Intent(appContext, Reminder.class);
        notifyIntentPrevious.addCategory(appContext.getPackageName() + ".category")
                            .setAction(appContext.getPackageName() + ".update")
                            .setDataAndType(Uri.parse("content://" + appContext.getPackageName()), data.toString());

        PendingIntent assignmentPiPrevious
                = PendingIntent.getBroadcast(appContext, 147, notifyIntentPrevious, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent assignmentPiCurrent
                = PendingIntent.getBroadcast(appContext, 141, notifyIntentCurrent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager manager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        assignmentPiCurrent.cancel();
        manager.cancel(assignmentPiCurrent);
        assignmentPiPrevious.cancel();
        manager.cancel(assignmentPiPrevious);
    }

    public void cancelNonRepeatingAlarm() {
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
        alarmReceiverIntent.addCategory("com.noah.timely.alarm.category");
        alarmReceiverIntent.setAction("com.noah.timely.alarm.cancel");
        alarmReceiverIntent.setDataAndType(Uri.parse("content://com.noah.timely/Alarms/alarm" + alarmMillis),
                                           "com.noah.timely.alarm.dataType");

        AlarmManager alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);

        PendingIntent alarmPI = PendingIntent.getBroadcast(appContext, 11789, alarmReceiverIntent,
                                                           PendingIntent.FLAG_CANCEL_CURRENT);
        alarmPI.cancel();
        alarmManager.cancel(alarmPI);
    }

    // This calculates the days between now and the day needed which is represented by closest repeat day.
    private int getNextRepeatingAlarmDistance() {
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return (Calendar.SATURDAY + getClosestRepeatDay(dayOfWeek) - dayOfWeek) % Calendar.SATURDAY;
    }

    // gets the closest repeat day making code more easier to understand. Returns -1 if no day was set to repeat.
    private int getClosestRepeatDay(int from) {
        Boolean[] selectedDays = params.getAlarmRepeatDays();
        if (from < 0 || from > 7) throw new IllegalArgumentException("Invalid day " + from);
        int result = -1;
        int nextSearch = from;
        // search from sunday to saturday
        for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
            if (selectedDays[nextSearch - 1]) {
                result = nextSearch;
                break;
            }
            nextSearch = nextSearch == Calendar.SATURDAY ? Calendar.SUNDAY : ++nextSearch;
        }

        return result;
    }

    public void cancelRepeatingAlarm() {
        Calendar calendar = Calendar.getInstance();
        String[] time = params.getAlarmTime();

        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(time[1]));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DATE, getNextRepeatingAlarmDistance());

        boolean isNextDay = System.currentTimeMillis() > calendar.getTimeInMillis();

        long alarmMillis = isNextDay ? calendar.getTimeInMillis() + TimeUnit.DAYS.toMillis(1)
                                     : calendar.getTimeInMillis();

        Intent alarmReceiverIntent = new Intent(appContext, AlarmReceiver.class);
        alarmReceiverIntent.putExtra("Label", params.getAlarmLabel());
        // This is just used to prevent cancelling all pending intent, because when PendingIntent#cancel is called,
        // all pending intent that matches the intent supplied to Intent#filterEquals (and it returns true), will be
        // cancelled because there was no difference between the intents. So this code segment was used to provide a
        // distinguishing effect.
        alarmReceiverIntent.addCategory("com.noah.timely.alarm.category");
        alarmReceiverIntent.setAction("com.noah.timely.alarm.cancel");
        alarmReceiverIntent.setDataAndType(Uri.parse("content://com.noah.timely/Alarms/alarm" + alarmMillis),
                                           "com.noah.timely.alarm.dataType");

        AlarmManager alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);

        PendingIntent alarmPI = PendingIntent.getBroadcast(appContext, 11789, alarmReceiverIntent,
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
        private final RequestParams requestParams = new RequestParams();

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

        public Builder setAlarmRepeatDays(Boolean[] alarmRepeatDays) {
            requestParams.setAlarmRepeatDays(alarmRepeatDays);
            return this;
        }

        public Builder setPagePosition(int position) {
            requestParams.setPagePosition(position);
            return this;
        }

        public Builder setImageList(List<Image> imageList) {
            requestParams.setImageList(imageList);
            return this;
        }
    }
}