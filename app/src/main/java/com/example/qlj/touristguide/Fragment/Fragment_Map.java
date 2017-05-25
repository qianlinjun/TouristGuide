package com.example.qlj.touristguide.Fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.example.qlj.touristguide.TraceManager.DBScan.DBScanService;
import com.example.qlj.touristguide.touristInfor.InfoWinAdapter;
import com.getbase.floatingactionbutton.FloatingActionButton;


import com.example.qlj.touristguide.databaseSQLite.MyDataBasehelp;
import com.example.qlj.touristguide.R;
import com.example.qlj.touristguide.TraceManager.DBScan.LocPoint;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.widgets.CompassView;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.services.Constants;
import com.mapbox.services.api.ServicesException;
import com.mapbox.services.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.FeatureCollection;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;

import com.mapbox.services.api.directions.v5.DirectionsCriteria;
import com.mapbox.services.api.directions.v5.MapboxDirections;
import com.mapbox.services.api.directions.v5.models.DirectionsResponse;
import com.mapbox.services.api.directions.v5.models.DirectionsRoute;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.ContentValues.TAG;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Qljqian on 2017/4/15.
 */

public class Fragment_Map extends Fragment implements View.OnClickListener, OnMapReadyCallback{
    //UI界面
    private EditText ed_searchLoc;//查询路线
    private Button bt_search;//搜索按钮
    private FloatingActionButton fabbt_tourismLine;
    private FloatingActionButton fabbt_Trace;
    private FloatingActionButton fabbt_ptsCluster;
    private boolean is_fabbt_tourismLineClicked=false;
    private boolean is_fabbt_TraceClicked=false;
    private boolean is_fabbt_ptsClusterClicked=false;

    //mapBox地图
    private MapView mapView;
    private MapboxMap map;
    private DirectionsRoute currentRoute;
    private CompassView cp;
    private Source geoJsonSource;//资源
    private LineLayer lineLayer_trace = null;//显示自己
    public static String[] array_Name;//储存景点名称
    public static String[] array_Hot;//储存景点热度
    public static String[] array_Ticket;//储存景点票价

    public static double target_lat,target_lng;//目标经纬度

    private String[] array_Lat;//储存景点纬度
    private String[] array_Lng;//储存景点经度
    public static HashMap<String, String> hashMap = new HashMap<String, String>();//储存景点/ID hashmap


    //路线Services API
    private Position origin;
    private Position destination;

    //定位
    private LocationManager locationManager;
    private IconFactory iconFactory;
    private double lat_pre,lon_pre,lat_cur,lon_cur;
    private Icon locIcon;//定位
    private Icon traceIcon;//足迹
    private Icon pictureIcon;//地图上显示图片
    private final int minDist = 5;
    private final int minTime = 5000;

    public final int drawRadius = 5;
    //sharedPreferences储存数据
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    //数据库
    private MyDataBasehelp dbHelper;
    private SQLiteDatabase db;
    private ContentValues value;
    //日期和时间数据
    private Date date;
    private long time;
    private static final  double EARTH_RADIUS = 6371.004;//赤道半径(单位m)

