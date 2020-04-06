package com.github.plugin.exalple.test

import android.util.Log
import com.github.plugin.exalple.Inject

class TestInjejct {
    @Inject
    fun test() {
        Log.e("tag", "test inject")
    }
}