package com.github.plugin.usercenter;

import android.util.Log;

import com.github.plugin.common.IComponent;

public class IComponentImpl implements IComponent {
    @Override
    public void onCreate() {
        Log.e("tag","IComponentImpl");
    }
}
