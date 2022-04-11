package com.noah.timely.auth.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.material.textfield.TextInputLayout;
import com.noah.timely.R;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CompleteRegistrationActivity extends AppCompatActivity {
   private AutoCompleteTextView listCountries;
   private EditText edt_datePicker;

   /**
    * Convenience method to start this activity and pass other details to it
    *
    * @param context the starter
    */
   public static void start(Context context) {
      Intent starter = new Intent(context, CompleteRegistrationActivity.class);
      context.startActivity(starter);
   }

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_complete_registration);

      listCountries = findViewById(R.id.list_countries);
      edt_datePicker = findViewById(R.id.date_picker);

      setupDateForm();

      Button signUp = findViewById(R.id.sign_up);

      // populate listCountries, with list of countries for auto-completion
      String[] countryArray = getResources().getStringArray(R.array.countries_array);
      ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, countryArray);

      listCountries.setAdapter(adapter);
      // Actions when calendar icon is clicked on the date of birth text field
      Calendar calendar = Calendar.getInstance();
      // final actions for date selection
//      DatePickerDialog.OnDateSetListener listener =
//              (view, year, month, dayOfMonth) -> {
//                 // calendar month starts from 0. 0 is not really a month to the user. Add 1
//                 String dateFormat = String.format(Locale.US, "%02d-%02d-%02d", dayOfMonth, month + 1, year);
//                 // Replace the whole string, because of errors
//                 edt_datePicker.setText(dateFormat);
//              };
      // listen for right | end drawable clicks
//        new DatePickerDialog(this,
//                             listener,
//                             calendar.get(Calendar.YEAR),
//                             calendar.get(Calendar.MONTH),
//                             calendar.get(Calendar.DAY_OF_MONTH))
//                .show();

      signUp.setOnClickListener(v -> validateFormData());
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

         edt_datePicker.setText(parsedDate);
      };

      edt_datePicker.setOnTouchListener((v, event) -> {
         final int DRAWABLE_RIGHT = 2;

         if (event.getAction() == MotionEvent.ACTION_UP) {
            int padding;
            int drawableWidth = edt_datePicker.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
               padding = edt_datePicker.getPaddingEnd();
            else padding = edt_datePicker.getPaddingRight();

            if (event.getX() >= (edt_datePicker.getWidth() - drawableWidth - padding)) {
               Calendar calendar = Calendar.getInstance();
               DatePickerDialog dpd = DatePickerDialog
                       .newInstance(odsl,
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

   // validate date input
   @SuppressWarnings("all")
   private void validateFormData() {
      String datePattern = "^((0[1-9]|[12][0-9]|3[01])[-/](0[1-9]|1[012])[-/](\\d){4})$";

      boolean isCountryEmpty = TextUtils.isEmpty(listCountries.getText());

      boolean dateMatches = edt_datePicker.getText().toString().matches(datePattern);

      if (dateMatches && !isCountryEmpty) {
//            startActivity(new Intent(this, AddFollowersPageActivity.class));
      } else {
         // because TextInputLayout has child FrameLayout that is the TextInputEditText's parent
         // view, calling getParent() directly on the TextInputEditText will return the FrameLayout
         // and not the TextInputLayout
         if (!dateMatches) {
            ViewGroup container = (ViewGroup) edt_datePicker.getParent();
            TextInputLayout til_datePickerParent = ((TextInputLayout) container.getParent());
            til_datePickerParent.setError("Format: dd-mm-yyyy or dd/mm/yyy");
         }

         if (isCountryEmpty) {
            ViewGroup container = (ViewGroup) listCountries.getParent();
            ((TextInputLayout) container.getParent()).setError("Field can't be empty");
         }
      }
   }

   @Override
   public void onBackPressed() {
      super.onBackPressed();
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
   }
}