package com.github.plugin.exalple;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.github.plugin.common.IComponent;
import com.github.plugin.common.InjectManager;

@SuppressLint("NewApi")
public class App extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        InjectManager instance = InjectManager.getInstance();
        instance.initComponent();
        for (IComponent component : InjectManager.getInstance().getComponents()) {
            component.attachBaseContext(base);
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        for (IComponent component : InjectManager.getInstance().getComponents()) {
            component.onCreate();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        for (IComponent component : InjectManager.getInstance().getComponents()) {
            component.onLowMemory();
        }
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        for (IComponent component : InjectManager.getInstance().getComponents()) {
            component.onTerminate();
        }
    }


    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        for (IComponent component : InjectManager.getInstance().getComponents()) {
            component.onTrimMemory(level);
        }
    }


}
