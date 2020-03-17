package com.github.plugin.exalple;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testAsm();
    }


    @Inject
    private void testAsm() {
        for (int i = 0; i < 10; i++) {
            Log.e("tag", "i = " + i);
        }
    }


    private void test() {
    }
}
