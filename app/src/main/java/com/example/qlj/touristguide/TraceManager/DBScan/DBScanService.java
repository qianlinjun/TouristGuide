package com.example.qlj.touristguide.TraceManager.DBScan;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.example.qlj.touristguide.databaseSQLite.MyDataBasehelp;
import com.mapbox.services.commons.models.Position;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Qljqian on 2017/4/18.
 */

public class DBScanService extends Service {
    private AlarmManager alarmManager;//定时任务
    private int semiHour = 1800000;//每隔30(min) * 60(sec) * 1000(mill)运算一次dbscan
    private long triggerAtTime;

    //数据库读取定位数据
    private MyDataBasehelp dbHelper;
    private SQLiteDatabase db;
    private Date now;//按时间查询
    private GregorianCalendar cal;//日历
    private long startTime;//查询开始时间
    private long endTime;//查询结束时间

    //定位点数据读取和聚类分析
    public static ArrayList<LocPoint> points;
    public static List<Position> routeCoordinates;
    public static int[] eachTypeTotalPts;//..Pts[i]表示类别为i的点数量
    private long[] lastTimeStamp;//每一个类别都有开始结束时间戳
    public static int[] eachTypeDuraTime;//..Time[i]表示类别i的轨迹总时间
    private long interval = 660000;//系统设定中有每隔10分钟强制记录一条定位数据，
                                   // 如果同类别相邻点时间间隔大于11(min) x 60（sec） x 1000（mil）,
                                   //说明不是连续定位
    private DBScan dbScan;//DBScan方法实例
    private final int radius = 15;//聚类半径
    private final int minPts = 3;//半径内最少点数


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();//第一次被调用
        alarmManager=(AlarmManager)getSystemService(ALARM_SERVICE);

        initDatabase();
        initDBScan();
    }

    @Override
    public int onStartCommand(Intent intent,  int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBScanAnalysis();
                stopSelf();//执行完毕关闭服务
            }
        }).start();

        triggerAtTime = SystemClock.elapsedRealtime()+semiHour;
        Intent i = new Intent(this,AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this,0,i,0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }


    //初始化数据库
    private void initDatabase()
    {
        dbHelper = new MyDataBasehelp(this, "user.db", null, 1);//构造dbHelper对象
        db = dbHelper.getWritableDatabase();
    }

    //初始化DBScan
    private void initDBScan()
    {
        points = new ArrayList<LocPoint>();//储存自己定位点
        routeCoordinates = new ArrayList<Position>();//储存mapbox显示轨迹的点
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

        try{
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
                    routeCoordinates.add(Position.fromCoordinates(lon,lat));
                } while (cursor.moveToNext());
                cursor.close();
            }
            int typeNum = dbScan.process(points);//聚类，给每一个点都赋值类别，同时返回聚类类别数
            eachTypeTotalPts = new int[typeNum+1];//..Pts[i]表示类别为i的点数量,多出来的1为杂点（非核心点）
            eachTypeDuraTime = new int[typeNum+1];//..Time[i]表示类别i的轨迹时间,多出来的1为杂点（非核心点）
            lastTimeStamp=new long[typeNum];//..TimeStamp[i]表示每个类别结束的时间戳
            for(int i=0;i<typeNum;i++)
                lastTimeStamp[i]=0;//时间戳初始化为0

            //遍历经过聚类的点集
            for(LocPoint point : points)
            {
                int cluster=point.getCluster();
                eachTypeTotalPts[cluster]++;//统计每个类别的点数量
                eachTypeDuraTime[cluster]+=getDurationTime(point);//统计每个类别的总时间
            }
        }catch (Exception e)
        {
            Log.e("DBScanService",e.toString());
        }

    }

    //获得同类别相邻记录点时间间隔
    public long getDurationTime(LocPoint point)
    {
        if(lastTimeStamp[point.getCluster()] == 0)
        {
            //如果是类别i第一个点则初始化时间戳i，返回0
            lastTimeStamp[point.getCluster()] = point.getTimestamp();
        }else if(point.getTimestamp()- lastTimeStamp[point.getCluster()] -interval < 0){
            //同类别相邻点时间间隔正常，返回时间间隔

            long duraTime=point.getTimestamp()- lastTimeStamp[point.getCluster()];
            lastTimeStamp[point.getCluster()] = point.getTimestamp();
            return duraTime;
        }else {
            //如果相邻点时间间隔大于定位间隔值，说明是两段不同定位,返回0
            lastTimeStamp[point.getCluster()] = point.getTimestamp();
        }
        return 0;
    }
}
