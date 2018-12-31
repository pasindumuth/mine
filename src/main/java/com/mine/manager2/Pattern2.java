package com.mine.manager2;

import java.util.ArrayList;
import java.util.List;

public class Pattern2 {

    private int patternID;
    private List<Long> startTimes;
    private List<Long> durations;

    public Pattern2(int patternID) {
        this.patternID = patternID;
        this.startTimes = new ArrayList<>();
        this.durations = new ArrayList<>();
    }

    public int getPatternID() {
        return patternID;
    }

    public List<Long> getStartTimes() {
        return startTimes;
    }

    public List<Long> getDurations() {
        return durations;
    }

    public void addInstance(long startTime, long endTime) {
        startTimes.add(startTime);
        durations.add(endTime - startTime);
    }

    public void reset() {
        startTimes = new ArrayList<>();
        durations = new ArrayList<>();
    }
}
