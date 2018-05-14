package net.vrgsoft.library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;

import static android.support.constraint.ConstraintLayout.LayoutParams.HORIZONTAL;
import static android.support.constraint.ConstraintLayout.LayoutParams.PARENT_ID;

public class Slider extends ConstraintLayout {
    static final long DEFAULT_ANIMATION_DURATION = 500;

    private static final int START_INDEX = 1000;
    private static final int DEFAULT_POINT_COUNT = 3;
    private static final int MIN_POINT_COUNT = 2;
    private static final int MAX_POINT_COUNT = 8;

    private int mPointsCount;
    private int mCurrentPosition;
    private int mPreviousPosition;
    private int mPointSize;
    private int mLineStrokeWidth;
    private int mOrientation;
    private long mDuration;

    private OnPointClickListener mPointClickListener;
    private ClickHandler mClickHandler;
    private LinkedHashMap<Integer, SliderPoint> mPoints;
    private SliderBgLine mBgLine;

    public Slider(Context context) {
        this(context, null);
    }

    public Slider(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Slider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        initDefaultValues(context);
        initAttrs(context, attrs);
        initBgLine(context, attrs);
        initPoints(context, attrs);
        initClickListeners();
        setCurrentPosition(mCurrentPosition);
    }

    private void initDefaultValues(Context context) {
        mPointsCount = DEFAULT_POINT_COUNT;
        mDuration = DEFAULT_ANIMATION_DURATION;
        mOrientation = LinearLayout.HORIZONTAL;
        mPoints = new LinkedHashMap<>();
        mClickHandler = new ClickHandler();
        mPointSize = context.getResources().getDimensionPixelSize(R.dimen.defaultPointSize);
        mLineStrokeWidth = context.getResources().getDimensionPixelSize(R.dimen.defaultLineHeight);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Slider);

            mPointsCount = a.getInteger(R.styleable.Slider_pointsCount, mPointsCount);
            if (mPointsCount > MAX_POINT_COUNT || mPointsCount < MIN_POINT_COUNT) {
                throw new IllegalArgumentException("Sms length should be in range from 1 to 8");
            }

            mPointSize = a.getDimensionPixelSize(R.styleable.Slider_pointSize, mPointSize);
            mLineStrokeWidth = a.getDimensionPixelSize(R.styleable.Slider_lineStrokeWidth, mLineStrokeWidth);
            mOrientation = a.getInt(R.styleable.Slider_android_orientation, mOrientation);
            mDuration = a.getInt(R.styleable.Slider_animationDuration, (int) mDuration);

            final String handlerName = a.getString(R.styleable.Slider_onPointClick);
            if (handlerName != null) {
                setPointClickListener(new DeclaredPointClickListener(this, handlerName));
            }

            a.recycle();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    private void initBgLine(Context context, AttributeSet attrs) {
        mBgLine = new SliderBgLine(context, attrs);
        mBgLine.setPointsCount(mPointsCount);
        mBgLine.setId(START_INDEX - 1);
        initLineLayoutParams();
        addView(mBgLine);
    }

    private void initPointsLayoutParams() {
        float bias = 1.0f / (mPointsCount - 1);
        float currentBias = 0;

        if (mOrientation == HORIZONTAL) {
            for (int i = START_INDEX; i < START_INDEX + mPointsCount; ++i) {
                SliderPoint point = mPoints.get(i);

                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(mPointSize, mPointSize);
                params.topToTop = PARENT_ID;
                params.bottomToBottom = PARENT_ID;
                params.startToStart = PARENT_ID;
                params.endToEnd = PARENT_ID;
                params.horizontalBias = currentBias;

                point.setLayoutParams(params);
                currentBias += bias;
            }
        } else {
            for (int i = START_INDEX; i < START_INDEX + mPointsCount; ++i) {
                SliderPoint point = mPoints.get(i);

                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(mPointSize, mPointSize);
                params.startToStart = PARENT_ID;
                params.endToEnd = PARENT_ID;
                params.startToStart = PARENT_ID;
                params.endToEnd = PARENT_ID;
                params.verticalBias = currentBias;

                point.setLayoutParams(params);
                currentBias += bias;
            }
        }
    }

