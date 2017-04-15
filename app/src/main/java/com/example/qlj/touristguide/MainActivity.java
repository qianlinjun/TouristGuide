package com.example.qlj.touristguide;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;

import java.util.Date;

import static com.amap.api.location.CoordinateConverter.calculateLineDistance;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, LocationSource, AMapLocationListener {
    //高德地图
    MapView mapView;
    AMap aMap;
    OnLocationChangedListener mListener;
    AMapLocationClient mlocationClient;
    AMapLocationClientOption mLocationOption;
    //sharedPreferences储存数据
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    public MyDataBasehelp dbHelper;
    public SQLiteDatabase db;
    public ContentValues value;

    //private AMapLocationClient locationClient = null;
    Button bt_touristInfor;
    Button bt_traceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //取消状态栏
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        initDatabase();

        mapView= (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        initMap();
        initView();
        initSharedPreferences();

    }//onCreate

    /*----------实现函数------------*/
    //初始化控件
    private void initView()
    {
        bt_touristInfor=(Button)findViewById(R.id.bt_touristInfor);
        bt_touristInfor.setOnClickListener(this);
        bt_traceManager=(Button)findViewById(R.id.bt_traceManager);
        bt_traceManager.setOnClickListener(this);
    }
    //初始化地图
    private void initMap()
    {
        aMap = mapView.getMap();
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.loc));// 设置小蓝点的图标
        //myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
        //myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        //myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        aMap.setLocationSource(this);// 设置定位监听
        aMap.setMyLocationEnabled(true);// true显示定位层并可触发定位，false隐藏定位层并不可触发定位，默认false
        aMap.setMyLocationStyle(myLocationStyle);// 定位模式，有定位、跟随或地图根旋转
        aMap.setMapType(AMap.MAP_TYPE_SATELLITE);// 卫星地图模式
    }
    //初始化数据库
    private void initDatabase()
    {
        dbHelper = new MyDataBasehelp(MainActivity.this, "user.db", null, 1);//构造dbHelper对象
        db = dbHelper.getWritableDatabase();
        value=new ContentValues();
    }
    //初始化SharedPreferences
    private void initSharedPreferences()
    {
        pref= getSharedPreferences("data",MODE_PRIVATE);
        editor = pref.edit();
    }

    //获得时间
    public long getTime(){
        Date d=new Date();
        long date=d.getDate();
        return date;
    }




    /*-------监听点击-------*/
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id)
        {
            case R.id.bt_touristInfor:
                Intent intent1 = new Intent(MainActivity.this,TouristInfor.class);
                startActivity(intent1);
                break;
            case R.id.bt_traceManager:
                Intent intent2 = new Intent(MainActivity.this,TraceManager.class);
                startActivity(intent2);
                break;
        }
    }//onClick



    /*-------高德定位-------*/

    //开始定位
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);//初始化定位
            mLocationOption = new AMapLocationClientOption();//初始化定位参数
           // mLocationOption.setMockEnable(true);
            mlocationClient.setLocationListener(this); //设置定位回调监听
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);//设置定位模式
            mlocationClient.setLocationOption(mLocationOption);//设置定位参数
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mLocationOption.setInterval(10000);
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

                if(distance_precur > 20)
                {
//                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                Date date = new Date(amapLocation.getTime());
//                df.format(date);//定位时间
                    //SQLite Database 将定位数据储存在数据库
                    value.put("Time", getTime());
                    value.put("Lat",lat_cur);
                    value.put("Lon",lon_cur);
                    value.put("val1", 0);
                    value.put("val2", 0);
                    db.insert("location", null, value);
                    value.clear();

                    //sharedPreferences 储存上一次定位数据
                    editor.putString("Lat", String.valueOf(lat_cur));
                    editor.putString("Lon", String.valueOf(lon_cur));
                    editor.commit();
                }//if distance

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



    /*------------System override------------------*/
    @Override
    protected void onStart() {
        super.onStart();
        Log.e("map","onStart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();//销毁地图
        if(null != mlocationClient){
            mlocationClient.onDestroy();
        }//销毁定位对象
        Log.e("map","onDestroy");
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();//重新绘制加载地图
        Log.e("map","onResume");
    }
    @Override
    protected void onPause() {
        super.onPause();
        //mapView.onPause();//暂停地图的绘制
        Log.e("map","onPause");
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);//保存地图当前的状态
    }
}
