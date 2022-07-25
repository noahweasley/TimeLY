package com.astrro.timely.auth.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputLayout;
import com.astrro.timely.R;
import com.astrro.timely.auth.data.model.UserAccount;
import com.astrro.timely.gallery.Image;
import com.astrro.timely.gallery.ImageDirectory;
import com.astrro.timely.gallery.ImageGallery;
import com.astrro.timely.io.IOUtils;
import com.astrro.timely.main.MainActivity;
import com.astrro.timely.util.Constants;
import com.astrro.timely.util.PatternUtils;
import com.squareup.picasso.Picasso;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.yalantis.ucrop.UCrop;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CompleteRegistrationActivity extends AppCompatActivity implements View.OnClickListener {
   private static final int MAX_IMAGE_WIDTH = 400;
   private static final int MAX_IMAGE_HEIGHT = 400;
   private static final String EXTRA_USER_ACCOUNT = "User Account";
   private AutoCompleteTextView listCountries, listSchools;
   private EditText edt_datePicker;
   private ShapeableImageView img_profileImage;

   /**
    * Convenience method to start this activity and pass other details to it
    *
    * @param context     the starter
    * @param userAccount the user account that would be used in completing registration
    */
   public static void start(Context context, UserAccount userAccount) {
      Intent starter = new Intent(context, CompleteRegistrationActivity.class);
      starter.putExtra(EXTRA_USER_ACCOUNT, userAccount);
      context.startActivity(starter);
   }

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_complete_registration);
      img_profileImage = findViewById(R.id.profile_image);
      // image picker
      View v_image_picker = findViewById(R.id.profile_image_picker);
      v_image_picker.setOnClickListener(v -> {
         Intent imagePickerIntent = new Intent(this, ImageDirectory.class);
         imagePickerIntent.setAction(ImageGallery.ACTION_SINGLE_SELECT);
         startActivity(imagePickerIntent);
      });
      // ...
      ImageButton exit = findViewById(R.id.exit);
      exit.setOnClickListener(v -> onBackPressed());

      listCountries = findViewById(R.id.list_countries);
      listSchools = findViewById(R.id.list_schools);
      edt_datePicker = findViewById(R.id.date_picker);

      setupDateForm();

      Button btn_complete = findViewById(R.id.complete);
      ImageButton btn_skip = findViewById(R.id.skip);

      btn_skip.setOnClickListener(this);
      btn_complete.setOnClickListener(this);

      // populate listCountries, with list of countries for auto-completion
      String[] countryArray = getResources().getStringArray(R.array.countries_array);
      String[] schoolArray = getResources().getStringArray(R.array.school_array);

      ArrayAdapter<String> countryAdapter =
              new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, countryArray);
      ArrayAdapter<String> schoolAdapter =
              new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, schoolArray);

      listCountries.setAdapter(countryAdapter);
      listSchools.setAdapter(schoolAdapter);
      btn_complete.setOnClickListener(v -> validateFormData());
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

   // validate date input
   private void validateFormData() {
      boolean isCountryEmpty = TextUtils.isEmpty(listCountries.getText());
      boolean isSchoolEmpty = TextUtils.isEmpty(listSchools.getText());
      boolean dateMatches = edt_datePicker.getText().toString().matches(PatternUtils.DATE_ALL);

      if (dateMatches && !isCountryEmpty && isSchoolEmpty) {
         registerNewUser();
      } else {
         // because TextInputLayout has child, FrameLayout, that is the TextInputEditText's parent
         // view, calling getParent() directly on the TextInputEditText will return the FrameLayout
         // and not the TextInputLayout
         if (!dateMatches) {
            ViewGroup container = (ViewGroup) edt_datePicker.getParent();
            TextInputLayout til_datePickerParent = ((TextInputLayout) container.getParent());
            til_datePickerParent.setError("Input a correct data format");
         }

         if (isCountryEmpty) {
            ViewGroup container = (ViewGroup) listCountries.getParent();
            ((TextInputLayout) container.getParent()).setError("Field can't be empty");
         }
      }
   }

   private void registerNewUser() {
      UserAccount userAccount = (UserAccount) getIntent().getSerializableExtra(EXTRA_USER_ACCOUNT);
      startActivity(new Intent(this, MainActivity.class));
   }

   @Override
   public void onBackPressed() {
      super.onBackPressed();
      overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
   }

   @Override
   public void onClick(View v) {
      int viewId = v.getId();
      if (viewId == R.id.skip) {
         Intent intent = new Intent(this, MainActivity.class);
         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
         startActivity(intent);
         overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
      }
   }

   @Override
   protected void onNewIntent(Intent intent) {
      super.onNewIntent(intent);
      if (intent.getAction().equals(Constants.ACTION.SHOW_PICTURE)) {
         Image selectedImage = (Image) intent.getSerializableExtra(Constants.EXTRA.EXTRA_IMAGE);

         if (selectedImage != null) {
            String tempImageFileName = String.format(Locale.US, "IMG%d", SystemClock.elapsedRealtime());
            IOUtils.createTempImage(this, tempImageFileName, file -> {
               // ... then start image cropper
               UCrop.Options options = new UCrop.Options();
               options.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
               options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
               options.setToolbarWidgetColor(ContextCompat.getColor(this, R.color.white));
               UCrop.of(selectedImage.getImageUri(), Uri.fromFile(file))
                    .withAspectRatio(6f, 6f)
                    .withOptions(options)
                    .withMaxResultSize(MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT)
                    .start(this);

            });
         }
      }
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode,
                                   @Nullable @org.jetbrains.annotations.Nullable Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
         Uri uri = UCrop.getOutput(data);
         Picasso.get().load(uri).fit().centerInside().into(img_profileImage);
      } else if (resultCode == UCrop.RESULT_ERROR) {
         //noinspection ThrowableNotThrown
         Throwable cropError = UCrop.getError(data);
      }
   }

}