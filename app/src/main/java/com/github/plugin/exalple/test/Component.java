package com.github.plugin.exalple.test;


import android.util.Log;

import com.github.plugin.common.IComponent;

public class Component implements IComponent {
    @Override
    public void onCreate() {
        Log.e("tag","Component");
    }
}
