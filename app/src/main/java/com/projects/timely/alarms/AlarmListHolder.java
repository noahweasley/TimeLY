package com.projects.timely.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.os.ConfigurationCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.kevalpatel.ringtonepicker.RingtonePickerDialog;
import com.kevalpatel.ringtonepicker.RingtoneUtils;
import com.projects.timely.R;
import com.projects.timely.core.DataModel;
import com.projects.timely.core.RequestRunner;
import com.projects.timely.core.SchoolDatabase;
import com.projects.timely.core.ThreadUtils;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.projects.timely.alarms.AlarmReceiver.ALARM_POS;
import static com.projects.timely.core.AppUtils.Alert;
import static com.projects.timely.core.AppUtils.isUserPreferred24Hours;
import static com.projects.timely.core.AppUtils.playAlertTone;

@SuppressWarnings("ConstantConditions")
class AlarmListHolder extends RecyclerView.ViewHolder {
    public static final String DELETE_REQUEST = "delete alarm";
    // array of drawables to be cached, declared here for easy access
    private static final int[] DRAWABLES = {
            R.drawable.round_bb,
            R.drawable.round_ol,
            R.drawable.round_tm,
            R.drawable.round_gd,
            R.drawable.round_pu,
            R.drawable.round_gl,
            R.drawable.round_bd,
            R.drawable.round_od,
            R.drawable.round_rl
    };
    private static ExpansionDetails details;
    private final ExpandableLayout detailLayout;
    private final ImageView expandStatus;
    private final Calendar calendar = Calendar.getInstance();
    private final SwitchCompat alarmStatus;
    private final TextView tv_alarmTime, btn_rngPicker, am_pm;
    private FragmentActivity mActivity;
    private FragmentManager mgr;
    private CoordinatorLayout coordinator;
    private List<DataModel> alarmModelList;
    private AlarmListFragment.AlarmAdapter alarmAdapter;
    private final RecyclerView rV_buttonRow;
    private SchoolDatabase database;
    private final CheckBox cbx_Repeat, cbx_Vibrate;
    private final ImageButton btn_deleteRow;
    private final TextView tv_label;
    private final View decoration;
    private AlarmModel thisAlarm;

    AlarmListHolder(@NonNull View rootView) {
        super(rootView);
        rootView.setActivated(false);
        decoration = rootView.findViewById(R.id.decoration);
        alarmStatus = rootView.findViewById(R.id.alarm_status);
        tv_alarmTime = rootView.findViewById(R.id.alarm_list_time);
        rV_buttonRow = rootView.findViewById(R.id.row_of_buttons);
        cbx_Repeat = rootView.findViewById(R.id.checkbox_repeat);
        cbx_Vibrate = rootView.findViewById(R.id.checkbox_vibrate);
        btn_deleteRow = rootView.findViewById(R.id.delete_row);
        tv_label = rootView.findViewById(R.id.tv_label);
        // The view to be expanded | collapsed
        detailLayout = rootView.findViewById(R.id.detail_layout);
        expandStatus = rootView.findViewById(R.id.expand_status);
        btn_rngPicker = rootView.findViewById(R.id.ringtone_picker);
        am_pm = rootView.findViewById(R.id.am_pm);

        registerAllListeners(rootView);
    }

