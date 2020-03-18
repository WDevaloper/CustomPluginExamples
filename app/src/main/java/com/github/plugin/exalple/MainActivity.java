package com.github.plugin.exalple;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;



@SuppressLint("NewApi")
public class MainActivity extends Activity {


    @Inject
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Inject
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
