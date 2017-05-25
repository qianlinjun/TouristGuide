package com.example.qlj.touristguide.databaseSQLite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Qljqian on 2017/4/13.
 */

public class MyDataBasehelp extends SQLiteOpenHelper {
    public static final String CREATE_LOCATION="create table location("
            + "ID integer primary key autoincrement, "//定位点ID，整型，主键，自增加
            + "Time integer, "//时间 ，long型
            + "Lat real, "//定位的纬度(精度为小数点后三位，下同)，浮点型
            + "Lon real, "//定位的经度(精度为小数点后三位，下同)，浮点型
            + "val1 integer,"
            + "val2 integer)";


    public MyDataBasehelp(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_LOCATION);
        Log.d("database", "create database ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
