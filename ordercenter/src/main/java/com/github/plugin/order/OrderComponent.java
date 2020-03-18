package com.github.plugin.order;

import android.content.Context;
import android.util.Log;

import com.github.plugin.common.IComponent;

public class OrderComponent implements IComponent {
    @Override
    public void attachBaseContext(Context base) {
        Log.e("tag", "OrderComponent for attachBaseContext method");
    }

    @Override
    public void onCreate() {
        Log.e("tag", "OrderComponent for onCreate method");
    }

    @Override
    public void onLowMemory() {
        Log.e("tag", "OrderComponent for onLowMemory method");
    }

    @Override
    public void onTerminate() {
        Log.e("tag", "OrderComponent for onTerminate method");
    }

    @Override
    public void onTrimMemory(int level) {
        Log.e("tag", "OrderComponent for onTrimMemory method");
    }
}
