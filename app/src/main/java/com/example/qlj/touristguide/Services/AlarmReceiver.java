package com.example.qlj.touristguide.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Qljqian on 2017/4/18.
 */

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1=new Intent(context,DBScanService.class);
        context.startService(intent1);//每隔30min接收AlarmReceiver消息，定时启动Dbscan聚类算法
    }
}
