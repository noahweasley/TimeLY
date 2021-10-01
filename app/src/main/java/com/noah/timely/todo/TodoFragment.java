package com.noah.timely.todo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.noah.timely.R;
import com.noah.timely.core.SchoolDatabase;
import com.noah.timely.util.Constants;
import com.noah.timely.util.ThreadUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TodoFragment#newInstance} factory method to create an instance of this fragment.
 */
public class TodoFragment extends Fragment {
   /*      Current todoTask sizes    */
   @SuppressWarnings("MismatchedReadAndWriteOfArray")
   private final int[] sizes = new int[10];
   private Map<String, Integer> todoGroupSizes = new HashMap<>();
   private SchoolDatabase database;
   /*      Views       */
   private ViewGroup vg_container;
   private TextView tv_catGen, tv_catWork, tv_catMusic,
           tv_catTravel, tv_catStudy,
           tv_catCreativity, tv_catHome,
           tv_catShop, tv_catFun, tv_catMisc;

   public TodoFragment() {
      // Required empty public constructor
   }

   /**
    * Use this factory method to create a new instance of this fragment
    *
    * @return A new instance of fragment TodoFragment.
    */
   public static TodoFragment newInstance() {
      TodoFragment fragment = new TodoFragment();
      Bundle args = new Bundle();
      fragment.setArguments(args);
      return fragment;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      EventBus.getDefault().register(this);
      database = new SchoolDatabase(getContext());
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      // Inflate the layout for this fragment
      return inflater.inflate(R.layout.fragment_todo, container, false);
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);

      FloatingActionButton fab_add = view.findViewById(R.id.fab_add_todo);
      fab_add.setOnClickListener(v -> AddTodoActivity.start(getContext(), false, "Miscelaneous"));
      vg_container = view.findViewById(R.id.p_container);
      ViewGroup vg_loaderView = view.findViewById(R.id.loader_view);
      // get the children from the containing layout because they were all cached instead of calling findViewById()
      ViewGroup vg_gen = (ViewGroup) vg_container.getChildAt(0);
      ViewGroup vg_work = (ViewGroup) vg_container.getChildAt(1);
      ViewGroup vg_music = (ViewGroup) vg_container.getChildAt(2);
      ViewGroup vg_travel = (ViewGroup) vg_container.getChildAt(3);
      ViewGroup vg_study = (ViewGroup) vg_container.getChildAt(4);
      ViewGroup vg_home = (ViewGroup) vg_container.getChildAt(5);
      ViewGroup vg_creativity = (ViewGroup) vg_container.getChildAt(6);
      ViewGroup vg_shopping = (ViewGroup) vg_container.getChildAt(7);
      ViewGroup vg_fun = (ViewGroup) vg_container.getChildAt(8);
      ViewGroup vg_misc = (ViewGroup) vg_container.getChildAt(9);

      tv_catGen = (TextView) vg_gen.getChildAt(2);
      tv_catWork = (TextView) vg_work.getChildAt(2);
      tv_catMusic = (TextView) vg_music.getChildAt(2);
      tv_catTravel = (TextView) vg_travel.getChildAt(2);
      tv_catStudy = (TextView) vg_study.getChildAt(2);
      tv_catCreativity = (TextView) vg_creativity.getChildAt(2);
      tv_catHome = (TextView) vg_home.getChildAt(2);
      tv_catShop = (TextView) vg_shopping.getChildAt(2);
      tv_catFun = (TextView) vg_fun.getChildAt(2);
      tv_catMisc = (TextView) vg_music.getChildAt(2);

      Runnable uiTask = () -> {
         refreshTodoCountStates(todoGroupSizes);
         vg_loaderView.setVisibility(View.GONE);
         vg_container.setVisibility(View.VISIBLE);
      };

      ThreadUtils.runBackgroundTask(() -> {
         todoGroupSizes = database.getTodoGroupSizes();
         if (isAdded()) getActivity().runOnUiThread(uiTask);
      });

