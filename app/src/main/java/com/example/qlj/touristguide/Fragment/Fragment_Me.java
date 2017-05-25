package com.example.qlj.touristguide.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.example.qlj.touristguide.Activity.MainActivity;
import com.example.qlj.touristguide.TraceManager.StepDetector.StepDetector;
import com.example.qlj.touristguide.databaseSQLite.MyDataBasehelp;
import com.example.qlj.touristguide.R;
import com.example.qlj.touristguide.TraceManager.DBScan.DBScanService;
import com.example.qlj.touristguide.TraceManager.Chart.BarChartManager;
import com.example.qlj.touristguide.TraceManager.DBScan.DBScan;
import com.example.qlj.touristguide.TraceManager.DBScan.LocPoint;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Qlj on 2017/4/14.
 */

public class Fragment_Me extends Fragment implements View.OnClickListener {

    //UI界面
    private TextView tv_weather;//天气
    private TextView tv_attract;//兴趣设置
    private boolean isTvAttractPressed;
    private LinearLayout layout_attract;//显示个人兴趣按钮
    private TextView hide_1;private boolean isChoosed1;//历史文化
    private TextView hide_2;private boolean isChoosed2;//博物展览
    private TextView hide_3;private boolean isChoosed3;//大学校园
    private TextView hide_4;private boolean isChoosed4;//户外自然
    private TextView hide_5;private boolean isChoosed5;//娱乐饮食

    private TextView bt_mytrace;//轨迹管理
    private TextView tv_stepAccount;//显示步数
    //BarChart
    private BarChart mBarChart;
    private ArrayList<String> xValues;
    private ArrayList<BarEntry> yValues;


    //数据库读取定位数据
    private MyDataBasehelp dbHelper;
    private SQLiteDatabase db;
    private Date now;//按时间查询
    private GregorianCalendar cal;//日历
    private long startTime;//查询开始时间
    private long endTime;//查询结束时间

