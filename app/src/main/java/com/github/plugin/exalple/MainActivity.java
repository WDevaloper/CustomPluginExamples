package com.github.plugin.exalple;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.github.plugin.common.IComponent;
import com.github.plugin.common.InjectManager;


@SuppressLint("NewApi")
public class MainActivity extends Activity {


    @Inject
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InjectManager.getInstance().getComponents().forEach(IComponent::onCreate);
        InjectManager.getInstance().getComponents().forEach(IComponent::onLow);
    }


    @Inject
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
