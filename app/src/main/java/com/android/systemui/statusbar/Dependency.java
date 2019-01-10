package com.android.systemui;

import android.content.Context;
import android.os.BatteryManager;
import android.os.Handler;

import com.android.systemui.statusbar.phone.DarkIconDispatcherImpl;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BatteryControllerImpl;
import com.android.systemui.statusbar.policy.DarkIconDispatcher;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tuner.TunerServiceImpl;

/* 

Created by Grouxho on 10/01/2019. 

*/
public class Dependency {

    Context mContext;




    public static BatteryController batteryController;
    public static DarkIconDispatcherImpl darkIconDispatcher;
    public static TunerService tunerService;

    public Dependency(Context context){
        mContext=context;
    batteryController = new BatteryControllerImpl(context);
    darkIconDispatcher = new DarkIconDispatcherImpl(context);
    tunerService = new TunerServiceImpl(context);
    }




    public static Object get(Class cl){
        if(cl == BatteryController.class) return  batteryController;
        if(cl==DarkIconDispatcher.class) return  darkIconDispatcher;
        if(cl==TunerService.class) return tunerService;
        return null;
    }

    public static final DependencyKey<Handler> TIME_TICK_HANDLER =
            new DependencyKey<>("time_tick_handler");

    public static final class DependencyKey<V> {
        private final String mDisplayName;

        public DependencyKey(String displayName) {
            mDisplayName = displayName;
        }

        @Override
        public String toString() {
            return mDisplayName;
        }
    }

}
