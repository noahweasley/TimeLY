package com.astrro.timely.main.chats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.astrro.timely.R;
import com.astrro.timely.core.DataModel;
import com.astrro.timely.util.ThreadUtils;
import com.astrro.timely.util.collections.CollectionUtils;
import com.astrro.timely.util.test.DummyGenerator;
import com.google.android.material.divider.MaterialDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {
   public static final String TOOLBAR_TITLE = "Chats";
   private static Fragment fragmentInstance;
   private List<DataModel> messageList = new ArrayList<>();
   private MessageAdapter messageAdapter;

   public static Fragment getInstance() {
      return fragmentInstance == null ? (fragmentInstance = new ChatsFragment()) : fragmentInstance;
   }

   public static String getToolbarTitle() {
      return TOOLBAR_TITLE;
   }

   @Nullable
   @org.jetbrains.annotations.Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater,
                            @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                            @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
      return inflater.inflate(R.layout.fragment_chats, container, false);
   }

   @Override
   public void onViewCreated(@NonNull View view,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);

      ProgressBar indeterminateProgress = view.findViewById(R.id.indeterminateProgress);
      ViewGroup vg_loaderView = view.findViewById(R.id.loader_view);
      RecyclerView rv_messages = view.findViewById(R.id.messages);
      rv_messages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true));
      rv_messages.setHasFixedSize(true);
      rv_messages.addItemDecoration(new MaterialDividerItemDecoration(getContext(), RecyclerView.VERTICAL));
      rv_messages.setAdapter(messageAdapter = new MessageAdapter());

      ThreadUtils.runBackgroundTask(() -> {
         messageList = DummyGenerator.getDummyMessages(20);
         if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
               messageAdapter.notifyDataSetChanged();
               if (CollectionUtils.isEmpty(messageList)) {
                  rv_messages.setVisibility(View.GONE);
                  vg_loaderView.setVisibility(View.VISIBLE);
               } else {
                  rv_messages.setVisibility(View.VISIBLE);
                  vg_loaderView.setVisibility(View.GONE);
               }
               indeterminateProgress.setVisibility(View.GONE);
            });
         }
      });
   }

   @Override
   public void onDetach() {
      fragmentInstance = null;
      super.onDetach();
   }

   @Override
   protected void finalize() throws Throwable {
      fragmentInstance = null;
      super.finalize();
   }

   private class MessageAdapter extends RecyclerView.Adapter<MessageRow> {

      @NonNull
      @Override
      public MessageRow onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         View view = getLayoutInflater().inflate(R.layout.message_row, parent, false);
         return new MessageRow(view);
      }

      @Override
      public void onBindViewHolder(@NonNull MessageRow holder, int position) {
         holder.with(position, messageList).bindView();
      }

      @Override
      public int getItemCount() {
         return messageList.size();
      }
   }
}
