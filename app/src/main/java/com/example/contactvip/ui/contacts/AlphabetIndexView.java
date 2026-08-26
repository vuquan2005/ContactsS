package com.example.contactvip.ui.contacts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class AlphabetIndexView extends View {
    private static final String[] ALPHABET = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};
    private Paint paint;
    private int chosenIndex = -1;
    private OnIndexSelectedListener listener;

    public interface OnIndexSelectedListener {
        void onIndexSelected(char letter);
    }

    public AlphabetIndexView(Context context) {
        super(context);
        init();
    }

    public AlphabetIndexView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setAntiAlias(true);
        paint.setTextSize(30);
        paint.setTextAlign(Paint.Align.CENTER);
    }

    public void setOnIndexSelectedListener(OnIndexSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = getHeight();
        int width = getWidth();
        int singleHeight = height / ALPHABET.length;

        for (int i = 0; i < ALPHABET.length; i++) {
            float xPos = width / 2;
            float yPos = singleHeight * i + singleHeight;
            canvas.drawText(ALPHABET[i], xPos, yPos, paint);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float y = event.getY();
        int oldChosenIndex = chosenIndex;
        int newChosenIndex = (int) (y / getHeight() * ALPHABET.length);

        switch (action) {
            case MotionEvent.ACTION_UP:
                chosenIndex = -1;
                invalidate();
                break;
            default:
                if (oldChosenIndex != newChosenIndex) {
                    if (newChosenIndex >= 0 && newChosenIndex < ALPHABET.length) {
                        if (listener != null) {
                            listener.onIndexSelected(ALPHABET[newChosenIndex].charAt(0));
                        }
                        chosenIndex = newChosenIndex;
                        invalidate();
                    }
                }
                break;
        }
        return true;
    }
}
