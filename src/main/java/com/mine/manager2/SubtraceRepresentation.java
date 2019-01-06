package com.mine.manager2;

import com.mine.Constants;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class SubtraceRepresentation {

    private final DistanceMap distanceMap;
    private final TreeMap<Integer, Integer> patternIds = new TreeMap<>();
    private final Integer baseFunction;
    private int depth;

    public SubtraceRepresentation(
            DistanceMap distanceMap,
            Integer baseFunction) {
        this.distanceMap = distanceMap;
        this.baseFunction = baseFunction;
        addPatternId(Constants.NULL_PATTERN_ID);
    }

    public void addPatternId(Integer patternId) {
        patternIds.putIfAbsent(patternId, 0);
        patternIds.put(patternId, patternIds.get(patternId) + 1);
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getBaseFunction() {
        return baseFunction;
    }

    public int getDepth() {
        return depth;
    }

    public Set<Integer> getPatternIds() {
        return patternIds.keySet();
    }

    public TreeMap<Integer, Integer> getPatternIdCounts() {
        return patternIds;
    }

    /**
     * Computes distance between this element and `otherElement`. We require
     * this distance to be a metric.
     *
     * This distance is defined as the Hausdorff distance on the set of nonempty
     * subsets of patterns. That is, the distance between elements e1 and e2 is:
     * d(e1, e2) = max{max_(p1 in e1){min_(p2 in e2){d(p1, p2)}}, max_(p2 in e2){min_(p1 in e1){d(p1, p2)}}}
     *
     * Since the Hausdorff distance turns the set of non empty subsets of a metric space into
     * a metric space, we see this function turns the set of all SequenceElements into
     * a metric space (since SequenceElements are essentially subsets of the set of all
     * patterns, which is itelf a metric space).
     */
    public double getDistance(SubtraceRepresentation other) {
        double leftDistance = 0;
        double rightDistance = 0;

        for (int thisPatternID : this.patternIds.keySet()) {
            double distanceToOther = Integer.MAX_VALUE;
            for (int otherPatternID : other.patternIds.keySet()) {
                double curDistance = distanceMap.getDistance(thisPatternID, otherPatternID);
                if (curDistance < distanceToOther) distanceToOther = curDistance;
            }
            if (distanceToOther > leftDistance) leftDistance = distanceToOther;
        }

        for (int otherPatternID : other.patternIds.keySet()) {
            double distanceToThis = Integer.MAX_VALUE;
            for (int thisPatternID : this.patternIds.keySet()) {
                double curDistance = distanceMap.getDistance(otherPatternID, thisPatternID);
                if (curDistance < distanceToThis) distanceToThis = curDistance;
            }
            if (distanceToThis > rightDistance) rightDistance = distanceToThis;
        }

        double patternSetDistance = Math.max(leftDistance, rightDistance);
        double functionDistance = getFunctionDistance(this.baseFunction, other.baseFunction);
        return patternSetDistance + functionDistance;
    }

    /**
     * This is a separate metric over the set of function ids. All non null function are
     * distance 1.0 away, while null is some <= 1.0 distance away. We can verify easily
     * this forms a metric.
     */
    private double getFunctionDistance(int f1, int f2) {
        if (f1 == f2)
            return 0.0;
        else if (f1 == Constants.NULL_FUNCTION_ID || f2 == Constants.NULL_FUNCTION_ID)
            return Constants.NULL_FUNCTION_DISTANCE;
        else
            return Constants.FUNCTION_DISTANCE;
    }

    public String prettyString(Map<Integer, Integer> singleFunctionPatterns) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // Add depth
        sb.append(depth);
        sb.append(", ");

        // Add base function
        sb.append("[");
        sb.append(baseFunction);
        sb.append("], ");

        sb.append("[");
        List<String> patternIdStrings = new ArrayList<>(); // single function patterns will just use their base functions
        for (int patternId : patternIds.keySet()) {
            if (singleFunctionPatterns.containsKey(patternId)) {
                patternIdStrings.add(String.valueOf(singleFunctionPatterns.get(patternId)));
            } else {
                patternIdStrings.add(String.valueOf(Constants.PATTERN_BASE + patternId));
            }
        }

        sb.append(String.join(", ", patternIdStrings));
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

    public JSONObject serialize(Map<Integer, Integer> singleFunctionPatterns) {
        JSONArray patternIds = new JSONArray();
        for (int patternId : this.patternIds.keySet()) {
            if (patternId != Constants.NULL_PATTERN_ID) { // Don't bother adding the null pattern
                if (singleFunctionPatterns.containsKey(patternId)) {
                    patternIds.put(singleFunctionPatterns.get(patternId));
                } else {
                    patternIds.put(Constants.PATTERN_BASE + patternId);
                }
            }
        }

        return new JSONObject()
                .put("depth", depth)
                .put("baseFunction", baseFunction)
                .put("patternIds", patternIds);
    }
}
