package net.vrgsoft.library;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

import static net.vrgsoft.library.Slider.DEFAULT_ANIMATION_DURATION;


class SliderBgLine extends View {
    private Paint paint;
    private int outerLineColor;
    private int innerLineColor;
    private long duration;
    private float directionLineWidth;
    private float innerLineWidth;
    private float outerLineWidth;
    private float[] positions;
    private int pointsCount;
    private float currentPosition;
    private ValueAnimator animator;
    private int mOrientation;

    public SliderBgLine(Context context) {
        this(context, null);
    }

    public SliderBgLine(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SliderBgLine(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        initDefaultValues(context);
        initAttrs(context, attrs);
    }

    private void initDefaultValues(Context context) {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        duration = DEFAULT_ANIMATION_DURATION;
        mOrientation = LinearLayout.HORIZONTAL;

        outerLineColor = context.getResources().getColor(R.color.defaultOuterLineColor);
        innerLineColor = context.getResources().getColor(R.color.defaultInnerLineColor);
    }

    @SuppressLint("CustomViewStyleable")
    private void initAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Slider);

            outerLineColor = a.getColor(R.styleable.Slider_lineOuterColor, outerLineColor);
            innerLineColor = a.getColor(R.styleable.Slider_lineInnerColor, outerLineColor);
            mOrientation = a.getInt(R.styleable.Slider_android_orientation, mOrientation);
            duration = a.getInt(R.styleable.Slider_animationDuration, (int) duration);

            a.recycle();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int pointGap;
        if (mOrientation == LinearLayout.HORIZONTAL) {
            directionLineWidth = h * 0.187f;
            innerLineWidth = h * 0.35f;
            outerLineWidth = h;
            pointGap = w / (pointsCount - 1);
        } else {
            directionLineWidth = w * 0.187f;
            innerLineWidth = w * 0.35f;
            outerLineWidth = w;
            pointGap = h / (pointsCount - 1);
        }

        positions = new float[pointsCount];
        for (int i = 0; i < pointsCount; ++i) {
            positions[i] = pointGap * i;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(innerLineColor);
        paint.setStrokeWidth(directionLineWidth);
        if (mOrientation == LinearLayout.HORIZONTAL) {
            canvas.drawLine(0, getHeight() * 0.5f, getWidth(), getHeight() * 0.5f, paint);
        } else {
            canvas.drawLine(getWidth() * 0.5f, 0, getWidth() * 0.5f, getHeight(), paint);
        }


        paint.setColor(outerLineColor);
        paint.setStrokeWidth(outerLineWidth);
        if (mOrientation == LinearLayout.HORIZONTAL) {
            canvas.drawLine(0, getHeight() * 0.5f, currentPosition, getHeight() * 0.5f, paint);
        } else {
            canvas.drawLine(getWidth() * 0.5f, 0, getWidth() * 0.5f, currentPosition, paint);
        }

        paint.setColor(innerLineColor);
        paint.setStrokeWidth(innerLineWidth);
        if (mOrientation == LinearLayout.HORIZONTAL) {
            canvas.drawLine(0, getHeight() * 0.5f, currentPosition, getHeight() * 0.5f, paint);
        } else {
            canvas.drawLine(getWidth() * 0.5f, 0, getWidth() * 0.5f, currentPosition, paint);
        }
    }

    void setPointsCount(int pointsCount) {
        this.pointsCount = pointsCount;
    }

    void startAnimation(int position) {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
        animator = ValueAnimator.ofFloat(currentPosition, positions[position]);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentPosition = (Float) animation.getAnimatedValue();
                SliderBgLine.this.invalidate();
            }
        });
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(duration);
        animator.start();
    }

    /**
     * @param outerLineColor color to be set
     */
    public void setOuterLineColor(@ColorInt int outerLineColor) {
        this.outerLineColor = outerLineColor;
        invalidate();
    }

    /**
     * @param innerLineColor color to be set
     */
    public void setInnerLineColor(@ColorInt int innerLineColor) {
        this.innerLineColor = innerLineColor;
        invalidate();
    }

    /**
     * @param duration duration to be set
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * @param orientation orientation to be set
     */
    public void setOrientation(@IntRange(from = LinearLayout.HORIZONTAL, to = LinearLayout.VERTICAL) int orientation) {
        mOrientation = orientation;
    }
}
