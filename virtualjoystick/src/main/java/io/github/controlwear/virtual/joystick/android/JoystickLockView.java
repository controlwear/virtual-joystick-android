package io.github.controlwear.virtual.joystick.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

@SuppressLint("ViewConstructor")
public class JoystickLockView extends View {

    private boolean mIsEnabled;
    private float mRadius;

    private final Paint mBackgroundPaint, mBorderPaint, mEnabledPaint;
    public JoystickLockView(Context context, float radius, Paint backgroundPaint, Paint borderPaint) {
        super(context);
        mRadius = radius;
        mBackgroundPaint = backgroundPaint;
        mBorderPaint = borderPaint;
        mEnabledPaint = new Paint(mBorderPaint);
        mEnabledPaint.setStyle(Paint.Style.FILL);
    }

    public void setEnabled(boolean isEnabled) {
        mIsEnabled = isEnabled;
        invalidate();
    }

    public void setRadius(float radius) {
        mRadius = radius;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float halfWidth = getWidth()/2f;
        float halfHeight = getHeight()/2f;
        canvas.drawCircle(halfWidth, halfHeight, mRadius, mBackgroundPaint);
        if(mIsEnabled)
            canvas.drawCircle(halfWidth, halfHeight, mRadius, mEnabledPaint);
        else
            canvas.drawCircle(halfWidth, halfHeight, mRadius, mBorderPaint);
    }
}
