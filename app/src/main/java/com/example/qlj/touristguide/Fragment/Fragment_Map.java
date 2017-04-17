package com.example.qlj.touristguide.Fragment;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.DPoint;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CircleOptions;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.maps2d.model.PolylineOptions;
import com.example.qlj.touristguide.Activity.MainActivity;
import com.example.qlj.touristguide.Database_SQLite.MyDataBasehelp;
import com.example.qlj.touristguide.R;
import com.example.qlj.touristguide.TraceAnalysis.DBScan;
import com.example.qlj.touristguide.TraceAnalysis.LocPoint;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static android.content.Context.MODE_PRIVATE;
import static com.amap.api.location.CoordinateConverter.calculateLineDistance;

/**
 * Created by Qljqian on 2017/4/15.
 */

public class Fragment_Map extends Fragment implements View.OnClickListener, LocationSource, AMapLocationListener {

    //UI界面
    private Button bt_TraceLine;
    private Button bt_pointsCluster;

    //高德地图
    MapView mapView;
    AMap aMap;
    LocationSource.OnLocationChangedListener mListener;
    AMapLocationClient mlocationClient;
    AMapLocationClientOption mLocationOption;
    public final int drawRadius = 5;
    //sharedPreferences储存数据
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    //数据库查询分析
    private MyDataBasehelp dbHelper;
    private SQLiteDatabase db;
    private ContentValues value;
    //日期和时间数据
    Date date;
    long time;

