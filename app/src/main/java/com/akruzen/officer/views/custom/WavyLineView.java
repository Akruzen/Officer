package com.akruzen.officer.views.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.akruzen.officer.R;

public class WavyLineView extends View {

    private Paint paint, fillPaintTop, fillPaintBottom;
    private Path wavePath, topFillPath, bottomFillPath;

    private float strokeWidth = 5f;       // Default stroke width
    private int waveColor = Color.BLACK;  // Default wave color
    private int waveSeparation = 50;        // Distance between two waves
    private float frequency;              // Calculated frequency
    private int waveTopColor = Color.TRANSPARENT;    // Default top fill color
    private int waveBottomColor = Color.TRANSPARENT; // Default bottom fill color

    public WavyLineView(Context context) {
        super(context);
        init();
    }

    public WavyLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttributes(context, attrs);
        init();
    }

    public WavyLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttributes(context, attrs);
        init();
    }

    private void initAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.WavyLineView, 0, 0);

        try {
            strokeWidth = a.getDimension(R.styleable.WavyLineView_waveStrokeWidth, strokeWidth);
            waveColor = a.getColor(R.styleable.WavyLineView_waveColor, waveColor);
            waveTopColor = a.getColor(R.styleable.WavyLineView_waveTopColor, waveTopColor);
            waveBottomColor = a.getColor(R.styleable.WavyLineView_waveBottomColor, waveBottomColor);
            waveSeparation = a.getInt(R.styleable.WavyLineView_waveSeparation, waveSeparation);
        } finally {
            a.recycle();
        }
    }

    private void init() {
        // Paint for the wave stroke
        paint = new Paint();
        paint.setColor(waveColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setAntiAlias(true);

        // Paint for the top fill
        fillPaintTop = new Paint();
        fillPaintTop.setColor(waveTopColor);
        fillPaintTop.setStyle(Paint.Style.FILL);

        // Paint for the bottom fill
        fillPaintBottom = new Paint();
        fillPaintBottom.setColor(waveBottomColor);
        fillPaintBottom.setStyle(Paint.Style.FILL);

        wavePath = new Path();
        topFillPath = new Path();
        bottomFillPath = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Recalculate frequency based on the number of waves and screen width
        frequency = (float) (2 * Math.PI / waveSeparation);

        invalidate(); // Force redraw
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        wavePath.reset();
        topFillPath.reset();
        bottomFillPath.reset();

        float amplitude = height / 16f; // Amplitude of the wave
        float centerY = height / 2f;

        // Build the wave path
        wavePath.moveTo(0, centerY);
        topFillPath.moveTo(0, 0);
        bottomFillPath.moveTo(0, height);

        for (int x = 0; x <= width; x++) {
            float y = (float) (amplitude * Math.sin(frequency * x)) + centerY;

            wavePath.lineTo(x, y);
            topFillPath.lineTo(x, y);
            bottomFillPath.lineTo(x, y);
        }

        // Complete the top and bottom fill paths
        topFillPath.lineTo(width, 0);
        topFillPath.close();

        bottomFillPath.lineTo(width, height);
        bottomFillPath.close();

        // Draw top fill, bottom fill, and the wave stroke
        if (waveTopColor != Color.TRANSPARENT) {
            canvas.drawPath(topFillPath, fillPaintTop);
        }

        if (waveBottomColor != Color.TRANSPARENT) {
            canvas.drawPath(bottomFillPath, fillPaintBottom);
        }

        canvas.drawPath(wavePath, paint);
    }
}