    private void registerAllListeners(View rootView) {
        // now deal with when the user wants to expand or collapse one of the alarm row
        rootView.setOnClickListener(v -> {
            detailLayout.toggle();
            details = new ExpansionDetails();
            details.setPreviousExpandedPos(getAbsoluteAdapterPosition());
            details.setExpanded(detailLayout.isExpanded());
            // When detailLayout expansion state has changed, rotate arrow and change
            // background color.
            final boolean isExpanded = detailLayout.isExpanded();
            rootView.setActivated(isExpanded);
            expandStatus.animate()
                        .rotation(isExpanded ? 180 : 0)
                        .setInterpolator(new BounceInterpolator())
                        .setDuration(detailLayout.getDuration());
        });

        // display the days to repeat if repeat checkbox is checked
        alarmStatus.setOnClickListener(v -> {
            if (database != null) {
                // Disable alarm if checkbox is unchecked but still keep the PendingIntent alive
                String ss = tv_label.getText().toString();
                String label = ss.equals("Label") ? null : ss;
                String[] time = thisAlarm.getTime().split(":");

                if (alarmStatus.isChecked()) rescheduleAlarm(label, time);
                else cancelAlarm(label, time);
                // now update the alarm status when user toggles the state of the switch
                database.updateAlarmState(getAbsoluteAdapterPosition(), alarmStatus.isChecked());
            }
        });

        tv_label.setOnClickListener(this::onLabelClick);

        tv_alarmTime.setOnClickListener(new TimeManager());

        btn_rngPicker.setOnClickListener(this::onSelectRingtone);

        cbx_Repeat.setOnClickListener(v -> {
            final int dataPos   // dataPos now refers to the alarms id in the database
                    = getAbsoluteAdapterPosition();
            rV_buttonRow.setVisibility(cbx_Repeat.isChecked() ? View.VISIBLE : View.GONE);
            if (database != null) {
                // now update the current alarm's repeat status when user toggles the current
                // state of the checkbox
                database.updateAlarmRepeatStatus(dataPos, cbx_Repeat.isChecked());
            }
        });

        cbx_Vibrate.setOnClickListener(v -> {
            final int dataPos   // dataPos now refers to the alarms id in the database
                    = getAbsoluteAdapterPosition();
            if (database != null) {
                // now update the current alarm's vibrate status when user toggles the current
                // state of the checkbox
                boolean isUpdated
                        = database.updateAlarmVibrateStatus(dataPos, cbx_Vibrate.isChecked());
            }
        });

        // delete a row on button click, but, post a delete request first
        btn_deleteRow.setOnClickListener(v -> {
            // cancel the pending  alarm first before properly deleting the row
            String ss = tv_label.getText().toString();
            String label = ss.equals("Label") ? null : ss;
            String[] time = thisAlarm.getTime().split(":");

            RequestRunner runner = RequestRunner.createInstance();
            RequestRunner.Builder builder = new RequestRunner.Builder();
            builder.setOwnerContext(mActivity)
                   .setAdapterPosition(this.getAbsoluteAdapterPosition())
                   .setAdapter(alarmAdapter)
                   .setModelList(alarmModelList)
                   .setAlarmLabel(label)
                   .setAlarmTime(time);

            runner.setRequestParams(builder.getParams())
                  .runRequest(DELETE_REQUEST);

            Snackbar snackBar = Snackbar.make(coordinator, "Alarm Deleted", Snackbar.LENGTH_LONG);
            snackBar.setActionTextColor(Color.YELLOW);
            snackBar.setAction("UNDO", x -> runner.undoRequest());
            snackBar.show();

        });
    }

    AlarmListHolder with(FragmentActivity activity, CoordinatorLayout coordinator,
                         List<DataModel> alarmModelList, SchoolDatabase database,
                         AlarmListFragment.AlarmAdapter alarmAdapter) {
        this.mActivity = activity;
        this.mgr = activity.getSupportFragmentManager();
        this.coordinator = coordinator;
        this.alarmModelList = alarmModelList;
        this.database = database;
        this.alarmAdapter = alarmAdapter;
        this.thisAlarm = (AlarmModel) alarmModelList.get(getAbsoluteAdapterPosition());
        return this;
    }