    //sharedPreferences储存数据
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    //饼图显示轨迹统计信息
    private PieChart mChart;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSharedPreferences();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_me, container, false);
        initView(view);
        initChart(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        editor.putBoolean("history", isChoosed1);
        editor.putBoolean("museum", isChoosed2);
        editor.putBoolean("campus", isChoosed3);
        editor.putBoolean("nature", isChoosed4);
        editor.putBoolean("recreation", isChoosed5);
        editor.commit();
    }

    //初始化界面
    private void initView(View view)
    {
        //天气
        tv_weather=(TextView)view.findViewById(R.id.tv_weather);
        tv_weather.setOnClickListener(this);
        //兴趣设置
        tv_attract=(TextView)view.findViewById(R.id.tv_attract);
        tv_attract.setOnClickListener(this);

        //兴趣设置布局
        layout_attract = (LinearLayout) view.findViewById(R.id.ll_hide);
        layout_attract.setVisibility(View.GONE);//这一句即隐藏布局LinearLayout区域
        hide_1=(TextView)view.findViewById(R.id.hide_1);//历史文化
        hide_2=(TextView)view.findViewById(R.id.hide_2);//博物展览
        hide_3=(TextView)view.findViewById(R.id.hide_3);//大学校园
        hide_4=(TextView)view.findViewById(R.id.hide_4);//户外自然
        hide_5=(TextView)view.findViewById(R.id.hide_5);//娱乐饮食
        hide_1.setOnClickListener(this);isChoosed1 = pref.getBoolean("history", false);
        hide_2.setOnClickListener(this);isChoosed2 = pref.getBoolean("museum", false);
        hide_3.setOnClickListener(this);isChoosed3 = pref.getBoolean("campus", false);
        hide_4.setOnClickListener(this);isChoosed4 = pref.getBoolean("nature", false);
        hide_5.setOnClickListener(this);isChoosed5 = pref.getBoolean("recreation", false);
        if(isChoosed1)
            hide_1.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        if(isChoosed2)
            hide_2.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        if(isChoosed3)
            hide_3.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        if(isChoosed4)
            hide_4.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        if(isChoosed5)
            hide_5.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        //轨迹管理
        bt_mytrace=(TextView)view.findViewById(R.id.tv_traceManager);
        bt_mytrace.setOnClickListener(this);
        //显示计步
        tv_stepAccount=(TextView)view.findViewById(R.id.tv_stepaccount2);
        tv_stepAccount.setText(String.valueOf(StepDetector.CURRENT_SETP)+"步");
        tv_stepAccount.setOnClickListener(this);
        isTvAttractPressed = false;
    }

    //初始化barchart
    public void initChart(View view)
    {
        //mBarChart.setDescription("专业情况(不含ICT)");
        //xValues = new ArrayList<String>();
        //yValues = new ArrayList<BarEntry>();
        //BarChartManager.setUnit("单位：万户");
        //BarChartManager.initBarChart(getActivity(),mBarChart,xValues,yValues);
        mChart = (PieChart) view.findViewById(R.id.spread_pie_chart);
        PieData mPieData = getPieData(4, 100);
        showChart(mChart, mPieData);
    }

    private void showChart(PieChart pieChart, PieData pieData) {
        //pieChart.setHoleColorTransparent(true);

        pieChart.setHoleRadius(60f);  //半径
        pieChart.setTransparentCircleRadius(64f); // 半透明圈
        //pieChart.setHoleRadius(0)  //实心圆

        //pieChart.setDescription("测试饼状图");

        // mChart.setDrawYValues(true);
        pieChart.setDrawCenterText(true);  //饼状图中间可以添加文字

        pieChart.setDrawHoleEnabled(true);

        pieChart.setRotationAngle(90); // 初始旋转角度

        // draws the corresponding description value into the slice
        // mChart.setDrawXValues(true);

        // enable rotation of the chart by touch
        pieChart.setRotationEnabled(true); // 可以手动旋转

        // display percentage values
        pieChart.setUsePercentValues(true);  //显示成百分比
        // mChart.setUnit(" €");
        // mChart.setDrawUnitsInChart(true);

        // add a selection listener
//      mChart.setOnChartValueSelectedListener(this);
        // mChart.setTouchEnabled(false);

//      mChart.setOnAnimationListener(this);

        pieChart.setCenterText("轨迹统计");  //饼状图中间的文字

        //设置数据
        pieChart.setData(pieData);

        // undo all highlights
//      pieChart.highlightValues(null);
//      pieChart.invalidate();

        Legend mLegend = pieChart.getLegend();  //设置比例图
        mLegend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);  //最右边显示
