package com.noah.timely.alarms;

import static com.noah.timely.alarms.AlarmReceiver.ALARM_POS;
import static com.noah.timely.util.MiscUtil.Alert;
import static com.noah.timely.util.MiscUtil.isUserPreferred24Hours;
import static com.noah.timely.util.MiscUtil.playAlertTone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.os.ConfigurationCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.kevalpatel.ringtonepicker.RingtonePickerDialog;
import com.kevalpatel.ringtonepicker.RingtoneUtils;
import com.noah.timely.R;
import com.noah.timely.core.DataModel;
import com.noah.timely.core.RequestRunner;
import com.noah.timely.core.SchoolDatabase;
import com.noah.timely.util.LogUtils;
import com.noah.timely.util.ThreadUtils;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog.OnTimeSetListener;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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

   private final TextView btn_sunday, btn_monday, btn_tuesday, btn_wednesday, btn_thursday, btn_friday, btn_saturday;
   private final ExpandableLayout detailLayout;
   private final ImageView expandStatus;
   private final Calendar calendar = Calendar.getInstance();
   private final SwitchCompat alarmStatus;
   private final TextView tv_alarmTime, btn_rngPicker, am_pm;
   private final CheckBox cbx_Repeat, cbx_Vibrate;
   private final ImageButton btn_deleteRow;
   private final TextView tv_label;
   private final View decoration;
   private final ViewGroup vg_buttonRow;
   private FragmentActivity mActivity;
   private FragmentManager mgr;
   private CoordinatorLayout coordinator;
   private List<DataModel> alarmModelList;
   private SchoolDatabase database;
   private AlarmModel thisAlarm;
   private Boolean[] selectedDays = new Boolean[7];

   AlarmListHolder(@NonNull View rootView) {
      super(rootView);
      rootView.setActivated(false);

      decoration = rootView.findViewById(R.id.decoration);
      alarmStatus = rootView.findViewById(R.id.alarm_status);
      tv_alarmTime = rootView.findViewById(R.id.alarm_list_time);
      cbx_Repeat = rootView.findViewById(R.id.checkbox_repeat);
      cbx_Vibrate = rootView.findViewById(R.id.checkbox_vibrate);
      btn_deleteRow = rootView.findViewById(R.id.delete_row);
      tv_label = rootView.findViewById(R.id.tv_label);
      // The view to be expanded | collapsed
      detailLayout = rootView.findViewById(R.id.detail_layout);
      expandStatus = rootView.findViewById(R.id.expand_status);
      btn_rngPicker = rootView.findViewById(R.id.ringtone_picker);
      am_pm = rootView.findViewById(R.id.am_pm);
      vg_buttonRow = rootView.findViewById(R.id.button_row);
      btn_sunday = rootView.findViewById(R.id.sunday);
      btn_monday = rootView.findViewById(R.id.monday);
      btn_tuesday = rootView.findViewById(R.id.tuesday);
      btn_wednesday = rootView.findViewById(R.id.wednesday);
      btn_thursday = rootView.findViewById(R.id.thursday);
      btn_friday = rootView.findViewById(R.id.friday);
      btn_saturday = rootView.findViewById(R.id.saturday);

      registerAllListeners(rootView);
   }

   AlarmListHolder with(FragmentActivity activity, CoordinatorLayout coordinator, List<DataModel> alarmModelList,
                        SchoolDatabase database, AlarmListFragment.AlarmAdapter alarmAdapter) {
      this.mActivity = activity;
      this.mgr = activity.getSupportFragmentManager();
      this.coordinator = coordinator;
      this.alarmModelList = alarmModelList;
      this.database = database;
      this.thisAlarm = (AlarmModel) alarmModelList.get(getAbsoluteAdapterPosition());
      return this;
   }

   private void registerAllListeners(View rootView) {
      // repeat buttons
      btn_sunday.setOnClickListener(this::onRepeatButtonClick);
      btn_monday.setOnClickListener(this::onRepeatButtonClick);
      btn_tuesday.setOnClickListener(this::onRepeatButtonClick);
      btn_wednesday.setOnClickListener(this::onRepeatButtonClick);
      btn_thursday.setOnClickListener(this::onRepeatButtonClick);
      btn_friday.setOnClickListener(this::onRepeatButtonClick);
      btn_saturday.setOnClickListener(this::onRepeatButtonClick);

      tv_label.setOnClickListener(this::onLabelClick);

      tv_alarmTime.setOnClickListener(new TimeManager());

      btn_rngPicker.setOnClickListener(this::onSelectRingtone);

      // now deal with when the user wants to expand or collapse one of the alarm row
      rootView.setOnClickListener(v -> {
         detailLayout.toggle();
         // When detailLayout expansion state has changed, rotate arrow and change background color.
         final boolean isExpanded = detailLayout.isExpanded();
         int position = isExpanded ? getAbsoluteAdapterPosition() : -1;
         rootView.setActivated(isExpanded);
         expandStatus.animate()
                     .rotation(isExpanded ? 180 : 0)
                     .setDuration(detailLayout.getDuration());
      });

      // display the days to repeat if repeat checkbox is checked
      alarmStatus.setOnClickListener((view) -> {
         if (database != null) {
            // disable alarm if checkbox is unchecked but still keep the PendingIntent alive
            String ss = thisAlarm.getLabel();
            String label = ss.equals("Label") ? null : ss;
            int position = getAbsoluteAdapterPosition();

            String[] alarmSnoozeState = database.getAlarmSnoozeStateAt(position);
            boolean isAlarmSnoozed = Boolean.parseBoolean(alarmSnoozeState[0]);

            String[] time;
            if (isAlarmSnoozed) {
               time = alarmSnoozeState[1].split(":");
            } else {
               time = thisAlarm.getTime().split(":");
            }

            boolean alarmOn = alarmStatus.isChecked();
            thisAlarm.setOn(alarmOn);
            // re-schedule alarm based on alarm ON state
            if (alarmOn) {
               if (!thisAlarm.isRepeated()) rescheduleNonRepeatingAlarm(label, time, true);
               else rescheduleRepeatingPendingAlarm2(label, time);

            } else {
               int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
               if (!thisAlarm.isRepeated()) cancelNonRepeatingAlarm(label, time);
               else cancelNextRepeatingAlarm(label, getNextRepeatingAlarmDistance(dayOfWeek, null));
            }
            // now update the alarm status when user toggles the state of the switch
            database.updateAlarmState(position, alarmOn);
         }
      });

      // companion operation to onClick handler
      cbx_Repeat.setOnCheckedChangeListener((v, chk) -> {
         thisAlarm.setRepeated(chk);
         final int dataPos = getAbsoluteAdapterPosition(); // the alarms id in the database
         vg_buttonRow.setVisibility(chk ? View.VISIBLE : View.GONE);
         if (database != null) database.updateAlarmRepeatStatus(dataPos, chk);

      });
      // companion operation to onCheckedChange handler
      cbx_Repeat.setOnClickListener(v -> {
         boolean repeated = cbx_Repeat.isChecked();
         final int dataPos = getAbsoluteAdapterPosition(); // the alarms id in the database
         // hide or show rows of button
         thisAlarm.setRepeated(repeated);
         // cancel previous repeating alarm, while leaving any alarm that was set to be triggered the day the
         // alarm was set or the day after, only if time has passed
         if (thisAlarm.isOn()) {
            if (repeated) updateRepeatingPendingAlarm();
            else updateRepeatingPendingAlarm2();
         }
      });

      cbx_Vibrate.setOnClickListener(v -> {
         boolean checked = cbx_Vibrate.isChecked();
         final int dataPos = getAbsoluteAdapterPosition(); // the alarms id in the database
         thisAlarm.setVibrate(checked);
         if (database != null) {
            // now update the current alarm's vibrate status when user toggles the current state of the checkbox
            boolean isUpdated = database.updateAlarmVibrateStatus(dataPos, checked);
         }
      });

      // delete a row on button click, but, post a delete request first
      btn_deleteRow.setOnClickListener(v -> {
         // bug exists while deleting multiple rows very quickly
         if (!SchoolDatabase.isDeleteTaskRunning()) {
            // cancel the pending  alarm first before properly deleting the row
            String ss = thisAlarm.getLabel();
            String label = ss.equals("Label") ? null : ss;
            String[] time = thisAlarm.getTime().split(":");

            RequestRunner runner = RequestRunner.createInstance();
            RequestRunner.Builder builder = new RequestRunner.Builder();
            builder.setOwnerContext(mActivity)
                   .setAdapterPosition(this.getAbsoluteAdapterPosition())
                   .setModelList(alarmModelList)
                   .setAlarmRepeatDays(selectedDays)
                   .setAlarmLabel(label)
                   .setAlarmTime(time);

            runner.setRequestParams(builder.getParams())
                  .runRequest(DELETE_REQUEST);

            Snackbar snackBar = Snackbar.make(coordinator, "Alarm Deleted", Snackbar.LENGTH_LONG);
            snackBar.setActionTextColor(Color.YELLOW);
            snackBar.setAction("UNDO", x -> runner.undoRequest());
            snackBar.show();

         } else mActivity.runOnUiThread(() -> Toast.makeText(mActivity, "Wait a bit :(", Toast.LENGTH_SHORT).show());
      });
   }

   private void onRepeatButtonClick(View view) {
      int buttonId = view.getId();
      int alarmPosition = getAbsoluteAdapterPosition();
      // cancel any previous alarm for this current item position ONLY, before proceeding
      if (thisAlarm.isOn()) cancelAllPendingAlarms();
      // then update the repeat days array
      if (buttonId == R.id.sunday) {
         selectedDays[0] = !selectedDays[0];
         btn_sunday.setBackgroundResource(!selectedDays[0] ? R.drawable.disabled_round_button
                                                           : R.drawable.enabled_round_button);
      } else if (buttonId == R.id.monday) {
         selectedDays[1] = !selectedDays[1];
         btn_monday.setBackgroundResource(!selectedDays[1] ? R.drawable.disabled_round_button
                                                           : R.drawable.enabled_round_button);
      } else if (buttonId == R.id.tuesday) {
         selectedDays[2] = !selectedDays[2];
         btn_tuesday.setBackgroundResource(!selectedDays[2] ? R.drawable.disabled_round_button
                                                            : R.drawable.enabled_round_button);
      } else if (buttonId == R.id.wednesday) {
         selectedDays[3] = !selectedDays[3];
         btn_wednesday.setBackgroundResource(!selectedDays[3] ? R.drawable.disabled_round_button
                                                              : R.drawable.enabled_round_button);
      } else if (buttonId == R.id.thursday) {
         selectedDays[4] = !selectedDays[4];
         btn_thursday.setBackgroundResource(!selectedDays[4] ? R.drawable.disabled_round_button
                                                             : R.drawable.enabled_round_button);
      } else if (buttonId == R.id.friday) {
         selectedDays[5] = !selectedDays[5];
         btn_friday.setBackgroundResource(!selectedDays[5] ? R.drawable.disabled_round_button
                                                           : R.drawable.enabled_round_button);
      } else if (buttonId == R.id.saturday) {
         selectedDays[6] = !selectedDays[6];
         btn_saturday.setBackgroundResource(!selectedDays[6] ? R.drawable.disabled_round_button
                                                             : R.drawable.enabled_round_button);
      }

      // check if all the buttons are cleared and deactivate repeat
      int sCount = 0;
      for (boolean selected : selectedDays) {
         if (selected) break;
         else if (++sCount == 7) cbx_Repeat.setChecked(false);
      }

      boolean updated = database.updateSelectedDays(alarmPosition, selectedDays);
      // don't update alarm when the current selected alarm is still off
      if (updated && thisAlarm.isOn()) updateRepeatingPendingAlarm();
   }

   // updated the next alarm trigger time based on the repeatStatus. When repeatStatus is true, then alarm would be
   // triggered according to the checked repeat days. If it is not, alarm would be triggered the exact closest time
   // as specified by alarm time.
   private void updateRepeatingPendingAlarm() {
      // This gets the current day of the week as of TODAY / NOW
      Calendar calendar = Calendar.getInstance();
      int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
      int closestRepeatDay = getClosestRepeatDay(dayOfWeek);
      int alarmDistance = getNextRepeatingAlarmDistance(dayOfWeek, closestRepeatDay);

      String[] time = thisAlarm.getTime().split(":");
      calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
      calendar.set(Calendar.MINUTE, Integer.parseInt(time[1]));
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);

      String ss = thisAlarm.getLabel();
      String label = ss.equals("Label") ? null : ss;

      if (closestRepeatDay != -1 && alarmDistance != 0) {
         calendar.add(Calendar.DATE, alarmDistance);
         rescheduleRepeatingPendingAlarm(calendar, thisAlarm.getTime(), alarmDistance);
      } else {
         rescheduleNonRepeatingAlarm(label, time, false);
      }
   }

   // A modified version of updateRepeatPendingAlarm. But ignored the repeat days while still scheduling a normal
   // alarm. Any alarm that was previously set; to be triggered in two or more days, that alarm is cancelled
   // immediately
   private void updateRepeatingPendingAlarm2() {
      // This gets the current day of the week as of TODAY / NOW
      Calendar calendar = Calendar.getInstance();
      int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
      int closestRepeatDay = getClosestRepeatDay(dayOfWeek);
      int alarmDistance = getNextRepeatingAlarmDistance(dayOfWeek, closestRepeatDay);

      String[] time = thisAlarm.getTime().split(":");
      calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
      calendar.set(Calendar.MINUTE, Integer.parseInt(time[1]));
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);

      String ss = thisAlarm.getLabel();
      String label = ss.equals("Label") ? null : ss;

      if (alarmDistance >= 2) {
         calendar.add(Calendar.DATE, alarmDistance);
         cancelNextRepeatingAlarm2(calendar, thisAlarm.getTime(), alarmDistance);
         rescheduleNonRepeatingAlarm(label, time, false);
      }
   }

   // This calculates the days between now and the day needed which is represented by closest repeat day.
   // to avoid unnecessary calling of getClosestRepeatDay(int), closestDaySeed was used. so if closestDaySeed != null,
   // then that closestDaySeed would be used. The seed is used to prevent unnecessary calculations.
   private int getNextRepeatingAlarmDistance(int dayOfWeek, Integer closestDaySeed) {
      if (closestDaySeed != null)
         if ((closestDaySeed < -1 || closestDaySeed > 7))
            throw new IllegalArgumentException("Incorrect day seed " + closestDaySeed);
      int closestRepeatDay;
      if (closestDaySeed == null)
         closestRepeatDay = getClosestRepeatDay(dayOfWeek);
      else closestRepeatDay = closestDaySeed;
      return (Calendar.SATURDAY + closestRepeatDay - dayOfWeek) % Calendar.SATURDAY;
   }

   // gets the closest repeat day making code more easier to understand. Returns -1 if no day was set to repeat.
   private int getClosestRepeatDay(int from) {
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

   private void cancelNextRepeatingAlarm2(Calendar calendar, String time, int alarmDistance) {
      long alarmMillis = calendar.getTimeInMillis();

      Intent alarmReceiverIntent = new Intent(mActivity, AlarmReceiver.class);
      String label = tv_label.getText().toString();
      alarmReceiverIntent.putExtra("Label", label);
      alarmReceiverIntent.putExtra(ALARM_POS, getAbsoluteAdapterPosition());

      alarmReceiverIntent.addCategory("com.noah.timely.alarm.category");
      alarmReceiverIntent.setAction("com.noah.timely.alarm.cancel");
      alarmReceiverIntent.setDataAndType(Uri.parse("content://com.noah.timely/Alarms/alarm" + alarmMillis),
                                         "com.noah.timely.alarm.dataType");

      AlarmManager alarmManager = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE);
      PendingIntent alarmPI = PendingIntent.getBroadcast(mActivity, 11789, alarmReceiverIntent,
                                                         PendingIntent.FLAG_UPDATE_CURRENT);
      alarmPI.cancel();
      alarmManager.cancel(alarmPI);
   }

   /*
    * This function is a little bit more complex than cancelAlarm#(String, String[]). It aims at cancelling the
    * previously  set repeating alarm.
    */
   private void cancelAllPendingAlarms() {
      Calendar calendar = Calendar.getInstance();
      String[] time = thisAlarm.getTime().split(":");
      String label = thisAlarm.getLabel();
      // when any of the repeat alarm day buttons state are toggled, get the next alarm trigger time, which
      // would obviously be the closest day, then cancel the alarm for that day only because only that alarm
      // really exists.
      if (thisAlarm.isRepeated()) {
         int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
         int closestRepeatDay = getClosestRepeatDay(dayOfWeek);
         cancelNextRepeatingAlarm(label, getNextRepeatingAlarmDistance(dayOfWeek, closestRepeatDay));
      } else {
         cancelNonRepeatingAlarm(label, time);
      }

   }

   private void cancelNextRepeatingAlarm(String label, int nextTriggerDate) {
      Calendar calendar = Calendar.getInstance();
      String[] time = thisAlarm.getTime().split(":");

      calendar.setTimeInMillis(System.currentTimeMillis());
      calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
      calendar.set(Calendar.MINUTE, Integer.parseInt(time[1]));
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);
      calendar.add(Calendar.DATE, nextTriggerDate);

      long alarmMillis = calendar.getTimeInMillis();

      LogUtils.debug(this, "cancelled next: " + new Date(alarmMillis));
      Intent alarmReceiverIntent = new Intent(mActivity, AlarmReceiver.class);
      alarmReceiverIntent.putExtra("Label", label);

      alarmReceiverIntent.putExtra(ALARM_POS, getAbsoluteAdapterPosition());
      // This is just used to prevent cancelling all pending intent, because when
      // PendingIntent#cancel is called, all pending intent that matches the intent supplied to
      // Intent#filterEquals (and it returns true), will be cancelled because there was no
      // difference between the intents. So this code segment was used to provide a distinguishing
      // effect.
      alarmReceiverIntent.addCategory("com.noah.timely.alarm.category");
      alarmReceiverIntent.setAction("com.noah.timely.alarm.cancel");
      alarmReceiverIntent.setDataAndType(Uri.parse("content://com.noah.timely/Alarms/alarm" + alarmMillis),
                                         "com.noah.timely.alarm.dataType");

      AlarmManager alarmManager = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE);
      PendingIntent alarmPI = PendingIntent.getBroadcast(mActivity, 11789, alarmReceiverIntent,
                                                         PendingIntent.FLAG_CANCEL_CURRENT);
      alarmPI.cancel();
      alarmManager.cancel(alarmPI);

   }

   /*
    * When alarm is set, user has the option to toggle the alarm status with the switch in the
    * list, so when the event that occurs when switch is toggled is a request to cancel
    * the former alarm, just cancel it with this method !!
    */
   private void cancelNonRepeatingAlarm(String label, String[] time) {
      String t = TextUtils.join(":", time);
      calendar.setTimeInMillis(System.currentTimeMillis());
      calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
      calendar.set(Calendar.MINUTE, Integer.parseInt(time[1]));
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);

      boolean isNextDay = System.currentTimeMillis() > calendar.getTimeInMillis();

      long alarmMillis = isNextDay ? calendar.getTimeInMillis() + TimeUnit.DAYS.toMillis(1)
                                   : calendar.getTimeInMillis();

      Intent alarmReceiverIntent = new Intent(mActivity, AlarmReceiver.class);
      alarmReceiverIntent.putExtra("Label", label);
      alarmReceiverIntent.putExtra(ALARM_POS, getAbsoluteAdapterPosition());
      // This is just used to prevent cancelling all pending intent, because when
      // PendingIntent#cancel is called, all pending intent that matches the intent supplied to
      // Intent#filterEquals (and it returns true), will be cancelled because there was no
      // difference between the intents. So this code segment was used to provide a distinguishing
      // effect.
      alarmReceiverIntent.addCategory("com.noah.timely.alarm.category");
      alarmReceiverIntent.setAction("com.noah.timely.alarm.cancel");
      alarmReceiverIntent.setDataAndType(Uri.parse("content://com.noah.timely/Alarms/alarm" + alarmMillis),
                                         "com.noah.timely.alarm.dataType");

      AlarmManager alarmManager = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE);
      PendingIntent alarmPI = PendingIntent.getBroadcast(mActivity, 11789, alarmReceiverIntent,
                                                         PendingIntent.FLAG_CANCEL_CURRENT);
      alarmPI.cancel();
      alarmManager.cancel(alarmPI);
   }

   private void rescheduleRepeatingPendingAlarm(Calendar calendar, String time, int alarmDistance) {
      long alarmMillis = calendar.getTimeInMillis();

      LogUtils.debug(this, "Scheduling next repeat: " + new Date(alarmMillis));
      Intent alarmReceiverIntent = new Intent(mActivity, AlarmReceiver.class);
      String label = tv_label.getText().toString();
      alarmReceiverIntent.putExtra("Label", label);
      alarmReceiverIntent.putExtra(ALARM_POS, getAbsoluteAdapterPosition());

      alarmReceiverIntent.addCategory("com.noah.timely.alarm.category");
      alarmReceiverIntent.setAction("com.noah.timely.alarm.cancel");
      alarmReceiverIntent.setDataAndType(Uri.parse("content://com.noah.timely/Alarms/alarm" + alarmMillis),
                                         "com.noah.timely.alarm.dataType");

      AlarmManager alarmManager = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE);
      PendingIntent alarmPI = PendingIntent.getBroadcast(mActivity, 11789, alarmReceiverIntent,
                                                         PendingIntent.FLAG_UPDATE_CURRENT);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // alarm has to be triggered even when device is in idle or doze mode.
            // This alarm is very important
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmMillis, alarmPI);
         } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmMillis, alarmPI);
         }
      } else {
         alarmManager.set(AlarmManager.RTC_WAKEUP, alarmMillis, alarmPI);
      }

      // notifications
      String[] splitTime = time.split(":");
      boolean is24 = isUserPreferred24Hours(mActivity);
      int hh = Integer.parseInt(splitTime[0]);
      int mm = Integer.parseInt(splitTime[1]);
      boolean isAM = hh >= 0 && hh < 12;

      String formattedHrAM = String.format(Locale.US, "%02d", (hh == 0 ? 12 : hh));
      String formattedHrPM = String.format(Locale.US, "%02d", (hh % 12 == 0 ? 12 : hh % 12));
      String formattedMinAM = String.format(Locale.US, "%02d", mm) + " AM";
      String formattedMinPM = String.format(Locale.US, "%02d", mm) + " PM";

      String _12H = isAM ? formattedHrAM + ":" + formattedMinAM
                         : formattedHrPM + ":" + formattedMinPM;

      String alarmTime = splitTime[0] + ":" + splitTime[1];
      String message = "Alarm set for: " + (is24 ? alarmTime : _12H)
              + (alarmDistance >= 2 ? " in " + alarmDistance + " days" : " tomorrow");

      mActivity.runOnUiThread(() -> {
         Toast alert = Toast.makeText(mActivity, message, Toast.LENGTH_LONG);
         alert.setGravity(Gravity.BOTTOM, 0, 100);
         alert.show();
      });
   }

   /*
    * When alarm is set, user has the option to toggle the alarm status with the switch in the
    * list, so when the event that occurs when switch is toggled is a request to reschedule
    * the former repeated alarm, just reschedule it with this method !!
    */
   private void rescheduleRepeatingPendingAlarm2(String label, String[] time) {
      // This gets the current day of the week as of TODAY / NOW
      Calendar calendar = Calendar.getInstance();

      int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
      int closestRepeatDay = getClosestRepeatDay(dayOfWeek);
      int alarmDistance = getNextRepeatingAlarmDistance(dayOfWeek, closestRepeatDay);

      calendar.setTimeInMillis(System.currentTimeMillis());
      calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
      calendar.set(Calendar.MINUTE, Integer.parseInt(time[1]));
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);
      calendar.add(Calendar.DATE, alarmDistance);

      long alarmMillis = calendar.getTimeInMillis();

      Intent alarmReceiverIntent = new Intent(mActivity, AlarmReceiver.class);
      alarmReceiverIntent.putExtra("Label", label);
      alarmReceiverIntent.putExtra(ALARM_POS, getAbsoluteAdapterPosition());

      alarmReceiverIntent.addCategory("com.noah.timely.alarm.category");
      alarmReceiverIntent.setAction("com.noah.timely.alarm.cancel");
      alarmReceiverIntent.setDataAndType(Uri.parse("content://com.noah.timely/Alarms/alarm" + alarmMillis),
                                         "com.noah.timely.alarm.dataType");

      AlarmManager alarmManager = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE);
      PendingIntent alarmPI = PendingIntent.getBroadcast(mActivity, 11789, alarmReceiverIntent,
                                                         PendingIntent.FLAG_UPDATE_CURRENT);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // alarm has to be triggered even when device is in idle or doze mode.
            // This alarm is very important
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmMillis, alarmPI);
         } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmMillis, alarmPI);
         }
      } else {
         alarmManager.set(AlarmManager.RTC_WAKEUP, alarmMillis, alarmPI);
      }

      // notifications
      boolean is24 = isUserPreferred24Hours(mActivity);
      int hh = Integer.parseInt(time[0]);
      int mm = Integer.parseInt(time[1]);
      boolean isAM = hh >= 0 && hh < 12;

      String formattedHrAM = String.format(Locale.US, "%02d", (hh == 0 ? 12 : hh));
      String formattedHrPM = String.format(Locale.US, "%02d", (hh % 12 == 0 ? 12 : hh % 12));
      String formattedMinAM = String.format(Locale.US, "%02d", mm) + " AM";
      String formattedMinPM = String.format(Locale.US, "%02d", mm) + " PM";

      String _12H = isAM ? formattedHrAM + ":" + formattedMinAM
                         : formattedHrPM + ":" + formattedMinPM;

      String alarmTime = time[0] + ":" + time[1];
      String message = "Alarm set for: " + (is24 ? alarmTime : _12H)
              + (alarmDistance >= 2 ? " in " + alarmDistance + " days" : " tomorrow");

      mActivity.runOnUiThread(() -> {
         Toast alert = Toast.makeText(mActivity, message, Toast.LENGTH_LONG);
         alert.setGravity(Gravity.BOTTOM, 0, 100);
         alert.show();
      });

   }

   // Updates the alarm pending intent extras
   private void updateAlarm(String time) {
      String[] realTime = time.split(":");
      String label = thisAlarm.getLabel();

      calendar.setTimeInMillis(System.currentTimeMillis());
      calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(realTime[0]));
      calendar.set(Calendar.MINUTE, Integer.parseInt(realTime[1]));
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);

      boolean isNextDay = System.currentTimeMillis() > calendar.getTimeInMillis();

      long alarmMillis = isNextDay ? calendar.getTimeInMillis() + TimeUnit.DAYS.toMillis(1)
                                   : calendar.getTimeInMillis();

      Intent alarmReceiverIntent = new Intent(mActivity, AlarmReceiver.class);
      alarmReceiverIntent.putExtra("Label", label);
      alarmReceiverIntent.putExtra(ALARM_POS, getAbsoluteAdapterPosition());

      alarmReceiverIntent.addCategory("com.noah.timely.alarm.category");
      alarmReceiverIntent.setAction("com.noah.timely.alarm.cancel");
      alarmReceiverIntent.setDataAndType(Uri.parse("content://com.noah.timely/Alarms/alarm" + alarmMillis),
                                         "com.noah.timely.alarm.dataType");

      AlarmManager alarmManager = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE);
      PendingIntent alarmPI = PendingIntent.getBroadcast(mActivity, 11789, alarmReceiverIntent,
                                                         PendingIntent.FLAG_UPDATE_CURRENT);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // alarm has to be triggered even when device is in idle or doze mode.
            // This alarm is very important
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmMillis, alarmPI);
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
   private void rescheduleNonRepeatingAlarm(String label, String[] time, boolean playSound) {

      calendar.setTimeInMillis(System.currentTimeMillis());
      calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
      calendar.set(Calendar.MINUTE, Integer.parseInt(time[1]));
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);

      boolean isNextDay = System.currentTimeMillis() > calendar.getTimeInMillis();
      // The amount of milliseconds to the next day
      long alarmMillis = isNextDay ? calendar.getTimeInMillis() + TimeUnit.DAYS.toMillis(1)
                                   : calendar.getTimeInMillis();

      String st = TextUtils.join(":", time);
      LogUtils.debug(this, "Scheduling non repeat: " + st);
      Intent alarmReceiverIntent = new Intent(mActivity, AlarmReceiver.class);
      alarmReceiverIntent.putExtra(ALARM_POS, getAbsoluteAdapterPosition());
      alarmReceiverIntent.putExtra("Label", label);     // This is just used to prevent cancelling all pending
      // intent, because when
      // PendingIntent#cancel is called, all pending intent that matches the intent supplied to
      // Intent#filterEquals (and it returns true), will be cancelled because there was no
      // difference between the intents. So this code segment was used to provide a distinguishing
      // effect.
      alarmReceiverIntent.addCategory("com.noah.timely.alarm.category");
      alarmReceiverIntent.setAction("com.noah.timely.alarm.cancel");
      alarmReceiverIntent.setDataAndType(Uri.parse("content://com.noah.timely/Alarms/alarms" + alarmMillis),
                                         "com.noah.timely.alarm.dataType");

      AlarmManager alarmManager = (AlarmManager) mActivity.getSystemService(Context.ALARM_SERVICE);
      PendingIntent alarmPI = PendingIntent.getBroadcast(mActivity, 11789, alarmReceiverIntent,
                                                         PendingIntent.FLAG_UPDATE_CURRENT);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Alarm has to be triggered even when device is in idle or doze mode.
            // This alarm is very important
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmMillis, alarmPI);
         } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmMillis, alarmPI);
         }
      } else {
         alarmManager.set(AlarmManager.RTC_WAKEUP, alarmMillis, alarmPI);
      }

      boolean is24 = isUserPreferred24Hours(mActivity);
      int hh = Integer.parseInt(time[0]);
      int mm = Integer.parseInt(time[1]);
      boolean isAM = hh >= 0 && hh < 12;

      String formattedHrAM = String.format(Locale.US, "%02d", (hh == 0 ? 12 : hh));
      String formattedHrPM = String.format(Locale.US, "%02d", (hh % 12 == 0 ? 12 : hh % 12));
      String formattedMinAM = String.format(Locale.US, "%02d", mm) + " AM";
      String formattedMinPM = String.format(Locale.US, "%02d", mm) + " PM";

      String _12H = isAM ? formattedHrAM + ":" + formattedMinAM : formattedHrPM + ":" + formattedMinPM;

      String alarmTime = time[0] + ":" + time[1];
      String message = isNextDay ? "Alarm set for: " + (is24 ? alarmTime : _12H) + " tomorrow"
                                 : "Alarm set for: " + (is24 ? alarmTime : _12H) + " today";

      mActivity.runOnUiThread(() -> {
         Toast alert = Toast.makeText(mActivity, message, Toast.LENGTH_LONG);
         alert.setGravity(Gravity.BOTTOM, 0, 100);
         alert.show();
      });

      if (playSound) playAlertTone(mActivity.getApplicationContext(), Alert.ALARM);
   }

   private void onLabelClick(View v) {
      new EditTextDialog()
              .setActionListener(label -> {
                 tv_label.setText(label);
                 thisAlarm.setLabel(label);
                 ThreadUtils.runBackgroundTask(() -> {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    database.updateAlarmLabel(getAbsoluteAdapterPosition(), label);
                    updateAlarm(thisAlarm.getTime());
                 });
              }).prepareAndShow(mActivity);
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
                if (ringtoneUri != null) ringtone = RingtoneUtils.getRingtoneName(mActivity, ringtoneUri);
                String actualRingtoneName = ringtoneName.equals("Default") ? ringtone : ringtoneName;
                btn_rngPicker.setText(actualRingtoneName);
                thisAlarm.setRingtone(actualRingtoneName);

                ThreadUtils.runBackgroundTask(() -> {
                   Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                   database.updateRingtone(getAbsoluteAdapterPosition(), ringtoneName, ringtoneUri);
                });

             }).show();
   }

   void bindView() {

      selectedDays = thisAlarm.getRepeatDays();

      btn_sunday.setBackgroundResource(!selectedDays[0] ? R.drawable.disabled_round_button
                                                        : R.drawable.enabled_round_button);

      btn_monday.setBackgroundResource(!selectedDays[1] ? R.drawable.disabled_round_button
                                                        : R.drawable.enabled_round_button);

      btn_tuesday.setBackgroundResource(!selectedDays[2] ? R.drawable.disabled_round_button
                                                         : R.drawable.enabled_round_button);

      btn_wednesday.setBackgroundResource(!selectedDays[3] ? R.drawable.disabled_round_button
                                                           : R.drawable.enabled_round_button);

      btn_thursday.setBackgroundResource(!selectedDays[4] ? R.drawable.disabled_round_button
                                                          : R.drawable.enabled_round_button);

      btn_friday.setBackgroundResource(!selectedDays[5] ? R.drawable.disabled_round_button
                                                        : R.drawable.enabled_round_button);

      btn_saturday.setBackgroundResource(!selectedDays[6] ? R.drawable.disabled_round_button
                                                          : R.drawable.enabled_round_button);

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
                               : String.format(Locale.US, "%02d", (hh % 12 == 0 ? 12 : hh % 12)) + ":" + mm;

      String realTime = is24 ? _24H : _12H;
      tv_alarmTime.setText(realTime);
      if (is24) am_pm.setVisibility(View.GONE);
      else {
         am_pm.setVisibility(View.VISIBLE);
         am_pm.setText(isForenoon ? "AM" : "PM");
      }

      alarmStatus.setChecked(thisAlarm.isOn());
      cbx_Repeat.setChecked(thisAlarm.isRepeated());

      vg_buttonRow.setVisibility(cbx_Repeat.isChecked() ? View.VISIBLE : View.GONE);

      cbx_Vibrate.setChecked(thisAlarm.isVibrate());
      btn_rngPicker.setText(thisAlarm.getRingTone());
      int rowDrawable = DRAWABLES[getAbsoluteAdapterPosition() % DRAWABLES.length];
      decoration.setBackground(ContextCompat.getDrawable(mActivity, rowDrawable));
   }

   // This class will handle everything related to alarms. I also tried avoided naming this class; AlarmManager :)
   private class TimeManager implements View.OnClickListener, OnTimeSetListener {

      /*
       * Called when the alarm_time text string has been clicked.
       */
      @Override
      public void onClick(View v) {
         chooseTime();
      }

      // display the time picker dialog to set time for alarm
      private void chooseTime() {
         boolean is24 = isUserPreferred24Hours(mActivity);
         Calendar calendar = Calendar.getInstance();

         FragmentManager manager = mActivity.getSupportFragmentManager();
         TimePickerDialog dpd = TimePickerDialog.newInstance(this,
                                                             calendar.get(Calendar.HOUR_OF_DAY),
                                                             calendar.get(Calendar.MINUTE),
                                                             is24);
         dpd.setVersion(TimePickerDialog.Version.VERSION_2);
         dpd.show(manager, "TimePickerDialog");
      }

      /*
       * Called when the user is done setting a new time and the dialog has closed.
       */
      @Override
      public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
         alarmStatus.setChecked(true);
         thisAlarm.setOn(true);

         String label = tv_label.getText().toString();
         String[] ts = thisAlarm.getTime().split(":");

         cancelAllPendingAlarms();

         calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
         calendar.set(Calendar.MINUTE, minute);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);

         boolean isUserPreferred24HourView = isUserPreferred24Hours(mActivity);

         Resources timePickerResources = view.getContext().getResources();
         Configuration config = timePickerResources.getConfiguration();
         Locale currentLocale = ConfigurationCompat.getLocales(config).get(0);

         SimpleDateFormat timeFormat24 = new SimpleDateFormat("HH:mm", currentLocale);
         SimpleDateFormat timeFormat12 = new SimpleDateFormat("hh:mm", currentLocale);

         String currentTime = isUserPreferred24HourView ? timeFormat24.format(calendar.getTime())
                                                        : timeFormat12.format(calendar.getTime());

         boolean isAM = hourOfDay >= 0 && hourOfDay < 12;
         tv_alarmTime.setText(currentTime);
         thisAlarm.setTime(currentTime);
         am_pm.setText(isAM ? "AM" : "PM");

         ThreadUtils.runBackgroundTask(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            // set alarm based on repeat status
            if (thisAlarm.isOn() && thisAlarm.isRepeated()) {
               updateRepeatingPendingAlarm();
            } else if (thisAlarm.isOn() && !thisAlarm.isRepeated()) {
               updateAlarm(currentTime);
               // notify user of changes made
               long calendarTime = calendar.getTimeInMillis();
               boolean isNextDay = System.currentTimeMillis() > calendarTime;

               boolean is24 = isUserPreferred24Hours(mActivity);

               String alarmTime = is24 ? timeFormat24.format(calendar.getTime())
                                       : timeFormat12.format(calendar.getTime());

               String message = isNextDay ? "Alarm set for: " + alarmTime + " tomorrow"
                                          : "Alarm set for: " + alarmTime + " today";

               mActivity.runOnUiThread(() -> {
                  // can't use toast outside the UI thread
                  Toast alert = Toast.makeText(mActivity, message, Toast.LENGTH_LONG);
                  alert.setGravity(Gravity.BOTTOM, 0, 100);
                  alert.show();
               });
            }

            database.updateTime(getAbsoluteAdapterPosition(), currentTime);
         });

         playAlertTone(mActivity.getApplicationContext(), Alert.ALARM);

      }

   }
}
