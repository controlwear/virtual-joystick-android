package io.github.controlwear.virtual.joystick.android;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class JoystickView extends View {


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

        /**
         * Called when a JoystickView's button has been locked forward
         * @param isLocked Whether the lock is enabled or not
         */
        default void onForwardLock(boolean isLocked){}
    }


    public enum AxisToCenter {
        BOTH, X, Y
    }


    /*
    CONSTANTS
    */

    /** Default value for both directions (horizontal and vertical movement) */
    public static int BUTTON_DIRECTION_BOTH = 0;

    /** Default refresh rate as a time in milliseconds to send move values through callback */
    private static final int DEFAULT_LOOP_INTERVAL = 50; // in milliseconds

    /** Used to allow a slight move without cancelling MultipleLongPress */
    private static final int DEFAULT_DEADZONE = 10;

    /** Default color for button */
    private static final int DEFAULT_COLOR_BUTTON = Color.BLACK;

    /** Default color for border */
    private static final int DEFAULT_COLOR_BORDER = Color.TRANSPARENT;

    /** Default alpha for border */
    private static final int DEFAULT_ALPHA_BORDER = 255;

    /** Default background color */
    private static final int DEFAULT_BACKGROUND_COLOR = Color.TRANSPARENT;

    /** Default View's size */
    private static final int DEFAULT_SIZE = 200;

    /** Default border's width */
    private static final int DEFAULT_WIDTH_BORDER = 3;

    /** Default behavior to fixed center (not auto-defined) */
    private static final boolean DEFAULT_FIXED_CENTER = true;


    /** Default behavior to auto re-center button (automatically recenter the button) */
    private static final boolean DEFAULT_AUTO_RECENTER_BUTTON = true;


    /** Default behavior to button stickToBorder (button stay on the border) */
    private static final boolean DEFAULT_BUTTON_STICK_TO_BORDER = false;

    /** Handler and associated runnable, dealing with sending updates from the main Thread */
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mHandlerRunnable = new Runnable() {
        @Override
        public void run() {
            notifyOnMove(getAngle(), getStrength());

            mHandler.postDelayed(this, mLoopInterval);
        }
    };


    // DRAWING
    private final Paint mPaintCircleButton;
    private final Paint mPaintCircleBorder;
    private final Paint mPaintBackground;

    /** Ratio use to define the size of the button */
    private float mButtonSizeRatio;

    /** Ratio use to define the size of the background */
    private float mBackgroundSizeRatio;

    // COORDINATE
    private int mPosX = 0;
    private int mPosY = 0;
    private int mCenterX = 0;
    private int mCenterY = 0;
    private int mForwardLockCenterX = 0;
    private int mForwardLockCenterY = 0;

    private int mFixedCenterX = 0;
    private int mFixedCenterY = 0;

    /** Whether or not the joystick is locked forward */
    private boolean mForwardLock = false;

    /** The distance before locking forward, 0 means forward locking is disabled */
    private int mForwardLockDistance;

    /** Used to adapt behavior whether it is auto-defined center (false) or fixed center (true) */
    private boolean mFixedCenter;

    /**
     * Used to adapt behavior whether the button is automatically re-centered (true)
     * when released or not (false)
     */
    private boolean mAutoReCenterButton;

    /**
     * Used to adapt behavior whether the button is stick to border (true) or
     * could be anywhere (when false - similar to regular behavior)
     */
    private boolean mButtonStickToBorder;


    /**
     * Used to enabled/disabled the Joystick. When disabled (enabled to false) the joystick button
     * can't move and onMove is not called.
     */
    private boolean mEnabled;

    // SIZE
    private int mButtonRadius;
    private int mBorderRadius;

    /** Alpha of the border (to use when changing color dynamically) */
    private int mBorderAlpha;

    /** Based on mBorderRadius but a bit smaller (minus half the stroke size of the border) */
    private float mBackgroundRadius;

    /** Listener used to dispatch OnMove event */
    private OnMoveListener mCallback;

    private long mLoopInterval = DEFAULT_LOOP_INTERVAL;

    /** PointerID used to track the original pointer triggering the joystick */
    private int pointerID = -1;

    /** The deadzone for the joystick from 0 to 100%*/
    private int mDeadzone;


    /**
     * The allowed direction of the button is define by the value of this parameter:
     * - a negative value for horizontal axe
     * - a positive value for vertical axe
     * - zero for both axes
     */
    private int mButtonDirection;

    /** axis to be centered */
    private AxisToCenter axisToCenter = AxisToCenter.BOTH;


    /*
    CONSTRUCTORS
     */

    /**
     * Simple constructor to use when creating a JoystickView from code.
     * Call another constructor passing null to Attribute.
     * @param context The Context the JoystickView is running in, through which it can
     *        access the current theme, resources, etc.
     */
    public JoystickView(Context context) {
        this(context, null);
    }


    public JoystickView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
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
            buttonColor = styledAttributes.getColor(R.styleable.JoystickView_JV_buttonColor, DEFAULT_COLOR_BUTTON);
            borderColor = styledAttributes.getColor(R.styleable.JoystickView_JV_borderColor, DEFAULT_COLOR_BORDER);
            mBorderAlpha = styledAttributes.getInt(R.styleable.JoystickView_JV_borderAlpha, DEFAULT_ALPHA_BORDER);
            backgroundColor = styledAttributes.getColor(R.styleable.JoystickView_JV_backgroundColor, DEFAULT_BACKGROUND_COLOR);
            borderWidth = styledAttributes.getDimensionPixelSize(R.styleable.JoystickView_JV_borderWidth, DEFAULT_WIDTH_BORDER);
            mFixedCenter = styledAttributes.getBoolean(R.styleable.JoystickView_JV_fixedCenter, DEFAULT_FIXED_CENTER);
            mAutoReCenterButton = styledAttributes.getBoolean(R.styleable.JoystickView_JV_autoReCenterButton, DEFAULT_AUTO_RECENTER_BUTTON);
            mButtonStickToBorder = styledAttributes.getBoolean(R.styleable.JoystickView_JV_buttonStickToBorder, DEFAULT_BUTTON_STICK_TO_BORDER);
            mDeadzone = styledAttributes.getInteger(R.styleable.JoystickView_JV_deadzone, DEFAULT_DEADZONE);
            mEnabled = styledAttributes.getBoolean(R.styleable.JoystickView_JV_enabled, true);
            mButtonSizeRatio = styledAttributes.getFraction(R.styleable.JoystickView_JV_buttonSizeRatio, 1, 1, 0.25f);
            mBackgroundSizeRatio = styledAttributes.getFraction(R.styleable.JoystickView_JV_backgroundSizeRatio, 1, 1, 0.75f);
            mButtonDirection = styledAttributes.getInteger(R.styleable.JoystickView_JV_buttonDirection, BUTTON_DIRECTION_BOTH);
            mForwardLockDistance = styledAttributes.getDimensionPixelSize(R.styleable.JoystickView_JV_forwardLockDistance, 0);
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

        if (borderColor != Color.TRANSPARENT) {
            mPaintCircleBorder.setAlpha(mBorderAlpha);
        }

        mPaintBackground = new Paint();
        mPaintBackground.setAntiAlias(true);
        mPaintBackground.setColor(backgroundColor);
        mPaintBackground.setStyle(Paint.Style.FILL);

    }


    private void initPosition() {
        // get the center of view to position circle
        mFixedCenterX = mForwardLockCenterX = mCenterX = mPosX = getWidth() / 2;
        mFixedCenterY = mCenterY = mPosY = getHeight() - (getWidth() / 2);
    }


    /**
     * Draw the background, the border and the button
     * @param canvas the canvas on which the shapes will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the background
        canvas.drawCircle(mFixedCenterX, mFixedCenterY, mBackgroundRadius, mPaintBackground);

        // Draw the circle border
        canvas.drawCircle(mFixedCenterX, mFixedCenterY, mBorderRadius, mPaintCircleBorder);

        // When the joystick is triggered, we need to display another circle for the lock on forward
        if(pointerID != -1 && mForwardLockDistance != 0){
            canvas.drawCircle(mFixedCenterX, mForwardLockCenterY, mButtonRadius, mPaintBackground);
            canvas.drawCircle(mFixedCenterX, mForwardLockCenterY, mButtonRadius, mPaintCircleBorder);
        }

        if(mForwardLock){
            canvas.drawCircle(
                    mFixedCenterX,
                    mFixedCenterY - mBackgroundRadius,
                    mButtonRadius,
                    mPaintCircleButton
            );
        }else {
            canvas.drawCircle(
                    mPosX + mFixedCenterX - mCenterX,
                    mPosY + mFixedCenterY - mCenterY,
                    mButtonRadius,
                    mPaintCircleButton
            );
        }
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

        initPosition();

        // radius based on smallest size : height OR width
        int d = Math.min(w, h);
        mButtonRadius = (int) (d / 2 * mButtonSizeRatio);
        mBorderRadius = (int) (d / 2 * mBackgroundSizeRatio);
        mBackgroundRadius = mBorderRadius - (mPaintCircleBorder.getStrokeWidth() / 2);

        // Compute how far the forward distance can go
        mForwardLockDistance = Math.min(mForwardLockDistance, mFixedCenterY - (int) mPaintCircleBorder.getStrokeWidth()/2);
        mForwardLockCenterY = mFixedCenterY - mForwardLockDistance;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // setting the measured values to resize the view to a certain width and height
        //int d = Math.min(measure(widthMeasureSpec), measure(heightMeasureSpec));
        if(heightMeasureSpec < widthMeasureSpec) widthMeasureSpec = heightMeasureSpec;
        setMeasuredDimension(measure(widthMeasureSpec), measure(heightMeasureSpec));
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
        // if disabled we don't move the joystick
        if (!mEnabled) return false;

        // to move the button according to the finger coordinate
        // (or limited to one axe according to direction option
        int pointerIndex;
        if( (pointerIndex = event.findPointerIndex(pointerID)) != -1 ){
            mPosY = mButtonDirection < 0 ? mCenterY : (int) event.getY(pointerIndex); // direction negative is horizontal axe
            mPosX = mButtonDirection > 0 ? mCenterX : (int) event.getX(pointerIndex); // direction positive is vertical axe
        }

        if(event.getAction() == MotionEvent.ACTION_POINTER_UP){
            if(event.findPointerIndex(pointerID) == event.getActionIndex())
                event.setAction(MotionEvent.ACTION_UP);
        }

        if(event.getAction() == MotionEvent.ACTION_POINTER_DOWN){
            if(pointerID == -1) event.setAction(MotionEvent.ACTION_DOWN);
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            // Reset the pointerID;
            pointerID = -1;

            // stop listener because the finger left the touch screen
            stop();

            // re-center the button or not (depending on settings)
            if (mAutoReCenterButton) {
                resetButtonPosition();

                // update now the last strength and angle which should be zero after resetButton
                notifyOnMove(getAngle(), getStrength());
            }

            // if mAutoReCenterButton is false we will send the last strength and angle a bit
            // later only after processing new position X and Y otherwise it could be above the border limit
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // Check if the pointer is inside the original joystick zone

            if(Math.hypot(Math.abs(mFixedCenterX - event.getX()), Math.abs(mFixedCenterY - event.getY())) > mBackgroundRadius + mPaintCircleBorder.getStrokeWidth())
                return false; // outside of the round joystick

            // Map the pointerID
            pointerID = event.getPointerId(0);
            mPosY = mButtonDirection < 0 ? mCenterY : (int) event.getY(); // direction negative is horizontal axe
            mPosX = mButtonDirection > 0 ? mCenterX : (int) event.getX(); // direction positive is vertical axe

            // Start sending events at a delayed fixed rate
            start();

            notifyOnMove(getAngle(), getStrength());
        }


        // handle first touch and long press with multiple touch only
        // when the first touch occurs we update the center (if set to auto-defined center)
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (!mFixedCenter) {
                mCenterX = mPosX;
                mCenterY = mPosY;
            }
        }

        double abs = Math.sqrt((mPosX - mCenterX) * (mPosX - mCenterX)
                + (mPosY - mCenterY) * (mPosY - mCenterY));

        // (abs > mBorderRadius) means button is too far therefore we limit to border
        // (buttonStickBorder && abs != 0) means wherever is the button we stick it to the border except when abs == 0
        if (abs > mBorderRadius || (mButtonStickToBorder && abs != 0)) {
            mPosX = (int) ((mPosX - mCenterX) * mBorderRadius / abs + mCenterX);
            mPosY = (int) ((mPosY - mCenterY) * mBorderRadius / abs + mCenterY);
        }

        // Compute the forward lock state, if ever needed
        if(mForwardLockDistance != 0){
            boolean forwardLock;
            forwardLock = (Math.hypot(Math.abs(mForwardLockCenterX - event.getX()), Math.abs(mForwardLockCenterY - event.getY())) < mButtonRadius + mPaintCircleBorder.getStrokeWidth());

            if(forwardLock != mForwardLock){
                mForwardLock = forwardLock;
                if(mCallback != null) mCallback.onForwardLock(mForwardLock);
            }
        }

        if (!mAutoReCenterButton) {
            // Now update the last strength and angle if not reset to center
            notifyOnMove(getAngle(), getStrength());
        }


        // to force a new draw
        invalidate();

        return true;
    }

    /** Check if a callback exists, and apply deadzone */
    private void notifyOnMove(int angle, int strength){
        if(mCallback == null) return;
        if(strength < mDeadzone) strength = 0;
        mCallback.onMove(angle, strength);

    }


    /*
    GETTERS
     */

    /**
     * Process the angle following the 360° counter-clock protractor rules.
     * @return the angle of the button
     */
    private int getAngle() {
        if(mForwardLock) return 90;

        int angle = (int) Math.toDegrees(Math.atan2(mCenterY - mPosY, mPosX - mCenterX));
        return angle < 0 ? angle + 360 : angle; // make it as a regular counter-clock protractor
    }

    /**
     * Process the strength as a percentage of the distance between the center and the border.
     * @return the strength of the button
     */
    private int getStrength() {
        if(mForwardLock) return 100;

        return (int) (100 * Math.sqrt((mPosX - mCenterX)
                * (mPosX - mCenterX) + (mPosY - mCenterY)
                * (mPosY - mCenterY)) / mBorderRadius);
    }


    /**
     * Reset the button position to the center.
     */
    public void resetButtonPosition() {
        switch (axisToCenter) {
            case BOTH:
                mPosX = mCenterX;
                mPosY = mCenterY;
                break;
            case X:
                mPosX = mCenterX;
                break;
            case Y:
                mPosY = mCenterY;
                break;
        }
    }


    /**
     * Return the current direction allowed for the button to move
     * @return Actually return an integer corresponding to the direction:
     * - A negative value is horizontal axe,
     * - A positive value is vertical axe,
     * - Zero means both axes
     */
    public int getButtonDirection() {
        return mButtonDirection;
    }


    /**
     * Return the state of the joystick. False when the button don't move.
     * @return the state of the joystick
     */
    public boolean isEnabled() {
        return mEnabled;
    }


    /**
     * Return the size of the button (as a ratio of the total width/height)
     * Default is 0.25 (25%).
     * @return button size (value between 0.0 and 1.0)
     */
    public float getButtonSizeRatio() {
        return mButtonSizeRatio;
    }


    /**
     * Return the size of the background (as a ratio of the total width/height)
     * Default is 0.75 (75%).
     * @return background size (value between 0.0 and 1.0)
     */
    public float getBackgroundSizeRatio() {
        return mBackgroundSizeRatio;
    }


    /**
     * Return the current behavior of the auto re-center button
     * @return True if automatically re-centered or False if not
     */
    public boolean isAutoReCenterButton() {
        return mAutoReCenterButton;
    }


    /**
     * Return the current behavior of the button stick to border
     * @return True if the button stick to the border otherwise False
     */
    public boolean isButtonStickToBorder() {
        return mButtonStickToBorder;
    }


    /**
     * Return the relative X coordinate of button center related
     * to top-left virtual corner of the border
     * @return coordinate of X (normalized between 0 and 100)
     */
    public int getNormalizedX() {
        if (getWidth() == 0) {
            return 50;
        }
        return Math.round((mPosX-mButtonRadius)*100.0f/(getWidth()-mButtonRadius*2));
    }


    /**
     * Return the relative Y coordinate of the button center related
     * to top-left virtual corner of the border
     * @return coordinate of Y (normalized between 0 and 100)
     */
    public int getNormalizedY() {
        if (getHeight() == 0) {
            return 50;
        }
        return Math.round((mPosY-mButtonRadius)*100.0f/(getHeight()-mButtonRadius*2));
    }


    /**
     * Return the alpha of the border
     * @return it should be an integer between 0 and 255 previously set
     */
    public int getBorderAlpha() {
        return mBorderAlpha;
    }

    /*
    SETTERS
     */

    /**
     * Set the button color for this JoystickView.
     * @param color the color of the button
     */
    public void setButtonColor(int color) {
        mPaintCircleButton.setColor(color);
        invalidate();
    }


    /**
     * Set the border color for this JoystickView.
     * @param color the color of the border
     */
    public void setBorderColor(int color) {
        mPaintCircleBorder.setColor(color);
        if (color != Color.TRANSPARENT) {
            mPaintCircleBorder.setAlpha(mBorderAlpha);
        }
        invalidate();
    }


    /**
     * Set the border alpha for this JoystickView.
     * @param alpha the transparency of the border between 0 and 255
     */
    public void setBorderAlpha(int alpha) {
        mBorderAlpha = alpha;
        mPaintCircleBorder.setAlpha(alpha);
        invalidate();
    }


    /**
     * Set the background color for this JoystickView.
     * @param color the color of the background
     */
    @Override
    public void setBackgroundColor(int color) {
        mPaintBackground.setColor(color);
        invalidate();
    }


    /**
     * Set the border width for this JoystickView.
     * @param width the width of the border
     */
    public void setBorderWidth(int width) {
        mPaintCircleBorder.setStrokeWidth(width);
        mBackgroundRadius = mBorderRadius - (width / 2.0f);
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
     * Set the joystick center's behavior (fixed or auto-defined)
     * @param fixedCenter True for fixed center, False for auto-defined center based on touch down
     */
    public void setFixedCenter(boolean fixedCenter) {
        // if we set to "fixed" we make sure to re-init position related to the width of the joystick
        if (fixedCenter) {
            initPosition();
        }
        mFixedCenter = fixedCenter;
        invalidate();
    }


    /**
     * Enable or disable the joystick
     * @param enabled False mean the button won't move and onMove won't be called
     */
    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }


    /**
     * Set the joystick button size (as a fraction of the real width/height)
     * By default it is 25% (0.25).
     * @param newRatio between 0.0 and 1.0
     */
    public void setButtonSizeRatio(float newRatio) {
        if (newRatio > 0.0f & newRatio <= 1.0f) {
            mButtonSizeRatio = newRatio;
        }
    }


    /**
     * Set the joystick button size (as a fraction of the real width/height)
     * By default it is 75% (0.75).
     * Not working if the background is an image.
     * @param newRatio between 0.0 and 1.0
     */
    public void setBackgroundSizeRatio(float newRatio) {
        if (newRatio > 0.0f & newRatio <= 1.0f) {
            mBackgroundSizeRatio = newRatio;
        }
    }


    /**
     * Set the current behavior of the auto re-center button
     * @param b True if automatically re-centered or False if not
     */
    public void setAutoReCenterButton(boolean b) {
        mAutoReCenterButton = b;
    }


    /**
     * Set the current behavior of the button stick to border
     * @param b True if the button stick to the border or False (default) if not
     */
    public void setButtonStickToBorder(boolean b) {
        mButtonStickToBorder = b;
    }


    /**
     * Set the current authorized direction for the button to move
     * @param direction the value will define the authorized direction:
     *                  - any negative value (such as -1) for horizontal axe
     *                  - any positive value (such as 1) for vertical axe
     *                  - zero (0) for the full direction (both axes)
     */
    public void setButtonDirection(int direction) {
        mButtonDirection = direction;
    }

    /**
     * get axis to be centered
     * @return
     */
    public AxisToCenter getAxisToCenter() {
        return axisToCenter;
    }

    /**
     * set axis to be centered
     * @param axisToCenter
     */
    public void setAxisToCenter(AxisToCenter axisToCenter) {
        this.axisToCenter = axisToCenter;
    }

    /*
    Thread simulation
     */

    /** Start sending joystick events */
    private void start(){
        mHandler.postDelayed(mHandlerRunnable, mLoopInterval);
    }

    /** Stop sending joystick events */
    private void stop(){
        mHandler.removeCallbacks(mHandlerRunnable);
    }

}