package com.projects.timely.core;

import android.net.Uri;

import com.projects.timely.assignment.AssignmentModel;

import java.util.List;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

// Parameters for RequestRunner
public class RequestParams {
    private FragmentActivity mActivity;
    private int adapterPosition;
    private RecyclerView.Adapter<?> adapter;
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

    public RecyclerView.Adapter<?> getAdapter() {
        return adapter;
    }

    public void setAdapter(RecyclerView.Adapter<?> adapter) {
        this.adapter = adapter;
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

    public void setPositionIndices(Integer[] positionIndices) {
        this.positionIndices = positionIndices;
    }

    public Integer[] getPositionIndices() {
        return positionIndices;
    }
}
