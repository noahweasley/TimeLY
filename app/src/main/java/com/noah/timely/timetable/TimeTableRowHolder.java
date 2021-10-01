package com.noah.timely.timetable;

import static com.noah.timely.timetable.DaysFragment.ARG_POSITION;
import static com.noah.timely.timetable.DaysFragment.ARG_TO_EDIT;
import static com.noah.timely.util.MiscUtil.isUserPreferred24Hours;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.os.ConfigurationCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.noah.timely.R;
import com.noah.timely.core.DataModel;
import com.noah.timely.core.RequestRunner;
import com.noah.timely.custom.VerticalTextView;
import com.noah.timely.error.ErrorDialog;
import com.noah.timely.scheduled.AddScheduledActivity;
import com.noah.timely.scheduled.AddScheduledDialog;
import com.noah.timely.scheduled.ScheduledTimetableFragment;
import com.noah.timely.util.DeviceInfoUtil;

import java.util.List;
import java.util.Locale;

public class TimeTableRowHolder extends RecyclerView.ViewHolder {

   private static final int[] COLORS_1 = {
           android.R.color.holo_blue_bright,
           android.R.color.holo_orange_light,
           R.color.pink,
           android.R.color.holo_green_dark,
           android.R.color.holo_purple,
           android.R.color.holo_green_light,
           android.R.color.holo_blue_dark,
           android.R.color.holo_orange_dark,
           android.R.color.holo_red_light
   };

   private static final int[] DRAWABLE_2 = {
           R.drawable.rounded_cl_pu,
           R.drawable.rounded_cl_pi,
           R.drawable.rounded_cl_gl,
           R.drawable.rounded_cl_bd,
           R.drawable.rounded_cl_od,
           R.drawable.rounded_cl_rl
   };

   private static final int[] DRAWABLE = {
           R.drawable.rounded_cl_bb,
           R.drawable.rounded_cl_ol,
           R.drawable.rounded_cl_pi,
           R.drawable.rounded_cl_gd,
           R.drawable.rounded_cl_pu,
           R.drawable.rounded_cl_gl,
           R.drawable.rounded_cl_bd,
           R.drawable.rounded_cl_od,
           R.drawable.rounded_cl_rl
   };

   private final ImageView img_schImp;
   private final View lIndicator, rIndicator;
   private final TextView tv_time, tv_course, tv_lecturer, atv_FCN;
   private final ImageButton btn_delete, btn_edit;
   private final VerticalTextView tv_day;
   private final View v_selectionOverlay;
   private Fragment user;
   private TimetableModel tModel;
   private List<DataModel> tList;
   private RecyclerView.Adapter<?> rowAdapter;
   private CoordinatorLayout coordinator;
   private String timetable;
   private boolean isChecked;
   private int pagePosition;

