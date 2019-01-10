/*
 * Copyright (C) 2015 The Android Open Source Project
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
 * limitations under the License
 */
package com.android.systemui.tuner;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;


import com.android.systemui.Dependency;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class TunerServiceImpl extends TunerService {

    private static final String TUNER_VERSION = "sysui_tuner_version";

    private static final int CURRENT_TUNER_VERSION = 1;

    private final Observer mObserver = new Observer();
    // Map of Uris we listen on to their settings keys.
    private final ArrayMap<Uri, String> mListeningUris = new ArrayMap<>();
    // Map of settings keys to the listener.
    private final HashMap<String, Set<Tunable>> mTunableLookup = new HashMap<>();
    // Set of all tunables, used for leak detection.
    private final HashSet<Tunable> mTunables =  new HashSet<>() ;
    private final Context mContext;

    private ContentResolver mContentResolver;
    private int mCurrentUser;

    public TunerServiceImpl(Context context) {
        mContext = context;
        mContentResolver = mContext.getContentResolver();



    }


    private boolean isLineageSystem(String key) {
        return key.startsWith("lineagesystem:");
    }

    private boolean isLineageSecure(String key) {
        return key.startsWith("lineagesecure:");
    }

    private boolean isSystem(String key) {
       if(true) return true; // for VDs simulation
        return key.startsWith("system:");
    }

    private String chomp(String key) {
        return key.replaceFirst("^(lineagesecure|lineagesystem|system):", "");
    }

    @Override
    public String getValue(String setting) {
        if (isLineageSecure(setting)) {
            return Settings.Secure.getString(
                    mContentResolver, chomp(setting));
        } else if (isLineageSystem(setting)) {
            return Settings.System.getString(
                    mContentResolver, chomp(setting));
        } else if (isSystem(setting)) {
            return Settings.System.getString(
                    mContentResolver, chomp(setting));
        } else {
            return Settings.Secure.getString(mContentResolver, setting);
        }
    }

    @Override
    public void setValue(String setting, String value) {
  /*      if (isLineageSecure(setting)) {
            LineageSettings.Secure.putStringForUser(
                    mContentResolver, chomp(setting), value, mCurrentUser);
        } else if (isLineageSystem(setting)) {
            LineageSettings.System.putStringForUser(
                    mContentResolver, chomp(setting), value, mCurrentUser);
        } else if (isSystem(setting)) {
            Settings.System.putStringForUser(
                    mContentResolver, chomp(setting), value, mCurrentUser);
        } else {
            Settings.Secure.putStringForUser(mContentResolver, setting, value, mCurrentUser);
        }*/
    }

    @Override
    public int getValue(String setting, int def) {
/*        if (isLineageSecure(setting)) {
            return LineageSettings.Secure.getIntForUser(
                    mContentResolver, chomp(setting), def, mCurrentUser);
        } else if (isLineageSystem(setting)) {
            return LineageSettings.System.getIntForUser(
                    mContentResolver, chomp(setting), def, mCurrentUser);
        } else if (isSystem(setting)) {
            return Settings.System.getIntForUser(
                    mContentResolver, chomp(setting), def, mCurrentUser);
        } else {
            return Settings.Secure.getIntForUser(mContentResolver, setting, def, mCurrentUser);
        }*/
    return 0;
    }

    @Override
    public String getValue(String setting, String def) {
        String ret;
        if (isLineageSecure(setting)) {
            ret = Settings.Secure.getString(
                    mContentResolver, chomp(setting));
        } else if (isLineageSystem(setting)) {
            ret = Settings.System.getString(
                    mContentResolver, chomp(setting));
        } else if (isSystem(setting)) {
            ret = Settings.System.getString(
                    mContentResolver, chomp(setting));
        } else {
            ret = Secure.getString(mContentResolver, setting);
        }
        if (ret == null) return def;
        return ret;
    }

    @Override
    public void setValue(String setting, int value) {
 /*       if (isLineageSecure(setting)) {
            LineageSettings.Secure.putIntForUser(
                    mContentResolver, chomp(setting), value, mCurrentUser);
        } else if (isLineageSystem(setting)) {
            LineageSettings.System.putIntForUser(
                    mContentResolver, chomp(setting), value, mCurrentUser);
        } else if (isSystem(setting)) {
            Settings.System.putIntForUser(mContentResolver, chomp(setting), value, mCurrentUser);
        } else {
            Settings.Secure.putIntForUser(mContentResolver, setting, value, mCurrentUser);
        }*/
    }

    @Override
    public void addTunable(Tunable tunable, String... keys) {
        for (String key : keys) {
            addTunable(tunable, key);
        }
    }

    private void addTunable(Tunable tunable, String key) {
        if (!mTunableLookup.containsKey(key)) {
            mTunableLookup.put(key, new ArraySet<Tunable>());
        }
        mTunableLookup.get(key).add(tunable);
        /*if (LeakDetector.ENABLED) {
            mTunables.add(tunable);
            Dependency.get(LeakDetector.class).trackCollection(mTunables, "TunerService.mTunables");
        }*/
        final Uri uri;
        if (isLineageSecure(key)) {
            uri = Settings.Secure.getUriFor(chomp(key));
        } else if (isLineageSystem(key)) {
            uri = Settings.System.getUriFor(chomp(key));
        } else if (isSystem(key)) {
            uri = Settings.System.getUriFor(chomp(key));
        } else {
            uri = Settings.Secure.getUriFor(key);
        }
        if (!mListeningUris.containsKey(uri)) {
            mListeningUris.put(uri, key);
            mContentResolver.registerContentObserver(uri, false, mObserver/*, mCurrentUser*/);
        }
        // Send the first state.
        String value = getValue(key);
        tunable.onTuningChanged(key, value);
    }

    @Override
    public void removeTunable(Tunable tunable) {
        for (Set<Tunable> list : mTunableLookup.values()) {
            list.remove(tunable);
        }
  /*      if (LeakDetector.ENABLED) {
            mTunables.remove(tunable);
        }*/
    }

    protected void reregisterAll() {
        if (mListeningUris.size() == 0) {
            return;
        }
        mContentResolver.unregisterContentObserver(mObserver);
        for (Uri uri : mListeningUris.keySet()) {
            mContentResolver.registerContentObserver(uri, false, mObserver/*, mCurrentUser*/);
        }
    }

    private void reloadSetting(Uri uri) {
        String key = mListeningUris.get(uri);
        Set<Tunable> tunables = mTunableLookup.get(key);
        if (tunables == null) {
            return;
        }
        String value = getValue(key);
        for (Tunable tunable : tunables) {
            tunable.onTuningChanged(key, value);
        }
    }

    private void reloadAll() {
        for (String key : mTunableLookup.keySet()) {
            String value = getValue(key);
            for (Tunable tunable : mTunableLookup.get(key)) {
                tunable.onTuningChanged(key, value);
            }
        }
    }

    @Override
    public void clearAll() {
        // A couple special cases.
     /*   Settings.Global.putString(mContentResolver, DemoMode.DEMO_MODE_ALLOWED, null);
        Intent intent = new Intent(DemoMode.ACTION_DEMO);
        intent.putExtra(DemoMode.EXTRA_COMMAND, DemoMode.COMMAND_EXIT);
        mContext.sendBroadcast(intent);*/

        for (String key : mTunableLookup.keySet()) {
            setValue(key, null);
        }
    }

    private class Observer extends ContentObserver {
        public Observer() {
            super(new Handler(Looper.getMainLooper()));
        }

        @Override
        public void onChange(boolean selfChange, Uri uri/*, int userId*/) {
         /*   if (userId == ActivityManager.getCurrentUser()) {
                reloadSetting(uri);
            }*/
         reloadSetting(uri);
        }
    }
}
