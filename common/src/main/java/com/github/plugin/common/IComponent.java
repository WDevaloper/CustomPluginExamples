package com.github.plugin.common;

import android.content.Context;

public interface IComponent {
    void attachBaseContext(Context base);

    void onCreate();

    void onLowMemory();

    void onTerminate();

    void onTrimMemory(int level);
}
