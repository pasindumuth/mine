package com.mine.manager2;

import com.mine.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Pattern2 {

    private final int patternId;
    private final int depth;
    private TreeMap<Integer, Integer> patternIdCounts = new TreeMap<>();
    private TreeMap<Integer, Integer> baseFunctionCounts = new TreeMap<>();
    private List<Long> startTimes = new ArrayList<>();
    private List<Long> durations = new ArrayList<>();

    public Pattern2(Integer patternId, SubtraceRepresentation representation) {
        this.patternId =patternId;
        this.depth = representation.getDepth();
    }

    public void addInstance(RepresentationContainer container) {
        SubtraceRepresentation representation = container.getRepresentation();
        for (Map.Entry<Integer, Integer> patternIdCount : representation.getPatternIdCounts().entrySet()) {
            Integer patternId = patternIdCount.getKey();
            Integer currentCount = patternIdCounts.get(patternId);
            if (currentCount == null) currentCount = 0;
            patternIdCounts.put(patternId, currentCount + patternIdCount.getValue());
        }
        Integer baseFunction = representation.getBaseFunction();
        Integer currentCount = baseFunctionCounts.get(baseFunction);
        if (currentCount == null) currentCount = 0;
        baseFunctionCounts.put(baseFunction, currentCount + 1);

        startTimes.add(container.getStartTime());
        durations.add(container.getEndTime() - container.getStartTime());
    }

    public int getPatternID() {
        return patternId;
    }

    public int getDepth() {
        return depth;
    }

    public List<Long> getStartTimes() {
        return startTimes;
    }

    public List<Long> getDurations() {
        return durations;
    }

    public void reset() {
        startTimes.clear();
        durations.clear();
    }

    public String toString(Map<Integer, Integer> singleFunctionPatterns) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // Add depth
        sb.append(depth);
        sb.append(", ");

        // Add base function set
        sb.append("[");
        List<String> baseFunctionCountStrings = new ArrayList<>();
        for (Map.Entry<Integer, Integer> baseFunctionCount : baseFunctionCounts.entrySet()) {
            baseFunctionCountStrings.add(baseFunctionCount.getKey() + " -> " + baseFunctionCount.getValue());
        }
        sb.append(String.join(", ", baseFunctionCountStrings));
        sb.append("], ");

        // Add patternId set
        sb.append("[");
        List<String> patternIdCountStrings = new ArrayList<>(); // single function patterns will just use their base functions
        for (Map.Entry<Integer, Integer> patternIdCount : patternIdCounts.entrySet()) {
            if (singleFunctionPatterns.containsKey(patternIdCount.getKey())) {
                String singleFunctionId = singleFunctionPatterns.get(patternIdCount.getKey()).toString();
                patternIdCountStrings.add(singleFunctionId + " -> " + patternIdCount.getValue());
            } else {
                patternIdCountStrings.add(patternIdCount.getKey() + Constants.PATTERN_BASE + " -> " + patternIdCount.getValue());
            }
        }
        sb.append(String.join(", ", patternIdCountStrings));
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

}