    void bindView() {
        if (details != null) {
            if (details.getPreviousExpandedPos() == getAbsoluteAdapterPosition()) {
                detailLayout.setExpanded(details.isExpanded(), false);
            }
        }
        AlarmModel thisAlarm = (AlarmModel) alarmModelList.get(getAbsoluteAdapterPosition());

        // bind data to views in row layout, as received from the database.
        // Use time format according to user settings.
        boolean is24 = isUserPreferred24Hours(mActivity);

        /////////////////////////////////////////////////////////////////////////////////////
        tv_label.setText(thisAlarm.getLabel());

        String[] splitStrings = thisAlarm.getTime().split(":");
        int hh = Integer.parseInt(splitStrings[0]);

        String mm = splitStrings[1];
        boolean isForenoon = hh >= 0 && hh < 12;
        String _24H = thisAlarm.getTime();

        String _12H = isForenoon ? String.format(Locale.US, "%02d", (hh == 0 ? 12 : hh)) + ":" + mm
                                 : String.format(Locale.US, "%02d",
                                                 (hh % 12 == 0 ? 12 : hh % 12)) + ":" + mm;

        String realTime = is24 ? _24H : _12H;
        tv_alarmTime.setText(realTime);
        if (is24) am_pm.setVisibility(View.GONE);
        else {
            am_pm.setVisibility(View.VISIBLE);
            am_pm.setText(isForenoon ? "AM" : "PM");
        }

        alarmStatus.setChecked(thisAlarm.isOn());
        cbx_Repeat.setChecked(thisAlarm.isRepeated());
        cbx_Vibrate.setChecked(thisAlarm.isVibrate());
        btn_rngPicker.setText(thisAlarm.getRingTone());
        int rowDrawable = DRAWABLES[getAbsoluteAdapterPosition() % DRAWABLES.length];
        decoration.setBackground(ContextCompat.getDrawable(mActivity, rowDrawable));

        rV_buttonRow.setVisibility(cbx_Repeat.isChecked() ? View.VISIBLE : View.GONE);
        rV_buttonRow.setHasFixedSize(true);
        rV_buttonRow.setLayoutManager(new LinearLayoutManager(mActivity,
                                                              LinearLayoutManager.HORIZONTAL,
                                                              false));
        rV_buttonRow.setAdapter(new ButtonListAdapter());
    }

    /*
     * When alarm is set, user has the option to toggle the alarm status with the switch in the
     * list, so when the event that occurs when switch is toggled is a request to cancel
     * the former alarm, just cancel it with this method !!
     */
    private void cancelAlarm(String label, String[] time) {

        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(time[1]));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        boolean isNextDay = System.currentTimeMillis() > calendar.getTimeInMillis();
        // The amount of milliseconds to the next day
        final int NEXT_DAY = 1000 * 60 * 60 * 24;

        long alarmMillis = isNextDay ? calendar.getTimeInMillis() + NEXT_DAY
                                     : calendar.getTimeInMillis();

        Intent alarmReceiverIntent = new Intent(mActivity, AlarmReceiver.class);
        alarmReceiverIntent.putExtra("Label", label);
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

