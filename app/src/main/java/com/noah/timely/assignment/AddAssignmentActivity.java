package com.noah.timely.assignment;

import static com.noah.timely.assignment.AssignmentFragment.DATE;
import static com.noah.timely.assignment.AssignmentFragment.DESCRIPTION;
import static com.noah.timely.assignment.AssignmentFragment.LECTURER_NAME;
import static com.noah.timely.assignment.AssignmentFragment.TITLE;
import static com.noah.timely.util.Utility.Alert;
import static com.noah.timely.util.Utility.playAlertTone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.TooltipCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.noah.timely.R;
import com.noah.timely.core.SchoolDatabase;
import com.noah.timely.error.ErrorDialog;
import com.noah.timely.gallery.ImageDirectory;
import com.noah.timely.gallery.ImageGallery;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AddAssignmentActivity extends AppCompatActivity {
    public static final String SCHEDULE_POS = "Schedule position";
    public static final String ADD_NEW = "com.noah.timely.addAssignmentActivity.add_new";
    static final String EDIT_POS = "Edit Position";
    public static final String NEXT_ALARM = "next alarm";
    public static List<Uri> mediaUris = new ArrayList<>();
    public static String POSITION = "database position";
    private List<String> courseList;
    private EditText edt_lecturerName, edt_title, edt_description, edt_date;
    private String courseCode, tmpCourseCode;
    private boolean isToEdit = false;
    private int editPos = -1;
    private TextInputLayout dateBox, lecturerBox, titleBox, descriptionBox;
    private SchoolDatabase database;
    private TextView fileCount;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        database = new SchoolDatabase(this);
        setContentView(R.layout.add_assignment);

        boolean shouldEdit = getIntent().getAction().equals("Edit");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // initialize the course code to the first entry to prevent a null value entry
        Spinner spin_courseCode = findViewById(R.id.chooseCourse);
        Button submitButton = findViewById(R.id.submitButton);
        submitButton.setText(shouldEdit ? R.string.update_text : R.string.register_text);

        FloatingActionButton btn_gallery = findViewById(R.id.gallery);

        edt_lecturerName = findViewById(R.id.choiceLecturer);
        edt_date = findViewById(R.id.submission_date);
        edt_title = findViewById(R.id.assignment_title);
        edt_description = findViewById(R.id.description);
        dateBox = findViewById(R.id.date_box);
        lecturerBox = findViewById(R.id.lecturer_box);
        titleBox = findViewById(R.id.title_box);
        descriptionBox = findViewById(R.id.description_box);
        setupDateForm();

        submitButton.setOnClickListener(v -> saveOrUpdateAssignment());

        btn_gallery.setOnClickListener(
                v -> {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
                        startActivity(new Intent(this, ImageDirectory.class)
                                              .putExtra(ImageDirectory.STORAGE_ACCESS_ROOT, ImageDirectory.EXTERNAL)
                                              .setAction(ADD_NEW));
                    else Toast.makeText(this, "Not supported yet", Toast.LENGTH_LONG).show();
                });

        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this,
                                                                R.layout.simple_spinner_item,
                                                                courseList = database.getAllRegisteredCourseCodes());

        courseAdapter.setDropDownViewResource(R.layout.simple_dropdown_item_1line);

        spin_courseCode.setAdapter(courseAdapter);
        spin_courseCode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                courseCode = courseList.get(position);
                tmpCourseCode = courseCode;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                courseCode = tmpCourseCode == null ? courseList.get(0) : tmpCourseCode;
            }
        });

        final Intent intent = getIntent();

        if (shouldEdit) {
            isToEdit = true;
            String lecturerName = intent.getStringExtra(LECTURER_NAME);
            String title = intent.getStringExtra(TITLE);
            String description = intent.getStringExtra(DESCRIPTION);
            String date = intent.getStringExtra(DATE);
            editPos = intent.getIntExtra(EDIT_POS, -1);

            edt_lecturerName.setText(lecturerName);
            edt_title.setText(title);
            edt_description.setText(description);
            edt_date.setText(date);
        }
    }

    private void setupDateForm() {

        DatePickerDialog.OnDateSetListener odsl = (view, year, monthOfYear, dayOfMonth) -> {
            String parsedDate;
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, monthOfYear, dayOfMonth);

            SimpleDateFormat d_dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
            SimpleDateFormat u_s_dateFormat = new SimpleDateFormat("dd_MM_yyyy", Locale.US);
            SimpleDateFormat s_dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            SimpleDateFormat p_dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.US);

            String dateKey = "a_date_format";
            String ddf = getString(R.string.default_date_format);
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            switch (sharedPreferences.getString(dateKey, ddf)) {
                case "dd_mm_yyyy":
                    parsedDate = u_s_dateFormat.format(calendar.getTime());
                    break;
                case "dd/mm/yyyy":
                    parsedDate = s_dateFormat.format(calendar.getTime());
                    break;
                case "dd.mm.yyyy":
                    parsedDate = p_dateFormat.format(calendar.getTime());
                    break;
                default:
                    parsedDate = d_dateFormat.format(calendar.getTime());
                    break;
            }

            edt_date.setText(parsedDate);
        };

        edt_date.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                int drawableWidth = edt_date.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();
                if (event.getX() >= (edt_date.getWidth() - drawableWidth)) {
                    Calendar calendar = Calendar.getInstance();
                    DatePickerDialog dpd = DatePickerDialog.newInstance(odsl,
                                                                        calendar.get(Calendar.YEAR),
                                                                        calendar.get(Calendar.MONTH),
                                                                        calendar.get(Calendar.DAY_OF_MONTH));
                    dpd.setVersion(DatePickerDialog.Version.VERSION_2);
                    dpd.show(getSupportFragmentManager(), "DatePickerDialog");
                    return true;
                }
            }
            return false;
        });

    }

    @Override
    protected void onDestroy() {
        mediaUris.clear();
        database.close();
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_menu_files, menu);
        View layout = menu.findItem(R.id.list_item_count).getActionView();
        fileCount = layout.findViewById(R.id.counter);
        fileCount.setText(String.valueOf(0));
        TooltipCompat.setTooltipText(fileCount, "Image Count");
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (fileCount != null) fileCount.setText(String.valueOf(intent.getIntExtra(ImageGallery.ARG_FILES_COUNT, -1)));
    }

    @Override
    public void onResume() {
        super.onResume();
        getSupportActionBar().setTitle(isToEdit ? "Update Assignment" : "Register Assignment");
    }

    private void saveOrUpdateAssignment() {
        String emptyErrorMessage = "Field can't be empty";
        String datePattern = "^(?:(3[01]|[12][0-9]|0[1-9])[/._-](1[0-2]|0[1-9]))[/._-][0-9]{4}$";
        String date = edt_date.getText().toString();

        boolean errorOccurred = false;

        if (!date.matches(datePattern)) {
            dateBox.setError("Invalid date format");
            errorOccurred = true;
        }

        if (TextUtils.isEmpty(edt_lecturerName.getText().toString())) {
            lecturerBox.setError(emptyErrorMessage);
            errorOccurred = true;
        }

        if (TextUtils.isEmpty(edt_title.getText().toString())) {
            titleBox.setError(emptyErrorMessage);
            errorOccurred = true;
        }

        if (TextUtils.isEmpty(edt_description.getText().toString())) {
            descriptionBox.setError(emptyErrorMessage);
            errorOccurred = true;
        }

        if (errorOccurred) return;

        String ln = edt_lecturerName.getText().toString();
        String tt = edt_title.getText().toString();
        String description = this.edt_description.getText().toString();
        String attachedPDF = "not implemented yet";
        String cc = this.courseCode;

        StringBuilder uriBuilder = new StringBuilder();

        for (Uri mediaUri : mediaUris) {
            uriBuilder.append(mediaUri.toString()).append(";");
        }

        String attachedImage = uriBuilder.toString();
        // Add alarm to database then, set alarm to go off +by 07:00 am on the day and a day before
        // the assignment is due for submission

        String message;
        boolean isSuccessful;
        AssignmentModel data;

        String[] splitDate = date.split("[/._-]");
        int dd = Integer.parseInt(splitDate[0]);
        int mm = Integer.parseInt(splitDate[1]) - 1;
        int yy = Integer.parseInt(splitDate[2]);

        int id = database.getLastAssignmentId() + 1;

        int pos = isToEdit ? editPos : id;

        data = new AssignmentModel(pos, ln, tt, description, date, cc, date, attachedPDF, attachedImage, false);

        if (database.isAssignmentPresent(data) && !tryScheduleNotifiers(yy, mm, dd, tt, ln, data, pos)) {
            ErrorDialog.Builder errorBuilder = new ErrorDialog.Builder();
            errorBuilder.setDialogMessage("Invalid assignment")
                        .setShowSuggestions(true)
                        .setSuggestionCount(2)
                        .setSuggestion1("Try setting assignments to the future")
                        .setSuggestion2("Check for assignment duplicates");

            new ErrorDialog().showErrorMessage(this, errorBuilder.build());
            return;
        }

        if (isToEdit) {
            message = "Assignment updated";
            boolean isSubmitted = isSuccessful = database.updateAssignmentData(data);
            EventBus.getDefault().post(new AUpdateMessage(data, AUpdateMessage.EventType.UPDATE_CURRENT));
        } else {
            message = "Registering Assignment";
            isSuccessful = database.addAssignmentData(data);
            EventBus.getDefault().post(new AUpdateMessage(data, AUpdateMessage.EventType.NEW));
        }

        if (isSuccessful) {
            Toast alert = Toast.makeText(this, message, Toast.LENGTH_SHORT);
            alert.setGravity(Gravity.CENTER, 0, 0);
            alert.show();
            playAlertTone(getApplicationContext(), Alert.ASSIGNMENT);

        } else Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show();

        onBackPressed();
    }

    private boolean tryScheduleNotifiers(int year, int month, int day, String title, String lecturer,
                                         AssignmentModel data, int pos) {
        Calendar calendar = Calendar.getInstance();
        // Time set to 07:00 am
        calendar.set(year, month, day);
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long CURRENT = calendar.getTimeInMillis();
        long NOW = System.currentTimeMillis();
        if (CURRENT < NOW) return false;

        final long PREVIOUS = CURRENT - TimeUnit.DAYS.toMillis(1);
        // Get the alarm manager
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        // Now set the next alarm
        String ln = truncateLecturerName(lecturer);
        Intent notifyIntentCurrent = new Intent(this, SubmissionNotifier.class);
        notifyIntentCurrent.putExtra(LECTURER_NAME, ln)
                           .putExtra(TITLE, title)
                           .putExtra(POSITION, pos)
                           .addCategory(getPackageName() + ".category")
                           .setAction(getPackageName() + ".update")
                           .setDataAndType(Uri.parse("content://" + getPackageName()), data.toString());

        Intent notifyIntentPrevious = new Intent(this, Reminder.class);
        notifyIntentPrevious.putExtra(LECTURER_NAME, ln)
                            .putExtra(TITLE, title)
                            .putExtra(NEXT_ALARM, CURRENT)
                            .addCategory(getPackageName() + ".category")
                            .setAction(getPackageName() + ".update")
                            .setDataAndType(Uri.parse("content://" + getPackageName()), data.toString());

        PendingIntent assignmentPiPrevious = PendingIntent.getBroadcast(this, 147, notifyIntentPrevious,
                                                                        PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent assignmentPiCurrent = PendingIntent.getBroadcast(this, 141, notifyIntentCurrent,
                                                                       PendingIntent.FLAG_UPDATE_CURRENT);
        // Exact alarms not used here, so that android can perform its normal operation on devices >= 4.4 (KITKAT) to
        // prevent unnecessary battery drain by alarms.
        //
        // Also set alarms for both that day, and a day before the assignment's deadline, to act as a reminder to the
        // deadline.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC, CURRENT, assignmentPiCurrent);
            if (PREVIOUS > NOW) alarmManager.setAndAllowWhileIdle(AlarmManager.RTC, PREVIOUS, assignmentPiPrevious);
        } else {
            alarmManager.set(AlarmManager.RTC, CURRENT, assignmentPiCurrent);
            if (PREVIOUS > NOW) alarmManager.set(AlarmManager.RTC, PREVIOUS, assignmentPiPrevious);
        }
        return true;
    }

    // Truncate lecturer's name to enable user view more of the name because if the name is too long, the system will
    // add ellipses at the end of the name, thereby removing some important parts of the name.
    private String truncateLecturerName(String fullName) {
        String[] nameTokens = fullName.split(" ");

        String[] titles = {"Barr", "Barrister", "Doc", "Doctor", "Dr", "Engineer", "Engr", "Mr",
                           "Mister", "Mrs", "Ms", "Prof", "Professor"};

        StringBuilder nameBuilder = new StringBuilder();
        String shortenedName = "";

        int iMax = nameTokens.length - 1;

        int nameLimit = getResources().getInteger(R.integer.name_limit);
        if (fullName.length() > nameLimit && nameTokens.length > 2) {
            if (startsWithAny(titles, fullName)) {
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
                    nameBuilder.append(Character.toUpperCase(nameTokens[i].charAt(0)));
                    if (i == iMax) break;
                    nameBuilder.append(" ");
                }
            } else {
                for (int i = 0; i <= iMax; i++) {
                    nameBuilder.append(Character.toUpperCase(nameTokens[i].charAt(0)));
                    if (i == iMax) break;
                    nameBuilder.append(" ");
                }
            }

            int bMaxLen = nameBuilder.length();

            return nameBuilder.replace(bMaxLen - 1, bMaxLen, nameTokens[iMax]).toString();

        } else {
            return fullName;
        }
    }

    // Determines if there was an added title in the lecturer's name
    private boolean startsWithAny(String[] titles, String s) {
        for (String title : titles) if (s.startsWith(title)) return true;
        return false;
    }
}