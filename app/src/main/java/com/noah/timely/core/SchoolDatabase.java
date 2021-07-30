package com.noah.timely.core;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.noah.timely.alarms.AlarmModel;
import com.noah.timely.assignment.AssignmentModel;
import com.noah.timely.courses.CourseModel;
import com.noah.timely.exam.ExamModel;
import com.noah.timely.gallery.Image;
import com.noah.timely.timetable.TimetableModel;
import com.noah.timely.util.CollectionUtils;
import com.noah.timely.util.Constants;
import com.noah.timely.util.LogUtils;
import com.noah.timely.util.ThreadUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static com.noah.timely.util.CollectionUtils.linearSearch;

/**
 * TimeLY's database manager
 */
public class SchoolDatabase extends SQLiteOpenHelper {

    public static final String TIMETABLE_MONDAY = "Monday";
    public static final String TIMETABLE_TUESDAY = "Tuesday";
    public static final String TIMETABLE_WEDNESDAY = "Wednesday";
    public static final String TIMETABLE_THURSDAY = "Thursday";
    public static final String TIMETABLE_FRIDAY = "Friday";
    public static final String TIMETABLE_SATURDAY = "Saturday";
    public static final String SCHEDULED_TIMETABLE = "Scheduled_Timetable";
    public static final String FIRST_SEMESTER = "First_Semester";
    public static final String SECOND_SEMESTER = "Second_Semester";
    public static final String COLUMN_INITIAL_POS = "Initial_Position";
    public static final String REGISTERED_COURSES = "Registered_Courses";

    private static final String ASSIGNMENT_TABLE = "Assignment";
    private static final String COLUMN_LECTURER_NAME = "Lecturer_Name";
    private static final String COLUMN_FULL_COURSE_NAME = "Full_Course_Name";
    private static final String COLUMN_END_TIME = "End_time";
    private static final String COLUMN_START_TIME = "Start_time";
    private static final String COLUMN_ASSIGNMENT_TITLE = "Title";
    private static final String COLUMN_ASSIGNMENT_DESCRIPTION = "Description";
    private static final String COLUMN_COURSE_CODE = "Course_Code";
    private static final String COLUMN_SUBMISSION_DATE = "Submission_Date";
    private static final String COLUMN_ATTACHED_PDF = "PDF";
    private static final String COLUMN_ATTACHED_IMAGE = "IMAGE";
    private static final String COLUMN_ID = "ID";

    private static final String COLUMN_DAY = "DAY";
    private static final String COLUMN_IMPORTANCE = "IMPORTANCE";
    private static final String ALARMS_TABLE = "Alarms";
    private static final String COLUMN_REPEAT_STAT = "Repeat";
    private static final String COLUMN_ON_STAT = "Status";
    private static final String COLUMN_TIME = "Time";
    private static final String COLUMN_RINGTONE_NAME = "Ringtone_Name";
    private static final String COLUMN_RINGTONE_URI = "Ringtone_Uri";
    private static final String COLUMN_REPEAT_DAYS = "Days";

    private static final String COLUMN_VIBRATE_STAT = "Vibrate";
    private static final String COLUMN_POS = "Position";
    private static final String COLUMN_LABEL = "Label";
    private static final String COLUMN_SUBMITTED = "Submitted";
    private static final String COLUMN_CREDITS = "Credits";
    private static final String COLUMN_CLASS_OVER_STAT = "Class_Over";
    private static final String COLUMN_EXAM_WEEK_COUNT = "Exam_Count";
    private static final String PREFERENCE_TABLE = "Preferences";
    private static final String COLUMN_WEEK = "Exam_Week";
    private static final String COLUMN_OFFSET_TIME = "Offset_Time";
    private static final String COLUMN_SNOOZED_TIME = "Snoozed_Time";
    private static final String COLUMN_SNOOZE_STAT = "Snoozed";

    private static final String TODO_TABLE = "Todo";
    private static final String COLUMN_TODO_CATEGORY = "Category";
    private static final String COLUMN_TODO_DESCRIPTION = "Description";
    private static final String COLUMN_TODO_TITLE = "Title";
    private static final String COLUMN_TODO_DATE = "Completion_date";
    private static final String COLUMN_TODO_TIME = "Completion_time";

    private static boolean mDeleting;

    private final String TAG = "SchoolDatabase";

    private final Context context;

    public SchoolDatabase(@Nullable Context context) {
        super(context, "SchoolDatabase.db", null, 2);
        this.context = context;
    }

    /**
     * Temporary fix to bug that exists when app crashes as a result of improper ended background task
     *
     * @return true if delete task is running
     */
    public static boolean isDeleteTaskRunning() {
        return mDeleting;
    }

    public List<DataModel> getTodos() {
        return null;
    }

    /**
     * Sort order of list's in the database
     */
    @SuppressWarnings("unused")
    public
    enum SortOrder {
        NATURAL_ORDER, UNORDERED, REVERSED_ORDERED
    }

    public Context getContext() {
        return context;
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of the app tables and the initial population of the tables should happen.
     *
     * @param db The apps database.
     * @author Noah
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // create all timetable
        createTimetable(db, TIMETABLE_MONDAY);
        createTimetable(db, TIMETABLE_TUESDAY);
        createTimetable(db, TIMETABLE_WEDNESDAY);
        createTimetable(db, TIMETABLE_THURSDAY);
        createTimetable(db, TIMETABLE_FRIDAY);
        createTimetable(db, TIMETABLE_SATURDAY);
//        createTimetable(db, TIMETABLE_SUNDAY);

        // CREATE THE SCHEDULED TIMETABLE
        createTimetable(db, SCHEDULED_TIMETABLE);

        // CREATE THE COURSE TABLE FOR BOTH FIRST AND SECOND SEMESTER
        createSemesterTable(db, FIRST_SEMESTER);
        createSemesterTable(db, SECOND_SEMESTER);
        createSemesterTable(db, REGISTERED_COURSES);

        int weekCount = 8;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String countValue = null;
        try {
            countValue = prefs.getString("exam weeks", "8");
            if (countValue != null) {
                weekCount = Integer.parseInt(countValue);
            }
        } catch (NumberFormatException exc) {
            Log.w(getClass().getSimpleName(),
                  "User specified week count of: " + countValue + " is ignored, using 8 weeks instead ");
        }

        createExamTables(db, weekCount);
        // CREATE ASSIGNMENT DATA TABLE
        String createAssignmentDB_stmt
                = "CREATE TABLE " + ASSIGNMENT_TABLE +
                "( " + COLUMN_ID + " INTEGER ,"
                + COLUMN_LECTURER_NAME + " TEXT , " +
                COLUMN_ASSIGNMENT_TITLE + " TEXT , " +
                COLUMN_ASSIGNMENT_DESCRIPTION + " TEXT , " +
                COLUMN_COURSE_CODE + " TEXT NOT NULL, " +
                COLUMN_SUBMISSION_DATE + " TEXT ," +
                COLUMN_ATTACHED_PDF + " TEXT , " +
                COLUMN_ATTACHED_IMAGE + " TEXT, " +
                COLUMN_SUBMITTED + " TEXT"
                + ")";

        // CREATE ALARM TABLE
        String createAlarmDB_stmt = "CREATE TABLE " + ALARMS_TABLE + " (" +
                COLUMN_POS + " INTEGER , " +
                COLUMN_INITIAL_POS + " INTEGER , " +
                COLUMN_TIME + " TEXT, " +
                COLUMN_VIBRATE_STAT + " TEXT, " +
                COLUMN_ON_STAT + " TEXT, " +
                COLUMN_REPEAT_DAYS + " TEXT, " +
                COLUMN_RINGTONE_NAME + " TEXT, " +
                COLUMN_REPEAT_STAT + " TEXT, " +
                COLUMN_LABEL + " TEXT, " +
                COLUMN_RINGTONE_URI + " TEXT, " +
                COLUMN_SNOOZE_STAT + " TEXT, " +
                COLUMN_SNOOZED_TIME + " TEXT "
                + ")";

        // CREATE PREFS TABLE
        String createPrefsDB_stmt = "CREATE TABLE " + PREFERENCE_TABLE + " (" + COLUMN_EXAM_WEEK_COUNT + " INTEGER )";

        db.execSQL(createPrefsDB_stmt);
        db.execSQL(createAlarmDB_stmt);
        db.execSQL(createAssignmentDB_stmt);

        // Insert default value in exam week count. Although it is not useful, as value will never
        // be retrieved, this is useful to use the update database operation on this table.
        ContentValues values = new ContentValues();
        values.put(COLUMN_EXAM_WEEK_COUNT, 8);

        db.insertOrThrow(PREFERENCE_TABLE, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // add or drop tables here
        if (oldVersion == 1 && newVersion == 2) createTodoListTable(db);
    }

    private void createTodoListTable(SQLiteDatabase db) {

        String createTodoTables_stmt = "CREATE TABLE " + TODO_TABLE + " (" +
                COLUMN_ID + " INTEGER, " +
                COLUMN_TODO_CATEGORY + " TEXT," +
                COLUMN_TODO_TITLE + " TEXT," +
                COLUMN_TODO_DESCRIPTION + " TEXT," +
                COLUMN_TODO_TIME + " TEXT," +
                COLUMN_TODO_DATE + " TEXT )";

        db.execSQL(createTodoTables_stmt);
    }

    private void createExamTables(SQLiteDatabase db, int weekCount) {
        for (int w = 1; w <= weekCount; w++) {
            String createExamTables_stmt = "CREATE TABLE " + "WEEK_" + w + " (" +
                    COLUMN_ID + " INTEGER, " +
                    COLUMN_WEEK + " TEXT, " +
                    COLUMN_DAY + " TEXT, " +
                    COLUMN_COURSE_CODE + " TEXT, " +
                    COLUMN_FULL_COURSE_NAME + " TEXT, " +
                    COLUMN_START_TIME + " INTEGER, " +
                    COLUMN_END_TIME + " INTEGER"
                    + ")";
            db.execSQL(createExamTables_stmt);
        }
    }

    // method to be called when creating all app's tables
    private void createSemesterTable(SQLiteDatabase db, String semester) {
        String createSemesterTable_stmt = "CREATE TABLE " + semester + " (" +
                COLUMN_ID + " INTEGER, " +
                COLUMN_COURSE_CODE + " TEXT, " +
                COLUMN_FULL_COURSE_NAME + " TEXT, " +
                COLUMN_CREDITS + " INTEGER"
                + ")";
        db.execSQL(createSemesterTable_stmt);
    }

    // method to be called to create all  timetables' database
    private void createTimetable(SQLiteDatabase db, String timetableName) {
        String createTimetableDB_stmt;

        if (timetableName.equals(SchoolDatabase.SCHEDULED_TIMETABLE)) {
            // CREATE SCHEDULED TIMETABLE
            createTimetableDB_stmt = "CREATE TABLE " + timetableName +
                    "( " + COLUMN_ID + " INTEGER ,"
                    + COLUMN_LECTURER_NAME + " TEXT, " +
                    COLUMN_COURSE_CODE + " TEXT NOT NULL, " +
                    COLUMN_FULL_COURSE_NAME + " TEXT, " +
                    COLUMN_END_TIME + " TEXT, " +
                    COLUMN_START_TIME + " TEXT, " +
                    COLUMN_OFFSET_TIME + " INTEGER, " +
                    COLUMN_DAY + " TEXT, " +
                    COLUMN_IMPORTANCE + " TEXT "
                    + ")";
        } else {
            // CREATE NORMAL TIMETABLE
            createTimetableDB_stmt = "CREATE TABLE " + timetableName + "( " +
                    COLUMN_ID + " INTEGER ," +
                    COLUMN_CLASS_OVER_STAT + " TEXT, " +
                    COLUMN_FULL_COURSE_NAME + " TEXT, " +
                    COLUMN_COURSE_CODE + " TEXT, " +
                    COLUMN_END_TIME + " TEXT , " +
                    COLUMN_START_TIME + " TEXT, " +
                    COLUMN_DAY + " TEXT "
                    + ")";
        }

        db.execSQL(createTimetableDB_stmt);
    }

    /**
     * inserts assignment data to app's database
     *
     * @param assignment the data model to be inserted into database
     * @return true if the data was inserted
     * @author Noah Wesley
     */
    public boolean addAssignmentData(AssignmentModel assignment) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues assignmentData = new ContentValues();

        assignmentData.put(COLUMN_ID, assignment.getPosition());
        assignmentData.put(COLUMN_LECTURER_NAME, sanitizeEntry(assignment.getLecturerName()));
        assignmentData.put(COLUMN_ASSIGNMENT_DESCRIPTION, sanitizeEntry(assignment.getDescription()));
        assignmentData.put(COLUMN_ASSIGNMENT_TITLE, sanitizeEntry(assignment.getTitle()));
        assignmentData.put(COLUMN_COURSE_CODE, assignment.getCourseCode());
        assignmentData.put(COLUMN_SUBMISSION_DATE, assignment.getSubmissionDate());
        assignmentData.put(COLUMN_ATTACHED_IMAGE, assignment.getAttachedImages());
        assignmentData.put(COLUMN_ATTACHED_PDF, assignment.getAttachedPDF());
        assignmentData.put(COLUMN_SUBMITTED, assignment.isSubmitted());

        long isInserted = db.insert(ASSIGNMENT_TABLE, null, assignmentData);
        return isInserted != -1;
    }

