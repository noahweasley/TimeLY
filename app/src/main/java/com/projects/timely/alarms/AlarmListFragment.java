package com.projects.timely.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.os.ConfigurationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.projects.timely.R;
import com.projects.timely.core.AppUtils.Alert;
import com.projects.timely.core.DataModel;
import com.projects.timely.core.EmptyListEvent;
import com.projects.timely.core.SchoolDatabase;
import com.projects.timely.core.TimeRefreshEvent;
import com.projects.timely.error.ErrorDialog;
import com.projects.timely.util.ThreadUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Locale;

import static com.projects.timely.alarms.AlarmReceiver.ALARM_POS;
import static com.projects.timely.alarms.AlarmReceiver.REPEAT_DAYS;
import static com.projects.timely.core.AppUtils.isUserPreferred24Hours;
import static com.projects.timely.core.AppUtils.playAlertTone;

@SuppressWarnings({"ConstantConditions"})
public class AlarmListFragment extends Fragment {
    public static final String DELETE_REQUEST = "delete alarm";
    FragmentActivity mActivity;
    private ConstraintLayout no_alarm_view;
    private LinkedList<DataModel> aList;
    private AlarmAdapter alarmAdapter;
    private final Calendar calendar = Calendar.getInstance();
    private SchoolDatabase database;
    private CoordinatorLayout coordinator;
    private ProgressBar indeterminateProgress;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mActivity = getActivity();
        database = new SchoolDatabase(mActivity);
        alarmAdapter = new AlarmAdapter();
        aList = new LinkedList<>();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        database.close();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        return inflater.inflate(R.layout.fragment_alarm_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle state) {

        coordinator = view.findViewById(R.id.coordinator);
        no_alarm_view = view.findViewById(R.id.no_alarm_view);
        RecyclerView rV_AlarmList = view.findViewById(R.id.alarm_list);
        indeterminateProgress = view.findViewById(R.id.indeterminateProgress);

        ThreadUtils.runBackgroundTask(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            aList = (LinkedList<DataModel>) database.getAlarms();
            if (isAdded())
                getActivity().runOnUiThread(() -> {
                    boolean empty = aList.isEmpty();
                    no_alarm_view.setVisibility(empty ? View.VISIBLE : View.GONE);

                    indeterminateProgress.animate()
                                         .scaleX(0.0f)
                                         .scaleY(0.0f)
                                         .setDuration(1000);

                    alarmAdapter.notifyDataSetChanged();
                });
        });

        FloatingActionButton fab_add_new = view.findViewById(R.id.add_alarm);
        fab_add_new.setOnClickListener(new TimeManager());

        rV_AlarmList.setHasFixedSize(true);
        rV_AlarmList.setAdapter(alarmAdapter);
        rV_AlarmList.addItemDecoration(
                new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        rV_AlarmList.setLayoutManager(new LinearLayoutManager(getActivity()));

///////////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                               //
//   Note:  Code commented out because a user requested but code remains for code re-usability   //
//                                                                                               //
///////////////////////////////////////////////////////////////////////////////////////////////////
//        ItemTouchHelper deleteSwiper = new ItemTouchHelper(new ItemTouchHelper.Callback() {    //
//            @Override                                                                          //
//            public int getMovementFlags(@NonNull RecyclerView recyclerView,                    //
//                                        @NonNull RecyclerView.ViewHolder viewHolder) {         //
//                return makeMovementFlags(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT);     //
//            }                                                                                  //
//                                                                                               //
//            @Override                                                                          //
//            public boolean onMove(@NonNull RecyclerView recyclerView,                          //
//                                  @NonNull RecyclerView.ViewHolder viewHolder,                 //
//                                  @NonNull RecyclerView.ViewHolder target) {                   //
//                // Not implemented, return false                                               //
//                return false;                                                                  //
//                                                                                               //
//            }                                                                                  //
//                                                                                               //
//            @Override                                                                          //
//            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { //
//                // post a delete request on the assignment database                            //
//                RequestRunner runner = RequestRunner.getInstance();                            //
//                Snackbar snackbar = Snackbar.make(coordinator, "Assignment Deleted",           //
//                                                  Snackbar.LENGTH_LONG)                        //
//                        .setAction("undo", v -> runner.undoRequest())                          //
//                        .setActionTextColor(Color.YELLOW);                                     //
//                                                                                               //
//                snackbar.show();                                                               //
//                                                                                               //
//                int pos = viewHolder.getAbsoluteAdapterPosition();                             //
//                String[] elements = database.getElementaryAlarmDataAt(pos);                    //
//                                                                                               //
//                RequestRunner.Builder builder = new RequestRunner.Builder();                   //
//                builder.setOwnerContext(getActivity())                                         //
//                        .setAdapterPosition(viewHolder.getAbsoluteAdapterPosition())           //
//                        .setAdapter(alarmAdapter)                                              //
//                        .setModelList(aList)                                                   //
//                        .setAlarmTime(elements);                                               //
//                                                                                               //
//                runner.setRequestParams(builder.getParams())                                   //
//                        .runRequest(DELETE_REQUEST);                                           //
//            }                                                                                  //
//        });                                                                                    //
//                                                                                               //
//        deleteSwiper.attachToRecyclerView(rV_AlarmList);                                       //
////////////////////////////////////////////////////////////////////////////////////////////////////

    }

