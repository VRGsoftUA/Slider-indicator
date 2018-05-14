#### [HIRE US](http://vrgsoft.net/)

#SliderIndicator
Custom view for sms code input with customization</br></br>
<img src="https://github.com/VRGsoftUA/Slider-indicator/blob/master/video.gif" width="270" height="480" />


# Usage

*For a working implementation, Have a look at the Sample Project - app*

1. Include the library as local library project.
```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    compile 'com.github.VRGsoftUA:Slider-indicator:1.0'
}
```
2. Include Slider class in your xml layout. For Example:
```
<net.vrgsoft.library.Slider
        android:id="@+id/slider"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        app:animationDuration="1000"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:onPointClick="onPointClick"
        app:pointInnerColor="@color/colorAccent"
        app:pointOuterColor="@android:color/holo_purple"
        app:pointPulseColor="#66ff4444"
        app:lineInnerColor="@color/colorPrimaryDark"
        app:lineStrokeWidth="2dp"
        app:lineOuterColor="@color/colorAccent"
        app:pointsCount="8" />
```

# Customization
| Attribute | Description |
| ------------- | ------------- |
| app:lineOuterColor | Direction line outer color |
| app:lineInnerColor | Direction line inner color |
| app:pointPulseColor | Point pulse color |
| app:pointInnerColor | Point inner circle color |
| app:pointOuterColor | Point outer circle color |
| app:pointsCount | The number of points to be drawn (from 2 to 8) |
| app:animationDuration | Duration for all animations |
| app:pointSize | The size of one point |
| app:lineStrokeWidth | Direction line width |
| app:onPointClick | Convinience attribute for receiving callbacks to activity or data binding |
| android:orientation | Sets the orientation of the view |

| Method  | Description |
| ------------- | ------------- |
| setPointSize(int pointSize) | Sets the size of one point |
| setLineStrokeWidth(int lineStrokeWidth) | Sets the line stroke width of one point |
| setOrientation(int orientation) | Sets the orientation of the view |
| setDuration(long duration) | Sets the aniamtion duration |
| setOuterLineColor(int outerLineColor) | Sets the outer line color |
| setInnerLineColor(int innerLineColor) | Sets the inner line color |
| setPointPulseColor(int pulseColor) | Sets the point pulse circle color |
| setPointOuterColor(int pulseColor) | Sets the point outer circle color |
| setPointInnerColor(int innerColor) | Sets the point inner circle color |

#### Contributing
* Contributions are always welcome
* If you want a feature and can code, feel free to fork and add the change yourself and make a pull request
