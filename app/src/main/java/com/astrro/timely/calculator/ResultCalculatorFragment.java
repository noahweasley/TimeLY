package com.astrro.timely.calculator;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.astrro.timely.R;
import com.astrro.timely.assignment.LayoutRefreshEvent;
import com.astrro.timely.core.DataModel;
import com.astrro.timely.core.SchoolDatabase;
import com.astrro.timely.courses.CoursesFragment;
import com.astrro.timely.main.MainActivity;
import com.astrro.timely.util.PreferenceUtils;
import com.astrro.timely.util.ThreadUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ResultCalculatorFragment extends Fragment {
   public static final String ARG_POSITION = "tab position";
   private List<DataModel> courseModelList = new ArrayList<>();
   private ViewGroup vg_container;
   private ResultListAdapter adapter;
   private TextView tv_gpaUnits;
   private boolean hasCheckedRegisteredCourses;

   public static Fragment newInstance(int position) {
      Bundle bundle = new Bundle();
      bundle.putInt(ARG_POSITION, position);
      ResultCalculatorFragment fragment = new ResultCalculatorFragment();
      fragment.setArguments(bundle);
      return fragment;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      EventBus.getDefault().register(this);
   }

   @Override
   public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
      inflater.inflate(R.menu.fragment_result_calculator, menu);
      super.onCreateOptionsMenu(menu, inflater);
   }

   @Override
   public boolean onOptionsItemSelected(@NonNull MenuItem item) {
      int menuItemId = item.getItemId();
      if (menuItemId == R.id.calculate_average) {
         // no list items but user still clicked
         if (adapter.getViewHolder() != null) {
            ThreadUtils.runBackgroundTask(() -> {
               Map<Integer, String[]>[] scoreMaps = adapter.getViewHolder().getScoreMaps();
               float avgGPA = Calculator.calulateAverageGPA(getContext(), scoreMaps);
               getActivity().runOnUiThread(() -> new GPAAveragerDialog().show(getContext(), avgGPA));

            });
         } else {
            // .. just show user a generic message
            Toast.makeText(getContext(), "No actions yet", Toast.LENGTH_SHORT).show();
         }
      }
      return super.onOptionsItemSelected(item);
   }

   private void calculateGPA() {
      Map<Integer, String[]> scoreMap = adapter.getViewHolder().getScoreMap();

      ThreadUtils.runBackgroundTask(() -> {
         float gpaValue = Calculator.calculateGPA(getContext(), scoreMap);

         if (isAdded())
            getActivity().runOnUiThread(() -> {
               ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.0f, gpaValue);
               valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
               valueAnimator.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
               // add value animation listeners for when the value updates
               valueAnimator.addUpdateListener(valueAnimator1 -> {
                  float value = (float) valueAnimator1.getAnimatedValue();
                  tv_gpaUnits.setText(String.format(Locale.US, "%.2f", value));
               });

               valueAnimator.start();
            });

      });

   }

   @Override
   public void onDetach() {
      // do clean up to avoid application crash
      ResultListRowHolder.doCleanUp();
      EventBus.getDefault().unregister(this);
      super.onDetach();
   }

   @Subscribe(threadMode = ThreadMode.MAIN)
   public void onRefreshLayoutEvent(LayoutRefreshEvent event) {
      adapter.notifyDataSetChanged();
   }

   @Nullable
   @org.jetbrains.annotations.Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater,
                            @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                            @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
      return inflater.inflate(R.layout.fragment_result_calculator, container, false);
   }

   @Override
   public void onViewCreated(@NonNull View view,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);

      tv_gpaUnits = view.findViewById(R.id.gpa_units);

      Button btn_calcuateGPA = view.findViewById(R.id.calculate);
      btn_calcuateGPA.setOnClickListener(v -> calculateGPA());

      TextView tv_unitTotal = view.findViewById(R.id.total_units);
      ViewGroup vg_loaderView = view.findViewById(R.id.loader_view);
      ViewGroup vg_noOpView = view.findViewById(R.id.no_courses_view);
      ViewGroup vg_calculateView = view.findViewById(R.id.calculate_view);
      // set up list
      RecyclerView rv_resultList = view.findViewById(R.id.result_list);
      rv_resultList.setAdapter((adapter = new ResultListAdapter()));
      rv_resultList.setLayoutManager(new LinearLayoutManager(getContext()));
      // then ...
      SchoolDatabase database = new SchoolDatabase(getContext());

      ThreadUtils.runBackgroundTask(() -> {
         if (getArguments() != null) {
            if (getArguments().getInt(ARG_POSITION) == 0) {
               courseModelList = database.getCoursesData(SchoolDatabase.FIRST_SEMESTER);
            } else {
               courseModelList = database.getCoursesData(SchoolDatabase.SECOND_SEMESTER);
            }
         }

         hasCheckedRegisteredCourses = true;
         double totalCredits = Calculator.getTotalCredits(getContext(), courseModelList);

         if (isAdded()) {
            getActivity().runOnUiThread(() -> {
               vg_loaderView.setVisibility(View.GONE);
               if (courseModelList.isEmpty()) {
                  vg_noOpView.setVisibility(View.VISIBLE);
                  vg_calculateView.setVisibility(View.GONE);
               } else {
                  adapter.notifyDataSetChanged();
                  vg_noOpView.setVisibility(View.GONE);
                  vg_calculateView.setVisibility(View.VISIBLE);

                  ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.0f, (float) totalCredits);
                  valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                  valueAnimator.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
                  // add value animation listeners for when the value updates
                  valueAnimator.addUpdateListener(valueAnimator1 -> {
                     float value = (float) valueAnimator1.getAnimatedValue();
                     getActivity().runOnUiThread(() -> tv_unitTotal.setText(String.format(Locale.US, "%.2f", value)));
                  });

                  valueAnimator.start();
               }
            });
         }

      });
   }

   @Override
   public void onResume() {
      // Prevent glitch on adding menu to the toolbar. Only show a particular semester's list rows, if that is the
      // only visible semester
      setHasOptionsMenu(true); // onCreateOptionsMenu will be called after this
      super.onResume();
      // .. and then
      boolean showInfo1 = PreferenceUtils.getBooleanValue(getContext(), PreferenceUtils.GPA_INFO_SHOWN_1, true);
      boolean showInfo2 = PreferenceUtils.getBooleanValue(getContext(), PreferenceUtils.GPA_INFO_SHOWN_2, true);

      // show educational UI
      if (!hasCheckedRegisteredCourses) return;
      if (courseModelList.isEmpty()) {
         // don't show info dialog anymore, if user doens't want it to be shown anymore
         if (getArguments().getInt(ARG_POSITION) == 0 && !showInfo1) {
            return;
         } else if (getArguments().getInt(ARG_POSITION) == 1 && !showInfo2) {
            return;
         }

         // inform user that courses need to be registered before they can use the G.P.A calculator
         new ResultCalculatorInfoDialog().show(getContext(), null).setOnActionReceviedListener(action -> {
            // naviagate to course fragment to add courses
            if (action == ResultCalculatorInfoDialog.ACTION_PROCEED) {
               if (getActivity() != null && getActivity() instanceof MainActivity) {
                  ((MainActivity) getActivity()).loadFragment(CoursesFragment.newInstance());
               } // end if
            } else if (action == ResultCalculatorInfoDialog.ACTION_DONT_SHOW) {
               if (getArguments().getInt(ARG_POSITION) == 0)
                  PreferenceUtils.setBooleanValue(getContext(), PreferenceUtils.GPA_INFO_SHOWN_1, false);
               else
                  PreferenceUtils.setBooleanValue(getContext(), PreferenceUtils.GPA_INFO_SHOWN_2, false);
            } // end if - else

         }); // end callback
      }

   }

   private class ResultListAdapter extends RecyclerView.Adapter<ResultListRowHolder> {
      private ResultListRowHolder vh;

      @NonNull
      @Override
      public ResultListRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         View view = getLayoutInflater().inflate(R.layout.result_list_row, parent, false);
         return (vh = new ResultListRowHolder(view));
      }

      @Override
      public void onBindViewHolder(@NonNull ResultListRowHolder holder, int position) {
         holder.with(position, getArguments().getInt(ARG_POSITION), courseModelList).bindView();
      }

      @Override
      public int getItemCount() {
         return courseModelList.size();
      }

      public ResultListRowHolder getViewHolder() {
         return vh;
      }

   }

}
