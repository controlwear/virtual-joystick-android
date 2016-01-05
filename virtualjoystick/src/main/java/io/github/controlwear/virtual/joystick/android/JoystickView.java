package io.github.controlwear.virtual.joystick.android;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class JoystickView extends View {


    private static final int DEFAULT_SIZE = 200;
    private static final double RATIO_SIZE_BUTTON = 0.25;
    private static final double RATIO_SIZE_BORDER = 0.75;

    // DRAWING
    private int mButtonColor;
    private int mBorderColor;
    private Paint mPaintCircleButton;
    private Paint mPaintCircleBorder;

    // COORDINATE
    private int mPosX = 0;
    private int mPosY = 0;
    private int mCenterX = 0;
    private int mCenterY = 0;

    // SIZE
    private int mBorderRadius;
    private int mButtonRadius;


    /*
    CONSTRUCTORS
     */


    public JoystickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.JoystickView,
                0, 0
        );

        try {
            mButtonColor = styledAttributes.getColor(R.styleable.JoystickView_buttonColor, Color.BLACK);
            mBorderColor = styledAttributes.getColor(R.styleable.JoystickView_borderColor, Color.BLACK);
        } finally {
            styledAttributes.recycle();
        }

        setupPaint();
    }


    public JoystickView(Context context) {
        super(context);
    }


    protected void setupPaint() {
        mPaintCircleButton = new Paint();
        mPaintCircleButton.setAntiAlias(true);
        mPaintCircleButton.setColor(mButtonColor);
        mPaintCircleButton.setStyle(Paint.Style.FILL);

        mPaintCircleBorder = new Paint();
        mPaintCircleBorder.setAntiAlias(true);
        mPaintCircleBorder.setColor(mBorderColor);
        mPaintCircleBorder.setStyle(Paint.Style.STROKE);
        mPaintCircleBorder.setStrokeWidth(3);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the circle border
        canvas.drawCircle(mCenterX, mCenterY, mBorderRadius, mPaintCircleBorder);

        // Draw the circle button
        canvas.drawCircle(mPosX, mPosY, mButtonRadius, mPaintCircleButton);
    }


    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);

        // get the center of view to position circle
        mCenterX = mPosX = getWidth() / 2;
        mCenterY = mPosY = getWidth() / 2;

        // radius based on smallest size : height OR width
        int d = Math.min(xNew, yNew);
        mButtonRadius = (int) (d / 2 * RATIO_SIZE_BUTTON);
        mBorderRadius = (int) (d / 2 * RATIO_SIZE_BORDER);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // setting the measured values to resize the view to a certain width and height
        int d = Math.min(measure(widthMeasureSpec), measure(heightMeasureSpec));
        setMeasuredDimension(d, d);
    }


    private int measure(int measureSpec) {
        if (MeasureSpec.getMode(measureSpec) == MeasureSpec.UNSPECIFIED) {
            // if no bounds are specified return a default size (200)
            return DEFAULT_SIZE;
        } else {
            // As you want to fill the available space
            // always return the full available bounds.
            return MeasureSpec.getSize(measureSpec);
        }
    }


    /*
    SETTERS
     */


    public void setButtonColor(int newColor){
        mButtonColor=newColor;
    }


    public void setBorderColor(int newColor){
        mBorderColor=newColor;
    }
}