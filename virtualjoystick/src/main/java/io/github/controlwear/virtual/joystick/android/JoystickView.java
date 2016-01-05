package io.github.controlwear.virtual.joystick.android;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

public class JoystickView extends View {


    private int mButtonColor;
    private int mBorderColor;


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
    }


    public JoystickView(Context context) {
        super(context);
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