package com.astrro.timely.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * A custom text view, where the text has been rotated 90 degrees, anti-clockwise
 */
public class VerticalTextView extends AppCompatTextView {
   private final Rect bounds = new Rect();
   private int width, height;

   public VerticalTextView(Context context) {
      super(context);
   }

   public VerticalTextView(Context context, AttributeSet attrs) {
      super(context, attrs);
   }

   public VerticalTextView(Context context, AttributeSet attrs, int defStyleAttr) {
      super(context, attrs, defStyleAttr);
   }

   @Override
   protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
      height = getMeasuredWidth();
      width = getMeasuredHeight();
      setMeasuredDimension(width, height);
   }

   @Override
   protected void onDraw(Canvas canvas) {
      canvas.save();
      canvas.translate(width, height);
      canvas.rotate(-90);

      TextPaint paint = getPaint();
      paint.setColor(getTextColors().getDefaultColor());

      String text = super.getText().toString();
      paint.getTextBounds(text, 0, text.length(), bounds);
      canvas.drawText(text, getCompoundPaddingLeft(), (bounds.height() - width) >> 1, paint);
      canvas.restore();
   }
}