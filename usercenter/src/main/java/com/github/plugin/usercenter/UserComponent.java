package com.github.plugin.usercenter;

import android.content.Context;
import android.util.Log;

import com.github.plugin.common.IComponent;

public class UserComponent implements IComponent {
    @Override
    public void attachBaseContext(Context base) {
        Log.e("tag", "UserComponent for attachBaseContext method");
    }

    @Override
    public void onCreate() {
        Log.e("tag", "UserComponent for onCreate method");
    }

    @Override
    public void onLowMemory() {
        Log.e("tag", "UserComponent for onLowMemory method");
    }

    @Override
    public void onTerminate() {
        Log.e("tag", "UserComponent for onTerminate method");
    }

    @Override
    public void onTrimMemory(int level) {
        Log.e("tag", "UserComponent for onTrimMemory method");
    }
}
