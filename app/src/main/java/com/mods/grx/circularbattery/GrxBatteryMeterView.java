package com.mods.grx.circularbattery;
/* 

Created by Grouxho on 10/01/2019. 

*/


import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.support.graphics.drawable.ArgbEvaluator;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import com.android.systemui.Dependency;

import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.DarkIconDispatcher;


@SuppressLint("RestrictedApi")
public class GrxBatteryMeterView extends ImageView implements
        BatteryController.BatteryStateChangeCallback, DarkIconDispatcher.DarkReceiver{

    private GrxBatteryDrawable mDrawable=null;
    private final String mSlotBattery = "grxbattery";

    int mFrameColorForDarkBgs, mFrameColorForLightBgs,
            mLevelColorForDarkBgs, mLevelColorForLightBg,
            mTextColorForDarkBgs,mTextColorForLightBgs;

    private int mBatterySize=24; //dpi default
    //private int mBatteryThinStyle=0;
    private boolean mIsEnabled = true;  // enabled by default
    private long mAnimationRefreshTime = 100, mAlphaAnimationDuration = 2000;

    private final Context mContext;
    private ArgbEvaluator mArgEvaluator=null;
    public BatterySettingsObserver myObserver;
    private float mDarkIntensity= 0f;

    private int mBatteryStrokeWidth = 2;


    public GrxBatteryMeterView(Context context) {
        this(context, null, 0);
    }

    public GrxBatteryMeterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("RestrictedApi")
    public GrxBatteryMeterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mArgEvaluator = new ArgbEvaluator();
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mContext = context;
        readBatteryColors();
        readViewOptions();
        createDrawable();
        myObserver = new BatterySettingsObserver(new Handler());

    }

    @SuppressLint("RestrictedApi")
    private int getColorForDarkIntensity(float darkIntensity, int lightColor, int darkColor) {

        return (int) mArgEvaluator.evaluate(darkIntensity, lightColor, darkColor);
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    private void addAsDarkReceiver(){
        DarkIconDispatcher darkIconDispatcher = (DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class);
        darkIconDispatcher.removeDarkReceiver((DarkIconDispatcher.DarkReceiver ) this);
        darkIconDispatcher.addDarkReceiver((DarkIconDispatcher.DarkReceiver ) this);

    }

    private void removeAsDarkReceiver(){
        DarkIconDispatcher darkIconDispatcher = (DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class);
        darkIconDispatcher.removeDarkReceiver((DarkIconDispatcher.DarkReceiver ) this);
    }


    private void startListening(){
        BatteryController batteryController = (BatteryController) Dependency.get(BatteryController.class);
        batteryController.removeCallback((Object) this);
        batteryController.addCallback((Object) this);

    }

    private void stopListening(){
        BatteryController batteryController = (BatteryController) Dependency.get(BatteryController.class);
        batteryController.removeCallback((Object) this);

    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(mIsEnabled) {
            addAsDarkReceiver();
            startListening();
            //   myObserver.register();

        }
    }


    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopListening();
        removeAsDarkReceiver();
        //myObserver.unregister();
    }

    @Override
    public void onPowerSaveChanged(boolean isPowerSave) {
        if(mDrawable!=null) mDrawable.onPowerSaveChanged(isPowerSave);
    }

    /****************************************************/
    /***    onBatteryLevelChanged  **********************/
    /****************************************************/

    /*

    If you want to generate Samsung PIE compatible code, Edit BatteryController.java and BatteryControllerImpl.java, look for "PIE" and replace  the oreo method / lines with those.

    Here you should replace the following method (onBatteryLevelChanged)

     */



    @Override   // For Samsung Oreo


    public void onBatteryLevelChanged(int level,
                                      boolean pluggedIn,
                                      boolean charging,
                                      int batterystatus,
                                      int batteryhealth,
                                      int batteryonline,
                                      int plugtype,
                                      boolean powersupply){

        if(mDrawable!=null) mDrawable.onBatteryLevelChanged(level,pluggedIn, charging);

    }