   public TimeTableRowHolder(@NonNull View rootView) {
      super(rootView);
      lIndicator = rootView.findViewById(R.id.indicator);
      rIndicator = rootView.findViewById(R.id.indicator2);
      tv_time = rootView.findViewById(R.id.time);
      tv_course = rootView.findViewById(R.id.subject);
      tv_lecturer = rootView.findViewById(R.id.name);
      atv_FCN = rootView.findViewById(R.id.full_course_name);
      img_schImp = rootView.findViewById(R.id.schedule_importance);
      tv_day = rootView.findViewById(R.id.vertical_text);
      v_selectionOverlay = rootView.findViewById(R.id.checked_overlay);

      btn_delete = rootView.findViewById(R.id.deleteButton);
      btn_edit = rootView.findViewById(R.id.editButton);

      btn_delete.setOnClickListener(v -> {
         // for  normal timetable
         if (user instanceof DaysFragment) tModel.setDay(timetable);

         String deleteRequest =
                 user instanceof ScheduledTimetableFragment ? ScheduledTimetableFragment.DELETE_REQUEST
                                                            : DaysFragment.DELETE_REQUEST;

         RequestRunner runner = RequestRunner.createInstance();
         RequestRunner.Builder builder = new RequestRunner.Builder();
         builder.setOwnerContext(user.getActivity())
                 .setAdapterPosition(getAbsoluteAdapterPosition())
                 .setPagePosition(tModel.getTimetablePosition())
                 .setModelList(tList)
                 .setTimetable(timetable);

         runner.setRequestParams(builder.getParams())
                 .runRequest(deleteRequest);

         Snackbar.make(coordinator, "Timetable Deleted", Snackbar.LENGTH_LONG)
                 .setActionTextColor(Color.YELLOW)
                 .setAction("undo", (view) -> runner.undoRequest())
                 .show();
      });

      btn_edit.setOnClickListener(v -> {
         tModel.setChronologicalOrder(getAbsoluteAdapterPosition());

         Context context = user.getContext();
         float[] resolution = DeviceInfoUtil.getDeviceResolutionDP(context);
         float requiredWidthDP = 368, requiredHeightDP = 750;

         SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
         boolean useDialog = preferences.getBoolean("prefer_dialog", true);
         boolean smallScreenSize = resolution[0] < requiredWidthDP || resolution[1] < requiredHeightDP;

         if (user instanceof DaysFragment) {
            // for  normal timetable
            tModel.setDay(timetable);

            Intent intent = new Intent(context, AddTimetableActivity.class);
            intent.putExtra(ARG_POSITION, tModel.getDayIndex())
                    .putExtra(ARG_TO_EDIT, true)
                    .putExtra(DaysFragment.ARG_DATA, tModel);
            // choose what kind of task-add method to use base on device width and user pref
            if (smallScreenSize) {
               context.startActivity(intent);
            } else {
               if (useDialog) {
                  new AddTimetableDialog().show(user.getContext(), true, tModel);

               } else {
                  context.startActivity(intent);
               }
            }
         } else {
            Intent intent = new Intent(context, AddScheduledActivity.class);
            intent.putExtra(ARG_POSITION, tModel.getDayIndex())
                    .putExtra(ARG_TO_EDIT, true)
                    .putExtra(ScheduledTimetableFragment.ARG_DATA, tModel);
            // choose what kind of task-add method to use base on device width and user pref
            if (smallScreenSize) {
               context.startActivity(intent);
            } else {
               if (useDialog) {
                  new AddScheduledDialog().show(user.getContext(), true, tModel);

               } else {
                  context.startActivity(intent);
               }
            }
         }
      });

      tv_course.setOnClickListener(v -> {
         if (TextUtils.equals(tv_course.getText(), "NIL")) {
            ErrorDialog.Builder builder = new ErrorDialog.Builder();
            builder.setDialogMessage("No matching course code found")
                    .setShowSuggestions(true)
                    .setSuggestionCount(2)
                    .setSuggestion1("Register courses first")
                    .setSuggestion2("After registration, use that course title");
            new ErrorDialog().showErrorMessage(user.getContext(), builder.build());
         }
      });

      // Multi - Select actions
      rootView.setOnLongClickListener(l -> {
         if (user instanceof DaysFragment) {

            DaysFragment.TimeTableRowAdapter rowAdapter
                    = (DaysFragment.TimeTableRowAdapter) this.rowAdapter;
            rowAdapter.setMultiSelectionEnabled(!rowAdapter.isMultiSelectionEnabled()
                    || rowAdapter.getCheckedTimetablesCount() != 0);
         } else {
            ScheduledTimetableFragment.TimeTableRowAdapter rowAdapter
                    = (ScheduledTimetableFragment.TimeTableRowAdapter) this.rowAdapter;
            rowAdapter.setMultiSelectionEnabled(!rowAdapter.isMultiSelectionEnabled()
                    || rowAdapter.getCheckedTimetablesCount() != 0);
         }
         trySelectTimetable();

         return true;
      });

      rootView.setOnClickListener(c -> {
         if (user instanceof DaysFragment) {
            DaysFragment.TimeTableRowAdapter rowAdapter = (DaysFragment.TimeTableRowAdapter) this.rowAdapter;

            if (rowAdapter.isMultiSelectionEnabled()) {
               trySelectTimetable();
               if (rowAdapter.getCheckedTimetablesCount() == 0) {
                  rowAdapter.setMultiSelectionEnabled(false);
               }
            }
         } else {
            ScheduledTimetableFragment.TimeTableRowAdapter rowAdapter
                    = (ScheduledTimetableFragment.TimeTableRowAdapter) this.rowAdapter;

            if (rowAdapter.isMultiSelectionEnabled()) {
               trySelectTimetable();
               if (rowAdapter.getCheckedTimetablesCount() == 0) {
                  rowAdapter.setMultiSelectionEnabled(false);
               }
            }
         }
      });
   }

