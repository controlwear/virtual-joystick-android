package io.github.controlwear.virtual.joystick.android;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class JoystickView extends View
        implements
        Runnable {


    /*
    INTERFACES
    */


    /**
     * Interface definition for a callback to be invoked when a
     * JoystickView's button is moved
     */
    public interface OnMoveListener {

        /**
         * Called when a JoystickView's button has been moved
         * @param angle current angle
         * @param strength current strength
         */
        void onMove(int angle, int strength);
    }


    /**
     * Interface definition for a callback to be invoked when a JoystickView
     * is touched and held by multiple pointers.
     */
    public interface OnMultipleLongPressListener {
        /**
         * Called when a JoystickView has been touch and held enough time by multiple pointers.
         */
        void onMultipleLongPress();
    }


    /*
    CONSTANTS
    */

    /**
     * Default refresh rate as a time in milliseconds to send move values through callback
     */
    private static final int DEFAULT_LOOP_INTERVAL = 50; // in milliseconds

    /**
     * Used to allow a slight move without cancelling MultipleLongPress
     */
    private static final int MOVE_TOLERANCE = 10;

    /**
     * Default color for button and border
     */
    private static final int DEFAULT_COLOR = Color.BLACK;

    /**
     * Default background color
     */
    private static final int DEFAULT_BACKGROUND_COLOR = Color.TRANSPARENT;

    /**
     * Default View's size
     */
    private static final int DEFAULT_SIZE = 200;

    /**
     * Ratio use to define the size of the button
     */
    private static final double RATIO_SIZE_BUTTON = 0.25;

    /**
     * Ratio use to define the size of border (as the distance from the center)
     */
    private static final double RATIO_SIZE_BORDER = 0.75;

    /**
     * Default border's width
     */
    private static final int DEFAULT_WIDTH_BORDER = 3;


    // DRAWING
    private Paint mPaintCircleButton;
    private Paint mPaintCircleBorder;
    private Paint mPaintBackground;

    // COORDINATE
    private int mPosX = 0;
    private int mPosY = 0;
    private int mCenterX = 0;
    private int mCenterY = 0;

    // SIZE
    private int mButtonRadius;
    private int mBorderRadius;


    /**
     * Listener used to dispatch OnMove event
     */
    private OnMoveListener mCallback;

    private long mLoopInterval = DEFAULT_LOOP_INTERVAL;
    private Thread mThread = new Thread(this);


    /**
     * Listener used to dispatch MultipleLongPress event
     */
    private OnMultipleLongPressListener mOnMultipleLongPressListener;

    private final Handler mHandlerMultipleLongPress = new Handler();
    private Runnable mRunnableMultipleLongPress;
    private int mMoveTolerance;


    /*
    CONSTRUCTORS
     */


    /**
     * Simple constructor to use when creating a JoystickView from code.
     * Call another constructor passing null to Attribute.
     */
    public JoystickView(Context context) {
        this(context, null);
    }


    public JoystickView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, null);
    }


    /**
     * Constructor that is called when inflating a JoystickView from XML. This is called
     * when a JoystickView is being constructed from an XML file, supplying attributes
     * that were specified in the XML file.
     * @param context The Context the JoystickView is running in, through which it can
     *        access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the JoystickView.
     */
    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.JoystickView,
                0, 0
        );

        int buttonColor;
        int borderColor;
        int backgroundColor;
        int borderWidth;
        try {
            buttonColor = styledAttributes.getColor(R.styleable.JoystickView_buttonColor, DEFAULT_COLOR);
            borderColor = styledAttributes.getColor(R.styleable.JoystickView_borderColor, DEFAULT_COLOR);
            backgroundColor = styledAttributes.getColor(R.styleable.JoystickView_backgroundColor, DEFAULT_BACKGROUND_COLOR);
            borderWidth = styledAttributes.getDimensionPixelSize(R.styleable.JoystickView_borderWidth, DEFAULT_WIDTH_BORDER);
        } finally {
            styledAttributes.recycle();
        }

        // Initialize the drawing according to attributes

        mPaintCircleButton = new Paint();
        mPaintCircleButton.setAntiAlias(true);
        mPaintCircleButton.setColor(buttonColor);
        mPaintCircleButton.setStyle(Paint.Style.FILL);

        mPaintCircleBorder = new Paint();
        mPaintCircleBorder.setAntiAlias(true);
        mPaintCircleBorder.setColor(borderColor);
        mPaintCircleBorder.setStyle(Paint.Style.STROKE);
        mPaintCircleBorder.setStrokeWidth(borderWidth);

        mPaintBackground = new Paint();
        mPaintBackground.setAntiAlias(true);
        mPaintBackground.setColor(backgroundColor);
        mPaintBackground.setStyle(Paint.Style.FILL);


        // Init Runnable for MultiLongPress

        mRunnableMultipleLongPress = new Runnable() {
            @Override
            public void run() {
                if (mOnMultipleLongPressListener != null)
                    mOnMultipleLongPressListener.onMultipleLongPress();
            }
        };
    }


    /**
     * Draw the background, the border and the button
     * @param canvas the canvas on which the shapes will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the background
        canvas.drawCircle(mCenterX, mCenterY, mBorderRadius, mPaintBackground);

        // Draw the circle border
        canvas.drawCircle(mCenterX, mCenterY, mBorderRadius, mPaintCircleBorder);

        // Draw the circle button
        canvas.drawCircle(mPosX, mPosY, mButtonRadius, mPaintCircleButton);
    }


    /**
     * This is called during layout when the size of this view has changed.
     * Here we get the center of the view and the radius to draw all the shapes.
     *
     * @param w Current width of this view.
     * @param h Current height of this view.
     * @param oldW Old width of this view.
     * @param oldH Old height of this view.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        // get the center of view to position circle
        mCenterX = mPosX = getWidth() / 2;
        mCenterY = mPosY = getWidth() / 2;

        // radius based on smallest size : height OR width
        int d = Math.min(w, h);
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
    USER EVENT
     */


    /**
     * Handle touch screen motion event. Move the button according to the
     * finger coordinate and detect longPress by multiple pointers only.
     *
     * @param event The motion event.
     * @return True if the event was handled, false otherwise.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // to move the button according to the finger coordinate
        mPosX = (int) event.getX();
        mPosY = (int) event.getY();

        double abs = Math.sqrt((mPosX - mCenterX) * (mPosX - mCenterX)
                + (mPosY - mCenterY) * (mPosY - mCenterY));

        // move the button only within its boundaries
        if (abs > mBorderRadius) {
            mPosX = (int) ((mPosX - mCenterX) * mBorderRadius / abs + mCenterX);
            mPosY = (int) ((mPosY - mCenterY) * mBorderRadius / abs + mCenterY);
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            resetButtonPosition();

            mThread.interrupt();

            if (mCallback != null)
                mCallback.onMove(getAngle(), getStrength());
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mThread != null && mThread.isAlive()) {
                mThread.interrupt();
            }

            mThread = new Thread(this);
            mThread.start();

            if (mCallback != null)
                mCallback.onMove(getAngle(), getStrength());
        }

        // handle long press with multiple touch only
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN: {
                // when the second finger touch
                if (event.getPointerCount() == 2) {
                    mHandlerMultipleLongPress.postDelayed(mRunnableMultipleLongPress, ViewConfiguration.getLongPressTimeout()*2);
                    mMoveTolerance = MOVE_TOLERANCE;
                }
                break;
            }

            case MotionEvent.ACTION_MOVE:
                mMoveTolerance--;
                if (mMoveTolerance == 0) {
                    mHandlerMultipleLongPress.removeCallbacks(mRunnableMultipleLongPress);
                }
                break;

            case MotionEvent.ACTION_POINTER_UP: {
                // when the last multiple touch is released
                if (event.getPointerCount() == 2) {
                    mHandlerMultipleLongPress.removeCallbacks(mRunnableMultipleLongPress);
                }
                break;
            }
        }

        // to force a new draw
        invalidate();

        return true;
    }


    /**
     * Process the angle following the 360° counter-clock protractor rules.
     * @return the angle of the button
     */
    private int getAngle() {
        int angle = (int) Math.toDegrees(Math.atan2(mCenterY - mPosY, mPosX - mCenterX));
        return angle < 0 ? angle + 360 : angle; // make it as a regular counter-clock protractor
    }


    /**
     * Process the strength as a percentage of the distance between the center and the border.
     * @return the strength of the button
     */
    private int getStrength() {
        return (int) (100 * Math.sqrt((mPosX - mCenterX)
                * (mPosX - mCenterX) + (mPosY - mCenterY)
                * (mPosY - mCenterY)) / mBorderRadius);
    }


    /**
     * Reset the button position to the center.
     */
    public void resetButtonPosition() {
        mPosX = mCenterX;
        mPosY = mCenterY;
    }


    /*
    SETTERS
     */


    /**
     * Set the button color for this JoystickView.
     * @param color the color of the button
     */
    public void setButtonColor(@ColorInt int color){
        mPaintCircleButton.setColor(color);
        invalidate();
    }


    /**
     * Set the border color for this JoystickView.
     * @param color the color of the border
     */
    public void setBorderColor(@ColorInt int color){
        mPaintCircleBorder.setColor(color);
        invalidate();
    }


    /**
     * Set the background color for this JoystickView.
     * @param color the color of the background
     */
    @Override
    public void setBackgroundColor(@ColorInt int color) {
        mPaintBackground.setColor(color);
        invalidate();
    }


    /**
     * Set the border width for this JoystickView.
     * @param width the width of the border
     */
    public void setBorderWidth(int width) {
        mPaintCircleBorder.setStrokeWidth(width);
        invalidate();
    }


    /**
     * Register a callback to be invoked when this JoystickView's button is moved
     * @param l The callback that will run
     */
    public void setOnMoveListener(OnMoveListener l) {
        setOnMoveListener(l, DEFAULT_LOOP_INTERVAL);
    }


    /**
     * Register a callback to be invoked when this JoystickView's button is moved
     * @param l The callback that will run
     * @param loopInterval Refresh rate to be invoked in milliseconds
     */
    public void setOnMoveListener(OnMoveListener l, int loopInterval) {
        mCallback = l;
        mLoopInterval = loopInterval;
    }


    /**
     * Register a callback to be invoked when this JoystickView is touch and held by multiple pointers
     * @param l The callback that will run
     */
    public void setOnMultiLongPressListener(OnMultipleLongPressListener l) {
        mOnMultipleLongPressListener = l;
    }


    /*
    IMPLEMENTS
     */


    @Override // Runnable
    public void run() {
        while (!Thread.interrupted()) {
            post(new Runnable() {
                public void run() {
                    if (mCallback != null)
                        mCallback.onMove(getAngle(), getStrength());
                }
            });

            try {
                Thread.sleep(mLoopInterval);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}