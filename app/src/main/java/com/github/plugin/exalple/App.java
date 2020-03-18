package com.github.plugin.exalple;

import android.app.Application;

import com.github.plugin.common.InjectManager;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        InjectManager instance = InjectManager.getInstance();
        instance.initComponent();
    }
}
