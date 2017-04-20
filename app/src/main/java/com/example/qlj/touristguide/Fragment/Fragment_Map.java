package com.example.qlj.touristguide.Fragment;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

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
import com.example.qlj.touristguide.Services.DBScanService;
import com.example.qlj.touristguide.TraceAnalysis.DBScan;
import com.example.qlj.touristguide.TraceAnalysis.LocPoint;
import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;


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
//    //定位点集 for dbscan analysi
//    private DBScan dbScan;//方法集
//    private int radius = 50;//聚类半径
//    private int minPts = 3;//半径内最少点数


    private ArrayList<LocPoint> dbPoints;//用于聚类
    private ArrayList<LatLng> locPoints=new ArrayList<LatLng>();//用于显示轨迹


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
        Log.d("map","onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("map","onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        Log.d("map","onDestroy");
    }
    @Override
    public void onResume() {
        super.onResume();
        //mapView.onResume();
        Log.d("map","onResume");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // mapView.onSaveInstanceState(outState);//保存地图当前的状态
    }

    /*----------实现函数------------*/
    //初始化控件
    private void initView(final View view)
    {
        //景点、轨迹点、轨迹
        FloatingActionButton fabbt_tourismPoint = (FloatingActionButton) view.findViewById(R.id.fabbutton_1);
        FloatingActionButton fabbt_TraceLine = (FloatingActionButton) view.findViewById(R.id.fabbutton_2);
        FloatingActionButton fabbt_ptsCluster = (FloatingActionButton) view.findViewById(R.id.fabbutton_3);
        fabbt_tourismPoint.setOnClickListener(this);//景点
        fabbt_TraceLine.setOnClickListener(this);//轨迹
        fabbt_ptsCluster.setOnClickListener(this);//轨迹点
    }



    //初始化地图
    private void initMap()
    {
        aMap = mapView.getMap();
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_loc1_128));// 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));// 设置圆形的填充颜色
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
        dbPoints = DBScanService.points;//储存自己定位点
        try {
            for (LocPoint p : dbPoints)
                locPoints.add(new LatLng(p.getX(), p.getY()));
        }catch (Exception e){
            Log.d("Fragment_Map",e.toString());
        }

        aMap.addPolyline((new PolylineOptions())
                .addAll(locPoints)
                .geodesic(true)
                .color(Color.BLUE));
    }

//    int[] color={R.color.一,R.color.二,R.color.三,R.color.四,R.color.五,R.color.六};
    //dbscan聚类
    public void dbscan()
    {
        dbPoints = DBScanService.points;//储存自己定位点
        for(LocPoint p : dbPoints)
        {
            int i=p.getCluster();
            if(i != 0)
            {
                aMap.addCircle(new CircleOptions()
                        .center(new LatLng(p.getX(),p.getY()))
                        .radius(5)
                        .strokeColor(R.color.lightcoral_)
                        .fillColor(R.color.lightcoral_));
//.strokeColor(color[p.getCluster()])
//                .fillColor(color[p.getCluster()]))
            }

        }
    }//dbscan



    /*-------监听点击-------*/
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id)
        {
            case R.id.fabbutton_2:
                showLocation();//展示轨迹
                break;
            case R.id.fabbutton_3:
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

                //考虑高德和手机设备存在的定位精度误差，如果先后两次定位距离超过25米记录一条数据，另外系统每隔20min强制记录一条数据
                if(distance_precur > 40 || ((minute%20 == 0) && (second>0 && second<10)))
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


                    aMap.addCircle(new CircleOptions()
                            .center(new LatLng(lat_cur,lon_cur))
                            .radius(5)
                            .strokeColor(R.color.lightcoral_));
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
