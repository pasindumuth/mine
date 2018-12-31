package com.mine.manager2;

import com.mine.Constants;

import java.util.*;

public class SubtraceRepresentation {

    private final DistanceMap distanceMap;
    private final Set<Integer> patternIds = new HashSet<>();
    private final Integer baseFunction;

    public SubtraceRepresentation(
            DistanceMap distanceMap,
            Integer baseFunction) {
        this.distanceMap = distanceMap;
        this.baseFunction = baseFunction;
        patternIds.add(Constants.NULL_PATTERN_ID);
    }

    public void addPatternId(Integer patternId) {
        patternIds.add(patternId);
    }

    public int getBaseFunction() {
        return baseFunction;
    }

    public Set<Integer> getPatternIds() {
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

        for (int thisPatternID : this.patternIds) {
            double distanceToOther = Integer.MAX_VALUE;
            for (int otherPatternID : other.patternIds) {
                double curDistance = distanceMap.getDistance(thisPatternID, otherPatternID);
                if (curDistance < distanceToOther) distanceToOther = curDistance;
            }
            if (distanceToOther > leftDistance) leftDistance = distanceToOther;
        }

        for (int otherPatternID : other.patternIds) {
            double distanceToThis = Integer.MAX_VALUE;
            for (int thisPatternID : this.patternIds) {
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

    public String toString(Map<Integer, Integer> singleFunctionPatterns) {
        StringBuilder sb = new StringBuilder();
        sb.append("{[");
        sb.append(baseFunction);
        sb.append("], ");
        List<String> patternIdStrings = new ArrayList<>(); // single function patterns will just use their base functions
        for (int patternId : patternIds) {
            if (singleFunctionPatterns.containsKey(patternId)) {
                patternIdStrings.add(String.valueOf(singleFunctionPatterns.get(patternId)));
            } else {
                patternIdStrings.add(String.valueOf(Constants.PATTERN_BASE + patternId));
            }
        }

        sb.append(String.join(", ", patternIdStrings));
        sb.append("}");
        return sb.toString();
    }
}
