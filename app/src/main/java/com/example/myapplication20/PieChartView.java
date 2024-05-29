package com.example.myapplication20;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

public class PieChartView extends View {

    private Paint paint;
    private List<Float> values;
    private List<String> labels;
    private List<Integer> colors;

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(30);
        paint.setColor(Color.BLACK);
    }

    public void setData(List<Float> values, List<String> labels, List<Integer> colors) {
        this.values = values;
        this.labels = labels;
        this.colors = colors;
        invalidate(); // Redraw the view
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (values == null || values.size() == 0) {
            return;
        }

        float total = 0;
        for (float value : values) {
            total += value;
        }

        float startAngle = 0;
        for (int i = 0; i < values.size(); i++) {
            paint.setColor(colors.get(i));
            float sweepAngle = (values.get(i) / total) * 360;
            canvas.drawArc(100, 100, getWidth() - 100, getHeight() - 100, startAngle, sweepAngle, true, paint);

            // Draw percentage text
            paint.setColor(Color.BLACK);
            float textAngle = startAngle + sweepAngle / 2;
            float x = (float) ((getWidth() / 2) + (getWidth() / 4) * Math.cos(Math.toRadians(textAngle)));
            float y = (float) ((getHeight() / 2) + (getHeight() / 4) * Math.sin(Math.toRadians(textAngle)));
            canvas.drawText(String.format("%.1f%%", (values.get(i) / total) * 100), x, y, paint);

            startAngle += sweepAngle;
        }
    }
}
