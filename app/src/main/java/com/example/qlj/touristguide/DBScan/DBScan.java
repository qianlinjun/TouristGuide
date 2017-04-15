package com.example.qlj.touristguide.DBScan;

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

    public void process(ArrayList<DBScanPoint> points) {
        int size = points.size();
        int idx = 0;
        int cluster = 1;
        while (idx<size) {
            DBScanPoint p = points.get(idx++);
            //choose an unvisited DBScanPoint
            if (!p.getVisit()) {
                p.setVisit(true);//set visited
                ArrayList<DBScanPoint> adjacentPoints = getAdjacentPoints(p, points);
                //set the DBScanPoint which adjacent points less than minPts noised
                if (adjacentPoints != null && adjacentPoints.size() < minPts) {
                    p.setNoised(true);
                } else {
                    p.setCluster(cluster);
                    for (int i = 0; i < adjacentPoints.size(); i++) {
                        DBScanPoint adjacentPoint = adjacentPoints.get(i);
                        //only check unvisited DBScanPoint, cause only unvisited have the chance to add new adjacent points
                        if (!adjacentPoint.getVisit()) {
                            adjacentPoint.setVisit(true);
                            ArrayList<DBScanPoint> adjacentAdjacentPoints = getAdjacentPoints(adjacentPoint, points);
                            //add DBScanPoint which adjacent points not less than minPts noised
                            if (adjacentAdjacentPoints != null && adjacentAdjacentPoints.size() >= minPts) {
                                adjacentPoints.addAll(adjacentAdjacentPoints);
                            }
                        }
                        //add DBScanPoint which doest not belong to any cluster
                        if (adjacentPoint.getCluster() == 0) {
                            adjacentPoint.setCluster(cluster);
                            //set DBScanPoint which marked noised before non-noised
                            if (adjacentPoint.getNoised()) {
                                adjacentPoint.setNoised(false);
                            }
                        }
                    }
                    cluster++;
                }
            }
        }
    }

    private ArrayList<DBScanPoint> getAdjacentPoints(DBScanPoint centerPoint,ArrayList<DBScanPoint> points) {
        ArrayList<DBScanPoint> adjacentPoints = new ArrayList<DBScanPoint>();
        for (DBScanPoint p:points) {
            //include centerPoint itself
            double distance = centerPoint.getDistance(p);
            if (distance<=radius) {
                adjacentPoints.add(p);
            }
        }
        return adjacentPoints;
    }
}