    private ArrayList<LocPoint> dbPoints;//用于聚类点可视化
    private List<Position> routeCoordinates;//用于在地图上显示轨迹


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getContext(), getString(R.string.access_token));
        initDatabase();
        initSharedPreferences();
        //获得景点名称和坐标信息
        array_Name=getResources().getStringArray(R.array.array_touristNames);//名称
        array_Hot=getResources().getStringArray(R.array.array_hot);//热度
        array_Ticket=getResources().getStringArray(R.array.array_ticket);//票价
        array_Lat=getResources().getStringArray(R.array.array_latitude);
        array_Lng=getResources().getStringArray(R.array.array_longitude);
        iconFactory = IconFactory.getInstance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.layout_map, container, false);
        initView(view);
        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;
        //cp.setMapboxMap(map);
        //cp.setEnabled(false);
        //显示定位
        map.setMyLocationEnabled(true);
        // Customize the user location icon using the getMyLocationViewSettings object.
        map.getMyLocationViewSettings().setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_loc1_40),new int[] {0, 0, 0, 0});;
        map.getMyLocationViewSettings().setForegroundTintColor(Color.parseColor("#2256B881"));

        //地图添加景点图标
        for(int i=0;i<array_Name.length;i++)
        {
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.valueOf(array_Lat[i]),Double.valueOf(array_Lng[i])))
                    .title(array_Name[i]));

            //用hashmap储存旅游景点名称和id
            hashMap.put(array_Name[i],String.valueOf(i));
        }

        //marker适配器
        InfoWinAdapter adapter =new InfoWinAdapter(getContext());
        map.setInfoWindowAdapter(adapter);

        //通过android自带GPS获得位置，并实现储存
        initLocation();

        //添加自己的拍照
        try {
            if (null != Fragment_Share.imagePath)
                addImageToMap(Fragment_Share.imagePath);//添加自己的照片
        }catch(Exception e)
        {
            Log.d("addError",e.toString());
        }
    }



    /*----------实现函数------------*/
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
        editor.putString("host", "10.133.165.211");//服务器地址
        editor.putString("port", "21568");//端口
        editor.commit();
    }
    //初始化UI
    private void initView(final View view)
    {
        ed_searchLoc=(EditText) view.findViewById(R.id.ed_searchLoc);
        bt_search=(Button)view.findViewById(R.id.bt_search);
        bt_search.setOnClickListener(this);
        //旅游路线、足迹、统计、
        fabbt_tourismLine = (FloatingActionButton) view.findViewById(R.id.fabbutton_1);
        fabbt_Trace = (FloatingActionButton) view.findViewById(R.id.fabbutton_2);
        fabbt_ptsCluster = (FloatingActionButton) view.findViewById(R.id.fabbutton_3);
        fabbt_tourismLine.setOnClickListener(this);//路线
        fabbt_Trace.setOnClickListener(this);//足迹
        fabbt_ptsCluster.setOnClickListener(this);//统计
        //cp=new CompassView(getContext());
    }
    //获得时间
    public long getTime(){
        date = new Date();
        time = date.getTime();
        return time;
    }

    //MapBox定位初始化
    public void initLocation() {
        locIcon = iconFactory.fromResource(R.drawable.icon_loc1_40);

        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        // 获取所有可用的位置提供器
        List<String> providerList = locationManager.getProviders(true);
        String provider;
        if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            // 当没有可用的位置提供器时,弹出Toast提示用户
            return;
        }
        Log.e("location", provider);


        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            // 显示当前设备的位置信息
            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(),location.getLongitude())) // Sets the new camera position
                    .zoom(13) // Sets the zoom
                    .bearing(25) // Rotate the camera
                    .tilt(45) // Set the camera tilt
                    .build(); // Creates a CameraPosition from the builder

            map.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), 5000);

        }
        //考虑高德和手机设备存在的定位精度误差，如果先后两次定位距离超过25米记录一条数据，另外系统每隔20min强制记录一条数据
        locationManager.requestLocationUpdates(provider, minTime, minDist, locationListener);
    }



    // 角度转化为弧度(rad)
    private static double rad(double d)
    {
        return d * Math.PI / 180.0;
    }

    //基于余弦定理求两经纬度距离返回的距离，单位km
    public static double calculateLineDistance(double lon1, double lat1,double lon2, double lat2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);

        double radLon1 = rad(lon1);
        double radLon2 = rad(lon2);

        if (radLat1 < 0)
            radLat1 = Math.PI / 2 + Math.abs(radLat1);// south
        if (radLat1 > 0)
            radLat1 = Math.PI / 2 - Math.abs(radLat1);// north
        if (radLon1 < 0)
            radLon1 = Math.PI * 2 - Math.abs(radLon1);// west
        if (radLat2 < 0)
            radLat2 = Math.PI / 2 + Math.abs(radLat2);// south
        if (radLat2 > 0)
            radLat2 = Math.PI / 2 - Math.abs(radLat2);// north
        if (radLon2 < 0)
            radLon2 = Math.PI * 2 - Math.abs(radLon2);// west
        double x1 = EARTH_RADIUS * Math.cos(radLon1) * Math.sin(radLat1);
        double y1 = EARTH_RADIUS * Math.sin(radLon1) * Math.sin(radLat1);
        double z1 = EARTH_RADIUS * Math.cos(radLat1);

        double x2 = EARTH_RADIUS * Math.cos(radLon2) * Math.sin(radLat2);
        double y2 = EARTH_RADIUS * Math.sin(radLon2) * Math.sin(radLat2);
        double z2 = EARTH_RADIUS * Math.cos(radLat2);

        double d = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)+ (z1 - z2) * (z1 - z2));
        //余弦定理求夹角
        double theta = Math.acos((EARTH_RADIUS * EARTH_RADIUS + EARTH_RADIUS * EARTH_RADIUS - d * d) / (2 * EARTH_RADIUS * EARTH_RADIUS));
        double dist = theta * EARTH_RADIUS;
        return dist*1000;
    }




    /*-----系统基本功能------*/
   // LatLng[] points = new LatLng[15];//储存景点坐标
    //景点查询
    private void searchTouristPoint(String locName)
    {
        if(null == hashMap.get(locName))
        {
            Toast.makeText(getContext(), "找不到该景点，重新输入试试", Toast.LENGTH_SHORT).show();
        }else {
            String name = hashMap.get(locName);
            int id= Integer.parseInt(hashMap.get(locName));//key：景点名称；value：景点id，方便获得经纬度
            double loc_lat=Double.valueOf(array_Lat[id]);
            double loc_lng=Double.valueOf(array_Lng[id]);
            //视角移到景点位置
            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(loc_lat,loc_lng)) // Sets the new camera position
                    .zoom(17) // Sets the zoom
                    .bearing(25) // Rotate the camera
                    .tilt(45) // Set the camera tilt
                    .build(); // Creates a CameraPosition from the builder

            map.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), 6000);
        }
