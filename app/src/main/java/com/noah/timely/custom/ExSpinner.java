package com.noah.timely.custom;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;

import com.noah.timely.R;

/**
 * A custom spinner that tries to mimic Spotify Android app spinner functionality
 */
public class ExSpinner extends AppCompatSpinner {
   private Paint linePaint, trianglePaint;
   private final Point point1 = new Point();
   private final Point point2 = new Point();
   private final Point point3 = new Point();
   private Path path;

   public ExSpinner(@NonNull Context context) {
      super(context);
      init();
   }

   public ExSpinner(@NonNull Context context, int mode) {
      super(context, mode);
      init();
   }

   public ExSpinner(@NonNull Context context,
                    @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
      super(context, attrs);
      init();
   }

   public ExSpinner(@NonNull Context context,
                    @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
      super(context, attrs, defStyleAttr);
      init();
   }

   public ExSpinner(@NonNull Context context,
                    @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr, int mode) {
      super(context, attrs, defStyleAttr, mode);
      init();
   }

   public ExSpinner(@NonNull Context context,
                    @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr, int mode,
                    Resources.Theme popupTheme) {
      super(context, attrs, defStyleAttr, mode, popupTheme);
      init();
   }

   private void init() {
      linePaint = new Paint();
      trianglePaint = new Paint();

      linePaint.setStyle(Paint.Style.STROKE);
      linePaint.setColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
      linePaint.setStrokeWidth(8.f);

      trianglePaint.setColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
      trianglePaint.setStyle(Paint.Style.FILL);

      path = new Path();
      path.setFillType(Path.FillType.EVEN_ODD);
   }

   @Override
   public void onDraw(Canvas canvas) {
      super.onDraw(canvas);
      setBackground(null);
      int width = getWidth();
      int height = getHeight();
      canvas.drawLine(0, height, width, height, linePaint);
      path.moveTo(width, height);
      path.lineTo(width, height - 20);
      path.lineTo(width - 20, height);
      path.close();
      canvas.drawPath(path, trianglePaint);
   }

   @Override
   public boolean onTouchEvent(MotionEvent event) {
      if (event.getAction() == MotionEvent.ACTION_DOWN) {
         linePaint.setColor(ContextCompat.getColor(getContext(), R.color.accent));
         trianglePaint.setColor(ContextCompat.getColor(getContext(), R.color.accent));
         invalidate();
      } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
         linePaint.setColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
         trianglePaint.setColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
         invalidate();
      }

      return super.onTouchEvent(event);
   }

}
