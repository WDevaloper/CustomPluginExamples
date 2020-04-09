package com.github.plugin.exalple;


import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.transition.TransitionValues;
import android.transition.Visibility;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.plugin.exalple.test.TestInjejct;


@SuppressLint("NewApi")
public class MainActivity extends Activity {

    MyVisibility myVisibility = new MyVisibility();
    TextView textView;

    @Inject
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new TestInjejct().test();

        textView = findViewById(R.id.textView);

        myVisibility.setDuration(3000);
        myVisibility.addTarget(textView);

    }

    public void onButton1(View view) {
        textView.setVisibility(View.GONE);
    }

    public void Button2(View view) {
        textView.setVisibility(View.VISIBLE);
    }

    class MyVisibility extends Visibility {


        @Override
        public Animator onAppear(ViewGroup sceneRoot,
                                 TransitionValues startValues,
                                 int startVisibility,
                                 TransitionValues endValues,
                                 int endVisibility) {
            Log.e("tag","onAppear>>>"+sceneRoot +"  ,"+startValues +"  ,"+startVisibility+"  ,"+
                    endValues + "  ,"+endVisibility);
            return super.onAppear(sceneRoot, startValues, startVisibility, endValues, endVisibility);
        }


        @Override
        public Animator onDisappear(ViewGroup sceneRoot, TransitionValues startValues, int startVisibility, TransitionValues endValues, int endVisibility) {
            Log.e("tag","onDisappear>>>"+sceneRoot
                    +"  ,"+startValues +"  ,"+startVisibility+"  ,"+
                    endValues + "  ,"+endVisibility);
            return super.onDisappear(sceneRoot, startValues, startVisibility, endValues, endVisibility);
        }

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
