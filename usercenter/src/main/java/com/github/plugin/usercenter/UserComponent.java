package com.github.plugin.usercenter;

import android.util.Log;

import com.github.plugin.common.IComponent;

public class UserComponent implements IComponent {
    @Override
    public void onCreate() {
        Log.e("tag","UserComponent");
    }
}