      // set-up all listeners
      vg_gen.setOnClickListener(this::onClick);
      vg_work.setOnClickListener(this::onClick);
      vg_music.setOnClickListener(this::onClick);
      vg_travel.setOnClickListener(this::onClick);
      vg_study.setOnClickListener(this::onClick);
      vg_home.setOnClickListener(this::onClick);
      vg_creativity.setOnClickListener(this::onClick);
      vg_shopping.setOnClickListener(this::onClick);
      vg_fun.setOnClickListener(this::onClick);
      vg_misc.setOnClickListener(this::onClick);
   }

   @Override
   public void onResume() {
      super.onResume();
      ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Todo List");
   }

   @Override
   public void onDestroyView() {
      super.onDestroyView();
      database.close();
   }

   @Override
   public void onDetach() {
      super.onDetach();
      EventBus.getDefault().unregister(this);
   }

   @Subscribe(threadMode = ThreadMode.MAIN)
   public void onRefreshLayout(TodoRefreshEvent refreshEvent) {
      TodoModel dataModel = refreshEvent.getDataModel();
      findRefreshTodoCountState(dataModel);
   }

   private void refreshTodoCountStates(Map<String, Integer> todoGroupSizes) {

      int generalSize = sizes[0] = todoGroupSizes.get(TodoModel.CATEGORIES[0]);
      int musicSize = sizes[1] = todoGroupSizes.get(TodoModel.CATEGORIES[2]);
      int workSize = sizes[2] = todoGroupSizes.get(TodoModel.CATEGORIES[1]);
      int travelSize = sizes[3] = todoGroupSizes.get(TodoModel.CATEGORIES[3]);
      int studySize = sizes[4] = todoGroupSizes.get(TodoModel.CATEGORIES[4]);
      int creativitySize = sizes[5] = todoGroupSizes.get(TodoModel.CATEGORIES[5]);
      int homeSize = sizes[6] = todoGroupSizes.get(TodoModel.CATEGORIES[6]);
      int shopSize = sizes[7] = todoGroupSizes.get(TodoModel.CATEGORIES[7]);
      int funSize = sizes[8] = todoGroupSizes.get(TodoModel.CATEGORIES[8]);
      int miscSize = sizes[9] = todoGroupSizes.get(TodoModel.CATEGORIES[9]);

      tv_catGen.setText(String.format(Locale.US, "%d %s", generalSize, "task(s)"));
      tv_catWork.setText(String.format(Locale.US, "%d %s", workSize, "task(s)"));
      tv_catMusic.setText(String.format(Locale.US, "%d %s", musicSize, "task(s)"));
      tv_catTravel.setText(String.format(Locale.US, "%d %s", travelSize, "task(s)"));
      tv_catStudy.setText(String.format(Locale.US, "%d %s", studySize, "task(s)"));
      tv_catCreativity.setText(String.format(Locale.US, "%d %s", creativitySize, "task(s)"));
      tv_catHome.setText(String.format(Locale.US, "%d %s", homeSize, "task(s)"));
      tv_catShop.setText(String.format(Locale.US, "%d %s", shopSize, "task(s)"));
      tv_catFun.setText(String.format(Locale.US, "%d %s", funSize, "task(s)"));
      tv_catMisc.setText(String.format(Locale.US, "%d %s", miscSize, "task(s)"));

   }

   private void findRefreshTodoCountState(TodoModel dataModel) {
      int searchIndex = Arrays.binarySearch(TodoModel.CATEGORIES, dataModel.getDBcategory());
      ViewGroup child = (ViewGroup) vg_container.getChildAt(searchIndex);
      TextView grandChildTextView = (TextView) child.getChildAt(2);
      String groupSize = String.format(Locale.US, "%d %s", sizes[searchIndex] = ++sizes[searchIndex], "task(s)");
      grandChildTextView.setText(groupSize);
   }

   public void onClick(View view) {
      String category = null;
      int id = view.getId();

      if (id == R.id.p_work) {
         category = Constants.TODO_WORK;
      } else if (id == R.id.p_general) {
         category = Constants.TODO_GENERAL;
      } else if (id == R.id.p_creativity) {
         category = Constants.TODO_CREATIVITY;
      } else if (id == R.id.p_fun) {
         category = Constants.TODO_FUN;
      } else if (id == R.id.p_home) {
         category = Constants.TODO_HOME;
      } else if (id == R.id.p_misc) {
         category = Constants.TODO_MISCELLANEOUS;
      } else if (id == R.id.p_music) {
         category = Constants.TODO_MUSIC;
      } else if (id == R.id.p_shopping) {
         category = Constants.TODO_SHOPPING;
      } else if (id == R.id.p_study) {
         category = Constants.TODO_STUDY;
      } else if (id == R.id.p_travel) {
         category = Constants.TODO_TRAVEL;
      }

      // Navigate to contents of clicked todo
      getActivity().getSupportFragmentManager().beginTransaction()
              .replace(R.id.frame, TodoContainerFragment.newInstance(category), "Todo")
              .setCustomAnimations(R.anim.slide_enter, R.anim.slide_exit)
              .addToBackStack(null).commit();
   }
}