    /**
     * Simply checks assignments database for any other entry that matches this assignment's data
     *
     * @return true if a duplicate assignment was found
     */
    public boolean isAssignmentPresent(AssignmentModel model) {
        String findAssignmentStmt = "SELECT * FROM " + ASSIGNMENT_TABLE
                + " WHERE " + COLUMN_ASSIGNMENT_TITLE + " = '" + sanitizeEntry(model.getTitle())
                + "' AND " + COLUMN_SUBMISSION_DATE + " = '" + model.getSubmissionDate()
                + "' AND " + COLUMN_LECTURER_NAME + " = '" + sanitizeEntry(model.getLecturerName())
                + "' AND " + COLUMN_COURSE_CODE + " = '" + model.getCourseCode() + "'";

        SQLiteDatabase db = getReadableDatabase();

        Cursor findCursor = db.rawQuery(findAssignmentStmt, null);
        findCursor.close();

        return findCursor.getColumnCount() > 0;
    }

    private String sanitizeEntry(String data) {
        // replace with character code value
        return data.replaceAll("'", "%39");
    }

    private String retrieveEntry(String data) {
        // replace with real value
        return data.replaceAll("%39", "'");
    }

    /**
     * inserts timetable data to app's database
     *
     * @param timetable     the data model to be inserted into database
     * @param timetableName the timetable name
     * @return true if the data was inserted
     * @author Noah Wesley
     */
    public int[] addTimeTableData(TimetableModel timetable, String timetableName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues timeTableData = new ContentValues();

        int lastId = getLastTimetableId(timetableName);

        timetable.setId(++lastId);  // to later be retrieved for chronology search
        timeTableData.put(COLUMN_ID, timetable.getId());
        timeTableData.put(COLUMN_FULL_COURSE_NAME, sanitizeEntry(timetable.getFullCourseName()));
        timeTableData.put(COLUMN_END_TIME, timetable.getEndTime());
        timeTableData.put(COLUMN_START_TIME, timetable.getStartTime());
        timeTableData.put(COLUMN_COURSE_CODE, timetable.getCourseCode());
        timeTableData.put(COLUMN_DAY, timetable.getDay());

        if (timetableName.equals(SCHEDULED_TIMETABLE)) {
            timeTableData.put(COLUMN_IMPORTANCE, timetable.getImportance());
            timeTableData.put(COLUMN_LECTURER_NAME, sanitizeEntry(timetable.getLecturerName()));
        }
        long resCode = db.insertOrThrow(timetableName, null, timeTableData);
        // Start timetable's chronological order search
        if (timetableName.equals(SCHEDULED_TIMETABLE)) {
            // Begin exam sorting
            Cursor sortElementCursor = db.rawQuery("SELECT " + COLUMN_START_TIME
                                                           + "," + COLUMN_DAY
                                                           + "," + COLUMN_ID
                                                           + " FROM " + timetableName, null);

            List<TimetableModel> timetables = new ArrayList<>();
            while (sortElementCursor.moveToNext()) {
                TimetableModel timetable1 = new TimetableModel();
                timetable1.setStartTime(sortElementCursor.getString(0));
                timetable1.setDay(sortElementCursor.getString(1));
                timetable1.setId(sortElementCursor.getInt(2));
                timetables.add(timetable1);
            }

            Collections.sort(timetables, (t1, t2) -> {
                int cmp = Integer.compare(t1.getDayIndex(), t2.getDayIndex());
                if (cmp != 0) return cmp;
                else return Integer.compare(t1.getStartTimeAsInt(), t2.getStartTimeAsInt());
            });

            sortElementCursor.close();
            // Compare all the former timetable entries in database with this timetable entry
            Comparator<? super TimetableModel> idComparator = (t1, t2) -> Integer.compare(t1.getId(), t2.getId());
            // impossible to sort timetable entries, linear-search instead
            int searchIndex = linearSearch(timetables, timetable, idComparator);
            return resCode != -1 ? new int[]{searchIndex, timetable.getId()}
                                 : new int[]{-1, -1};
        } else {
            List<String> sList = new ArrayList<>();
            Cursor chronologyCursor = db.rawQuery("SELECT " + COLUMN_START_TIME
                                                          + " FROM " + timetableName, null);

            while (chronologyCursor.moveToNext()) sList.add(chronologyCursor.getString(0));

            List<Integer> sIList = new ArrayList<>();
            for (String s : sList) {
                String[] ss = s.split(":");
                sIList.add(Integer.parseInt(ss[0] + ss[1]));
            }
            Collections.sort(sIList);

            chronologyCursor.close();
            int searchIndex = Collections.binarySearch(sIList, timetable.getStartTimeAsInt());
            return (resCode != -1) ? new int[]{searchIndex, timetable.getId()}
                                   : new int[]{-1, -1};
        }
    }

    /**
     * Gets the last ID of last inserted assignment
     *
     * @return the last id
     * @author Noah Wesley
     */
    public int getLastAssignmentId() {
        String getIdStmt = "SELECT " + COLUMN_ID + " FROM " + ASSIGNMENT_TABLE;
        SQLiteDatabase db = getReadableDatabase();
        Cursor idCursor = db.rawQuery(getIdStmt, null);

        int last;
        if (idCursor.moveToLast()) last = idCursor.getInt(idCursor.getColumnCount() - 1);
        else last = -1;

        idCursor.close();
        return last;
    }


    /**
     * Gets the last ID of last inserted timetable data
     *
     * @return the last id
     * @author Noah Wesley
     */
    public int getLastTimetableId(String timetableName) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor idCursor = db.rawQuery("SELECT " + COLUMN_ID + " FROM " + timetableName, null);

        int last;
        if (idCursor.moveToLast()) last = idCursor.getInt(idCursor.getColumnCount() - 1);
        else last = -1;

