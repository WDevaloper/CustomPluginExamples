package com.github.plugin.exalple.test;


import android.content.Context;
import android.util.Log;

import com.github.plugin.common.IComponent;

public class MainComponent implements IComponent {
    @Override
    public void attachBaseContext(Context base) {
        Log.e("tag", "MainComponent for attachBaseContext method");
    }

    @Override
    public void onCreate() {
        Log.e("tag", "MainComponent for onCreate method");
    }

    @Override
    public void onLowMemory() {
        Log.e("tag", "MainComponent for onLowMemory method");
    }

    @Override
    public void onTerminate() {
        Log.e("tag", "MainComponent for onTerminate method");
    }

    @Override
    public void onTrimMemory(int level) {
        Log.e("tag", "MainComponent for onTrimMemory method");
    }
}
