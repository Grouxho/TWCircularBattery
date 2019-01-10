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

package com.android.systemui.statusbar.phone;


import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.util.ArrayMap;
import android.view.View;
import android.widget.ImageView;


import com.android.systemui.statusbar.policy.DarkIconDispatcher;

public class DarkIconDispatcherImpl implements DarkIconDispatcher {


    private final Rect mTintArea = new Rect();
    private final ArrayMap<Object, DarkReceiver> mReceivers = new ArrayMap<>();

    private int mIconTint = DEFAULT_ICON_TINT;
    private float mDarkIntensity=0f;
    private int mDarkModeIconColorSingleTone;
    private int mLightModeIconColorSingleTone;

    int mGrxDarkIntensity=0;





    public DarkIconDispatcherImpl(Context context) {


    }



    public void addDarkReceiver(DarkReceiver receiver) {
        mReceivers.put(receiver, receiver);
        receiver.onDarkChanged(mTintArea, mDarkIntensity, mIconTint);
    }

    public void addDarkReceiver(ImageView imageView) {

    }

    public void removeDarkReceiver(DarkReceiver object) {
        mReceivers.remove(object);
    }

    public void removeDarkReceiver(ImageView object) {
        mReceivers.remove(object);
    }

    public void applyDark(ImageView object) {
        mReceivers.get(object).onDarkChanged(mTintArea, mDarkIntensity, mIconTint);
    }


    public void setIconsDarkArea(Rect darkArea) {
        if (darkArea == null && mTintArea.isEmpty()) {
            return;
        }
        if (darkArea == null) {
            mTintArea.setEmpty();
        } else {
            mTintArea.set(darkArea);
        }
        applyIconTint();
    }

    private void setIconTintInternal(float darkIntensity) {
        mDarkIntensity = darkIntensity;
        mIconTint = 1;
        applyIconTint();
    }

    public void simulateTint(float dark){
        setIconTintInternal(dark);
    }

    private void applyIconTint() {
        for (int i = 0; i < mReceivers.size(); i++) {
            mReceivers.valueAt(i).onDarkChanged(mTintArea, mDarkIntensity, mIconTint);
        }
    }




}