//      mLegend.setForm(LegendForm.LINE);  //设置比例图的形状，默认是方形
        mLegend.setXEntrySpace(7f);
        mLegend.setYEntrySpace(5f);

        pieChart.animateXY(1000, 1000);  //设置动画
        // mChart.spin(2000, 0, 360);
    }

    /**
     *
     * @param count 分成几部分
     * @param range
     */
    private PieData getPieData(int count, float range) {

        ArrayList<String> xValues = new ArrayList<String>();  //xVals用来表示每个饼块上的内容

        for (int i = 0; i < count; i++) {
            xValues.add("Quarterly" + (i + 1));  //饼块上显示成Quarterly1, Quarterly2, Quarterly3, Quarterly4
        }

        ArrayList<PieEntry> yValues = new ArrayList<PieEntry>();  //yVals用来表示封装每个饼块的实际数据

        // 饼图数据
        /**
         * 将一个饼形图分成四部分， 四部分的数值比例为14:14:34:38
         * 所以 14代表的百分比就是14%
         */
        float quarterly1 = 14;
        float quarterly2 = 14;
        float quarterly3 = 34;
        float quarterly4 = 38;

        yValues.add(new PieEntry(quarterly1, 0));
        yValues.add(new PieEntry(quarterly2, 1));
        yValues.add(new PieEntry(quarterly3, 2));
        yValues.add(new PieEntry(quarterly4, 3));

        //y轴的集合
        PieDataSet pieDataSet = new PieDataSet(yValues, "Quarterly Revenue 2014");/*显示在比例图上*/
        pieDataSet.setSliceSpace(0f); //设置个饼状图之间的距离

        ArrayList<Integer> colors = new ArrayList<Integer>();

        // 饼图颜色
        colors.add(Color.rgb(205, 205, 205));
        colors.add(Color.rgb(114, 188, 223));
        colors.add(Color.rgb(255, 123, 124));
        colors.add(Color.rgb(57, 135, 200));

        pieDataSet.setColors(colors);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float px = 5 * (metrics.densityDpi / 160f);
        pieDataSet.setSelectionShift(px); // 选中态多出的长度

        PieData pieData = new PieData(pieDataSet);

        return pieData;
    }



    //初始化数据库
    private void initDatabase()
    {
        dbHelper = new MyDataBasehelp(getActivity(), "user.db", null, 1);//构造dbHelper对象
        db = dbHelper.getWritableDatabase();
    }


    //初始化SharedPreferences
    private void initSharedPreferences()
    {
        pref= getActivity().getSharedPreferences("data",MODE_PRIVATE);
        editor = pref.edit();
    }


    //writeData To buffer 提高数据读取效率
    public static void writeData(ArrayList<LocPoint> points,String path) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(path));
            for (LocPoint point:points) {
                bw.write(point.toString()+"\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //按钮点击事件监听
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id) {
            case R.id.tv_weather:
                PackageManager packageManager = getContext().getPackageManager();
                Intent intent1 = new Intent();
                intent1 = packageManager.getLaunchIntentForPackage("com.oppo.weather");
                startActivity(intent1);
                break;

            /*------------兴趣设置-------------*/
            case R.id.tv_attract://显示布局LinearLayout区域
                if (!isTvAttractPressed) {
                    layout_attract.setVisibility(View.VISIBLE);
                    isTvAttractPressed = true;
                } else {
                    layout_attract.setVisibility(View.GONE);
                    isTvAttractPressed = false;
                }
                break;

            case R.id.hide_1://历史文化
                if (isChoosed1) {
                    isChoosed1 = false;
                    hide_1.setBackgroundColor(getResources().getColor(R.color.white));
                } else {
                    isChoosed1 = true;
                    hide_1.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }

                break;
            case R.id.hide_2://博物展览
                if (isChoosed2) {
                    isChoosed2 = false;
                    hide_2.setBackgroundColor(getResources().getColor(R.color.white));
                } else {
                    isChoosed2 = true;
                    hide_2.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
                break;
            case R.id.hide_3://大学校园
                if (isChoosed3) {
                    isChoosed3 = false;
                    hide_3.setBackgroundColor(getResources().getColor(R.color.white));
                } else {
                    isChoosed3 = true;
                    hide_3.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
                break;
            case R.id.hide_4://户外自然
                if (isChoosed4) {
                    isChoosed4 = false;
                    hide_4.setBackgroundColor(getResources().getColor(R.color.white));
                } else {
                    isChoosed4 = true;
                    hide_4.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
                break;
            case R.id.hide_5://娱乐饮食
                if (isChoosed5) {
                    isChoosed5 = false;
                    hide_5.setBackgroundColor(getResources().getColor(R.color.white));
                } else {
                    isChoosed5 = true;
                    hide_5.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
                break;

            case R.id.bt_mytrace:
                // tv_result.setText((int) MainActivity.mCount);
                break;
            case R.id.tv_stepaccount2:
                tv_stepAccount.setText(String.valueOf(StepDetector.CURRENT_SETP)+"步");
                break;
        }
    }//onClick

}

