package com.wilson.easyslide;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private EasySlideView mSlideView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSlideView = (EasySlideView)findViewById(R.id.activity_main);
        mSlideView.setSwipeStateChangeListener(new EasySlideView.onSwipeListener() {
            @Override
            public void onSwipeStateChange(EasySlideView.State state) {
                Log.d("Easy","state:"+state.name());
            }
        });
    }
}