   private void trySelectTimetable() {
      if (user instanceof DaysFragment) {
         DaysFragment.TimeTableRowAdapter rowAdapter = (DaysFragment.TimeTableRowAdapter) this.rowAdapter;

         TimetableModel timetableModel = (TimetableModel) tList.get(getAbsoluteAdapterPosition());

         isChecked = !isChecked;
         v_selectionOverlay.setVisibility(isChecked ? View.VISIBLE : View.GONE);
         rowAdapter.onChecked(getAbsoluteAdapterPosition(), isChecked, timetableModel.getId());

      } else {
         ScheduledTimetableFragment.TimeTableRowAdapter rowAdapter
                 = (ScheduledTimetableFragment.TimeTableRowAdapter) this.rowAdapter;

         TimetableModel timetableModel = (TimetableModel) tList.get(getAbsoluteAdapterPosition());

         isChecked = !isChecked;
         v_selectionOverlay.setVisibility(isChecked ? View.VISIBLE : View.GONE);
         rowAdapter.onChecked(getAbsoluteAdapterPosition(), isChecked, timetableModel.getId());
      }
   }

   // Disable click on views not allowed to fire View#onClick while in multi-selection mode
   private void tryDisableViews(boolean disable) {
      btn_edit.setFocusable(!disable);
      btn_edit.setEnabled(!disable);
      btn_delete.setFocusable(!disable);
      btn_delete.setEnabled(!disable);
   }

   public TimeTableRowHolder with(Fragment user,
                                  RecyclerView.Adapter<?> rowAdapter,
                                  List<DataModel> tList,
                                  CoordinatorLayout coordinator,
                                  int position) {
      this.user = user;
      this.tModel = (TimetableModel) tList.get(position);
      this.tList = tList;
      this.rowAdapter = rowAdapter;
      this.coordinator = coordinator;
      return this;
   }

   public void bindView() {
      Context context = user.getContext();
      // Set the importance drawable according to scheduled timetable importance.
      // getImportance() will be null if it is called by TableFragment
      if (tModel.getImportance() != null) {
         switch (tModel.getImportance()) {
            case "Not Important":
               img_schImp.setBackground(ContextCompat.getDrawable(context, R.drawable.schedule_not_important));
               break;
            case "Very Important":
               img_schImp.setBackground(ContextCompat.getDrawable(context, R.drawable.schedule_very_important));
               break;
            case "Less Important":
               img_schImp.setBackground(ContextCompat.getDrawable(context, R.drawable.schedule_less_important));
         }
         TooltipCompat.setTooltipText(img_schImp, tModel.getImportance());
      }
      if (user instanceof DaysFragment) {
         int background = DRAWABLE[getAbsoluteAdapterPosition() % DRAWABLE.length];
         lIndicator.setBackground(ContextCompat.getDrawable(context, background));
      } else {
         // indicator2 and verticalTextView will return null for users other than
         // the ScheduledTimetableFragment, so use them only for that particular fragment
         // to prevent application crash.
         int dayBackground = DRAWABLE_2[tModel.getDayIndex()];
         lIndicator.setBackground(ContextCompat.getDrawable(context, dayBackground));
         rIndicator.setBackground(ContextCompat.getDrawable(context, dayBackground));
         tv_day.setText(tModel.getDay());
         tv_lecturer.setText(truncateName(tModel.getLecturerName()));
      }

      tv_course.setText(tModel.getCourseCode());
      atv_FCN.setText(tModel.getFullCourseName());

      String start, end;
      if (isUserPreferred24Hours(context)) {
         start = tModel.getStartTime();
         end = tModel.getEndTime();
      } else {
         start = convertTime(tModel.getStartTime(), context);
         end = convertTime(tModel.getEndTime(), context);
      }

      tv_time.setText(String.format("%s - %s", start, end));

      if (user instanceof DaysFragment) {
         DaysFragment.TimeTableRowAdapter rowAdapter = (DaysFragment.TimeTableRowAdapter) this.rowAdapter;

         isChecked = rowAdapter.isChecked(getAbsoluteAdapterPosition());
         v_selectionOverlay.setVisibility(isChecked ? View.VISIBLE : View.GONE);
         tryDisableViews(rowAdapter.isMultiSelectionEnabled());
      } else {
         ScheduledTimetableFragment.TimeTableRowAdapter rowAdapter
                 = (ScheduledTimetableFragment.TimeTableRowAdapter) this.rowAdapter;
         isChecked = rowAdapter.isChecked(getAbsoluteAdapterPosition());
         v_selectionOverlay.setVisibility(isChecked ? View.VISIBLE : View.GONE);
         tryDisableViews(rowAdapter.isMultiSelectionEnabled());
      }
   }

