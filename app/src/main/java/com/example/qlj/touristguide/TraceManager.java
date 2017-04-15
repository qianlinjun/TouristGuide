package com.example.qlj.touristguide;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.r0adkll.slidr.Slidr;

/**
 * Created by Qlj on 2017/4/14.
 */

public class TraceManager extends AppCompatActivity implements View.OnClickListener {
    Button bt_mytrace;

    //数据库读取定位数据
    public MyDataBasehelp dbHelper;
    public SQLiteDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracemanager);
        initView();
        initDatabase();
    }

    //初始化界面
    private void initView()
    {
        bt_mytrace=(Button)findViewById(R.id.bt_mytrace);
        bt_mytrace.setOnClickListener(this);
        int primary = getResources().getColor(R.color.primaryDark);
        int secondary = getResources().getColor(R.color.secondaryDark);
        Slidr.attach(this, primary, secondary);
    }

    //初始化数据库
    private void initDatabase()
    {
        dbHelper = new MyDataBasehelp(TraceManager.this, "user.db", null, 1);//构造dbHelper对象
        db = dbHelper.getWritableDatabase();
    }

    //点击事件监听
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id)
        {
            case R.id.bt_mytrace:
                Cursor cursor = db.query("location",null, null,null, null,null, null);
                if (cursor.moveToFirst()) {
                    do {
                        int ID=cursor.getInt(cursor.getColumnIndex("ID"));
                        Double Lat=cursor.getDouble(cursor.getColumnIndex("Lat"));
                        Double Long = cursor.getDouble(cursor.getColumnIndex("Lon"));
                        //int hour=loc2/60;
                        //int minute=loc2%60;

                        //绘制marker
//                        Marker marker = aMap.addMarker(new MarkerOptions()
//                                .position(new LatLng(Lat,Long))
//                                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
//                                        .decodeResource(getResources(),R.drawable.location)))
//                                .draggable(true));

//                      // 绘制曲线
//                      aMap.addPolyline((new PolylineOptions())
//                      .add(new LatLng(43.828, 87.621), new LatLng(45.808, 126.55))
//                      .geodesic(true).color(Color.RED));
                    } while (cursor.moveToNext());
                    cursor.close();
                }
                break;
        }

    }
}
