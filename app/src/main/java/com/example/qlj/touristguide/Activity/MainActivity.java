package com.example.qlj.touristguide.Activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;

import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.qlj.touristguide.Fragment.FragAdapter;
import com.example.qlj.touristguide.Fragment.Fragment_Map;
import com.example.qlj.touristguide.Fragment.Fragment_Me;
import com.example.qlj.touristguide.Fragment.Fragment_SightseeingList;
import com.example.qlj.touristguide.Fragment.Fragment_Share;
import com.example.qlj.touristguide.R;
import com.example.qlj.touristguide.TraceManager.DBScan.DBScanService;
import com.example.qlj.touristguide.TraceManager.StepDetector.StepService;


import java.util.ArrayList;
import java.util.List;


public class MainActivity extends FragmentActivity implements RadioGroup.OnCheckedChangeListener, ViewPager.OnPageChangeListener{

    private RadioGroup mRadioGroup;
    //viewPage+Fragment
    private ViewPager viewPager;
    List<Fragment> fragmentList;
    private FragAdapter adapter;
    private Fragment_Map frag1;//地图
    private Fragment_SightseeingList frag2;//旅游景点
    private Fragment_Share frag3;//共享
    private Fragment_Me frag4;//账户管理
    private int currenttab=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.layout_activitymain);
        initView();
        try{
            Intent intent_DBScan=new Intent(MainActivity.this, DBScanService.class);
            startService(intent_DBScan);
            Intent intent_StepDetec = new Intent(MainActivity.this, StepService.class);
            startService(intent_StepDetec);
        }catch (Exception e){
            Log.d("MainActivity_",e.toString());
        }

        int page = this.getIntent().getIntExtra("page",5);//默认为5（不存在）
        if(page < 5) viewPager.setCurrentItem(page);
    }//onCreate

    /*----------实现函数------------*/
    //初始化pageview和fragment
    private void initView()
    {
        mRadioGroup=(RadioGroup)findViewById(R.id.radioGroup);
        mRadioGroup.setOnCheckedChangeListener(this);
        fragmentList=new ArrayList<Fragment>();
        frag1=new Fragment_Map();//地图Fragment
        frag2=new Fragment_SightseeingList();//旅游景点Fragment
        frag3=new Fragment_Share();//照片日记Fragment
        frag4=new Fragment_Me();
        fragmentList.add(frag1);
        fragmentList.add(frag2);
        fragmentList.add(frag3);
        fragmentList.add(frag4);
        adapter=new FragAdapter(getSupportFragmentManager(), fragmentList);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(this);

        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        ((TextView) findViewById(R.id.title_text)).setText("旅游助手");
    }


    //底部radiobutton没选中和选中显示的图标
    private int[] unselectedIconIds = {
            R.drawable.icon_umap_40,
            R.drawable.icon_utourism_40,
            R.drawable.icon_ushare_40,
            R.drawable.icon_umanager_40
    };
    private int[] selectedIconIds = {
            R.drawable.icon_map_40,
            R.drawable.icon_tourism_40,
            R.drawable.icon_share_40,
            R.drawable.icon_manager_40
    };

    String[] titleText={"旅游助手","发现景点","分享精彩","我"};
    //radio button切换效果
    private void selectPage(int position) {
        // 将所有的tab的icon变成灰色的
        for (int i = 0; i < mRadioGroup.getChildCount(); i++) {
            Drawable gray = getResources().getDrawable(unselectedIconIds[i]);
            // 不能少，少了不会显示图片
            gray.setBounds(0, 0, gray.getMinimumWidth(),
                    gray.getMinimumHeight());
            RadioButton child = (RadioButton) mRadioGroup.getChildAt(i);
            child.setCompoundDrawables(null, gray, null, null);
            child.setTextColor(getResources().getColor(R.color.divider));
        }
        // 切换页面
        viewPager.setCurrentItem(position, false);
        // 改变图标
        Drawable yellow = getResources().getDrawable(selectedIconIds[position]);
        yellow.setBounds(0, 0, yellow.getMinimumWidth(),
                yellow.getMinimumHeight());
        RadioButton select = (RadioButton) mRadioGroup.getChildAt(position);
        select.setCompoundDrawables(null, yellow, null, null);
        select.setTextColor(getResources().getColor(R.color.primaryDark));

        ((TextView) findViewById(R.id.title_text)).setText(titleText[position]);
    }


    //RadioGroup 按钮点击事件监听
    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        switch (checkedId) {
            case R.id.bt_map: // map radiobt
                selectPage(0);
                break;
            case R.id.bt_touristinfor: // 景点 radiobt
                selectPage(1);
                break;
            case R.id.bt_share: // 分享 radiobt
                selectPage(2);
                break;
            case R.id.btn_me: // 个人中心 radiobt
                selectPage(3);
                break;
        }
    }


    //viewpage页面改变监听
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        selectPage(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return super.onMenuItemSelected(featureId, item);
    }


    /*------------System override------------------*/
    @Override
    protected void onStart() {
        super.onStart();
        Log.e("main","onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        int page = this.getIntent().getIntExtra("page",5);//默认为5（不存在）
        if(page < 5) viewPager.setCurrentItem(page);
        Log.e("main","onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("main","onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("main","onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("main","onDestroy");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
