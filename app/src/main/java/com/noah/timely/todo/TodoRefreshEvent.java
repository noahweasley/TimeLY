package com.noah.timely.todo;

import java.util.Map;

public class TodoRefreshEvent {
   private Map<String, Integer> todoGroupSizes;
   private TodoModel dataModel;

   public TodoRefreshEvent(Map<String, Integer> todoGroupSizes) {
      this.todoGroupSizes = todoGroupSizes;
   }

   public TodoRefreshEvent(TodoModel todoModel) {
      this.dataModel = todoModel;
   }

   public TodoModel getDataModel() {
      return dataModel;
   }

   public void setDataModel(TodoModel dataModel) {
      this.dataModel = dataModel;
   }

   public Map<String, Integer> getTodoGroupSizes() {
      return todoGroupSizes;
   }

   public void setTodoGroupSizes(Map<String, Integer> todoGroupSizes) {
      this.todoGroupSizes = todoGroupSizes;
   }
}
