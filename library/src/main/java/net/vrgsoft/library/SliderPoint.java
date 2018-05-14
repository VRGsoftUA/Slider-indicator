package net.vrgsoft.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import static net.vrgsoft.library.Slider.DEFAULT_ANIMATION_DURATION;

class SliderPoint extends View {
    private static final int PULSE_INITIAL_ALPHA = 0x80;
    private static final int PULSE_INITIAL_RADIUS = 0;

    private Paint paint;

    private float endPulseRadius;
    private float endOuterRadius;
    private float endInnerRadius;
    private float startPulseRadius;
    private float startOuterRadius;
    private float startInnerRadius;
    private float middleInnerRadius;

    private float currentPulseRadius;
    private float currentOuterRadius;
    private float currentInnerRadius;

    private int centerX;
    private int centerY;

    private int pulseColor;
    private int outerColor;
    private int innerColor;

    private int pulseAlpha;

    private ValueAnimator mainAnimator;
    private ValueAnimator innerCircleSecondHalfAnimator;
    private ValueAnimator innerCircleFirstHalfAnimator;
    private ValueAnimator transitAnimator;

    private long duration;

    public SliderPoint(Context context) {
        this(context, null);
    }

    public SliderPoint(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SliderPoint(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
        paint.setStyle(Paint.Style.FILL);
        duration = DEFAULT_ANIMATION_DURATION;

        pulseColor = context.getResources().getColor(R.color.defaultPointPulseColor);
        outerColor = context.getResources().getColor(R.color.defaultPointOuterColor);
        innerColor = context.getResources().getColor(R.color.defaultPointInnerColor);
    }

    @SuppressLint("CustomViewStyleable")
    private void initAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Slider);

            pulseColor = a.getColor(R.styleable.Slider_pointPulseColor, pulseColor);
            outerColor = a.getColor(R.styleable.Slider_pointOuterColor, outerColor);
            innerColor = a.getColor(R.styleable.Slider_pointInnerColor, innerColor);
            duration = a.getInt(R.styleable.Slider_animationDuration, (int) duration);

            a.recycle();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int baseValue = w > h ? h : w;

        centerX = w / 2;
        centerY = h / 2;

        endPulseRadius = baseValue * 0.44f;
        endOuterRadius = baseValue * 0.26f;
        endInnerRadius = baseValue * 0.11f;

        startPulseRadius = endOuterRadius / 2;
        startOuterRadius = endOuterRadius / 2;
        startInnerRadius = endInnerRadius / 2;
        middleInnerRadius = endOuterRadius * 0.85f;

        initDefaultState();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setColor(pulseColor);
        paint.setAlpha(pulseAlpha);
        canvas.drawCircle(centerX, centerY, currentPulseRadius, paint);

        paint.setColor(outerColor);
        paint.setAlpha(0xFF);
        canvas.drawCircle(centerX, centerY, currentOuterRadius, paint);

        paint.setColor(innerColor);
        canvas.drawCircle(centerX, centerY, currentInnerRadius, paint);
    }

    public void startTransitAnimation(long startDelay) {
        transitAnimator = ValueAnimator.ofFloat(0, 1);
        transitAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                pulseAlpha = (int) ((1 - value) * 255);

                currentPulseRadius = endOuterRadius + value * (endPulseRadius - endOuterRadius);
                SliderPoint.this.invalidate();
            }
        });
        transitAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                initDefaultState();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                initDefaultState();
            }
        });
        transitAnimator.setStartDelay(startDelay);
        transitAnimator.setInterpolator(new DecelerateInterpolator());
        transitAnimator.setDuration(duration);
        transitAnimator.start();
    }

    public void startSelectAnimation() {
        cancelAllAnimations();
        startMainAnimation();
        startInnerCircleAnimation();
    }

    private void startMainAnimation() {
        mainAnimator = ValueAnimator.ofFloat(0, 1);
        mainAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                currentPulseRadius = startPulseRadius + value * (endPulseRadius - startPulseRadius);
                currentOuterRadius = startOuterRadius + value * (endOuterRadius - startOuterRadius);
                SliderPoint.this.invalidate();
            }
        });
        mainAnimator.setInterpolator(new LinearInterpolator());
        mainAnimator.setDuration(duration);
        mainAnimator.start();
    }

    private void startInnerCircleAnimation() {
        innerCircleFirstHalfAnimator = ValueAnimator.ofFloat(startInnerRadius, middleInnerRadius);
        innerCircleFirstHalfAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentInnerRadius = (Float) animation.getAnimatedValue();
                SliderPoint.this.invalidate();
            }
        });
        innerCircleFirstHalfAnimator.setDuration(duration);
        innerCircleFirstHalfAnimator.setInterpolator(new AccelerateInterpolator(1.2f));
        innerCircleFirstHalfAnimator.start();

        innerCircleSecondHalfAnimator = ValueAnimator.ofFloat(middleInnerRadius, endInnerRadius);
        innerCircleSecondHalfAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentInnerRadius = (Float) animation.getAnimatedValue();
                SliderPoint.this.invalidate();
            }
        });
        innerCircleSecondHalfAnimator.setInterpolator(new DecelerateInterpolator(2f));
        innerCircleSecondHalfAnimator.setDuration(duration);
        innerCircleSecondHalfAnimator.setStartDelay(duration);
        innerCircleSecondHalfAnimator.start();
    }

    public void cancelAllAnimations() {
        if (mainAnimator != null && mainAnimator.isRunning()) {
            mainAnimator.cancel();
        }
        if (innerCircleFirstHalfAnimator != null && innerCircleFirstHalfAnimator.isRunning()) {
            innerCircleFirstHalfAnimator.cancel();
        }
        if (innerCircleSecondHalfAnimator != null) {
            innerCircleSecondHalfAnimator.cancel();
        }
        if (transitAnimator != null && transitAnimator.isRunning()) {
            transitAnimator.cancel();
        }
    }

    public void initDefaultState() {
        pulseAlpha = PULSE_INITIAL_ALPHA;
        currentPulseRadius = PULSE_INITIAL_RADIUS;
        currentOuterRadius = endOuterRadius;
        currentInnerRadius = endInnerRadius;
        invalidate();
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setPulseColor(int pulseColor) {
        this.pulseColor = pulseColor;
        invalidate();
    }

    public void setOuterColor(int outerColor) {
        this.outerColor = outerColor;
        invalidate();
    }

    public void setInnerColor(int innerColor) {
        this.innerColor = innerColor;
        invalidate();
    }
}