    private void initLineLayoutParams() {
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mLineStrokeWidth);
        params.topToTop = PARENT_ID;
        params.bottomToBottom = PARENT_ID;
        params.endToEnd = PARENT_ID;
        params.startToStart = PARENT_ID;
        if (mOrientation == HORIZONTAL) {
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = mLineStrokeWidth;
            params.leftMargin = mPointSize / 2;
            params.rightMargin = mPointSize / 2;
        } else {
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            params.width = mLineStrokeWidth;
            params.topMargin = mPointSize / 2;
            params.bottomMargin = mPointSize / 2;
        }
        mBgLine.setLayoutParams(params);
    }

    private void initPoints(Context context, AttributeSet attrs) {
        for (int i = START_INDEX; i < START_INDEX + mPointsCount; i++) {
            SliderPoint point = new SliderPoint(context, attrs);
            point.setId(i);
            mPoints.put(i, point);
            addView(point);
        }
        initPointsLayoutParams();
    }

    private void initClickListeners() {
        for (SliderPoint point : mPoints.values()) {
            point.setOnClickListener(mClickHandler);
        }
    }

    public void setCurrentPosition(int position) {
        if (mCurrentPosition == position) return;
        mPreviousPosition = mCurrentPosition;
        mCurrentPosition = position;
        post(new Runnable() {
            @Override
            public void run() {
                Slider.this.startAnimation();
            }
        });
        if (mPointClickListener != null) {
            mPointClickListener.onPointClick(mCurrentPosition);
        }
    }

    private void startAnimation() {
        cancelAllAnimations();
        initDefaultState();

        handleTransitAnimation();

        mPoints.get(mCurrentPosition + 1000).startSelectAnimation();
        mBgLine.startAnimation(mCurrentPosition);
    }

    private void handleTransitAnimation() {
        int transitPointsCount = Math.abs(mCurrentPosition - mPreviousPosition) - 1;
        if (transitPointsCount > 0) {
            long onePointTime = mDuration / (transitPointsCount + 2);
            long startDelay = 0;
            if (mCurrentPosition - mPreviousPosition > 1) {
                for (int i = 1; i < transitPointsCount + 1; ++i) {
                    startDelay += onePointTime;
                    mPoints.get(START_INDEX + mPreviousPosition + i).startTransitAnimation(startDelay);
                }
            } else {
                for (int i = 1; i < transitPointsCount + 1; ++i) {
                    startDelay += onePointTime;
                    mPoints.get(START_INDEX + mPreviousPosition - i).startTransitAnimation(startDelay);
                }
            }
        }
    }

    private void initDefaultState() {
        for (SliderPoint point : mPoints.values()) {
            point.initDefaultState();
        }
    }

    private void cancelAllAnimations() {
        for (SliderPoint point : mPoints.values()) {
            point.cancelAllAnimations();
        }
    }

    public void setPointClickListener(OnPointClickListener pointClickListener) {
        mPointClickListener = pointClickListener;
    }

    public interface OnPointClickListener {
        void onPointClick(int position);
    }

    @SuppressLint("ResourceType")
    private class ClickHandler implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() - START_INDEX == mCurrentPosition) return;
            setCurrentPosition(v.getId() - START_INDEX);
            if (mPointClickListener != null) {
                mPointClickListener.onPointClick(mCurrentPosition);
            }
        }
    }

    private static class DeclaredPointClickListener implements OnPointClickListener {
        private final View mHostView;
        private final String mMethodName;

        private Method mResolvedMethod;
        private Context mResolvedContext;

        DeclaredPointClickListener(@NonNull View hostView, @NonNull String methodName) {
            mHostView = hostView;
            mMethodName = methodName;
        }

        @Override
        public void onPointClick(int position) {
            if (mResolvedMethod == null) {
                resolveMethod(mHostView.getContext());
            }

            try {
                mResolvedMethod.invoke(mResolvedContext, position);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(
                        "Could not execute non-public method for onSubmit", e);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException(
                        "Could not execute method for onSubmit", e);
            }
        }

        private void resolveMethod(@Nullable Context context) {
            while (context != null) {
                try {
                    if (!context.isRestricted()) {
                        final Method method = context.getClass().getMethod(mMethodName, int.class);
                        if (method != null) {
                            mResolvedMethod = method;
                            mResolvedContext = context;
                            return;
                        }
                    }
                } catch (NoSuchMethodException e) {
                    // Failed to find method, keep searching up the hierarchy.
                }

                if (context instanceof ContextWrapper) {
                    context = ((ContextWrapper) context).getBaseContext();
                } else {
                    // Can't search up the hierarchy, null out and fail.
                    context = null;
                }
            }

            final int id = mHostView.getId();
            final String idText = id == NO_ID ? "" : " with id '"
                    + mHostView.getContext().getResources().getResourceEntryName(id) + "'";
            throw new IllegalStateException("Could not find method " + mMethodName
                    + "(View) in a parent or ancestor Context for onSubmit "
                    + "attribute defined on view " + mHostView.getClass() + idText);
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.setPosition(mCurrentPosition);
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        setCurrentPosition(ss.getPosition());
        super.onRestoreInstanceState(ss.getSuperState());
    }

    /**
     * @param pointSize point width and height
     */
    public void setPointSize(int pointSize) {
        mPointSize = pointSize;
        initPointsLayoutParams();
    }

    /**
     * @param lineStrokeWidth guide line stroke width
     */
    public void setLineStrokeWidth(int lineStrokeWidth) {
        mLineStrokeWidth = lineStrokeWidth;
        initLineLayoutParams();
    }

    /**
     * @param orientation LinearLayout.HORIZONTAL or LinearLayout.VERTICAL
     */
    public void setOrientation(@IntRange(from = LinearLayout.HORIZONTAL, to = LinearLayout.VERTICAL) int orientation) {
        mOrientation = orientation;
        mBgLine.setOrientation(orientation);
        initLineLayoutParams();
        initPointsLayoutParams();
    }

    /**
     * @param duration animation duration in ms
     */
    public void setDuration(long duration) {
        mDuration = duration;
        mBgLine.setDuration(duration);
        for(SliderPoint point : mPoints.values()){
            point.setDuration(duration);
        }
    }

    /**
     * @param outerLineColor color to be set
     */
    public void setOuterLineColor(@ColorInt int outerLineColor) {
        mBgLine.setOuterLineColor(outerLineColor);
    }

    /**
     * @param innerLineColor color to be set
     */
    public void setInnerLineColor(@ColorInt int innerLineColor) {
        mBgLine.setInnerLineColor(innerLineColor);
    }

    /**
     * @param pulseColor color to be set
     */
    public void setPointPulseColor(int pulseColor) {
        for(SliderPoint point : mPoints.values()){
            point.setPulseColor(pulseColor);
        }
    }

    /**
     * @param outerColor color to be set
     */
    public void setPointOuterColor(int outerColor) {
        for(SliderPoint point : mPoints.values()){
            point.setOuterColor(outerColor);
        }
    }

    /**
     * @param innerColor color to be set
     */
    public void setPointInnerColor(int innerColor) {
        for(SliderPoint point : mPoints.values()){
            point.setInnerColor(innerColor);
        }
    }

    private static final class SavedState extends BaseSavedState {
        private int mPosition;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            mPosition = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mPosition);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        public int getPosition() {
            return mPosition;
        }

        public void setPosition(int position) {
            mPosition = position;
        }

    }
}
