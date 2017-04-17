package com.example.qlj.touristguide.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.TextViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.qlj.touristguide.Database_SQLite.MyDataBasehelp;
import com.example.qlj.touristguide.R;
import com.example.qlj.touristguide.TraceAnalysis.BarChartManager;
import com.example.qlj.touristguide.TraceAnalysis.DBScan;
import com.example.qlj.touristguide.TraceAnalysis.LocPoint;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarEntry;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Qlj on 2017/4/14.
 */

public class Fragment_Me extends Fragment implements View.OnClickListener {

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

    //定位点数据读取和聚类分析
    private ArrayList<LocPoint> points;
    private int[] eachTypeTotalPts;//..Pts[i]表示类别为i的点数量
    private long[] lastTimeStamp;//每一个类别都有开始结束时间戳
    private int[] eachTypeDuraTime;//..Time[i]表示类别i的轨迹总时间
    //private long[] newtimeStamp;
    private long interval = 660000;//系统设定中有每隔10分钟强制记录一条定位数据，
                                   // 如果同类别相邻点时间间隔大于11(min) x 60（sec） x 1000（mil）,
                                   //说明不是连续定位

    private DBScan dbScan;//DBScan方法实例
    private int radius = 50;//聚类半径
    private int minPts = 3;//半径内最少点数


    //UI界面
    Button bt_mytrace;
    protected BarChart mBarChart;
    ArrayList<String> xValues;
    ArrayList<BarEntry> yValues;
    private Context context;
    TextView tv_result;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDatabase();
        initSharedPreferences();
        initDBScan();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_tracemanager, container, false);
        initView(view);
        initChart();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    //初始化界面
    private void initView(View view)
    {
        bt_mytrace=(Button)view.findViewById(R.id.bt_mytrace);
        bt_mytrace.setOnClickListener(this);
        tv_result=(TextView)view.findViewById(R.id.edit_queryResult);
        mBarChart = (BarChart) view.findViewById(R.id.chart_bar);
    }

    //初始化barchart
    public void initChart()
    {
        //mBarChart.setDescription("专业情况(不含ICT)");
        xValues = new ArrayList<>();
        yValues = new ArrayList<>();
        BarChartManager.setUnit("单位：万户");
        BarChartManager.initBarChart(getActivity(),mBarChart,xValues,yValues);
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

    //初始化DBScan
    private void initDBScan()
    {
        points = new ArrayList<LocPoint>();//储存自己定位点
        dbScan = new DBScan(radius,minPts);//参数为半径和半径范围内最少点数量
    }

    //DBScan分析
    private void DBScanAnalysis()
    {
        now = new Date();//Data 封装当前时间
        cal = new GregorianCalendar();//标准日历
        cal.setTime(now);
        //可以根据需要设置时区
        // cal.setTimeZone(TimeZone.getDefault());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        //毫秒可根据系统需要清除或不清除
        cal.set(Calendar.MILLISECOND, 0);
        startTime = cal.getTimeInMillis();
        endTime = startTime + 24 * 3600 * 1000;

        //取出当天的定位数据
        Cursor cursor = db.rawQuery("SELECT * FROM location WHERE " +
                "Time >= ? and Time < ?",
                 new String[] { String.valueOf(startTime), String.valueOf(endTime) });
        if (cursor.moveToFirst())
        {
            do {
                int ID=cursor.getInt(cursor.getColumnIndex("ID"));
                Long timeStamp = cursor.getLong(cursor.getColumnIndex("Time"));
                Double lat=cursor.getDouble(cursor.getColumnIndex("Lat"));
                Double lon = cursor.getDouble(cursor.getColumnIndex("Lon"));
                points.add(new LocPoint(lat,lon,timeStamp));
            } while (cursor.moveToNext());
            cursor.close();
        }
        int typeNum = dbScan.process(points);//聚类，给每一个点都赋值类别，同时返回类别数
        eachTypeTotalPts = new int[typeNum];//..Pts[i]表示类别为i的点数量
        eachTypeDuraTime = new int[typeNum];//..Time[i]表示类别i的轨迹时间
        lastTimeStamp=new long[typeNum];//..TimeStamp[i]表示每个类别结束的时间戳
        for(int i=0;i<typeNum;i++)
            lastTimeStamp[i]=0;//时间戳初始化为0

        //遍历处理完成的点集
        for(LocPoint point : points)
        {
            eachTypeTotalPts[point.getCluster()-1]++;//统计每个类别的点数量
            eachTypeDuraTime[point.getCluster()-1]+=getDurationTime(point);//统计每个类别的总时间
            tv_result.append("纬度："+String.valueOf(point.getX())+"类别："+String.valueOf(point.getCluster())+"\n");
        }


        //准备barChart
        for(int size=0;size<eachTypeTotalPts.length;size++)
        {
            xValues.add("地点"+String.valueOf(size+1));
            yValues.add(new BarEntry(eachTypeTotalPts[size],size));
        }//
        BarChartManager.initBarChart(getActivity(),mBarChart,xValues,yValues);
    }

    //获得同类别相邻记录点时间间隔
    public long getDurationTime(LocPoint point)
    {
        if(lastTimeStamp[point.getCluster()-1] == 0)
        {
            //如果是类别i第一个点则初始化时间戳i，返回0
            lastTimeStamp[point.getCluster()-1] = point.getTimestamp();
        }else if(point.getTimestamp()- lastTimeStamp[point.getCluster()-1] -interval < 0){
            //同类别相邻点时间间隔正常，返回时间间隔
            lastTimeStamp[point.getCluster()-1] = point.getTimestamp();
            long duraTime=point.getTimestamp()- lastTimeStamp[point.getCluster()-1];
            return duraTime;
        }else {
            //如果相邻点时间间隔大于定位间隔值，说明是两段不同定位,返回0
            lastTimeStamp[point.getCluster()-1] = point.getTimestamp();
        }
        return 0;
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
        switch(id)
        {
            case R.id.bt_mytrace:
                DBScanAnalysis();
                break;
        }
    }//onClick

}


//int hour=loc2/60;
//int minute=loc2%60;

//绘制marker
//                        Marker marker = aMap.addMarker(new MarkerOptions()
//                                .position(new LatLng(Lat,Long))
//                                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
//                                        .decodeResource(getResources(),R.drawable.location)))
//                                .draggable(true));