        idCursor.close();
        return last;
    }

    /**
     * Gets the last ID of last inserted alarm
     *
     * @return the last id
     * @author Noah Wesley
     */
    public int getLastAlarmId() {
        String getIdStmt = "SELECT " + "MAX(" + COLUMN_POS + ") FROM " + ALARMS_TABLE;
        SQLiteDatabase db = getReadableDatabase();
        Cursor lastId = db.rawQuery(getIdStmt, null);

        int last = 0;
        if (getAlarmCount(db) == 0) return -1;
        if (lastId.moveToLast()) last = lastId.getInt(0);
        lastId.close();

        return last;
    }

    /**
     * retrieves a List of all the assignments added to the database
     *
     * @return the list
     * @author Noah Wesley
     */
    public List<DataModel> getAssignmentData() {

        List<DataModel> data = new ArrayList<>();
        String selectStmt
                = "SELECT " + COLUMN_ASSIGNMENT_TITLE + ", "
                + COLUMN_SUBMISSION_DATE + ", "
                + COLUMN_ASSIGNMENT_DESCRIPTION + ", "
                + COLUMN_LECTURER_NAME + ", "
                + COLUMN_COURSE_CODE + ", "
                + COLUMN_ATTACHED_IMAGE + ", "
                + COLUMN_ID + ", "
                + COLUMN_SUBMITTED + " FROM " + ASSIGNMENT_TABLE;

        SQLiteDatabase db = getReadableDatabase();
        Cursor result = db.rawQuery(selectStmt, null);

        if (result.moveToFirst()) {
            do {
                String title = result.getString(0);
                String date = result.getString(1);
                String description = result.getString(2);
                String lecturerName = result.getString(3);
                String course = result.getString(4);
                String attachedImages = result.getString(5);
                int id = result.getInt(6);
                boolean submitted = result.getInt(7) == 1;

                AssignmentModel assignment = new AssignmentModel();
                assignment.setTitle(retrieveEntry(title));
                assignment.setDate(date);
                assignment.setSubmissionDate(date);
                assignment.setDescription(retrieveEntry(description));
                assignment.setLecturerName(retrieveEntry(lecturerName));
                assignment.setCourseCode(course);
                assignment.setAttachedImage(attachedImages);
                assignment.setPosition(id);
                assignment.setSubmitted(submitted);

                data.add(assignment);
            } while (result.moveToNext());

        }
        result.close();
        return data;
    }

    /**
     * @return a list of the combination of all normal timetables; MONDAY, TUESDAY, WEDNESDAY,
     * THURSDAY, FRIDAY AND SATURDAY
     */
    public List<DataModel> getAllNormalSchoolTimetable() {
        SQLiteDatabase db = getReadableDatabase();

        List<DataModel> data = new ArrayList<>(); // timetable list to be retrieved

        // query database, merge all normal timetables together all into one
        String selectStmt = "SELECT * "
                + " FROM " + TIMETABLE_MONDAY
                + " UNION ALL " +
                " SELECT * "
                + " FROM " + TIMETABLE_TUESDAY
                + " UNION ALL " +
                " SELECT * "
                + " FROM " + TIMETABLE_WEDNESDAY
                + " UNION ALL " +
                " SELECT *"
                + " FROM " + TIMETABLE_THURSDAY
                + " UNION ALL " +
                " SELECT * "
                + " FROM " + TIMETABLE_FRIDAY
                + " UNION ALL " +
                " SELECT * "
                + " FROM " + TIMETABLE_SATURDAY;

        Cursor result = db.rawQuery(selectStmt, null);

        // query cursor, retrieving  column data, incrementing rows
        while (result.moveToNext()) {

            TimetableModel model = new TimetableModel();
            model.setId(result.getInt(0));
            model.setClassOver(Boolean.parseBoolean(result.getString(1)));
            model.setFullCourseName(result.getString(2));
            model.setCourseCode(result.getString(3));
            model.setEndTime(result.getString(4));
            model.setStartTime(result.getString(5));
            model.setDay(result.getString(6));
            // after query, add to the list of timetables to be retrieved
            data.add(model);
        }

        result.close();
        return data;
    }

    /**
     * retrieves a List of all the timetable entries added to the database
     *
     * @return the list
     * @author Noah Wesley
     */
    public List<DataModel> getTimeTableData(String timetableName) {
        SQLiteDatabase db = getReadableDatabase();

        List<DataModel> data = new ArrayList<>();
        String selectStmt;

        if (timetableName.equals(SchoolDatabase.SCHEDULED_TIMETABLE)) {
            selectStmt = "SELECT " + COLUMN_LECTURER_NAME + ", "
                    + COLUMN_START_TIME + ", "
                    + COLUMN_END_TIME + ", " +
                    COLUMN_FULL_COURSE_NAME + ", " +
                    COLUMN_COURSE_CODE + ", " +
                    COLUMN_ID + ", " +
                    COLUMN_IMPORTANCE + ", " +
                    COLUMN_DAY + " FROM " + timetableName;

        } else {
            selectStmt = "SELECT "
                    + COLUMN_START_TIME + ", "
                    + COLUMN_END_TIME + ", " +
                    COLUMN_FULL_COURSE_NAME + ", " +
                    COLUMN_COURSE_CODE + ", " +
                    COLUMN_ID + ", " +
                    COLUMN_CLASS_OVER_STAT +
                    " FROM " + timetableName;
        }

        Cursor result = db.rawQuery(selectStmt, null);
        if (result.moveToFirst()) {
            do {
                TimetableModel model = new TimetableModel();
                String startTime = result.getString(0);
                String endTime = result.getString(1);
                String fullCourseName = result.getString(2);
                int id = result.getInt(4);
                String courseCode = result.getString(3);
                // for scheduled timetable only, will be null for normal timetable
                if (timetableName.equals(SCHEDULED_TIMETABLE)) {
                    String lecturerName = result.getString(0);
                    startTime = result.getString(1);
                    endTime = result.getString(2);
                    fullCourseName = result.getString(3);
                    courseCode = result.getString(4);
                    id = result.getInt(5);
                    String importance = result.getString(6);
                    String day = result.getString(7);
                    model.setImportance(importance);
                    model.setDay(day);
                    model.setLecturerName(retrieveEntry(lecturerName));
                } else {
                    // For normal timetable only
                    model.setClassOver(result.getInt(5) == 1);
                }
                // General timetable data
                model.setCourseCode(courseCode);
                model.setStartTime(startTime);
                model.setEndTime(endTime);
                model.setFullCourseName(retrieveEntry(fullCourseName));
                model.setId(id);

                data.add(model);
            } while (result.moveToNext());

        }
        result.close();
        return data;
    }


    /**
     * <p><Strong>Description</Strong></p>
     * This method deletes assignment from the app's database
     *
     * @param entry the particular assignment to be deleted
     * @return true, if assignment was deleted from database
     * @author Noah Wesley
     */
    boolean deleteAssignmentEntry(AssignmentModel entry) {
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = COLUMN_ID + "= ?";
        String[] whereArg = {String.valueOf(entry.getPosition())};

        int resultCode = db.delete(ASSIGNMENT_TABLE, whereClause, whereArg);
        return resultCode != -1;
    }

    /**
     * <p><Strong>Description</Strong></p>
     * This method deletes timetable from the app's database
     *
     * @param entry the particular timetable to be deleted
     * @return true, if assignment was deleted from database
     * @author Noah Wesley
     */
    boolean deleteTimetableEntry(TimetableModel entry, String tableName) {
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = COLUMN_ID + "= ?";
        String[] whereArg = {String.valueOf(entry.getId())};

        int resultCode = db.delete(tableName, whereClause, whereArg);
        return resultCode != -1;
    }

    /**
     * Simply adds a single alarm to the app's database
     *
     * @param alarm the alarm to be added
     * @return true if alarm was successfully added
     */
    public boolean addAlarm(AlarmModel alarm) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues alarmData = new ContentValues();
        alarmData.put(COLUMN_POS, alarm.getPosition());
        alarmData.put(COLUMN_INITIAL_POS, alarm.getInitialPosition());
        alarmData.put(COLUMN_TIME, alarm.getTime());
        alarmData.put(COLUMN_ON_STAT, String.valueOf(alarm.isOn()));
        alarmData.put(COLUMN_REPEAT_STAT, String.valueOf(alarm.isRepeated()));
        alarmData.put(COLUMN_VIBRATE_STAT, String.valueOf(alarm.isVibrate()));
        alarmData.put(COLUMN_REPEAT_DAYS, TextUtils.join(",", alarm.getRepeatDays()));
        alarmData.put(COLUMN_SNOOZE_STAT, "false");

        long isInserted = db.insertOrThrow(ALARMS_TABLE, null, alarmData);
        return isInserted != -1;
    }

    /**
     * Simply return all the alarms in the database
     *
     * @return the List of Alarms added to the app's database
     */
    public List<DataModel> getAlarms() {
        SQLiteDatabase db = getReadableDatabase();
        List<DataModel> alarms = new LinkedList<>();

        String alarmsQuery_stmt =
                "SELECT " + COLUMN_POS
                        + ", " + COLUMN_TIME
                        + ", " + COLUMN_ON_STAT
                        + ", " + COLUMN_REPEAT_STAT
                        + ", " + COLUMN_RINGTONE_NAME
                        + ", " + COLUMN_REPEAT_DAYS
                        + ", " + COLUMN_VIBRATE_STAT
                        + ", " + COLUMN_LABEL
                        + ", " + COLUMN_SNOOZE_STAT
                        + ", " + COLUMN_SNOOZED_TIME
                        + " FROM " + ALARMS_TABLE;

        Cursor alarmCursor = db.rawQuery(alarmsQuery_stmt, null);

        while (alarmCursor.moveToNext()) {
            int pos = alarmCursor.getInt(0);
            String time = alarmCursor.getString(1);
            String ringtone = alarmCursor.getString(4);
            String rD = alarmCursor.getString(5);
            String[] repeatDays = rD != null ? rD.split(",") : null;
            boolean vibrate = Boolean.parseBoolean(alarmCursor.getString(6));
            boolean onStat = Boolean.parseBoolean(alarmCursor.getString(2));
            boolean repeatStat = Boolean.parseBoolean(alarmCursor.getString(3));
            String label = alarmCursor.getString(7);
            boolean isSnoozed = Boolean.parseBoolean(alarmCursor.getString(8));
            String snoozedTime = alarmCursor.getString(9);

            if (!TextUtils.isEmpty(label)) retrieveEntry(label);

            AlarmModel alarmModel = new AlarmModel(pos, time, onStat, repeatStat, ringtone, repeatDays, vibrate, label);
            alarmModel.setSnoozed(isSnoozed);
            alarmModel.setSnoozedTime(snoozedTime);
            alarms.add(alarmModel);
        }

        alarmCursor.close();
        return alarms;
    }

    /**
     * Gets the snoozed state of a particular alarm at position specified by <code>position</code>
     *
     * @param position the position of the alarm in alarm database
     * @return the snoozed state
     */
    public String[] getAlarmSnoozeStateAt(int position) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor stateCursor =
                db.rawQuery("SELECT " + COLUMN_SNOOZE_STAT + ", "
                                    + COLUMN_SNOOZED_TIME +
                                    " FROM " + ALARMS_TABLE +
                                    " WHERE " + COLUMN_POS + " = " + position, null);

        String snoozedState = null, snoozedTime = null;

        while (stateCursor.moveToNext()) {
            snoozedState = stateCursor.getString(0);
            snoozedTime = stateCursor.getString(1);
        }

        stateCursor.close();
        return new String[]{snoozedState, snoozedTime};
    }

    /**
     * Updates the state of the alarm
     *
     * @param pos   the id in the database
     * @param state the state of the switch
     */
    public boolean updateAlarmState(int pos, boolean state) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues statValue = new ContentValues();
        statValue.put(COLUMN_ON_STAT, String.valueOf(state));

        int resultCode = db.update(ALARMS_TABLE, statValue, COLUMN_POS + " = " + pos, null);
        return resultCode != -1;
    }

    /**
     * Updates the repeat stat of the alarm
     *
     * @param pos      the id in the database
     * @param isRepeat the state of the checkbox
     * @author Noah
     */
    public void updateAlarmRepeatStatus(int pos, boolean isRepeat) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues repeatValue = new ContentValues();
        repeatValue.put(COLUMN_REPEAT_STAT, String.valueOf(isRepeat));

        db.update(ALARMS_TABLE, repeatValue, COLUMN_POS + " = " + pos, null);
    }

    /**
     * This method deletes assignment from the app's database
     *
     * @param entry the particular alarm to be deleted
     * @return true, if assignment was deleted from database
     * @author Noah
     */
    public boolean deleteAlarmEntry(AlarmModel entry) {
        mDeleting = true;
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = COLUMN_POS + "= ?";
        String[] whereArg = {String.valueOf(entry.getPosition())};

        int resultCode = db.delete(ALARMS_TABLE, whereClause, whereArg);
        // after delete, re-arrange alarm position. This is a background task
        ThreadUtils.runBackgroundTask(this::re_arrangeAlarmPosition);
        return resultCode != -1;
    }

    /**
     * Updates the repeat stat of the alarm
     *
     * @param pos       the id in the database
     * @param isVibrate the state of the checkbox
     * @author Noah
     */
    public boolean updateAlarmVibrateStatus(int pos, boolean isVibrate) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues repeatValue = new ContentValues();
        repeatValue.put(COLUMN_VIBRATE_STAT, String.valueOf(isVibrate));

        int resultCode = db.update(ALARMS_TABLE, repeatValue, COLUMN_POS + " = " + pos, null);
        return resultCode != -1;
    }

    /**
     * retrieves repeat stat of the alarm at pos
     *
     * @param pos the id in the database
     * @return true if alarm was set to vibrate, false otherwise
     * @author Noah
     */
    public boolean getAlarmVibrateStatus(int pos) {
        boolean isVibrate = false;
        SQLiteDatabase db = getWritableDatabase();

        String getAlarmVibrateStatusStmt = "SELECT " + COLUMN_VIBRATE_STAT
                + " FROM " + ALARMS_TABLE + " WHERE " + COLUMN_POS + " = " + pos;

        Cursor vibrateStatCursor = db.rawQuery(getAlarmVibrateStatusStmt, null);
        if (vibrateStatCursor.moveToNext()) isVibrate = Boolean.parseBoolean(vibrateStatCursor.getString(0));

        vibrateStatCursor.close();
        return isVibrate;
    }

    /**
     * Get alarm count, private to class only
     *
     * @return the note entry count in database
     * @author Noah
     */
    private int getAlarmCount() {
        int noteCount = 0;
        String countStmt = "SELECT COUNT(*) FROM " + ALARMS_TABLE;
        SQLiteDatabase db = getReadableDatabase();

        Cursor count = db.rawQuery(countStmt, null);
        if (count.moveToLast()) noteCount = count.getInt(0);

        count.close();
        return noteCount;
    }

    /**
     * Get alarm count, but the SQLiteDatabase instance used in the method won't be closed !!
     * Use {@link SchoolDatabase#getAlarmCount()} instead, if you would like it to be closed
     *
     * @return the note entry count in database
     * @author Noah
     */
    private int getAlarmCount(SQLiteDatabase db) {
        int alarmCount = 0;
        String countStmt = "SELECT COUNT(*) FROM " + ALARMS_TABLE;

        Cursor count = db.rawQuery(countStmt, null);
        if (count.moveToLast()) alarmCount = count.getInt(0);

        count.close();
        return alarmCount;
    }

    /**
     * Re-arrange alarm position so there is no gap or skipping of position values.
     * This function will be useful when deleting or adding alarm
     *
     * @author Noah
     */

    void re_arrangeAlarmPosition() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        int alarmCount = getAlarmCount();

        String updateStmt = "SELECT " + COLUMN_POS + " FROM " + ALARMS_TABLE;
        SQLiteDatabase db = getWritableDatabase();

        Cursor data = db.rawQuery(updateStmt, null);
        data.moveToFirst();

        if (data.getColumnCount() == 0) return;

        int pos;
        for (int updateVal = 0; updateVal < alarmCount; updateVal++) {
            pos = data.getInt(0);    // get alarm position from row
            ContentValues value = new ContentValues();
            value.put(COLUMN_POS, updateVal);    // set value for update
            db.update(ALARMS_TABLE, value, COLUMN_POS + " = " + pos, null);

            data.moveToNext();
        }  // end for
        data.close();
        mDeleting = false;
    }

    /**
     * Update the selected days obtained from toggling the state of the round buttons
     *
     * @param pos          the position in the list
     * @param selectedDays the selected days
     * @return true if selectedDays was updated in the database
     * @author Noah
     */
    public boolean updateSelectedDays(int pos, Boolean[] selectedDays) {
        String selectedDay = TextUtils.join(",", selectedDays);
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_REPEAT_DAYS, selectedDay);

        int updated = db.update(ALARMS_TABLE, values, COLUMN_POS + " = " + pos, null);
        return updated != -1;
    }

    /**
     * Get the selected days stored in the database
     *
     * @param pos the position in the database
     * @return the selected days, as an array of states
     * @author Noah
     */
    public Boolean[] getSelectedDays(int pos) {
        SQLiteDatabase db = getReadableDatabase();
        String getSelectedDays_stmt = "SELECT " + COLUMN_REPEAT_DAYS +
                " FROM " + ALARMS_TABLE + " WHERE " + COLUMN_POS + " = " + pos;

        Cursor daysCursor = db.rawQuery(getSelectedDays_stmt, null);
        daysCursor.moveToFirst();

        Boolean[] days = convertToBooleanArray(daysCursor.getString(0).split(","));

        daysCursor.close();
        return days;
    }

    // Convert database string array into the required boolean values.
    private Boolean[] convertToBooleanArray(String[] repeatDays) {
        boolean _1 = Boolean.parseBoolean(repeatDays[0]);
        boolean _2 = Boolean.parseBoolean(repeatDays[1]);
        boolean _3 = Boolean.parseBoolean(repeatDays[2]);
        boolean _4 = Boolean.parseBoolean(repeatDays[3]);
        boolean _5 = Boolean.parseBoolean(repeatDays[4]);
        boolean _6 = Boolean.parseBoolean(repeatDays[5]);
        boolean _7 = Boolean.parseBoolean(repeatDays[6]);
        return new Boolean[]{_1, _2, _3, _4, _5, _6, _7};
    }

    /**
     * Update the former alarm's time to the current one specified by time
     *
     * @param pos  the position in the database
     * @param time the time to insert into the database
     * @author Noah
     */
    public void updateTime(int pos, String time) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TIME, time);

        db.update(ALARMS_TABLE, values, COLUMN_POS + " = " + pos, null);
        db.close();
    }

    /**
     * Update the former alarm's snooze time to the current one specified by time. At the same
     * time, this would set the alarm's status specified at <code>pos</code> to be snoozed.
     *
     * @param pos  the position in the database
     * @param time the time to insert into the database
     * @author Noah
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean updateSnoozedTime(int pos, String time) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_SNOOZED_TIME, time);
        values.put(COLUMN_SNOOZE_STAT, "true");

        long resCode = db.update(ALARMS_TABLE, values, COLUMN_POS + " = " + pos, null);
        db.close();
        return resCode != -1;
    }

    /**
     * Update the former alarm's ringtone to the current selected ringtone
     *
     * @param pos          the position in the database
     * @param ringtoneName the ringtone to insert into the database
     * @param ringtoneUri  the ringtone's uri to be inserted into the database
     */
    public void updateRingtone(int pos, String ringtoneName, Uri ringtoneUri) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_RINGTONE_NAME, ringtoneName);
        values.put(COLUMN_RINGTONE_URI, ringtoneUri.toString());

        db.update(ALARMS_TABLE, values, COLUMN_POS + " = " + pos, null);
    }

    /**
     * Update the former alarm's label to the current one
     *
     * @param pos   the position in the database
     * @param label the new label
     */
    public void updateAlarmLabel(int pos, String label) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_LABEL, sanitizeEntry(label));

        db.update(ALARMS_TABLE, values, COLUMN_POS + " = " + pos, null);
    }

    /**
     * Gets the proper ringtone uri of the selected ringtone for the alarm
     *
     * @param position the position of the alarm in the database
     * @return the string representation of the uri
     */
    public String getRingtoneURIAt(int position) {
        SQLiteDatabase db = getReadableDatabase();
        String getRingtone_stmt = "SELECT " + COLUMN_RINGTONE_URI + " FROM " + ALARMS_TABLE
                + " WHERE " + COLUMN_POS + " = " + position;
        Cursor ringtoneCursor = db.rawQuery(getRingtone_stmt, null);
        String ringtoneURI = null;
        if (ringtoneCursor.moveToFirst()) {
            ringtoneURI = ringtoneCursor.getString(0);
        }
        ringtoneCursor.close();
        return ringtoneURI;
    }

    /**
     * Gets a particular alarm at a particular index
     *
     * @param position the position of the alarm in the database
     * @return the alarm at the position specified by position
     */
    public DataModel getAlarmAt(int position) {
        SQLiteDatabase db = getReadableDatabase();
        DataModel model = null; // The alarm to be returned

        // Get the alarm at the specified position by filtering the database query with the
        // WHERE clause.
        String alarmsQuery_stmt =
                "SELECT " + COLUMN_POS
                        + ", " + COLUMN_TIME
                        + ", " + COLUMN_ON_STAT
                        + ", " + COLUMN_REPEAT_STAT
                        + ", " + COLUMN_RINGTONE_NAME
                        + ", " + COLUMN_REPEAT_DAYS
                        + ", " + COLUMN_VIBRATE_STAT
                        + ", " + COLUMN_LABEL
                        + " FROM " + ALARMS_TABLE
                        + " WHERE " + COLUMN_POS + " = " + position;

        Cursor alarmCursor = db.rawQuery(alarmsQuery_stmt, null);

        if (alarmCursor.moveToFirst()) {
            int pos = alarmCursor.getInt(0);
            String time = alarmCursor.getString(1);
            String ringtone = alarmCursor.getString(4);
            String rD = alarmCursor.getString(5);
            String[] repeatDays = rD != null ? rD.split(",") : null;
            boolean vibrate = Boolean.parseBoolean(alarmCursor.getString(6));
            boolean onStat = Boolean.parseBoolean(alarmCursor.getString(2));
            boolean repeatStat = Boolean.parseBoolean(alarmCursor.getString(3));
            String label = alarmCursor.getString(7);
            model = new AlarmModel(pos, time, onStat, repeatStat, ringtone, repeatDays, vibrate, label);
        }
        alarmCursor.close();
        return model;
    }

    /**
     * Get the required alarm database label at the specified index
     *
     * @param pos the position of the label in the database to be retrieved
     * @return the label at the specified index
     */
    public String getInitialAlarmLabelAt(int pos) {
        SQLiteDatabase db = getReadableDatabase();
        String getStmt = "SELECT " + COLUMN_LABEL + " FROM " + ALARMS_TABLE
                + " WHERE " + COLUMN_INITIAL_POS + " = " + pos;
        String label = null;
        Cursor getCursor = db.rawQuery(getStmt, null);
        if (getCursor.moveToFirst())
            label = getCursor.getString(0);
        getCursor.close();

        return label;
    }

    /**
     * Runs a query, to find out if the alarm with time <code>time</code> exists
     *
     * @param time the time string to use to get the search result
     * @return true if the alarm exists
     */
    public boolean isAlarmPresent(String time) {
        SQLiteDatabase db = getReadableDatabase();
        String findStmt = "SELECT * FROM " + ALARMS_TABLE + " WHERE " + COLUMN_TIME + " = '" + time + "'";
        Cursor findCursor = db.rawQuery(findStmt, null);

        boolean isPresent = findCursor.moveToFirst();
        findCursor.close();
        return isPresent;
    }

    /**
     * Gets the most important alarm data (label and time)
     *
     * @param pos the index in the database
     * @return a string array representing the basic alarm data
     */
    public String[] getElementaryAlarmDataAt(int pos) {
        SQLiteDatabase db = getReadableDatabase();
        String getStmt = "SELECT " + COLUMN_LABEL + "," + COLUMN_TIME
                + " FROM " + ALARMS_TABLE
                + " WHERE " + COLUMN_POS + " = " + pos;

        String label = null, time = null;

        Cursor getCursor = db.rawQuery(getStmt, null);
        if (getCursor.moveToFirst()) {
            label = getCursor.getString(0);
            time = getCursor.getString(1);
        }
        getCursor.close();

        return new String[]{label, time};
    }

    /**
     * Get the required alarm database time at the specified index.
     * Because of alarm delete operation, position of the alarm might change and this index is
     * invalid, so I saved the initial index in {@link SchoolDatabase#COLUMN_INITIAL_POS}, so that
     * when alarm tries to get the time of the set alarm, it refers to the initial version used
     * to trigger the alarm. <b>Clever, right ??</b> :)
     *
     * @param pos the initial position of the time string in the database to be retrieved
     * @return the time string at the specified initial position
     */

    public String getTimeAtInitialPosition(int pos) {
        SQLiteDatabase db = getReadableDatabase();
        String getStmt = "SELECT " + COLUMN_TIME + " FROM " + ALARMS_TABLE
                + " WHERE " + COLUMN_INITIAL_POS + " = " + pos;
        String time = null;
        Cursor getCursor = db.rawQuery(getStmt, null);
        if (getCursor.moveToFirst()) time = getCursor.getString(0);
        getCursor.close();

        return time;
    }

    /**
     * Get the required alarm database's snoozed time at the specified index.
     * Because of alarm delete operation, position of the alarm might change and this index is
     * invalid, so I saved the initial index in {@link SchoolDatabase#COLUMN_INITIAL_POS}, so that
     * when alarm tries to get the time of the set alarm, it refers to the initial version used
     * to trigger the alarm. <b>Clever, right ??</b> :). The only difference between this function
     * and {@link SchoolDatabase#getTimeAtInitialPosition(int)} is that, it returns the snoozed
     * alarm time if the alarm was previously snoozed, but returns the original time if the alarm
     * wasn't snoozed.
     *
     * @param pos the initial position of the time string in the database to be retrieved
     * @return the time string at the specified initial position
     */

    public String getSnoozedTimeAtPosition(int pos) {
        SQLiteDatabase db = getReadableDatabase();
        String getStmt
                = "SELECT " + COLUMN_SNOOZE_STAT + ", " + COLUMN_SNOOZED_TIME + ", " + COLUMN_TIME
                + " FROM " + ALARMS_TABLE
                + " WHERE " + COLUMN_POS + " = " + pos;

        String originalTime = null, snoozedTime = null;
        boolean isAlarmSnoozed = false;

        Cursor getCursor = db.rawQuery(getStmt, null);

        if (getCursor.moveToFirst()) {
            isAlarmSnoozed = Boolean.parseBoolean(getCursor.getString(0));
            snoozedTime = getCursor.getString(1);
            originalTime = getCursor.getString(2);
        }

        getCursor.close();

        return isAlarmSnoozed ? snoozedTime : originalTime;
    }

    /**
     * Another variation of {@link SchoolDatabase#updateAlarmState(int, boolean)}, but doesn't
     * update at the position specified by <code>pos</code>, but instead this <code>pos</code>
     * represents the former position used to set the alarm, which is final and will not change
     * unlike the normal alarm position
     *
     * @param pos   the initial position in the database to be retrieved
     * @param state the new state
     */
    public void updateAlarmStateFromInitialPosition(int pos, boolean state) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues statValue = new ContentValues();
        statValue.put(COLUMN_ON_STAT, String.valueOf(state));
        statValue.put(COLUMN_SNOOZE_STAT, "false");

        db.update(ALARMS_TABLE, statValue, COLUMN_INITIAL_POS + " = " + pos, null);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean updateAlarmStatesAt(int dataPos, boolean snoozed, boolean on) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_SNOOZE_STAT, snoozed);
        values.put(COLUMN_ON_STAT, String.valueOf(on));

        long resCode = db.update(ALARMS_TABLE, values, COLUMN_POS + " = " + dataPos, null);
        db.close();
        return resCode != -1;
    }

    /**
     * Simply updates the assignment in the database at the specified index at
     * {@link AssignmentModel#getPosition()}
     *
     * @param model the assignment to replace the data in assignment database
     * @return true if the assignment was updated
     */
    public boolean updateAssignmentData(AssignmentModel model) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues assignmentData = new ContentValues();

        assignmentData.put(COLUMN_ID, model.getPosition());
        assignmentData.put(COLUMN_LECTURER_NAME, sanitizeEntry(model.getLecturerName()));
        assignmentData.put(COLUMN_ASSIGNMENT_DESCRIPTION, sanitizeEntry(model.getDescription()));
        assignmentData.put(COLUMN_ASSIGNMENT_TITLE, sanitizeEntry(model.getTitle()));
        assignmentData.put(COLUMN_COURSE_CODE, model.getCourseCode());
        assignmentData.put(COLUMN_SUBMISSION_DATE, model.getSubmissionDate());
        assignmentData.put(COLUMN_ATTACHED_IMAGE, model.getAttachedImages());
        assignmentData.put(COLUMN_ATTACHED_PDF, model.getAttachedPDF());

        long isUpdated = db.update(ASSIGNMENT_TABLE,
                                   assignmentData,
                                   COLUMN_ID + " = " + model.getPosition(),
                                   null);

        return isUpdated != -1;
    }

    /**
     * Simply gets the number of assignments from the database
     *
     * @return the number of assignments
     */
    @SuppressWarnings("unused")
    public int getAssignmentCount() {
        SQLiteDatabase db = getReadableDatabase();
        String countQuery = "SELECT COUNT(*) FROM " + ASSIGNMENT_TABLE;
        Cursor countCursor = db.rawQuery(countQuery, null);
        int count = 0;
        if (countCursor.moveToFirst()) count = countCursor.getInt(0);
        countCursor.close();
        return count;
    }

    /**
     * Updates the submission status of assignment at <code>pos</code>.
     *
     * @param pos the position in database in which the assignment exists
     * @param b   the new submitted status
     * @return true if the assignment was updated successfully
     */
    public boolean updateAssignmentStatus(int pos, boolean b) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues value = new ContentValues();
        value.put(COLUMN_SUBMITTED, b);

        int updateStat = db.update(ASSIGNMENT_TABLE, value, COLUMN_ID + " = " + pos, null);

        return updateStat != -1;
    }

    /**
     * Updates the submission status of timetable at <code>pos</code>.
     *
     * @param pos the position in database, of a particular day, in which the timetable exists
     * @param b   the new class-over status
     * @return true if the assignment was updated successfully
     */
    @SuppressWarnings("unused")
    public boolean updateTimetableStatus(int pos, String TIMETABLE, boolean b) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues value = new ContentValues();
        value.put(COLUMN_CLASS_OVER_STAT, b);

        int updateStat = db.update(TIMETABLE, value, COLUMN_ID + " = " + pos, null);

        return updateStat != -1;
    }

    /**
     * Retrieves the number of rows in timetable
     *
     * @param TIMETABLE the timetable to count
     * @return the number of entries in timetable
     */
    @SuppressWarnings("unused")
    public int getTimetableCount(String TIMETABLE) {
        SQLiteDatabase db = getReadableDatabase();
        String countQuery = "SELECT COUNT(*) FROM " + TIMETABLE;
        Cursor countCursor = db.rawQuery(countQuery, null);
        int count = 0;
        if (countCursor.moveToFirst()) count = countCursor.getInt(0);
        countCursor.close();
        return count;
    }

    /**
     * Retrieves the number of registered courses for a semester
     *
     * @return the number of registered courses for a particular semester
     */
    @SuppressWarnings("unused")
    public int getCoursesCount(String SEMESTER) {
        String count_stmt = "SELECT COUNT(*) FROM " + SEMESTER;
        SQLiteDatabase db = getReadableDatabase();
        Cursor countCursor = db.rawQuery(count_stmt, null);

        int count = 0;
        if (countCursor.moveToFirst()) {
            count = countCursor.getInt(0);
        }
        countCursor.close();
        return count;
    }

    /**
     * Retrieves the number of registered courses for a particular semester
     *
     * @param SEMESTER the semester in which it's data is to be retrieved
     * @return all registered courses in that semester in alphabetic order of course names
     */
    public List<DataModel> getCoursesData(String SEMESTER) {
        List<DataModel> courseData = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String coursesQuery_stmt =
                "SELECT " + COLUMN_ID
                        + ", " + COLUMN_CREDITS
                        + ", " + COLUMN_COURSE_CODE
                        + ", " + COLUMN_FULL_COURSE_NAME
                        + " FROM " + SEMESTER
                        + " ORDER BY " + COLUMN_FULL_COURSE_NAME;

        if (!db.isOpen()) db = getReadableDatabase();
        Cursor courseCursor = db.rawQuery(coursesQuery_stmt, null);

        while (courseCursor.moveToNext()) {
            CourseModel courseModel = new CourseModel();
            courseModel.setSemester(SEMESTER);
            courseModel.setCourseCode(retrieveEntry(courseCursor.getString(2)));
            courseModel.setCourseName(retrieveEntry(courseCursor.getString(3)));
            courseModel.setCredits(courseCursor.getInt(1));
            courseModel.setId(courseCursor.getInt(0));
            courseData.add(courseModel);
        }
        // reverse the order of alarms to show the most recent alarm
//        Collections.reverse(alarms);
        courseCursor.close();
        return courseData;
    }

    /**
     * Adds a course to the semester specified
     *
     * @param courseModel the course to be added
     * @param SEMESTER    the semester where course would be added
     * @return the row id for the add operation (-1 for unsuccessful)
     */
    public int[] addCourse(CourseModel courseModel, String SEMESTER) {
        String getIdStmt = "SELECT " + COLUMN_ID + " FROM " + SEMESTER;
        SQLiteDatabase db = getReadableDatabase();
        Cursor idCursor = db.rawQuery(getIdStmt, null);

        int lastID;
        if (idCursor.moveToLast()) lastID = idCursor.getInt(0);
        else lastID = -1;

        ContentValues courseValues = new ContentValues();
        int insertPos = ++lastID;
        courseValues.put(COLUMN_ID, insertPos);
        courseValues.put(COLUMN_CREDITS, courseModel.getCredits());
        courseValues.put(COLUMN_COURSE_CODE, sanitizeEntry(courseModel.getCourseCode()));
        courseValues.put(COLUMN_FULL_COURSE_NAME, sanitizeEntry(courseModel.getCourseName()));

        long resCode = db.insert(SEMESTER, null, courseValues);

        // begin get course chronological order
        List<String> names = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT " + COLUMN_FULL_COURSE_NAME + " FROM "
                                       + SEMESTER + " ORDER BY " + COLUMN_FULL_COURSE_NAME, null);

        while (c.moveToNext()) names.add(c.getString(0));

        // add to global semester
        db.insert(REGISTERED_COURSES, null, courseValues);
        idCursor.close();
        c.close();
        return resCode != -1 ? new int[]{Collections.binarySearch(names, courseModel.getCourseName()), insertPos}
                             : new int[]{-1, -1};
    }

    /**
     * Deletes a course
     *
     * @param model    the course to be deleted
     * @param SEMESTER the semester in which the course would be deleted
     * @return true if course have been deleted
     */
    public boolean deleteCourseEntry(CourseModel model, String SEMESTER) {
        SQLiteDatabase db = getWritableDatabase();

        int resultCode = db.delete(SEMESTER, COLUMN_ID + " = " + model.getId(), null);
        // Delete from global course code
        db.delete(REGISTERED_COURSES,
                  COLUMN_FULL_COURSE_NAME + " = '" + sanitizeEntry(model.getCourseName()) + "'", null);

        return resultCode != -1;
    }

    /**
     * Simply returns all the registered courses
     *
     * @return all the registered courses
     */
    public List<String> getAllRegisteredCourses() {
        List<String> courses = new ArrayList<>();
        String getRegisteredCourses_stmt =
                "SELECT " + COLUMN_FULL_COURSE_NAME
                        + " FROM " + REGISTERED_COURSES
                        + " ORDER BY " + COLUMN_FULL_COURSE_NAME;

        SQLiteDatabase db = getReadableDatabase();

        Cursor getCoursesCursor = db.rawQuery(getRegisteredCourses_stmt, null);

        if (getCoursesCursor.getColumnCount() == 0) courses.add("nil");

        while (getCoursesCursor.moveToNext())
            courses.add(retrieveEntry(getCoursesCursor.getString(0)));

        getCoursesCursor.close();
        return courses;
    }

    /**
     * Simply returns all the registered courses
     *
     * @return all the registered courses
     */
    public List<String> getAllRegisteredCourseCodes() {
        List<String> courses = new ArrayList<>();
        String getRegisteredCourses_stmt =
                "SELECT " + COLUMN_COURSE_CODE + " FROM " + REGISTERED_COURSES + " ORDER BY " + COLUMN_COURSE_CODE;

        SQLiteDatabase db = getReadableDatabase();

        Cursor getCodesCursor = db.rawQuery(getRegisteredCourses_stmt, null);

        while (getCodesCursor.moveToNext())
            courses.add(retrieveEntry(getCodesCursor.getString(0)));

        if (courses.isEmpty()) courses.add("NIL");
        getCodesCursor.close();
        return courses;
    }

    /**
     * Intelligently gets the course code from the course name
     *
     * @param COURSE_NAME the course name entry to be used to get the course code
     * @return the course code corresponding to the given course name
     */
    public String getCourseCodeFromName(String COURSE_NAME) {

        SQLiteDatabase db = getReadableDatabase();
        String getFromName_stmt = "SELECT " + COLUMN_COURSE_CODE
                + " FROM " + REGISTERED_COURSES
                + " WHERE " + COLUMN_FULL_COURSE_NAME + " = '" + COURSE_NAME + "'";

        String courseCode = "NIL";
        Cursor getCursor = db.rawQuery(getFromName_stmt, null);

        if (getCursor.moveToFirst()) {
            courseCode = retrieveEntry(getCursor.getString(0));
        }

        getCursor.close();
        return courseCode;
    }

    /**
     * Checks if a particular timetable is present
     *
     * @param timetable the timetable in database to in which the search would take place
     * @param model     the timetable data to confirm its existence
     * @return true if found
     */
    public boolean isTimeTableAbsent(String timetable, TimetableModel model) {
        SQLiteDatabase db = getReadableDatabase();
        String search_stmt1 = "SELECT * FROM " + timetable
                + " WHERE " + COLUMN_START_TIME + " = '" + model.getStartTime() + "'";
        String search_stmt2 = "SELECT * FROM " + timetable
                + " WHERE " + COLUMN_START_TIME + " = '" + model.getStartTime() + "'"
                + "  AND " + COLUMN_DAY + " = '" + model.getDay() + "'";

        Cursor searchCursor;
        if (timetable.equals(SCHEDULED_TIMETABLE))
            searchCursor = db.rawQuery(search_stmt2, null);
        else searchCursor = db.rawQuery(search_stmt1, null);

        boolean isAbsent = searchCursor.getCount() == 0;
        searchCursor.close();
        return isAbsent;
    }

    /**
     * Checks if a particular exam is present in database
     *
     * @param WEEK  the exam week in database to in which the search would take place
     * @param model the exam data to confirm its existence
     * @return true if exam was not found in database
     */
    public boolean isExamAbsent(String WEEK, ExamModel model) {
        SQLiteDatabase db = getReadableDatabase();
        String search_stmt =
                "SELECT * FROM " + WEEK
                        + " WHERE "
                        + COLUMN_FULL_COURSE_NAME + " = '" + sanitizeEntry(model.getCourseName())
                        + "'" + " AND "
                        + COLUMN_DAY + " = '" + model.getDay() + "'";
        Cursor searchCursor = db.rawQuery(search_stmt, null);

        boolean isAbsent = searchCursor.getCount() == 0;
        searchCursor.close();
        return isAbsent;
    }

    /**
     * This task should be run as a background task, because it might be cpu intensive.
     * Because, when the user tries to access the data in a specific exam timetable, if it is not
     * yet created, it will be created then probably would return empty data to the user. This was
     * useful because when the user changes the number of exam weeks, some tables are not created
     * and don't exist yet, so create them, when user wants to use them.
     *
     * @param index the timetable to be retrieved with <strong>index 0</strong>, meaning
     *              <strong>WEEK 1</strong>
     * @return empty data if table doesn't exists yet and then create it or returns the current
     * exam timetable data in database
     */
    public List<DataModel> getExamTimetableDataFor(int index) {
        String examTable = "WEEK_" + (index + 1);
        SQLiteDatabase db = getReadableDatabase();
        // Create this table if it doesn't exist
        String createExamTables_stmt = "CREATE TABLE IF NOT EXISTS " + examTable + " (" +
                COLUMN_ID + " INTEGER, " +
                COLUMN_WEEK + " TEXT, " +
                COLUMN_DAY + " TEXT, " +
                COLUMN_COURSE_CODE + " TEXT, " +
                COLUMN_FULL_COURSE_NAME + " TEXT, " +
                COLUMN_START_TIME + " INTEGER, " +
                COLUMN_END_TIME + " INTEGER"
                + ")";

        db.execSQL(createExamTables_stmt);

        List<DataModel> exams = new ArrayList<>();

        String getExams_stmt
                = "SELECT " + COLUMN_ID + ","
                + COLUMN_COURSE_CODE + ","
                + COLUMN_FULL_COURSE_NAME + ","
                + COLUMN_START_TIME + ","
                + COLUMN_END_TIME + ","
                + COLUMN_WEEK + ","
                + COLUMN_DAY
                + " FROM " + examTable;

        Cursor getExamsCursor = db.rawQuery(getExams_stmt, null);
        while (getExamsCursor.moveToNext()) {
            int id = getExamsCursor.getInt(0);
            String courseCode = getExamsCursor.getString(1);
            String courseName = retrieveEntry(getExamsCursor.getString(2));
            String startTime = getExamsCursor.getString(3);
            String endTime = getExamsCursor.getString(4);
            String examWeek = getExamsCursor.getString(5);
            String day = getExamsCursor.getString(6);
            exams.add(new ExamModel(day, examWeek, id, courseCode, courseName, startTime, endTime));
        }
        getExamsCursor.close();
        return exams;
    }

    /**
     * Removes redundant exam timetables from TimeLY's database
     */
    public void dropRedundantExamTables() {
        ThreadUtils.runBackgroundTask(() -> {
            SQLiteDatabase db = getWritableDatabase();
            // First get the user preference for the number of weeks, which corresponds to the
            // number of tables in database.
            int wStart = 8;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            String countValue = null;
            try {
                countValue = prefs.getString("exam weeks", "8");
                if (countValue != null) wStart = Integer.parseInt(countValue);
            } catch (NumberFormatException exc) {
                Log.w(TAG, "Ignoring user selected Week count of: " + countValue + ", using 8 weeks instead ");
            }
            Cursor getEndCursor = db.rawQuery("SELECT " + COLUMN_EXAM_WEEK_COUNT + " FROM " + PREFERENCE_TABLE, null);
            // now perform the main operation of this action
            int wEnd = wStart;
            if (getEndCursor.moveToFirst())
                wEnd = getEndCursor.getInt(0);
            if (wStart != wEnd) {
                // drop all the unwanted tables, so as to reduce app's data
                for (int i = wStart + 1; i <= wEnd; i++)
                    db.execSQL("DROP TABLE " + "WEEK_" + i);
                // Update week count to be retrieved later when dropping redundant tables
                ContentValues values = new ContentValues();
                values.put(COLUMN_EXAM_WEEK_COUNT, wStart);
                db.update(PREFERENCE_TABLE, values, null, null);
            }
            getEndCursor.close();
            db.close();
        });
    }

    /**
     * Adds an exam to a particular exam week specified by examWeek
     *
     * @param exam     the exam to be added to database
     * @param examWeek the week in which that exam would be added
     * @return the position at which the exam was added and the position it would appear in the
     * list.
     */
    public int[] addExam(ExamModel exam, String examWeek) {
        SQLiteDatabase db = getWritableDatabase();
        String getIdStmt = "SELECT " + COLUMN_ID + " FROM " + examWeek;
        Cursor idCursor = db.rawQuery(getIdStmt, null);

        int lastID;
        if (idCursor.moveToLast())
            lastID = idCursor.getInt(0);
        else lastID = -1;

        exam.setId(++lastID);  // This would be used in comparing values
        ContentValues values = new ContentValues();
        values.put(COLUMN_FULL_COURSE_NAME, sanitizeEntry(exam.getCourseName()));
        values.put(COLUMN_START_TIME, exam.getStart());
        values.put(COLUMN_END_TIME, exam.getEnd());
        values.put(COLUMN_COURSE_CODE, exam.getCourseCode());
        values.put(COLUMN_WEEK, exam.getWeek());
        values.put(COLUMN_DAY, exam.getDay());
        values.put(COLUMN_ID, exam.getId());

        long resultCode = db.insertOrThrow(examWeek, null, values);
        // Begin exam sorting
        Cursor sortElementCursor
                = db.rawQuery("SELECT " + COLUMN_START_TIME + "," + COLUMN_DAY + "," + COLUMN_ID + " FROM " + examWeek,
                              null);

        List<ExamModel> exams = new ArrayList<>();
        while (sortElementCursor.moveToNext()) {
            ExamModel exam1 = new ExamModel();
            exam1.setStart(sortElementCursor.getString(0));
            exam1.setDay(sortElementCursor.getString(1));
            exam1.setId(sortElementCursor.getInt(2));
            exams.add(exam1);
        }

        Collections.sort(exams, (e1, e2) -> {
            int cmp = Integer.compare(e1.getDayIndex(), e2.getDayIndex());
            if (cmp != 0) return cmp;
            else return Integer.compare(e1.getStartAsInt(), e2.getStartAsInt());
        });

        idCursor.close();
        sortElementCursor.close();
        // Compare all the former exams in database with this exam
        Comparator<? super ExamModel> idComparator = (e1, e2) -> Integer.compare(e1.getId(), e2.getId());
        // impossible to sort exams, linear-search instead
        return resultCode != -1 ? new int[]{linearSearch(exams, exam, idComparator), exam.getId()}
                                : new int[]{-1, -1};
    }

    /**
     * Delete a single exam entry from a particular exam week
     *
     * @param examModel the exam to be deleted
     * @param WEEK      the week at which the exam exists
     * @return true if exam was deleted, false otherwise
     */
    public boolean deleteExamEntry(ExamModel examModel, String WEEK) {
        SQLiteDatabase db = getWritableDatabase();
        int resultCode = db.delete(WEEK, COLUMN_ID + " = " + examModel.getId(), null);
        return resultCode != -1;
    }

    /**
     * Updates the timetable specified by <code>timetableName</code>
     *
     * @param timetable     the data to update
     * @param timetableName the timetable to update
     * @return true if timetable was updated
     */
    public boolean updateTimetableData(TimetableModel timetable, String timetableName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues timeTableData = new ContentValues();
        timeTableData.put(COLUMN_FULL_COURSE_NAME, sanitizeEntry(timetable.getFullCourseName()));
        timeTableData.put(COLUMN_END_TIME, timetable.getEndTime());
        timeTableData.put(COLUMN_START_TIME, timetable.getStartTime());
        timeTableData.put(COLUMN_COURSE_CODE, timetable.getCourseCode());

        if (timetableName.equals(SCHEDULED_TIMETABLE)) {
            timeTableData.put(COLUMN_IMPORTANCE, timetable.getImportance());
            timeTableData.put(COLUMN_LECTURER_NAME, sanitizeEntry(timetable.getLecturerName()));
        }

        int resultCode = db.update(timetableName, timeTableData, COLUMN_ID + " = " + timetable.getId(), null);
        return resultCode != -1;
    }

    /**
     * Checks if a particular course exists in app's database
     *
     * @param courseModel the course to check it's existence
     * @return true if course does not exist
     */
    public boolean isCourseAbsent(CourseModel courseModel) {
        SQLiteDatabase db = getReadableDatabase();

        String search_stmt = "SELECT * FROM " + courseModel.getSemester()
                + " WHERE " + COLUMN_FULL_COURSE_NAME + " = '"
                + sanitizeEntry(courseModel.getCourseName()) + "'";

        Cursor searchCursor = db.rawQuery(search_stmt, null);

        boolean isAbsent = searchCursor.getCount() == 0;
        searchCursor.close();
        return isAbsent;
    }

    /**
     * Gets the URIs of the attached images when assignment was added to database
     *
     * @param position the position of assignment in database
     * @return the attached images or null if no images were assigned
     */
    public List<?>[] getAttachedImagesAsUriList(int position) {
        String getAttachedImages_stmt
                = "SELECT " + COLUMN_ATTACHED_IMAGE + " FROM " + ASSIGNMENT_TABLE
                + " WHERE " + COLUMN_ID + " = " + position;
        SQLiteDatabase db = getReadableDatabase();
        Cursor attachedImagesCursor = db.rawQuery(getAttachedImages_stmt, null);

        String attachedImages = "";
        if (attachedImagesCursor.moveToFirst())
            attachedImages = attachedImagesCursor.getString(0);

        attachedImagesCursor.close();

        List<Uri> uris = new ArrayList<>();
        List<Image> images = new ArrayList<>();

        if (!TextUtils.isEmpty(attachedImages)) {
            String[] ais = attachedImages.split(";");

            for (String ai : ais) {
                Uri uri = Uri.parse(ai);
                uris.add(uri);
                images.add(Image.createImageFromUri(uri));
            }
        }

        return new List[]{uris, images};
    }

    /**
     * Gets the URIs of the attached images when assignment was added to database
     *
     * @param position the position of assignment in database
     * @return the attached images or null if no images were assigned
     */
    public List<String> getAttachedImagesAsStringList(int position) {
        String getAttachedImages_stmt
                = "SELECT " + COLUMN_ATTACHED_IMAGE + " FROM " + ASSIGNMENT_TABLE
                + " WHERE " + COLUMN_ID + " = " + position;
        SQLiteDatabase db = getReadableDatabase();
        Cursor attachedImagesCursor = db.rawQuery(getAttachedImages_stmt, null);

        String attachedImages = "";
        if (attachedImagesCursor.moveToFirst())
            attachedImages = attachedImagesCursor.getString(0);

        attachedImagesCursor.close();

        List<String> uris = new ArrayList<>();

        if (!TextUtils.isEmpty(attachedImages)) {
            String[] ais = attachedImages.split(";");
            uris.addAll(Arrays.asList(ais));
        }

        return uris;
    }

    /**
     * Gets the URIs of the attached images when assignment was added to database
     *
     * @param position the position of assignment in database
     * @return the attached images or null if no images were assigned
     */
    public String getAttachedImagesAsString(int position) {
        String getAttachedImages_stmt
                = "SELECT " + COLUMN_ATTACHED_IMAGE + " FROM " + ASSIGNMENT_TABLE
                + " WHERE " + COLUMN_ID + " = " + position;
        SQLiteDatabase db = getReadableDatabase();
        Cursor attachedImagesCursor = db.rawQuery(getAttachedImages_stmt, null);

        String attachedImages = "";
        if (attachedImagesCursor.moveToFirst())
            attachedImages = attachedImagesCursor.getString(0);

        attachedImagesCursor.close();
        LogUtils.debug(this, "images: " + attachedImages);
        return attachedImages;
    }

    /**
     * @param position the position of the assignment, in which the delete operation would take
     *                 place.
     * @param uri      the uri to be deleted
     * @return the new assignment image uris
     */
    public String deleteImage(int position, Uri uri) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues uriValues = new ContentValues();

        String uris = getAttachedImagesAsString(position).replace(uri.toString() + ";", "");
        uriValues.put(COLUMN_ATTACHED_IMAGE, uris);

        long updated = db.update(ASSIGNMENT_TABLE, uriValues, COLUMN_ID + " = " + position, null);
        LogUtils.debug(this, "Results: " + uris);
        return updated != -1 ? uris : null;
    }

    /**
     * Appends the new uri specified by <code>uris</code> to the end of the attached images
     * count of the assignment at the specified position.
     *
     * @param position position of the assignment in database in which the uris would be attached
     * @param uris     the uris to append
     * @return true if operation was successful
     */
    public boolean updateUris(int position, String[] uris) {
        String aI = getAttachedImagesAsString(position);

        String joint = aI + TextUtils.join(";", uris) + ";";

        ContentValues uriValues = new ContentValues();
        uriValues.put(COLUMN_ATTACHED_IMAGE, joint);

        SQLiteDatabase db = getWritableDatabase();
        long updated = db.update(ASSIGNMENT_TABLE, uriValues, COLUMN_ID + " = " + position, null);
        LogUtils.debug(this, "Updating to: " + joint);
        return updated != -1;
    }

    /**
     * Deletes multiple images from the attached images assigned to a particular assignment
     *
     * @param position    the position of the assignment that owns the images at
     *                    <code>itemIndices</code>
     * @param itemIndices the indices of the attached images to deleted
     * @return true if the image(s) were deleted
     */
    public boolean deleteMultipleImages(int position, Integer[] itemIndices) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues uriValues = new ContentValues();
        LogUtils.debug(this, "Deleting at: " + Arrays.toString(itemIndices));
        List<String> uris = getAttachedImagesAsStringList(position);
        Arrays.sort(itemIndices, Collections.reverseOrder());

        for (int i : itemIndices) uris.remove(i);

        String[] s = uris.toArray(new String[0]);
        String joint = TextUtils.join(";", s) + ";";

        uriValues.put(COLUMN_ATTACHED_IMAGE, joint);
        long resCode = db.update(ASSIGNMENT_TABLE, uriValues, COLUMN_ID + " = " + position, null);
        LogUtils.debug(this, "M-Deleting: " + Arrays.toString(s));
        return resCode != -1;
    }

    /**
     * Deletes multiple data models from the list of data models in app's database.
     *
     * @param itemIndices the indices of the attached images to deleted
     * @param clazz       the class of the data model that should be deleted
     * @return true if the data were deleted
     */
    public synchronized boolean deleteDataModels(Class<?> clazz, String[] metadata,
                                                 Integer[] itemIndices, List<DataModel> data) {
        boolean resultCode;

        switch (clazz.getName()) {
            case Constants.ASSIGNMENT_MODEL:
                resultCode = deleteMultipleAssignments(itemIndices);
                break;
            case Constants.COURSE_MODEL:
                resultCode = deleteMultipleCourses(metadata[0], itemIndices, data);
                break;
            case Constants.EXAM_MODEL:
                resultCode = deleteMultipleExams(metadata[1], itemIndices);
                break;
            case Constants.TIMETABLE_MODEL:
                resultCode = deleteMultipleTimetables(metadata[2], itemIndices);
                break;
            default:
                throw new IllegalArgumentException(clazz.getName() + " is not supported");
        }
        return resultCode;
    }

    // Delete timetables at specified indices
    private boolean deleteMultipleTimetables(String TIMETABLE, Integer[] itemIndices) {
        // Transform all indices from Integers to Strings
        if (TIMETABLE == null) throw new IllegalArgumentException("Timetable can't be null");

        boolean resCode = false;
        SQLiteDatabase db = getWritableDatabase();

        for (int index : itemIndices) {
            resCode |= db.delete(TIMETABLE, COLUMN_ID + " = " + index, null) != -1;
        }

        return resCode;
    }

    // Delete exams at specified indices
    private boolean deleteMultipleExams(String WEEK, Integer[] itemIndices) {
        // Transform all indices from Integers to Strings
        if (WEEK == null) throw new IllegalArgumentException("Week can't be null");

        boolean resCode = false;
        SQLiteDatabase db = getWritableDatabase();

        for (int index : itemIndices) resCode |= db.delete(WEEK, COLUMN_ID + " = " + index, null) != -1;

        return resCode;
    }

    // Delete courses at specified indices
    private boolean deleteMultipleCourses(String SEMESTER, Integer[] itemIndices,
                                          List<DataModel> data) {
        // Transform all indices from Integers to Strings
        if (SEMESTER == null) throw new IllegalArgumentException("Semester can't be null");

        boolean resCode = false;
        SQLiteDatabase db = getWritableDatabase();

        for (int x = 0; x < itemIndices.length; x++) {
            CourseModel model = (CourseModel) data.get(x);
            resCode |=
                    db.delete(SEMESTER, COLUMN_ID + " = " + itemIndices[x], null) != -1
                            & /* delete registered course entry */
                            db.delete(REGISTERED_COURSES, COLUMN_FULL_COURSE_NAME + " = '"
                                    + sanitizeEntry(model.getCourseName()) + "'", null) != -1;
        }

        return resCode;
    }

    // Delete assignments at specified indices
    private boolean deleteMultipleAssignments(Integer[] itemIndices) {
        // Delete assignments with  the whereArgs specified
        boolean resCode = false;
        SQLiteDatabase db = getWritableDatabase();

        for (int index : itemIndices) {
            resCode |= db.delete(ASSIGNMENT_TABLE, COLUMN_ID + " = " + index, null) != -1;
        }

        return resCode;
    }

    /**
     * @return list of  alarms currently be in their active states
     */
    public List<DataModel> getActiveAlarms() {
        return CollectionUtils.filterList(getAlarms(), model -> ((AlarmModel) model).isOn());
    }

    /**
     * @return a list of pending assignments
     */
    public List<DataModel> getPendingAssignments() {
        return CollectionUtils.filterList(getAssignmentData(), model -> !((AssignmentModel) model).isSubmitted());
    }
}