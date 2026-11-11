package com.mm.mmaps;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CompassView extends View {
    private float currentBearing = 0f;
    private Paint textPaint;
    private Paint linePaint;
    private Paint bgPaint;

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Text style for directions (N, E, S, W, etc.)
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFFFFFFFF); // White text
        textPaint.setTextSize(36f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // Tick marks style
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(0x80FFFFFF); // Semi-transparent white
        linePaint.setStrokeWidth(4f);

        // Dark transparent HUD background style
        bgPaint = new Paint();
        bgPaint.setColor(0x99000000); // Semi-transparent dark background
    }

    public void setBearing(float bearing) {
        this.currentBearing = bearing;
        invalidate(); // Redraw view when camera moves
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Draw HUD background bar
        canvas.drawRect(0, 0, width, height, bgPaint);

        // Compass directions mapping with their angles
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        float[] angles = {0, 45, 90, 135, 180, 225, 270, 315};

        float pixelsPerDegree = width / 120f; // Shows 120 degrees of view at a time

        for (int i = 0; i < directions.length; i++) {
            float angleDiff = angles[i] - currentBearing;

            // Normalize angle diff to -180 to 180
            while (angleDiff < -180) angleDiff += 360;
            while (angleDiff > 180) angleDiff -= 360;

            // Calculate X position on the screen based on bearing difference
            float xPos = (width / 2f) + (angleDiff * pixelsPerDegree);

            // Only draw if it's visible on the screen bar
            if (xPos > -50 && xPos < width + 50) {
                // Draw text label
                canvas.drawText(directions[i], xPos, height / 1.8f, textPaint);
                // Draw small tick line under text
                canvas.drawLine(xPos, height - 10f, xPos, height, linePaint);
            }
        }

        // Draw a center indicator triangle/line for the current exact facing direction
        Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setColor(0xFFFF0000); // Red center marker line
        centerPaint.setStrokeWidth(6f);
        canvas.drawLine(width / 2f, 0, width / 2f, 15f, centerPaint);
    }
}