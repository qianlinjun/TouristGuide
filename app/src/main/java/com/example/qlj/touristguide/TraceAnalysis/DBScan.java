package com.example.qlj.touristguide.TraceAnalysis;

import com.example.qlj.touristguide.TraceAnalysis.LocPoint;

import java.util.ArrayList;

/**
 * Created by Qljqian on 2017/4/14.
 */

public class DBScan {
    private double radius;
    private int minPts;

    public DBScan(double radius,int minPts) {
        this.radius = radius;
        this.minPts = minPts;
    }

    public int process(ArrayList<LocPoint> points) {
        int size = points.size();
        int idx = 0;
        int clusterNum = 1;
        while (idx<size) {
            LocPoint p = points.get(idx++);
            //choose an unvisited LocPoint
            if (!p.getVisit()) {
                p.setVisit(true);//set visited
                ArrayList<LocPoint> adjacentPoints = getAdjacentPoints(p, points);
                //set the LocPoint which adjacent points less than minPts noised
                if (adjacentPoints != null && adjacentPoints.size() < minPts) {
                    p.setNoised(true);
                } else {
                    p.setCluster(clusterNum);
                    for (int i = 0; i < adjacentPoints.size(); i++) {
                        LocPoint adjacentPoint = adjacentPoints.get(i);
                        //only check unvisited LocPoint, cause only unvisited have the chance to add new adjacent points
                        if (!adjacentPoint.getVisit()) {
                            adjacentPoint.setVisit(true);
                            ArrayList<LocPoint> adjacentAdjacentPoints = getAdjacentPoints(adjacentPoint, points);
                            //add LocPoint which adjacent points not less than minPts noised
                            if (adjacentAdjacentPoints != null && adjacentAdjacentPoints.size() >= minPts) {
                                adjacentPoints.addAll(adjacentAdjacentPoints);
                            }
                        }
                        //add LocPoint which doest not belong to any cluster
                        if (adjacentPoint.getCluster() == 0) {
                            adjacentPoint.setCluster(clusterNum);
                            //set LocPoint which marked noised before non-noised
                            if (adjacentPoint.getNoised()) {
                                adjacentPoint.setNoised(false);
                            }
                        }
                    }//for
                    clusterNum++;
                }
            }
        }//while
        return clusterNum--;//返回聚类类别数
    }

    private ArrayList<LocPoint> getAdjacentPoints(LocPoint centerPoint,ArrayList<LocPoint> points) {
        ArrayList<LocPoint> adjacentPoints = new ArrayList<LocPoint>();
        for (LocPoint p:points) {
            //include centerPoint itself
            double distance = centerPoint.getDistance(p);
            if (distance <= radius) {
                adjacentPoints.add(p);
            }
        }
        return adjacentPoints;
    }
}
