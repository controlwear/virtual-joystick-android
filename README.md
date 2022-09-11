# virtual-joystick-android

[![](https://jitpack.io/v/Mathias-Boulay/virtual-joystick-android.svg)](https://jitpack.io/#Mathias-Boulay/virtual-joystick-android)

**v1.10.1** _(New version -  button & background size, limited direction, normalized coordinate, alpha border)_

_I created this very simple library as a learning process and I have been inspired by this project [JoystickView](https://github.com/zerokol/JoystickView) (the author is a genius!)_

This library provides a very simple and **ready-to-use** custom view which emulates a joystick for Android.

![Alt text](/misc/virtual-joystick-android.png?raw=true "Double Joystick with custom size and colors")

### Gist
Here is a very simple snippets to use it. Just set the `onMoveListener` to retrieve its angle and strength.

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ...

    JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
    joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
        @Override
        public void onMove(int angle, int strength) {
            // do whatever you want
        }
        
        @Override
        public void onForwardLock(boolean forwardLock){
            // do whatever you want, overriding this function is optionnal
        }
    });
}
```
The **angle** follow the rules of a simple **counter-clock** protractor. The **strength is percentage** of how far the button is **from the center to the border**.

![Alt text](/misc/virtual-joystick.png?raw=true "Explanation")

By default the **refresh rate** to get the data is **20/sec (every 50ms)**. If you want more or less just set the listener with one more parameters to set the refresh rate in milliseconds.
```java
joystick.setOnMoveListener(new JoystickView.OnMoveListener() { ... }, 17); // around 60/sec
```

### Attributes

You can customize the joystick according to these attributes `JV_buttonColor`, `JV_buttonSizeRatio`, `JV_borderColor`, `JV_borderAlpha`, `JV_borderWidth`, `JV_backgroundColor`, `JV_backgroundSizeRatio`, `JV_fixedCenter`, `JV_autoReCenterButton`, `JV_buttonStickToBorder`, `JV_enabled`, `JV_buttonDirection`, `JV_deadzone` and `JV_forwardLockDistance`

Here is an example for your layout resources:
```xml
<io.github.controlwear.virtual.joystick.android.JoystickView
    xmlns:custom="http://schemas.android.com/apk/res-auto"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    custom:JV_buttonColor="#FF6E40"
    custom:JV_buttonSizeRatio="15%"
    custom:JV_borderColor="#00796B"
    custom:JV_backgroundColor="#009688"
    custom:JV_borderWidth="4dp"
    custom:JV_fixedCenter="false"/>
```

#### SizeRatio
We can change the default size of the button and background.
The size is calculated as a percentage of the total width/height.

By default, the button is 25% (0.25) and the background 75% (0.25), as the first screenshot above.

If the total (background + button) is above 1.0, the button will probably be a bit cut when on the border.

```xml
<...
    custom:JV_buttonSizeRatio="50%"
    custom:JV_backgroundSizeRatio="10%"/>

```

```java
joystick.setBackgroundSizeRatio(0.5);
joystick.setButtonSizeRatio(0.1);
```

#### FixedCenter or Not? (and auto re-center)
If you don’t set up this parameter, it will be FixedCenter by default, which is the regular behavior.

However, sometimes, it is convenient to have an auto-defined center which will be defined each time you touch down the screen with your finger (center position will be limited inside the JoystickView’s width/height).
As every parameter you can set it up in xml (as above) or in Java:
```java
joystick.setFixedCenter(false); // set up auto-define center
```

UnfixedCenter (set to false) is particularly convenient when the user can’t (or doesn’t want to) see the screen (e.g. a drone's controller).

We can also remove the automatically re-centered button, just set it to false.
```java
joystick.setAutoReCenterButton(false);
```
_(The behavior is a bit weird if we set remove both the FixedCenter and the AutoReCenter.)_

#### Enabled
By default the joystick is enabled (set to True), but you can disable it either in xml or Java. Then, the button will stop moving and `onMove()` won’t be called anymore.
```java
joystick.setEnabled(false); // disabled the joystick
joystick.isEnabled(); // return enabled state
```

#### ButtonDirection
By default the button can move in both direction X,Y (regular behavior), but we can limit the movement through one axe horizontal or vertical.
```xml
<...
    custom:JV_buttonDirection="horizontal"/>
```
In the layout file (xml), this option can be set to `horizontal`, `vertical` or `both`.

We can also set this option in the Java file by setting an integer value:
- any negative value (e.g. -1) for the horizontal axe
- any positive value (e.g. 1) for the vertical axe
- zero (0) for both (which is the default option)

```java
joystick.setButtonDirection(1); // vertical
```

#### Deadzone
Most joysticks have an inner deadzone, else games would feel hypersensitive in their inputs.
By default, the deadzone is at 10% strength.
To change the deadzone, you can specify the following in xml.
```xml
<...
    custom:JV_deadzone="integer"/>
```

#### ForwardLockDistance
By default, the button doesn't have any forward locking, as it may not be desirable in all situations
To enable forward locking, you need to specify a distance != 0 in xml.

```xml
<...
    custom:JV_forwardLockDistance="dimension"/>
```

_For those who don't know, forward locking is this thing in CODM and PUBG where the stick can stay forward so you can keep moving without wasting fingers_



## Required
Minimum API level is 10 (Android 2.3.6 - Gingerbread) which cover **100%** of the Android platforms as of February 2022 according to the <a href="https://developer.android.com/about/dashboards" class="user-mention">distribution dashboard</a>.

## Download
### Gradle
```java
dependencies {
	        implementation 'com.github.Mathias-Boulay:virtual-joystick-android:1.12.3'
	}
```

## Contributing
If you would like to contribute code, you can do so through GitHub by forking the repository and sending a pull request.
When submitting code, please make every effort to follow existing conventions and style in order to keep the code as readable as possible.

## License
```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Authors

This repository is maintained by <a href="https://github.com/Mathias-Boulay" class="user-mention">@Mathias-Boulay</a>

**virtual-joystick-android** is an open source project created by <a href="https://github.com/makowildcat" class="user-mention">@makowildcat</a> (mostly spare time) and partially funded by [Black Artick](http://blackartick.com/) and [NSERC](http://www.nserc-crsng.gc.ca/index_eng.asp).

Also, thanks to <a href="https://github.com/Bernix01" class="user-mention">Bernix01</a>, <a href="https://github.com/teancake" class="user-mention">teancake</a>, <a href="https://github.com/Spettacolo83" class="user-mention">Spettacolo83</a>, <a href="https://github.com/djjaysmith" class="user-mention">djjaysmith</a>, <a href="https://github.com/jaybkim1" class="user-mention">jaybkim1</a>, <a href="https://github.com/sikrinick" class="user-mention">sikrinick</a>, <a href="https://github.com/AlexandrDavydov" class="user-mention">AlexandrDavydov</a>, <a href="https://github.com/indrek-koue" class="user-mention">indrek-koue</a>, <a href="https://github.com/QitmentX7" class="user-mention">QitmentX7</a>, <a href="https://github.com/esplemea" class="user-mention">esplemea</a>, <a href="https://github.com/FenixGit" class="user-mention">FenixGit</a>, <a href="https://github.com/AlexanderShniperson" class="user-mention">AlexanderShniperson</a>
and <a href="https://github.com/GijsGoudzwaard" class="user-mention">GijsGoudzwaard</a> for contributing.
