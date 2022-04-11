package com.noah.timely.calculator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.noah.timely.R;
import com.noah.timely.core.DataModel;
import com.noah.timely.core.SchoolDatabase;
import com.noah.timely.courses.CoursesFragment;
import com.noah.timely.main.MainActivity;
import com.noah.timely.util.PreferenceUtils;
import com.noah.timely.util.ThreadUtils;

import java.util.ArrayList;
import java.util.List;

public class ResultCalculatorFragment extends Fragment {
   public static final String ARG_POSITION = "tab position";
   private List<DataModel> courseModelList = new ArrayList<>();
   private ViewGroup vg_container;

   public static Fragment newInstance(int position) {
      Bundle bundle = new Bundle();
      bundle.putInt(ARG_POSITION, position);
      ResultCalculatorFragment fragment = new ResultCalculatorFragment();
      fragment.setArguments(bundle);
      return fragment;
   }

   @Override
   public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
      inflater.inflate(R.menu.fragment_result_calc, menu);
      super.onCreateOptionsMenu(menu, inflater);
   }

   @Override
   public boolean onOptionsItemSelected(@NonNull MenuItem item) {
      int menuItemId = item.getItemId();
      if (menuItemId == R.id.calculate_average) calculateAvargeGPA();
      return super.onOptionsItemSelected(item);
   }

   private void calculateAvargeGPA() {

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
      setHasOptionsMenu(true);
      ProgressBar indeterminateProgress = view.findViewById(R.id.indeterminateProgress);
      // set up list
      RecyclerView rv_resultList = view.findViewById(R.id.result_list);
      rv_resultList.setAdapter(new ResultListAdapter());
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
      });

      vg_container = view.findViewById(R.id.no_courses_view);
      if (courseModelList.isEmpty()) {
         vg_container.setVisibility(View.VISIBLE);
         rv_resultList.setVisibility(View.GONE);

      } else {
         vg_container.setVisibility(View.GONE);
         rv_resultList.setVisibility(View.VISIBLE);
      }
   }

   @Override
   public void onResume() {
      super.onResume();
      boolean showInfo1 = PreferenceUtils.getBooleanValue(getContext(), PreferenceUtils.GPA_INFO_SHOWN_1, false);
      boolean showInfo2 = PreferenceUtils.getBooleanValue(getContext(), PreferenceUtils.GPA_INFO_SHOWN_2, false);

      if (courseModelList.isEmpty()) {
         // show educational UI
         vg_container.setVisibility(View.VISIBLE);

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
         } else {
            Toast.makeText(getContext(), "Course is not empty", Toast.LENGTH_LONG).show( );
         } // end if - else

      }

   }

   private class ResultListAdapter extends RecyclerView.Adapter<ResultListRowHolder> {

      @NonNull
      @Override
      public ResultListRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         return new ResultListRowHolder(getLayoutInflater().inflate(R.layout.result_list_row, parent, false));
      }

      @Override
      public void onBindViewHolder(@NonNull ResultListRowHolder holder, int position) {
         holder.with(position, courseModelList).bindView();
      }

      @Override
      public int getItemCount() {
         return courseModelList.size();
      }

   }

}