    //  Updates the alarm pending intent extras
    private void updateAlarm(String label, String time) {
        String[] realTime = time.split(":");
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(realTime[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(realTime[1]));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        boolean isNextDay = System.currentTimeMillis() > calendar.getTimeInMillis();
        // The amount of milliseconds to the next day
        final int NEXT_DAY = 1000 * 60 * 60 * 24;

        long alarmMillis = isNextDay ? calendar.getTimeInMillis() + NEXT_DAY
                                     : calendar.getTimeInMillis();

        Intent alarmReceiverIntent = new Intent(mActivity, AlarmReceiver.class);
        if (label != null) {
            alarmReceiverIntent.putExtra("Label", label);
        }
        alarmReceiverIntent.putExtra(ALARM_POS, getAbsoluteAdapterPosition());
        alarmReceiverIntent.putExtra("Time", time);

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
                                                           PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // alarm has to be triggered even when device is in idle or doze mode.
                // This alarm is very important
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmMillis,
                                                       alarmPI);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmMillis, alarmPI);
            }
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmMillis, alarmPI);
        }
        // Also toggle the alarm status switch state to the ON state
        database.updateAlarmState(getAbsoluteAdapterPosition(), true);
    }

    /*
     * When alarm is set, user has the option to toggle the alarm status with the switch in the
     * list, so when the event that occurs when switch is toggled is a request to reschedule
     * the former alarm, just reschedule it with this method !!
     */
    private void rescheduleAlarm(String label, String[] time) {
        boolean is24 = isUserPreferred24Hours(mActivity);

        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(time[1]));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        boolean isNextDay = System.currentTimeMillis() > calendar.getTimeInMillis();
        // The amount of milliseconds to the next day
        long alarmMillis = isNextDay ? calendar.getTimeInMillis() + TimeUnit.DAYS.toMillis(1)
                                     : calendar.getTimeInMillis();

        Intent alarmReceiverIntent = new Intent(mActivity, AlarmReceiver.class);
        alarmReceiverIntent.putExtra("Label", label);
        alarmReceiverIntent.putExtra("Time", time[0] + time[1]);
        // This is just used to prevent cancelling all pending intent, because when
        // PendingIntent#cancel is called, all pending intent that matches the intent supplied to
        // Intent#filterEquals (and it returns true), will be cancelled because there was no
        // difference between the intents. So this code segment was used to provide a distinguishing
        // effect.
        alarmReceiverIntent.addCategory("com.projects.timely.alarm.category");
        alarmReceiverIntent.setAction("com.projects.timely.alarm.cancel");
        alarmReceiverIntent.setDataAndType(
                Uri.parse("content://com.projects.timely/Alarms/alarms" + alarmMillis),
                "com.projects.timely.alarm.dataType");

        AlarmManager alarmManager
                = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmPI = PendingIntent.getBroadcast(mActivity,
                                                           1189765,
                                                           alarmReceiverIntent,
                                                           PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmMillis, alarmPI);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmMillis, alarmPI);
        }

        int hh = Integer.parseInt(time[0]);
        int mm = Integer.parseInt(time[1]);
        boolean isAM = hh >= 0 && hh < 12;

        Resources aResources = tv_alarmTime.getContext().getResources();
        Configuration config = aResources.getConfiguration();
        Locale locale = ConfigurationCompat.getLocales(config).get(0);

        String formattedHrAM = String.format(locale, "%02d", (hh == 0 ? 12 : hh));
        String formattedHrPM = String.format(locale, "%02d", (hh % 12 == 0 ? 12 : hh % 12));
        String formattedMinAM = String.format(locale, "%02d", mm) + " AM";
        String formattedMinPM = String.format(locale, "%02d", mm) + " PM";

        String _12H = isAM ? formattedHrAM + ":" + formattedMinAM
                           : formattedHrPM + ":" + formattedMinPM;

        String alarmTime = time[0] + ":" + time[1];
        String message = isNextDay ? "Alarm set for: " + (is24 ? alarmTime : _12H) + " tomorrow"
                                   : "Alarm set for: " + (is24 ? alarmTime : _12H) + " today";

        Toast alert = Toast.makeText(mActivity, message, Toast.LENGTH_LONG);
        alert.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        alert.show();

        playAlertTone(mActivity.getApplicationContext(), Alert.ALARM);
    }

    private void onLabelClick(View v) {
        new EditTextDialog(mActivity)
                .prepareAndShow()
                .setActionListener(label -> {
                    tv_label.setText(label);
                    ThreadUtils.runBackgroundTask(() -> {
                        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                        database.updateAlarmLabel(getAbsoluteAdapterPosition(), label);
                        updateAlarm(label, thisAlarm.getTime());
                    });
                });
    }

    private void onSelectRingtone(View v) {
        RingtonePickerDialog.Builder builder = new RingtonePickerDialog.Builder(mActivity, mgr);
        builder.setCancelButtonText(android.R.string.cancel)
               .setPositiveButtonText(android.R.string.ok)
               .setTitle("Select Ringtone")
               .displaySilentRingtone(true)
               .displayDefaultRingtone(true)
               .setPlaySampleWhileSelection(true)
               .addRingtoneType(RingtonePickerDialog.Builder.TYPE_ALARM)
               .setListener((String ringtoneName, @Nullable Uri ringtoneUri) -> {
                   String ringtone = null;
                   if (ringtoneUri != null)
                       ringtone = RingtoneUtils.getRingtoneName(mActivity, ringtoneUri);
                   String actualName = ringtoneName.equals("Default") ? ringtone : ringtoneName;
                   btn_rngPicker.setText(actualName);
                   ThreadUtils.runBackgroundTask(() -> {
                       Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                       database.updateRingtone(getAbsoluteAdapterPosition(),
                                               ringtoneName,
                                               ringtoneUri);
                   });
               }).show();
    }

    private static class ExpansionDetails {
        private int previousExpandedPos;
        private boolean isExpanded;

        public int getPreviousExpandedPos() {
            return previousExpandedPos;
        }

        public void setPreviousExpandedPos(int previousExpandedPos) {
            this.previousExpandedPos = previousExpandedPos;
        }

        public boolean isExpanded() {
            return isExpanded;
        }

        public void setExpanded(boolean expanded) {
            isExpanded = expanded;
        }
    }

    // This class will handle everything related to alarms.
    // I also tried avoided naming this class; AlarmManager :)
    private class TimeManager implements View.OnClickListener, TimePickerDialog.OnTimeSetListener {

        /**
         * Called when the alarm_time text string has been clicked.
         *
         * @param v The alarm_time
         */
        @Override
        public void onClick(View v) {
            chooseTime();
        }

        // display the time picker dialog to set time for alarm
        private void chooseTime() {
            boolean isUserPreferred24HourView = isUserPreferred24Hours(mActivity);
            Calendar calendar = Calendar.getInstance();

            new TimePickerDialog(tv_alarmTime.getContext(),
                                 this,
                                 calendar.get(Calendar.HOUR_OF_DAY),
                                 calendar.get(Calendar.MINUTE),
                                 isUserPreferred24HourView)
                    .show();

        }

        /**
         * Called when the user is done setting a new time and the dialog has
         * closed.
         *
         * @param v         the view associated with this listener
         * @param hourOfDay the hour that was set
         * @param minute    the minute that was set
         */
        @Override
        public void onTimeSet(TimePicker v, int hourOfDay, int minute) {
            AlarmModel thisAlarm = (AlarmModel) database.getAlarmAt(getAbsoluteAdapterPosition());
            String ll = tv_label.getText().toString();
            String[] ts = thisAlarm.getTime().split(":");

            cancelAlarm(ll, ts);

            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            //////////////////////////////  Code duplication  ///////////////////////////////////

            boolean isUserPreferred24HourView = isUserPreferred24Hours(mActivity);

            Resources timePickerResources = v.getContext().getResources();
            Configuration config = timePickerResources.getConfiguration();
            Locale currentLocale = ConfigurationCompat.getLocales(config).get(0);

            SimpleDateFormat timeFormat24 = new SimpleDateFormat("HH:mm", currentLocale);
            SimpleDateFormat timeFormat12 = new SimpleDateFormat("hh:mm", currentLocale);

            String currentTime;

            currentTime = isUserPreferred24HourView ? timeFormat24.format(calendar.getTime())
                                                    : timeFormat12.format(calendar.getTime());
            ////////////////////////////////////////////////////////////////////////////////////////
            boolean isAM = hourOfDay >= 0 && hourOfDay < 12;
            tv_alarmTime.setText(currentTime);
            am_pm.setText(isAM ? "AM" : "PM");

            ThreadUtils.runBackgroundTask(() -> {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                updateAlarm(ll.equals("Label") ? null : ll, currentTime);
                database.updateTime(getAbsoluteAdapterPosition(), currentTime);
            });
            notify(v); // notify the user
        }

        // Notify user that he/she has changed the state of the alarm
        private void notify(TimePicker view) {
            long calendarTime = calendar.getTimeInMillis();
            boolean isNextDay = System.currentTimeMillis() > calendarTime;

            Resources timePickerResources = view.getContext().getResources();
            Configuration config = timePickerResources.getConfiguration();
            Locale currentLocale = ConfigurationCompat.getLocales(config).get(0);

            SimpleDateFormat timeFormat24 = new SimpleDateFormat("HH:mm", currentLocale);
            SimpleDateFormat timeFormat12 = new SimpleDateFormat("hh:mm:aa", currentLocale);

            boolean is24 = isUserPreferred24Hours(mActivity);

            String alarmTime = is24 ? timeFormat24.format(calendar.getTime())
                                    : timeFormat12.format(calendar.getTime());

            String message = isNextDay ? "Alarm set for: " + alarmTime + " tomorrow"
                                       : "Alarm set for: " + alarmTime + " today";
            Toast alert = Toast.makeText(mActivity, message, Toast.LENGTH_LONG);
            int yOffset = mActivity.getResources().getInteger(R.integer.toast_y_offset);
            alert.setGravity(Gravity.CENTER_HORIZONTAL, 0, yOffset);
            alert.show();

            playAlertTone(mActivity.getApplicationContext(), Alert.ALARM);
        }

    }

    class ButtonListAdapter extends RecyclerView.Adapter<ButtonListHolder> {

        @NonNull
        @Override
        public ButtonListHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new ButtonListHolder(
                    LayoutInflater.from(mActivity).inflate(R.layout.button_row, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ButtonListHolder viewHolder, int btn_pos) {
            viewHolder.with(btn_pos,
                            getAbsoluteAdapterPosition(),
                            database)
                      .bindView();
        }

        @Override
        public int getItemCount() {
            return 7;
        }
    }
}