   // Convert to 12 hours clock format
   private String convertTime(String time, Context context) {
      try {
         String[] st = time.split(":");
         int hh = Integer.parseInt(st[0]);
         int mm = Integer.parseInt(st[1]);

         Resources aResources = context.getResources();
         Configuration config = aResources.getConfiguration();
         Locale locale = ConfigurationCompat.getLocales(config).get(0);

         String formattedHrAM = String.format(locale, "%02d", (hh == 0 ? 12 : hh));
         String formattedHrPM = String.format(locale, "%02d", (hh % 12 == 0 ? 12 : hh % 12));
         String formattedMinAM = String.format(locale, "%02d", mm) + " AM";
         String formattedMinPM = String.format(locale, "%02d", mm) + " PM";

         boolean isAM = hh >= 0 && hh < 12;

         return isAM ? formattedHrAM + ":" + formattedMinAM
                     : formattedHrPM + ":" + formattedMinPM;
      } catch (NumberFormatException exc) {
         return null;
      }
   }

   public TimeTableRowHolder setTimetableDay(String timetable) {
      this.timetable = timetable;
      return this;
   }

   // Determines if there was an added title in the lecturer's name
   private boolean startsWithAny(String[] titles, String s) {
      for (String title : titles) if (s.startsWith(title)) return true;
      return false;
   }

   // Truncate lecturer's name to enable user view more of the name because
   // if the name is too long, the system will add ellipses at the end of the name
   // thereby removing some important parts of the name.
   private String truncateName(String fullName) {
      String[] nameTokens = fullName.split(" ");

      String[] titles = {"Barr", "Barrister", "Doc", "Doctor", "Dr", "Engineer", "Engr", "Mr",
              "Mister", "Mrs", "Ms", "Prof", "Professor"};

      StringBuilder nameBuilder = new StringBuilder();
      int iMax = nameTokens.length - 1;
      int nameLimit = user.getContext().getResources().getInteger(R.integer.name_limit);

      if (nameTokens.length > 2 && fullName.length() > nameLimit && startsWithAny(titles, fullName)) {
         // Append the title if there is one
         switch (nameTokens[0]) {
            case "Barrister":
               nameBuilder.append(titles[0] /* Barr */).append(" ");
               break;
            case "Doctor":
            case "Doc":
               nameBuilder.append(titles[4] /* Dr */).append(" ");
               break;
            case "Engineer":
               nameBuilder.append(titles[6] /* Engr */).append(" ");
               break;
            case "Mister":
               nameBuilder.append(titles[7] /* Mr */).append(" ");
               break;
            case "Mrs":
               nameBuilder.append(titles[10] /* Mr */).append(" ");
               break;
            case "Professor":
               nameBuilder.append(titles[11] /* Prof */).append(" ");
               break;
            default:
               nameBuilder.append(nameTokens[0]).append(" ");
         }
         // Shorten the names to their first characters in uppercase
         for (int i = 1; i <= iMax; i++) {
            if (i == iMax) {
               nameBuilder.append(" ");
               break;
            }
            nameBuilder.append(Character.toUpperCase(nameTokens[i].charAt(0))).append(".");
            nameBuilder.append(" ");
         }

         int bMaxLen = nameBuilder.length();
         return nameBuilder.replace(bMaxLen - 1, bMaxLen, nameTokens[iMax]).toString();
      }
      return fullName;
   }
}