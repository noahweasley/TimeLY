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
   public static final String DISPLAY_FORMAT_MINUTES = "minutes";
   public static final String DISPLAY_FORMAT_SECONDS = "seconds";
   public static final String DISPLAY_FORMAT_HOURS = "hours";
   private String displayFormat = DISPLAY_FORMAT_SECONDS;
   private long counter = 0L;
   private long initialCount = 0L;
   private volatile boolean wantToStopOp;
   private volatile boolean isTimerRunning;
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
            setCounter(tarr.getInt(R.styleable.CountdownTimer_start_count, 0));
            if (tarr.hasValue(R.styleable.CountdownTimer_display_type))
               displayFormat = tarr.getString(R.styleable.CountdownTimer_display_type);
         } finally {
            tarr.recycle();
         }
      }
   }

   @Override
   protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);
   }

   /**
    * Clears the timers, resetting it to start
    */
   public void clearTimer() {
      counter = 0;
      pause();
      timerThread = null;
      isTimerRunning = false;

      String formattedTime = null;
      if (displayFormat.equals(DISPLAY_FORMAT_MINUTES)) {
         formattedTime = Converter.convertMillisToRealTime(counter, Converter.Options.INCLUDE_MIN);
      } else if (displayFormat.equals(DISPLAY_FORMAT_HOURS)) {
         formattedTime = Converter.convertMillisToRealTime(counter, Converter.Options.INCLUDE_HOUR);
      } else {
         formattedTime = Converter.convertMillisToRealTime(counter, Converter.Options.SECONDS_ONLY);
      }

      setText(formattedTime);
   }

   /**
    * @return the status of the countdown timer
    */
   public boolean isTimerRunning() {
      return isTimerRunning;
   }

   /**
    * Stops the timer and resets it
    */
   public void stopTimer() {
      pauseTimer();
      pause();
      timerThread = null;
      isTimerRunning = false;
      setCounter(0L);
      setText(R.string.time_min_default);
   }

   private void pause() {
      wantToStopOp = true;
      isTimerRunning = false;
   }

   /**
    * Pause the timer but timer is still active. All callbacks would not be invoked.
    */
   public void pauseTimer() {
      isTimerRunning = true;
   }

   /**
    * Resumes excecution of timer. All callbacks would be invoked now.
    */
   public void resumeTimer() {
      isTimerRunning = true;
   }

   /**
    * Sets the display format of the count-down timer
    *
    * @param displayFormat the format to be displayed
    */
   public void setDisplayFormat(@MagicConstant(
           stringValues = { DISPLAY_FORMAT_HOURS, DISPLAY_FORMAT_MINUTES, DISPLAY_FORMAT_SECONDS }) String displayFormat) {
      this.displayFormat = displayFormat;
   }

   /**
    * Sets the count-down timer update listener
    *
    * @param listener the listener in which it's callback functions would to be invoked
    */
   public void setOnTimerUpdateListener(OnTimerUpdateListener listener) {
      this.listener = listener;
   }

   /**
    * Sets the counter start decrement value
    * @param millis the start decrement in milli-seconds
    */
   public void setCounter(long millis) {
      this.counter = millis;
      this.initialCount = millis;
   }

   /**
    * @return the display format of the count-down timer
    */
   public String getDisplayFormat() {
      return displayFormat;
   }

   /**
    * If the count-down timer was stopped, this would restart it, and the timer starts counting from
    * value set at {@link this#setCounter(long)}
    */
   public void restart() {
      if (!isTimerRunning) {
         this.counter = this.initialCount;
         if (timerThread == null) {
            timerThread = new Thread(this);
            timerThread.start();
         }
         resumeTimer();
      } else {
         throw new IllegalStateException("timer is already running");
      }
   }

   /**
    * Call this to start running the timer. Count-down timer never runs, until this is called.
    */
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
         if (isTimerRunning) {
            String formattedTime = null;
            if (displayFormat.equals(DISPLAY_FORMAT_MINUTES)) {
               formattedTime = Converter.convertMillisToRealTime(counter, Converter.Options.INCLUDE_MIN);
            } else if (displayFormat.equals(DISPLAY_FORMAT_HOURS)) {
               formattedTime = Converter.convertMillisToRealTime(counter, Converter.Options.INCLUDE_HOUR);
            } else {
               formattedTime = Converter.convertMillisToRealTime(counter, Converter.Options.SECONDS_ONLY);
            }

            final String finalFormattedTime = formattedTime;

            SystemClock.sleep(1000);
            counter -= 1000;

            if (getHandler() != null)
               getHandler().post(() -> {
                  if (listener != null) {
                     listener.onTimeDecrement(counter);
                     if (counter == 0) {
                        listener.onTimerEnd();
                        pauseTimer();
                     }
                  }
                  setText(finalFormattedTime);
               });
         }
      } while (!wantToStopOp);
   }

   @Override
   protected void finalize() throws Throwable {
      super.finalize();
      listener = null;
   }

   /**
    * Callbacks that would be invoked when timer counts-down to zero
    */
   public interface OnTimerUpdateListener {
      void onTimerEnd();

      void onTimeDecrement(long countRemain);
   }
}
