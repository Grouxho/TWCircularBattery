package com.mods.grx.circularbattery;
/* 

Created by Grouxho on 10/01/2019. 

*/

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;


import static android.util.TypedValue.applyDimension;

public class GrxBatteryDrawable extends Drawable {

    private int mIntrinsicWidth;
    private int mIntrinsicHeight;
    private int mHeight;
    private int mWidth;

    private final Context mContext;
    private final Handler mHandler;

    private int mLevel = -1;
    private boolean mPluggedIn, mIsCharging;

    private float mTextX, mTextY;
    private int mTypeFaceStyle=0; //0 bold, 1 normal */

    private float mStrokeWith;

    private boolean mInitialized;

    private ValueAnimator mAlphaAnimator;
    public float mStartAnimationAngle=270f;
    public boolean mIsAnimationRunning = false;
    public long mAlphaAnimationDuration=2000, mAnimationUpdateTime=300;
    Runnable mAnimationRunnable;

    protected Paint mBackgroundPaint, mTextPaint, mLevelPaint;
    private RectF mFrameRectF = new RectF();




    public GrxBatteryDrawable(Context context, Handler handler, int batsize, int strokewidth, int boldtext ) {
        mContext = context;
        mHandler = handler;

        mStrokeWith = dpi2pixels(strokewidth);

        mIntrinsicWidth = (int) dpi2pixels(batsize);
        mIntrinsicHeight = (int) dpi2pixels(batsize);


        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setDither(true);

        mLevelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLevelPaint.setDither(true);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTypeFaceStyle=boldtext;
        Typeface font = Typeface.create("sans-serif-condensed", (mTypeFaceStyle==0)?Typeface.NORMAL : Typeface.BOLD);//to do  -> add option
        mTextPaint.setTypeface(font);

        mAnimationRunnable = new Runnable() {
            @Override
            public void run() {
                if(mStartAnimationAngle>360f) mStartAnimationAngle=0f;
                else mStartAnimationAngle+=3f;
                mIsAnimationRunning=false;
                invalidateSelf();
            }
        };

    }

    public void setAnimationOptions(long alphaanimationduration,long animationupdatetime){
        mAlphaAnimationDuration = alphaanimationduration;
        mAnimationUpdateTime=animationupdatetime;
    }


    @Override
    public void setAlpha(int alpha) {
    }


    private void initDrawingDimensions(){
        if (mWidth <= 0 || mHeight <= 0) return;

        final int batsize = Math.min(mWidth, mHeight);

        mTextPaint.setTextSize(batsize / 2f - mStrokeWith/4f);

     //   mFrameRectF.set(0, 0, mWidth, mHeight);
        Rect bounds = new Rect();
        mTextPaint.getTextBounds("99", 0, "99".length(), bounds);
        mTextX = batsize/2;
        mTextY = batsize/2 + bounds.height() / 2.0f;

        mFrameRectF.set(
                mStrokeWith / 2.0f ,
                mStrokeWith / 2.0f,
                batsize - mStrokeWith/ 2.0f ,
                batsize - mStrokeWith / 2.0f);

        mBackgroundPaint.setStrokeWidth(mStrokeWith);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);

        mLevelPaint.setStrokeWidth(mStrokeWith);
        mLevelPaint.setStyle(Paint.Style.STROKE);

        mInitialized=true;
    }


    @Override
    public void draw(Canvas canvas) {
        if(!mInitialized){
            initDrawingDimensions();
        }

        final int level = mLevel;
        canvas.drawArc(mFrameRectF, 270, 360, false, mBackgroundPaint);

        if(mIsCharging){
            canvas.drawArc(mFrameRectF, mStartAnimationAngle , 3.6f * level, false, mLevelPaint);
            if(!mIsAnimationRunning) {
                mIsAnimationRunning = true;
                mHandler.postDelayed(mAnimationRunnable,mAnimationUpdateTime);
            }
        }else{
            if (level > 0) {
                canvas.drawArc(mFrameRectF, 270, 3.6f * level, false, mLevelPaint);
            }
        }

        String leveltext = String.valueOf(level);
        canvas.drawText(leveltext, mTextX, mTextY, mTextPaint);


    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        mHeight = bottom - top;
        mWidth = right - left;

    }

    @Override
    public int getIntrinsicHeight() {
        return mIntrinsicHeight;
    }

    @Override
    public int getIntrinsicWidth() {
        return mIntrinsicWidth;
    }

    private float dpi2pixels(int dips) {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        return applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) dips, metrics);
    }

    public void animateCircleBattery(int level, boolean pluggedIn, boolean charging) {

        if(charging && mInitialized){
            if (mAlphaAnimator != null) mAlphaAnimator.cancel();
            final int defaultAlpha = Color.alpha(mLevelPaint.getColor());
            final int defaultframealpha=Color.alpha(mBackgroundPaint.getColor());

            mAlphaAnimator = ValueAnimator.ofInt(defaultAlpha, 0, defaultAlpha);
            mAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int alphavalue = (int) animation.getAnimatedValue();
                    if(alphavalue<=defaultAlpha) mLevelPaint.setAlpha(alphavalue);
                    if(alphavalue<=defaultframealpha) mBackgroundPaint.setAlpha(alphavalue);
                    invalidateSelf();
                }
            });
            mAlphaAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    mLevelPaint.setAlpha(defaultAlpha);
                    mBackgroundPaint.setAlpha(defaultframealpha);
                    mAlphaAnimator = null;
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLevelPaint.setAlpha(defaultAlpha);
                    mBackgroundPaint.setAlpha(defaultframealpha);
                    mAlphaAnimator = null;
                }
            });
            mAlphaAnimator.setDuration(mAlphaAnimationDuration);
            mAlphaAnimator.start();
        }
    }


    public void onPowerSaveChanged(boolean isPowerSave) {
        invalidateSelf();
    }

    private void postInvalidate() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                invalidateSelf();
            }
        });
    }


    public void onBatteryLevelChanged(int level,boolean pluggedIn, boolean charging){
        mPluggedIn = pluggedIn;
        mIsCharging=charging;
        mLevel = level;
        animateCircleBattery(level, pluggedIn, charging);
        postInvalidate();
    }

    public void setColors(int framecolor, int batcolor, int textcolor){
        mBackgroundPaint.setColor(framecolor);
        mLevelPaint.setColor(batcolor);
        mTextPaint.setColor(textcolor);
        //invalidateSelf();
    }

}