/*
    @Override  // For "PIE"
    public void onBatteryLevelChanged(int level,
                                      boolean pluggedIn,
                                      boolean charging,
                                      int batterystatus,
                                      int batteryhealth,
                                      int batteryonline){

        if(mDrawable!=null) mDrawable.onBatteryLevelChanged(level,pluggedIn, charging);

    }
*/

    private int dip2px(float dip) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dip, mContext.getResources().getDisplayMetrics()
        );
    }

    private void removeDrawable(){
        if(mDrawable!=null) {
            mDrawable=null;
            setImageDrawable(null);
        }
    }

    //private void createDrawable(int size, int thin, int bold){
    private void createDrawable(){
        if(!mIsEnabled) return;
        if(mDrawable!=null) removeDrawable();
        final int frameColor = 0x20fafafa;
        mDrawable = new GrxBatteryDrawable(mContext, new Handler(), mBatterySize, mBatteryStrokeWidth,mBoldText);
        mDrawable.setAnimationOptions(mAlphaAnimationDuration,mAnimationRefreshTime);
        mDrawable.setColors(
                getColorForDarkIntensity(mDarkIntensity,mFrameColorForDarkBgs,mFrameColorForLightBgs),
                getColorForDarkIntensity(mDarkIntensity,mLevelColorForDarkBgs, mLevelColorForLightBg),
                getColorForDarkIntensity(mDarkIntensity,mTextColorForDarkBgs, mTextColorForLightBgs)
                );
        setImageDrawable(mDrawable);
    }



    public void updateBatteryOptions(){  // method for on fly
        readBatteryColors();
        boolean needsrebuild = readViewOptions();
        if(needsrebuild) {
            if (mIsEnabled) {
                createDrawable();
                addAsDarkReceiver();
                startListening();
            } else {
                removeAsDarkReceiver();
                stopListening();
                removeDrawable();
            }
        }
        if(mDrawable!=null && !needsrebuild) {
            mDrawable.setAnimationOptions(mAlphaAnimationDuration,mAnimationRefreshTime);
            mDrawable.setColors(
                    getColorForDarkIntensity(mDarkIntensity,mFrameColorForDarkBgs,mFrameColorForLightBgs),
                    getColorForDarkIntensity(mDarkIntensity,mLevelColorForDarkBgs, mLevelColorForLightBg),
                    getColorForDarkIntensity(mDarkIntensity,mTextColorForDarkBgs, mTextColorForLightBgs));
            mDrawable.invalidateSelf();
        }


/*

        if(mIsEnabled){
            if(mDrawable!=null) {
                mDrawable.setBatteryOptions(
                        getColorForDarkIntensity(mDarkIntensity,mLevelColorForDarkBgs, mLevelColorForLightBg),
                        getColorForDarkIntensity(mDarkIntensity,mTextColorForDarkBgs, mTextColorForLightBgs),
                        getColorForDarkIntensity(mDarkIntensity,mFrameColorForDarkBgs,mFrameColorForLightBgs),
                        mBatterySize, mBatteryStrokeWidth, mBoldText,mAlphaAnimationDuration,mAnimationRefreshTime);
                invalidate();
            }else {
                createDrawable();
                addAsDarkReceiver();
                startListening();
            }
        }else{
            removeAsDarkReceiver();
            stopListening();
            removeDrawable();
        }

        */

        setVisibility(mIsEnabled? View.VISIBLE: View.GONE);
    }

    private boolean readViewOptions(){
        boolean needsrebuild = false;

        int oldboldtext = mBoldText;
        int oldbatsize = mBatterySize;
        //int oldTihin = mBatteryThinStyle;
        float oldStrokeWidth = mBatteryStrokeWidth;
        boolean oldenabled = mIsEnabled;

        mIsEnabled = Settings.System.getInt(mContext.getContentResolver(), "grxbat_enabled",1) == 1 ? true : false;

        mBoldText = Settings.System.getInt(mContext.getContentResolver(),"grxbat_bold",0);
        mBatterySize = Settings.System.getInt(mContext.getContentResolver(),"grxbat_size",24);
        mBatteryStrokeWidth = Settings.System.getInt(mContext.getContentResolver(),"grxbat_stroke",2);

        mAnimationRefreshTime = (long) Settings.System.getInt(mContext.getContentResolver(),"grxbat_animtime",100);
        mAlphaAnimationDuration = (long) (1000*Settings.System.getInt(mContext.getContentResolver(),"grxbat_alphatime",2));

        //mBatteryThinStyle = Settings.System.getInt(mContext.getContentResolver(),"grxbat_thin",0);

        if(oldenabled!=mIsEnabled || oldboldtext!=mBoldText || oldbatsize!=mBatterySize || oldStrokeWidth!=mBatteryStrokeWidth) needsrebuild = true;
        return needsrebuild;
    }

    int mBoldText = 0;


    private void readBatteryColors(){

        ContentResolver contentResolver = getContext().getContentResolver();

        mFrameColorForDarkBgs = Settings.System.getInt(contentResolver,"grxbat_darkbg_frame",0x20fafafa);
        mTextColorForDarkBgs  = Settings.System.getInt(contentResolver,"grxbat_darkbg_text",0xfffafafa);
        mLevelColorForDarkBgs = Settings.System.getInt(contentResolver,"grxbat_darkbg_bat",0xfffafafa);


        mFrameColorForLightBgs = Settings.System.getInt(contentResolver,"grxbat_lightbg_frame",0x20343434);
        mTextColorForLightBgs =  Settings.System.getInt(contentResolver,"grxbat_lightbg_text",0xff343434);
        mLevelColorForLightBg =  Settings.System.getInt(contentResolver,"grxbat_lightbg_bat",0xff343434);
    }


    @Override
    public void onDarkChanged(Rect area, float darkIntensity, int tint) {
        if(darkIntensity==mDarkIntensity) return;
        mDarkIntensity=darkIntensity;
        updateBatteryColors();

    }

    private void updateBatteryColors(){
        if(mDrawable!=null){
            int frcolor = getColorForDarkIntensity(mDarkIntensity,mFrameColorForDarkBgs,mFrameColorForLightBgs);
            int batcolor = getColorForDarkIntensity(mDarkIntensity,mLevelColorForDarkBgs, mLevelColorForLightBg);
            int textcolor = getColorForDarkIntensity(mDarkIntensity,mTextColorForDarkBgs, mTextColorForLightBgs);
            mDrawable.setColors(frcolor,batcolor,textcolor);
            mDrawable.invalidateSelf();
        }
    }


    public class BatterySettingsObserver extends ContentObserver{


        public BatterySettingsObserver(android.os.Handler handler){
            super(handler);
            register();
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            updateBatteryOptions();
        }


        public void register(){
            ContentResolver contentResolver = getContext().getContentResolver();

            Uri uri = Settings.System.getUriFor("grxbat_enabled");
            contentResolver.registerContentObserver(uri,false,this);

            uri = Settings.System.getUriFor("grxbat_bold");
            contentResolver.registerContentObserver(uri,false,this);

            uri = Settings.System.getUriFor("grxbat_size");
            contentResolver.registerContentObserver(uri,false,this);

       /*     uri = Settings.System.getUriFor("grxbat_thin");
            contentResolver.registerContentObserver(uri,false,this); */

            uri = Settings.System.getUriFor("grxbat_stroke");
            contentResolver.registerContentObserver(uri,false,this);

            uri = Settings.System.getUriFor("grxbat_animtime");
            contentResolver.registerContentObserver(uri,false,this);

            uri = Settings.System.getUriFor("grxbat_alphatime");
            contentResolver.registerContentObserver(uri,false,this);

            uri = Settings.System.getUriFor("grxbat_darkbg_frame");
            contentResolver.registerContentObserver(uri,false,this);

            uri = Settings.System.getUriFor("grxbat_darkbg_bat");
            contentResolver.registerContentObserver(uri,false,this);

            uri = Settings.System.getUriFor("grxbat_darkbg_text");
            contentResolver.registerContentObserver(uri,false,this);

            uri = Settings.System.getUriFor("grxbat_lightbg_frame");
            contentResolver.registerContentObserver(uri,false,this);

            uri = Settings.System.getUriFor("grxbat_lightbg_bat");
            contentResolver.registerContentObserver(uri,false,this);

            uri = Settings.System.getUriFor("grxbat_lightbg_text");
            contentResolver.registerContentObserver(uri,false,this);
        }

        public void unregister(){

            getContext().getContentResolver().unregisterContentObserver(this);

        }

    }



}
