/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.android.systemui.statusbar.policy;

import android.graphics.Color;
import android.graphics.Rect;
import android.view.View;
import android.widget.ImageView;



public interface DarkIconDispatcher {



    void setIconsDarkArea(Rect r);


    void addDarkReceiver(DarkReceiver receiver);
    void addDarkReceiver(ImageView imageView);

    void removeDarkReceiver(DarkReceiver object);
    void removeDarkReceiver(ImageView object);

    void applyDark(ImageView object);

    int DEFAULT_ICON_TINT = Color.WHITE;
    Rect sTmpRect = new Rect();
    int[] sTmpInt2 = new int[2];



    void simulateTint(float dark);



    interface DarkReceiver {
        void onDarkChanged(Rect area, float darkIntensity, int tint);


    }

    static boolean isInArea(Rect area, View view) {
        if (area.isEmpty()) {
            return true;
        }
        sTmpRect.set(area);
        view.getLocationOnScreen(sTmpInt2);
        int left = sTmpInt2[0];

        int intersectStart = Math.max(left, area.left);
        int intersectEnd = Math.min(left + view.getWidth(), area.right);
        int intersectAmount = Math.max(0, intersectEnd - intersectStart);

        boolean coversFullStatusBar = area.top <= 0;
        boolean majorityOfWidth = 2 * intersectAmount > view.getWidth();
        return majorityOfWidth && coversFullStatusBar;
    }
}