package com.noah.timely.core;

import android.net.Uri;

import androidx.fragment.app.FragmentActivity;

import com.noah.timely.assignment.AssignmentModel;
import com.noah.timely.gallery.Image;

import java.util.List;

// Parameters for RequestRunner
public class RequestParams {
   private FragmentActivity mActivity;
   private int adapterPosition;
   private List<DataModel> dList;
   private String alarmLabel;
   private String[] alarmTime;
   private AssignmentModel assignmentData;
   private String timetable;
   private List<Uri> mediaUris;
   private int assignmentPosition;
   private Class<? extends DataModel> dataClass;
   private Integer[] itemIndices;
   private Integer[] positionIndices;
   private String semester;
   private MetaDataType metadataType;
   private Boolean[] alarmRepeatDays;
   private int pagePosition;
   private List<Image> imageList;

   public String getSemester() {
      return semester;
   }

   public void setSemester(String semester) {
      this.semester = semester;
   }

   public MetaDataType getMetadataType() {
      return metadataType;
   }

   public void setMetadataType(MetaDataType metadataType) {
      this.metadataType = metadataType;
   }

   public FragmentActivity getActivity() {
      return mActivity;
   }

   public void setActivity(FragmentActivity mActivity) {
      this.mActivity = mActivity;
   }

   public int getAdapterPosition() {
      return adapterPosition;
   }

   public void setAdapterPosition(int adapterPosition) {
      this.adapterPosition = adapterPosition;
   }

   public List<DataModel> getModelList() {
      return dList;
   }

   public void setModelList(List<DataModel> dList) {
      this.dList = dList;
   }

   public String getAlarmLabel() {
      return alarmLabel;
   }

   public void setAlarmLabel(String alarmLabel) {
      this.alarmLabel = alarmLabel;
   }

   public String[] getAlarmTime() {
      return alarmTime;
   }

   public void setAlarmTime(String[] alarmTime) {
      this.alarmTime = alarmTime;
   }

   public AssignmentModel getAssignmentData() {
      return assignmentData;
   }

   public void setAssignmentData(AssignmentModel assignmentData) {
      this.assignmentData = assignmentData;
   }

   public String getTimetable() {
      return timetable;
   }

   public void setTimetable(String timetable) {
      this.timetable = timetable;
   }

   public List<Uri> getMediaUris() {
      return mediaUris;
   }

   public void setMediaUris(List<Uri> mediaUris) {
      this.mediaUris = mediaUris;
   }

   public int getAssignmentPosition() {
      return assignmentPosition;
   }

   public void setAssignmentPosition(int assignmentPosition) {
      this.assignmentPosition = assignmentPosition;
   }

   public Class<? extends DataModel> getDataClass() {
      return dataClass;
   }

   public void setDataClass(Class<? extends DataModel> dataClass) {
      this.dataClass = dataClass;
   }

   public Integer[] getItemIndices() {
      return itemIndices;
   }

   public void setItemIndices(Integer[] itemIndices) {
      this.itemIndices = itemIndices;
   }

   public Integer[] getPositionIndices() {
      return positionIndices;
   }

   public void setPositionIndices(Integer[] positionIndices) {
      this.positionIndices = positionIndices;
   }

   public Boolean[] getAlarmRepeatDays() {
      return alarmRepeatDays;
   }

   public void setAlarmRepeatDays(Boolean[] alarmRepeatDays) {
      this.alarmRepeatDays = alarmRepeatDays;
   }

   public int getPagePosition() {
      return pagePosition;
   }

   public void setPagePosition(int pagePosition) {
      this.pagePosition = pagePosition;
   }

   public List<Image> getImageList() {
      return imageList;
   }

   public void setImageList(List<Image> imageList) {
      this.imageList = imageList;
   }

   public enum MetaDataType {
      COURSE, NO_DATA, TIMETABLE, EXAM, TODO
   }
}
