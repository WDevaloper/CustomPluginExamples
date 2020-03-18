package com.github.plugin.exalple.test;


import android.content.Context;
import android.util.Log;

import com.github.plugin.common.IComponent;

public class Component implements IComponent {
    @Override
    public void attachBaseContext(Context base) {
        Log.e("tag", "Component for attachBaseContext method");
    }

    @Override
    public void onCreate() {
        Log.e("tag", "Component for onCreate method");
    }

    @Override
    public void onLowMemory() {
        Log.e("tag", "Component for onLowMemory method");
    }

    @Override
    public void onTerminate() {
        Log.e("tag", "Component for onTerminate method");
    }

    @Override
    public void onTrimMemory(int level) {
        Log.e("tag", "Component for onTrimMemory method");
    }
}
