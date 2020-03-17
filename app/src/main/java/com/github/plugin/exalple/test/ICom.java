package com.github.plugin.exalple.test;


import android.util.Log;

public class ICom implements IComponent {
    @Override
    public void onCreate() {
        Log.e("tag","ICom");
    }
}
