package com.mine.manager2;

import java.util.List;

/**
 * Maps pairs of pattern IDs to a distance value. This is a metric.
 */
public class DistanceMap {
    private List<List<Double>> distanceMap;

    public DistanceMap(List<List<Double>> distanceMap) {
        this.distanceMap = distanceMap;
    }
    
    public double getDistance(int p1, int p2) {
        return distanceMap.get(p1).get(p2);
    }

    List<List<Double>> get() {
        return distanceMap;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (List<Double> distances : distanceMap) {
            for (Double distance : distances) {
                sb.append(distance);
                sb.append("\t");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