    private Date now;//按时间查询
    private GregorianCalendar cal;//日历
    private long startTime;//查询开始时间
    private long endTime;//查询结束时间
    //定位点集 for dbscan analysi
    private DBScan dbScan;//方法集
    private int radius = 50;//聚类半径
    private int minPts = 3;//半径内最少点数
    private ArrayList<LatLng> points;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDatabase();
        initSharedPreferences();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.layout_map, container, false);
        initView(view);
        mapView= (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        initMap();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        Log.e("map","onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e("map","onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        Log.e("map","onDestroy");
    }
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        Log.e("map","onResume");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // mapView.onSaveInstanceState(outState);//保存地图当前的状态
    }

    /*----------实现函数------------*/
    //初始化控件
    private void initView(View view)
    {
        bt_TraceLine=(Button)view.findViewById(R.id.bt_traceLine);
        bt_TraceLine.setOnClickListener(this);
        bt_pointsCluster=(Button)view.findViewById(R.id.bt_pointsCluster);
        bt_pointsCluster.setOnClickListener(this);
    }
    //初始化地图
    private void initMap()
    {
        aMap = mapView.getMap();
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_loc1_128));// 设置小蓝点的图标
        //myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
        //myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        //myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        aMap.setLocationSource(this);// 设置定位监听
        aMap.setMyLocationEnabled(true);// true显示定位层并可触发定位，false隐藏定位层并不可触发定位，默认false
        aMap.setMyLocationStyle(myLocationStyle);// 定位模式，有定位、跟随或地图根旋转
        //aMap.setMapType(AMap.MAP_TYPE_SATELLITE);// 卫星地图模式
    }
    //初始化数据库
    private void initDatabase()
    {
        dbHelper = new MyDataBasehelp(getActivity(), "user.db", null, 1);//构造dbHelper对象
        db = dbHelper.getWritableDatabase();
        value=new ContentValues();
        points = new ArrayList<LatLng>();//储存自己定位点
        dbScan = new DBScan(radius,minPts);//参数为半径和半径范围内最少点数量
    }
    //初始化SharedPreferences
    private void initSharedPreferences()
    {
        pref= getActivity().getSharedPreferences("data",MODE_PRIVATE);
        editor = pref.edit();
    }
    //获得时间
    public long getTime(){
        date = new Date();
        time = date.getTime();
        return time;
    }
    //显示历史轨迹
    public void showLocation(){
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
                Double lat=cursor.getDouble(cursor.getColumnIndex("Lat"));
                Double lon = cursor.getDouble(cursor.getColumnIndex("Lon"));
                //绘制marker
                aMap.addCircle(new CircleOptions()
                        .center(new LatLng(lat,lon))
                        .radius(drawRadius) //半径
                        .fillColor(Color.RED) //里面的颜色
                        .strokeColor(Color.RED)); //边框的颜色
                points.add(new LatLng(lat,lon));
            } while (cursor.moveToNext());
            cursor.close();
        }
        aMap.addPolyline((new PolylineOptions())
                .addAll(points)
                .geodesic(true).color(Color.BLUE));
    }

    //dbscan聚类
    public void dbscan()
    {
        //多线程
        Date now = new Date();//Data 封装当前时间
        GregorianCalendar cal = new GregorianCalendar();//标准日历
        cal.setTime(now);
        //可以根据需要设置时区
        // cal.setTimeZone(TimeZone.getDefault());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        //毫秒可根据系统需要清除或不清除
        cal.set(Calendar.MILLISECOND, 0);
        long startTime = cal.getTimeInMillis();
        long endTime = startTime + 24 * 3600 * 1000;

        Cursor cursor = db.rawQuery("SELECT * FROM location WHERE " +
                        "Time >= ? and Time < ?",
                new String[] { String.valueOf(startTime), String.valueOf(endTime) });
        if (cursor.moveToFirst())
        {
            do {
                int ID=cursor.getInt(cursor.getColumnIndex("ID"));
                Long TimeStamp = cursor.getLong(cursor.getColumnIndex("Time"));
                Double Lat=cursor.getDouble(cursor.getColumnIndex("Lat"));
                Double Lon = cursor.getDouble(cursor.getColumnIndex("Lon"));
                //points.add(new LocPoint(Lat,Lon,TimeStamp));
            } while (cursor.moveToNext());
            cursor.close();
        }

//        for(LocPoint p : points)
//        {
//            if (p.getCluster()==1)
//                aMap.addCircle(new CircleOptions()
//                        .center(new LatLng(p.getX(),p.getY()))
//                        .radius(10)
//                        .fillColor(R.color.二));
//            if (p.getCluster()==2)
//                aMap.addCircle(new CircleOptions()
//                        .center(new LatLng(p.getX(),p.getY()))
//                        .radius(10)
//                        .fillColor(R.color.三));
//        }
    }


    /*-------监听点击-------*/
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id)
        {
            case R.id.bt_traceLine:
                showLocation();//展示轨迹
                break;
            case R.id.bt_pointsCluster:
                dbscan();
                break;
        }
    }//onClick



    /*-------高德定位-------*/
    //开始定位
    @Override
    public void activate(LocationSource.OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(getActivity());//初始化定位
            mLocationOption = new AMapLocationClientOption();//初始化定位参数
            // mLocationOption.setMockEnable(true);
            mlocationClient.setLocationListener(this); //设置定位回调监听
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);//设置定位模式
            mlocationClient.setLocationOption(mLocationOption);//设置定位参数
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除

            mLocationOption.setInterval(5000);//每5秒定位一次
            mlocationClient.startLocation();//启动定位

        }
    }//activate


    //定位改变触发事件
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点

                double lat_pre,lon_pre,lat_cur,lon_cur;
                DPoint oldLatlng,newLatlng;
                double distance_precur;
                lat_pre = Double.valueOf(pref.getString("Lat", "0"));//100默认值
                lon_pre = Double.valueOf(pref.getString("Lon", "0"));//100默认值
                lat_cur = aMapLocation.getLatitude();
                lon_cur = aMapLocation.getLongitude();

                oldLatlng = new DPoint(lat_pre,lon_pre);
                newLatlng = new DPoint(lat_cur,lon_cur);
                distance_precur = calculateLineDistance(oldLatlng, newLatlng);

                Time t=new Time("GMT+8"); // or Time t=new Time("GMT+8"); 加上Time Zone资料。
                t.setToNow(); // 取得系统时间。
                int minute = t.minute;
                int second = t.second;

                //考虑高德和手机设备存在的定位精度误差，如果先后两次定位距离超过25米或者每隔10min强制记录数据
                if(distance_precur > 20 || ((minute%10 == 0) && (second>0 && second<10)))
                {
                    value.put("Time", getTime());
                    value.put("Lat",lat_cur);
                    value.put("Lon",lon_cur);
                    value.put("val1", 0);
                    value.put("val2", 0);
                    db.insert("location", null, value);
                    value.clear();

                    //sharedPreferences 储存上一次成功定位数据
                    editor.putString("Lat", String.valueOf(lat_cur));
                    editor.putString("Lon", String.valueOf(lon_cur));
                    editor.commit();
                }
            } else {
                Log.e("AmapErr","定位失败," + aMapLocation.getErrorCode()+ ": " + aMapLocation.getErrorInfo());
            }
        }
    }//onLocationChanged


    //停止定位
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }//deactivate


}
