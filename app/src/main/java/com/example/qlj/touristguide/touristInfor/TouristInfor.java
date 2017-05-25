package com.example.qlj.touristguide.touristInfor;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.example.qlj.touristguide.R;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

/**
 * Created by Qljqian on 2017/3/28.
 */

public class TouristInfor extends AppCompatActivity {

    //SlidrConfig mSlidrConfig;
    //SlidrConfig.Builder mBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //取消状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.layout_specifyinfor);

        int primary = getResources().getColor(R.color.primaryDark);
        int secondary = getResources().getColor(R.color.secondaryDark);
        Slidr.attach(this, primary, secondary);
    }
}