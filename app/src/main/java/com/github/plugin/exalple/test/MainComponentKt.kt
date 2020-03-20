package com.github.plugin.exalple.test


import android.content.Context
import android.util.Log

import com.github.plugin.common.IComponent


//兼容kotlin
class MainComponentKt : IComponent {
    override fun attachBaseContext(base: Context) {
        Log.e("tag", "MainComponentKt for attachBaseContext method")
    }

    override fun onCreate() {
        Log.e("tag", "MainComponentKt for onCreate method")
    }

    override fun onLowMemory() {
        Log.e("tag", "MainComponentKt for onLowMemory method")
    }

    override fun onTerminate() {
        Log.e("tag", "MainComponentKt for onTerminate method")
    }

    override fun onTrimMemory(level: Int) {
        Log.e("tag", "MainComponentKt for onTrimMemory method")
    }
}
