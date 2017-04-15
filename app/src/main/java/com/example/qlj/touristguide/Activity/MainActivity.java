package com.example.qlj.touristguide.Activity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.DPoint;
import com.amap.api.maps2d.LocationSource;
import com.example.qlj.touristguide.Database_SQLite.MyDataBasehelp;
import com.example.qlj.touristguide.Fragment.FragAdapter;
import com.example.qlj.touristguide.Fragment.Fragment_Map;
import com.example.qlj.touristguide.Fragment.Fragment_SightseeingList;
import com.example.qlj.touristguide.Fragment.Fragment_TraceManager;
import com.example.qlj.touristguide.R;
import com.example.qlj.touristguide.TraceAnalysis.DBScan;
import com.example.qlj.touristguide.TraceAnalysis.LocPoint;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static com.amap.api.location.CoordinateConverter.calculateLineDistance;

public class MainActivity extends FragmentActivity{

    //viewPage+Fragment
    private ViewPager viewPager;
    List<Fragment> fragmentList;
    private FragAdapter adapter;
    Fragment_Map frag1;
    Fragment_SightseeingList frag2;
    Fragment_TraceManager frag3;
    int currenttab=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//取消状态栏
        setContentView(R.layout.layout_activitymain);
        initView();
    }//onCreate

    /*----------实现函数------------*/
    //初始化pageview和fragment
    private void initView()
    {
        fragmentList=new ArrayList<Fragment>();
        frag1=new Fragment_Map();//地图Fragment
        frag2=new Fragment_SightseeingList();//旅游景点Fragment
        frag3=new Fragment_TraceManager();//轨迹管理Fragment
        fragmentList.add(frag1);
        fragmentList.add(frag2);
        fragmentList.add(frag3);
        adapter=new FragAdapter(getSupportFragmentManager(), fragmentList);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
    }


    /*------------System override------------------*/
    @Override
    protected void onStart() {
        super.onStart();
        Log.e("map","onStart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
       // mapView.onSaveInstanceState(outState);//保存地图当前的状态
    }
}
