package fr.spse.joystickdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JoystickView joystick = findViewById(R.id.joystick);

        Switch fixedCenterSwitch = findViewById(R.id.switch_fixed_center);
        fixedCenterSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> joystick.setFixedCenter(isChecked));
        joystick.setFixedCenter(fixedCenterSwitch.isChecked());

        Switch stickyBorderSwitch = findViewById(R.id.switch_sticky_border);
        stickyBorderSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> joystick.setButtonStickToBorder(isChecked));
        joystick.setButtonStickToBorder(stickyBorderSwitch.isChecked());

        Switch autoCenterSwitch = findViewById(R.id.switch_auto_center);
        autoCenterSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> joystick.setAutoReCenterButton(isChecked));
        joystick.setAutoReCenterButton(autoCenterSwitch.isChecked());


    }
}