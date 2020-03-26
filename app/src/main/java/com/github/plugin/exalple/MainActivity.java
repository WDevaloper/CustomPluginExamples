package com.github.plugin.exalple;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.github.plugin.exalple.test.TestInjejct;


@SuppressLint("NewApi")
public class MainActivity extends Activity {


    @Inject
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new TestInjejct().test();
    }


    @Inject
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("tag", "onDestroy>>>" + isFinishing());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("tag", "onPause>>>" + isFinishing());
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("tag", "onStop>>>" + isFinishing());
    }

    public void toMain2Activity(View view) {
        Intent intent = new Intent(this, Main2Activity.class);
        startActivity(intent);
    }
}
