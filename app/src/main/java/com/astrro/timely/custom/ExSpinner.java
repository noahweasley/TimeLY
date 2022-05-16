package com.astrro.timely.custom;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;

import com.astrro.timely.R;

/**
 * A custom spinner that tries to mimic Spotify Android app spinner functionality
 */
public class ExSpinner extends AppCompatSpinner {
   private final RectF rect = new RectF();
   private Paint linePaint;
   private final Point point1 = new Point();
   private final Point point2 = new Point();
   private final Point point3 = new Point();
   private float radius;

   public ExSpinner(@NonNull Context context) {
      super(context);
      init(null);
   }

   public ExSpinner(@NonNull Context context, int mode) {
      super(context, mode);
      init(null);
   }

   public ExSpinner(@NonNull Context context,
                    @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
      super(context, attrs);
      init(attrs);
   }

   public ExSpinner(@NonNull Context context,
                    @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
      super(context, attrs, defStyleAttr);
      init(attrs);
   }

   public ExSpinner(@NonNull Context context,
                    @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr, int mode) {
      super(context, attrs, defStyleAttr, mode);
      init(attrs);
   }

   public ExSpinner(@NonNull Context context,
                    @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr, int mode,
                    Resources.Theme popupTheme) {
      super(context, attrs, defStyleAttr, mode, popupTheme);
      init(attrs);
   }

   private void init(AttributeSet attrs) {
//      TypedArray tarr = attrs.getAttributeValue(R.styleable.);

      radius = getContext().getResources().getDimension(R.dimen.corner_radius);
      linePaint = new Paint();

      linePaint.setStyle(Paint.Style.STROKE);
      linePaint.setColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
      linePaint.setStrokeWidth(8.f);
   }

   @Override
   public void onDraw(Canvas canvas) {
      super.onDraw(canvas);
      rect.top = 0;
      rect.left = 0;
      rect.bottom = getHeight();
      rect.right = getWidth();
      canvas.drawRoundRect(rect, radius, radius, linePaint);
   }

   @Override
   public boolean onTouchEvent(MotionEvent event) {
      if (event.getAction() == MotionEvent.ACTION_DOWN) {
         linePaint.setColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
         invalidate();
      } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
         linePaint.setColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
         invalidate();
      }

      return super.onTouchEvent(event);
   }

}
