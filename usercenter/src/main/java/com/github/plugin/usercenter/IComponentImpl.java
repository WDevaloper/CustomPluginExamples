package com.github.plugin.usercenter;

import android.content.Context;
import android.util.Log;

import com.github.plugin.common.IComponent;

public class IComponentImpl implements IComponent {
    @Override
    public void attachBaseContext(Context base) {
        Log.e("tag", "IComponentImpl for attachBaseContext method");
    }

    @Override
    public void onCreate() {
        Log.e("tag", "IComponentImpl for onCreate method");
    }

    @Override
    public void onLowMemory() {
        Log.e("tag", "IComponentImpl for onLowMemory method");
    }

    @Override
    public void onTerminate() {
        Log.e("tag", "IComponentImpl for onTerminate method");
    }

    @Override
    public void onTrimMemory(int level) {
        Log.e("tag", "IComponentImpl for onTrimMemory method");
    }
}
