/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.policy;





public interface BatteryController /*extends DemoMode */{
    /**
     * Prints the current state of the {@link BatteryController} to the given {@link PrintWriter}.
     */


    /**
     * Sets if the current device is in power save mode.
     */
    void setPowerSaveMode(boolean powerSave);

    /**
     * Returns {@code true} if the device is currently in power save mode.
     */
    boolean isPowerSave();

    void addCallback(BatteryStateChangeCallback cb);
    void removeCallback(BatteryStateChangeCallback cb);
    void addCallback(Object cb);
    void removeCallback(Object cb);

    /**
        Listener interface for Samsung Oreo
     */



    interface BatteryStateChangeCallback {
        void onBatteryLevelChanged(
                                    int level,
                                   boolean pluggedIn,
                                   boolean charging,
                                   int batterystatus,
                                   int batteryhealth,
                                   int batteryonline,
                                   int plugtype,
                                   boolean powersupply);
        void onPowerSaveChanged(boolean isPowerSave);
    }



    /*   Interface compatible with "PIE"  */

/*
        interface BatteryStateChangeCallback {
        void onBatteryLevelChanged(
                                    int level,
                                   boolean pluggedIn,
                                   boolean charging,
                                   int batterystatus,
                                   int batteryhealth,
                                   int batteryonline
                                   );
        void onPowerSaveChanged(boolean isPowerSave);
    }
*/


}