//        map.addMarker(new MarkerOptions()
//                .position(new LatLng(30.529434,114.339172))
//                .title("景点名称"));
//
//        CameraPosition cameraPosition = new CameraPosition.Builder()
//                .target(new LatLng(30.529434,114.339172))
//                .zoom(15)
//                .build();
//        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 5000, null);
//
//        origin = Position.fromCoordinates(lon_cur, lat_cur);//当前位置
//        destination = Position.fromCoordinates(114.339172,30.529434);//景点位置
//        try {
//            getRoute(origin, destination);
//        } catch (ServicesException servicesException) {
//            servicesException.printStackTrace();
//        }
    }

    //路线推荐
    public void showRecomendRoute()
    {
        List<Position> routePoints = new ArrayList<Position>();//储存组成推荐路线的点;
        //routePoints.add()
        //同时展示三条路线
        //1、经典路线
        LineString lineString = LineString.fromCoordinates(routeCoordinates);
        FeatureCollection featureCollection = FeatureCollection.fromFeatures(new Feature[]{Feature.fromGeometry(lineString)});
        Source geoJsonSource = new GeoJsonSource("line-source", featureCollection);
        map.addSource(geoJsonSource);
        LineLayer lineLayer = new LineLayer("linelayer", "line-source");
        // The layer properties for our line. This is where we make the line dotted, set the
        // color, etc.
        lineLayer.setProperties(
                //PropertyFactory.lineDasharray(new Float[]{0.01f, 2f}),
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.lineWidth(5f),
                PropertyFactory.lineColor(Color.parseColor("#87CEEB"))
        );
        map.addLayer(lineLayer);
        //2、骑行路线

        //3、可能感兴趣

    }


    //mapbox 路线service
    private void getRoute(Position origin, Position destination) throws ServicesException {

        MapboxDirections client = new MapboxDirections.Builder()
                .setOrigin(origin)
                .setDestination(destination)
                .setProfile(DirectionsCriteria.PROFILE_CYCLING)
                .setAccessToken(getString(R.string.access_token))
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                // You can get the generic HTTP info about the response
                Log.d(TAG, "Response code: " + response.code());
                if (response.body() == null) {
                    Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().getRoutes().size() < 1) {
                    Log.e(TAG, "No routes found");
                    return;
                }

                // Print some info about the route
                currentRoute = response.body().getRoutes().get(0);
                Log.d(TAG, "Distance: " + currentRoute.getDistance());
                Toast.makeText(
                        getContext(),
                        "距离目的地: " + currentRoute.getDistance() + " 米远",
                        Toast.LENGTH_SHORT).show();

                // Draw the route on the map
                drawRoute(currentRoute);
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                //Log.e(TAG, "Error: " + throwable.getMessage());
                Toast.makeText(getContext(), "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    //绘制路线
    private void drawRoute(DirectionsRoute route) {
        // Convert LineString coordinates into LatLng[]
        LineString lineStr1 = LineString.fromPolyline(route.getGeometry(), Constants.OSRM_PRECISION_V5);
        List<Position> coordinates = lineStr1.getCoordinates();

        LatLng[] points = new LatLng[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            points[i] = new LatLng(
                    coordinates.get(i).getLatitude()/10,
                    coordinates.get(i).getLongitude()/10);
        }
        map.addPolyline(new PolylineOptions()
                .add(points)
                .color(Color.parseColor("#87CEEB"))
                .width(5f));
    }


    /*--------轨迹管理--------*/
    //不同类别的点符号
    int[] int_icon={R.drawable.pt1,R.drawable.pt9,R.drawable.pt3,R.drawable.pt4,
                    R.drawable.pt11,R.drawable.pt6,R.drawable.pt7,R.drawable.pt8,
                    R.drawable.pt2,R.drawable.pt10,R.drawable.pt5};
    //足迹和轨迹显示
    public void showTrace(){
        if(lineLayer_trace != null)
        {
            map.removeLayer(lineLayer_trace);
            map.removeSource(geoJsonSource);
            lineLayer_trace=null;
        }else{
            dbPoints = DBScanService.points;//聚类点
            routeCoordinates = DBScanService.routeCoordinates;//绘制路径的点

            //map添加轨迹线
            if(routeCoordinates.size()>0) {
                // Create the LineString from the list of coordinates and then make a GeoJSON
                // FeatureCollection so we can add the line to our map as a layer.
                LineString lineString = LineString.fromCoordinates(routeCoordinates);
                FeatureCollection featureCollection = FeatureCollection.fromFeatures(new Feature[]{Feature.fromGeometry(lineString)});
                geoJsonSource = new GeoJsonSource("traceLineSrc", featureCollection);
                map.addSource(geoJsonSource);
                lineLayer_trace = new LineLayer("tracelinelayer", "traceLineSrc");
                // The layer properties for our line. This is where we make the line dotted, set the
                // color, etc.
                lineLayer_trace.setProperties(
                        //PropertyFactory.lineDasharray(new Float[]{0.01f, 2f}),
                        PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                        PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                        PropertyFactory.lineWidth(5f),
                        PropertyFactory.lineColor(Color.parseColor("#87CEEB"))
                );
                map.addLayer(lineLayer_trace);
            }

            //map添加聚类点
            if(dbPoints.size()>0) {
                for (LocPoint p : dbPoints) {
                    int i = p.getCluster();
                    if (i != 0) {
                        traceIcon = iconFactory.fromResource(int_icon[i]);
                        map.addMarker(new MarkerOptions()
                                .position(new LatLng(p.getX(), p.getY()))
                                .title("Cape Town Harbour")
                                .snippet("One of the busiest ports in South Africa")
                                .icon(traceIcon));
                    }
                }
            }//if(dbPoints.size()>0)
        }//else
    }



    /*------照片功能------*/
    public void showPicture()
    {
//        pictureIcon = iconFactory.fromResource(R.drawable.tourism_whu0);
//        map.addMarker(new MarkerOptions()
//                .position(new LatLng(31,110))
//                .title("Cape Town Harbour")
//                .snippet("One of the busiest ports in South Africa")
//                .icon(pictureIcon));
    }

    //添加照片到地图上
    public void addImageToMap(String path)
    {
        Bitmap myPicture = getSmallBitmap(path);
        Icon myPictureIcon = iconFactory.fromBitmap(myPicture);
        lat_pre = Double.valueOf(pref.getString("Lat", "0"));//100默认值
        lon_pre = Double.valueOf(pref.getString("Lon", "0"));//100默认值
        map.addMarker(new MarkerOptions()
                .position(new LatLng(lat_pre,lon_pre))
                .title("Cape Town Harbour")
                .snippet("One of the busiest ports in South Africa")
                .icon(myPictureIcon));

    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth,
                                             int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    // 根据路径获得突破并压缩返回bitmap用于显示
    private static Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 80, 190);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }



        /*******连接服务器*******/
    //上传定位
    private final int HANDLER_MSG_TELL_RECV = 0x124;
    //上传数据部分
    private void startNetThread(final String host, final int port, final String data) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(host, port);
                    OutputStream outputStream = socket.getOutputStream();

                    /*Resources res = getResources();
                    Bitmap bmp= BitmapFactory.decodeResource(res, R.drawable.picture6);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                    outputStream.write(baos.toByteArray());*/
                    outputStream.write((data).getBytes());
                    Log.i("post","ready");
                    outputStream.flush();
                    Log.i("post","success");
                    System.out.println(socket);

                    InputStream is = socket.getInputStream();
                    byte[] bytes = new byte[1024];
                    int n = is.read(bytes);
                    System.out.println(new String(bytes, 0, n));

                    Message msg = handler.obtainMessage(HANDLER_MSG_TELL_RECV, new String(bytes, 0, n));
                    Log.i("get","ready");
                    msg.sendToTarget();
                    Log.i("get","success");
                    is.close();
                    socket.close();
                } catch (Exception e) {
                    Log.i("Exception",e.toString());
                }
            }
        };

        thread.start();
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("来自服务器的数据：" + (String)msg.obj);
            builder.create().show();
        };
    };


    /*-------view监听点击-------*/
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id)
        {
            case R.id.bt_search://景点查询
                String locName= (ed_searchLoc.getText()).toString();//输入的名称
                searchTouristPoint(locName);
                break;
            case R.id.fabbutton_1: //旅游路线
                break;
            case R.id.fabbutton_2://足迹
                if(!is_fabbt_TraceClicked) {
                    is_fabbt_TraceClicked=true;
                    showTrace();//显示
                }else {
                    is_fabbt_TraceClicked=false;
                }
                break;
            case R.id.fabbutton_3://照片
                showPicture();
                break;

            case R.id.navigation_LL:
                origin = Position.fromCoordinates(lon_cur, lat_cur);//当前位置
                destination = Position.fromCoordinates(target_lng,target_lat);//景点位置
                getRoute(origin,destination);
                break;
        }
    }//onClick


    /*------系统方法重载-------*/
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
        if (locationManager != null) {
            // 关闭程序时将监听器移除
            locationManager.removeUpdates(locationListener);
        }
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
        mapView.onSaveInstanceState(outState);//保存地图当前的状态
    }


    //定位监听接口,5s/5m定一次位，和上次储存的距离差40m或者每隔20min强行记录
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle
                extras) {
        }
        @Override
        public void onProviderEnabled(String provider) {
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
        @Override
        public void onLocationChanged(Location location) {
            double distance_precur;
            lat_pre = Double.valueOf(pref.getString("Lat", "0"));//100默认值
            lon_pre = Double.valueOf(pref.getString("Lon", "0"));//100默认值
            lat_cur = location.getLatitude();
            lon_cur = location.getLongitude();

            distance_precur = calculateLineDistance(lon_pre,lat_pre,lon_cur,lat_cur);

            Time t=new Time("GMT+8"); // or Time t=new Time("GMT+8"); 加上Time Zone资料。
            t.setToNow(); // 取得系统时间。
            int minute = t.minute;
            int second = t.second;

            if(distance_precur > 40 || ((minute%30 == 0) && (second>0 && second<10))) {
                value.put("Time", getTime());
                value.put("Lat", lat_cur);
                value.put("Lon", lon_cur);
                value.put("val1", 0);
                value.put("val2", 0);
                db.insert("location", null, value);
                value.clear();

                //sharedPreferences 储存上一次成功定位数据
                editor.putString("Lat", String.valueOf(lat_cur));
                editor.putString("Lon", String.valueOf(lon_cur));
                editor.commit();
                try {//上传经纬度
                    String content=new String(String.valueOf(lon_cur)+String.valueOf(lat_cur));
                    startNetThread(pref.getString("host", "0"), Integer.parseInt(pref.getString("port", "0")), content);
                }catch (Exception e) {
                    Log.d("upLoadData",e.toString());
                }
            }
        }//onLocationChanged
    };
}
