package com.example.qlj.touristguide.TraceAnalysis;

import com.amap.api.location.DPoint;
import com.amap.api.maps2d.model.LatLng;

import static com.amap.api.location.CoordinateConverter.calculateLineDistance;

/**
 * Created by Qljqian on 2017/4/14.
 */

public class LocPoint {
    private double x;
    private double y;
    private Long timeStamp;
    private boolean isVisit;
    private int cluster;
    private boolean isNoised;

    public LocPoint(double x,double y,Long TimeStamp) {
        this.x = x;
        this.y = y;
        this.timeStamp=TimeStamp;
        this.isVisit = false;
        this.cluster = 0;
        this.isNoised = false;
    }

    public double getDistance(LocPoint point) {
        return calculateLineDistance(new DPoint(x,y),new DPoint(point.getX(),point.getY()));
        //return Math.sqrt((x-point.x)*(x-point.x)+(y-point.y)*(y-point.y));
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getX() {
        return x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getY() {
        return y;
    }

    public long getTimestamp()
    {
        return timeStamp;
    }

    public void setVisit(boolean isVisit) {
        this.isVisit = isVisit;
    }

    public boolean getVisit() {
        return isVisit;
    }

    public int getCluster() {
        return cluster;
    }

    public void setNoised(boolean isNoised) {
        this.isNoised = isNoised;
    }

    public void setCluster(int cluster) {
        this.cluster = cluster;
    }

    public boolean getNoised() {
        return this.isNoised;
    }

    @Override
    public String toString() {
        return x+" "+y+" "+cluster+" "+(isNoised?1:0);
    }
}