    @Override
    public void onDetach() {
        EventBus.getDefault().unregister(this);
        super.onDetach();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doEmptyResponse(EmptyListEvent alarmEvent) {
        no_alarm_view.setVisibility(aList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doLayoutRefresh(TimeRefreshEvent refreshEvent) {
        if (alarmAdapter != null)
            alarmAdapter.notifyDataSetChanged();
    }

    private class TimeManager implements View.OnClickListener, TimePickerDialog.OnTimeSetListener {

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            boolean is24 = isUserPreferred24Hours(getContext());
            Calendar calendar = Calendar.getInstance();
            new TimePickerDialog(getContext(),
                                 this,
                                 calendar.get(Calendar.HOUR_OF_DAY),
                                 calendar.get(Calendar.MINUTE),
                                 is24)
                    .show();
        }

        /**
         * z
         * Called when the user is done setting a new time and the dialog has
         * closed.
         *
         * @param view      the view associated with this listener
         * @param hourOfDay the hour that was set
         * @param minute    the minute that was set
         */
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            boolean is24 = isUserPreferred24Hours(getContext());
            Configuration config = getResources().getConfiguration();
            Locale currentLocale = ConfigurationCompat.getLocales(config).get(0);

            SimpleDateFormat timeFormat24 = new SimpleDateFormat("HH:mm", currentLocale);
            SimpleDateFormat timeFormat12 = new SimpleDateFormat("hh:mm aa", currentLocale);

            String time = timeFormat24.format(calendar.getTime());

            if (database.isAlarmPresent(time)) {
                String sTime = is24 ? timeFormat24.format(calendar.getTime())
                                    : timeFormat12.format(calendar.getTime());

                ErrorDialog.Builder errorBuilder = new ErrorDialog.Builder();
                errorBuilder.setShowSuggestions(true)
                            .setDialogMessage(String.format("Alarm for %s exists", sTime))
                            .setSuggestionCount(2)
                            .setSuggestion1("Consider editing former alarm")
                            .setSuggestion2("Delete former alarm");

                new ErrorDialog().showErrorMessage(getContext(), errorBuilder.build());

                return;
            }

            scheduleAlarm(is24, time);

        }

        private void scheduleAlarm(boolean is24, String alarmTime) {

            int lastId = database.getLastAlarmId();
            int DB_initialPos;
            AlarmModel newAlarm = new AlarmModel();
            newAlarm.setOn(true);
            newAlarm.setTime(alarmTime);
            newAlarm.setVibrate(true);
            newAlarm.setSnoozed(false);
            newAlarm.setRepeatDays(new Boolean[]{true, true, true, true, true, true, true});
            newAlarm.setPosition((DB_initialPos = ++lastId));
            newAlarm.setInitialPosition(DB_initialPos);

            boolean isAlarmAdded = database.addAlarm(newAlarm);

            // The amount of milliseconds to the next day
            final int NEXT_DAY = 1000 * 60 * 60 * 24;

            String[] time = alarmTime.split(":");
            int hh = Integer.parseInt(time[0]);
            int mm = Integer.parseInt(time[1]);
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hh);
            calendar.set(Calendar.MINUTE, mm);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            boolean isNextDay = System.currentTimeMillis() > calendar.getTimeInMillis();

            if (isAlarmAdded) {
                aList.add(newAlarm);

                boolean isForenoon = hh >= 0 && hh < 12;

                String formattedHrAM = String.format(Locale.US, "%02d", (hh == 0 ? 12 : hh));
                String formattedHrPM = String.format(Locale.US, "%02d",
                                                     (hh % 12 == 0 ? 12 : hh % 12));
                String formattedMinAM = String.format(Locale.US, "%02d", mm) + " AM";
                String formattedMinPM = String.format(Locale.US, "%02d", mm) + " PM";

                String _12H = isForenoon ? formattedHrAM + ":" + formattedMinAM
                                         : formattedHrPM + ":" + formattedMinPM;

                String message = isNextDay ? "Alarm set for: " + (is24 ? alarmTime
                                                                       : _12H) + " tomorrow"
                                           : "Alarm set for: " + (is24 ? alarmTime
                                                                       : _12H) + " today";

                long alarmMillis = isNextDay ? calendar.getTimeInMillis() + NEXT_DAY
                                             : calendar.getTimeInMillis();

                Intent alarmReceiverIntent = new Intent(mActivity, AlarmReceiver.class);
                alarmReceiverIntent.putExtra(ALARM_POS, DB_initialPos);
                alarmReceiverIntent.putExtra("Time", alarmTime);

                boolean[] repeatDays = convertToPrimitive(newAlarm.getRepeatDays());
                alarmReceiverIntent.putExtra(REPEAT_DAYS, repeatDays);

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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    // alarm has to be triggered even when device is in idle or doze mode.
                    // This alarm is very important
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                                                               alarmMillis, alarmPI);
                    } else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmMillis, alarmPI);
                    }
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmMillis, alarmPI);
                }

                // notify the user using the changes in the UI that an alarm was added

                alarmAdapter.notifyItemInserted(aList.size() - 1);
                no_alarm_view.setVisibility(View.GONE);

                // play alert tone if user activated alert tones in user settings
                Toast alert = Toast.makeText(mActivity, message, Toast.LENGTH_LONG);
                int yOffset = getResources().getInteger(R.integer.toast_y_offset);
                alert.setGravity(Gravity.CENTER_HORIZONTAL, 0, yOffset);
                alert.show();
                playAlertTone(mActivity.getApplicationContext(), Alert.ALARM);

            } else
                // This shouldn't show at all, it was only used in application testing
                Toast.makeText(mActivity, "A problem occurred", Toast.LENGTH_SHORT).show();
        }

    }

    // convert to primitive boolean array
    private boolean[] convertToPrimitive(Boolean[] source) {
        boolean[] dest = new boolean[source.length];
        for (int i = 0; i < source.length; i++) {
            dest[i] = source[i];
        }
        return dest;
    }

    // The layout for the alarms occupying the alarm_list RecyclerView
    class AlarmAdapter extends RecyclerView.Adapter<AlarmListHolder> {

        @NonNull
        @Override
        public AlarmListHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View rowView = getLayoutInflater().inflate(R.layout.alarms_row, viewGroup, false);
            return new AlarmListHolder(rowView);
        }

        @Override
        public void onBindViewHolder(@NonNull AlarmListHolder viewHolder, int pos) {
            viewHolder.with(mActivity, coordinator, aList, database, alarmAdapter).bindView();
        }

        @Override
        public int getItemCount() {
            return aList.size();
        }
    }
}