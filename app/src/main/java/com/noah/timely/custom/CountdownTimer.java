package com.noah.timely.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.noah.timely.R;
import com.noah.timely.util.Converter;

import org.intellij.lang.annotations.MagicConstant;

/**
 * A nomal TextView sometimes :) but is mainly used as a countdown timer
 */
public class CountdownTimer extends AppCompatTextView implements Runnable {
   public static final String DISPLAY_TYPE_MINUTES = "minutes";
   public static final String DISPLAY_TYPE_SECONDS = "seconds";
   public static final String DISPLAY_TYPE_HOURS = "hours";
   private String displayType = DISPLAY_TYPE_SECONDS;
   private long startCount = 0L;
   private volatile boolean wantToStopOp;
   private volatile boolean isTimerPaused;
   private boolean isTimerRunning;
   private Thread timerThread;
   private OnTimerUpdateListener listener;

   public CountdownTimer(Context context) {
      super(context);
      init(context, null);
   }

   public CountdownTimer(Context context, @Nullable AttributeSet attrs) {
      super(context, attrs);
      init(context, attrs);
   }

   public CountdownTimer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
      super(context, attrs, defStyleAttr);
      init(context, attrs);
   }

   private void init(Context context, AttributeSet attrs) {
      if (attrs != null) {
         TypedArray tarr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CountdownTimer, 0, 0);
         try {
            startCount = tarr.getInt(R.styleable.CountdownTimer_start_count, 0);
            if (tarr.hasValue(R.styleable.CountdownTimer_display_type))
               displayType = tarr.getString(R.styleable.CountdownTimer_display_type);
         } finally {
            tarr.recycle();
         }
      }
   }

   @Override
   protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);
   }

   public void clearTimer() {
      startCount = 0;
      pause();
      timerThread = null;
      isTimerRunning = false;

      String formattedTime = null;
      if (displayType.equals(DISPLAY_TYPE_MINUTES)) {
         formattedTime = Converter.convertMillisToRealTime(startCount, Converter.Options.INCLUDE_MIN);
      } else if (displayType.equals(DISPLAY_TYPE_HOURS)) {
         formattedTime = Converter.convertMillisToRealTime(startCount, Converter.Options.INCLUDE_HOUR);
      } else {
         formattedTime = Converter.convertMillisToRealTime(startCount, Converter.Options.SECONDS_ONLY);
      }

      setText(formattedTime);
   }

   public boolean isTimerRunning() {
      return isTimerRunning;
   }

   private void pause() {
      wantToStopOp = true;
   }

   public void pauseTimer() {
      isTimerPaused = true;
   }

   public void resumeTimer() {
      isTimerPaused = false;
   }

   public void setDisplayType(@MagicConstant(
           stringValues = { DISPLAY_TYPE_HOURS, DISPLAY_TYPE_MINUTES, DISPLAY_TYPE_SECONDS }) String displayType) {
      this.displayType = displayType;
   }

   public void setOnTimerUpdateListener(OnTimerUpdateListener listener) {
      this.listener = listener;
   }

   public void setStartCount(long millis) {
      this.startCount = millis;
   }

   public String getDisplayType() {
      return displayType;
   }

   public void start() {
      if (timerThread == null) {
         timerThread = new Thread(this);
         timerThread.start();
      } else {
         throw new IllegalStateException("timer is already running");
      }
   }

   @Override
   public void run() {
      do {
         if (!isTimerPaused) {
            String formattedTime = null;
            if (displayType.equals(DISPLAY_TYPE_MINUTES)) {
               formattedTime = Converter.convertMillisToRealTime(startCount, Converter.Options.INCLUDE_MIN);
            } else if (displayType.equals(DISPLAY_TYPE_HOURS)) {
               formattedTime = Converter.convertMillisToRealTime(startCount, Converter.Options.INCLUDE_HOUR);
            } else {
               formattedTime = Converter.convertMillisToRealTime(startCount, Converter.Options.SECONDS_ONLY);
            }

            final String finalFormattedTime = formattedTime;

            SystemClock.sleep(1000);
            startCount -= 1000;

            if (listener != null) {
               listener.onTimeDecrement(startCount);
               if (startCount == 0) {
                  listener.onTimerEnd();
               }
            }

            getHandler().post(() -> setText(finalFormattedTime));
         }
      } while (startCount > 0 && !wantToStopOp);
   }

   public interface OnTimerUpdateListener {
      void onTimerEnd();

      void onTimeDecrement(long counRemain);
   }
}
