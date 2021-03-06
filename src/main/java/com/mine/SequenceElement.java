package com.mine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The `getDistance` method makes the set of SequenceElements (for set of PatternIDs that form a metric space)
 * into a metric space. We use the SequenceElement gotten from createNullSequenceElement()
 */
public class SequenceElement {

    /**
     * The distance between each pair of patternIDs must 
     * be <= Constants.PATTERN_SIMILARITY_THRESHOLD
     */

    /**
     * Maps patternID to count
     */
    private Map<Integer, Integer> patternIDCounts = new HashMap<>();
    private PatternManager.PatternDistances patternDistances;

    public SequenceElement(PatternManager.PatternDistances patternDistances, Integer patternID) {
        this.patternDistances = patternDistances;
        this.patternIDCounts.put(patternID, 1);
    }

    /** The patternIDCounts is an array of key-value pairs, where the first is pattern ID, and the second is the count. */
    public SequenceElement(PatternManager.PatternDistances patternDistances, int[][] patternIDCounts) {
        this.patternDistances = patternDistances;
        for (int[] pair : patternIDCounts) {
            this.patternIDCounts.put(pair[0], pair[1]);
        }
    }

    public SequenceElement(PatternManager.PatternDistances patternDistances) {
        this.patternDistances = patternDistances;
    }

    /** Importantly, although we designate this SequenceElement as 'null', it is part of the metric space just the same. */
    public static SequenceElement createNullSequenceElement(PatternManager.PatternDistances patternDistances) {
        return new SequenceElement(patternDistances, PatternManager.NULL_PATTERN_ID);
    }

    /**
     * Merges another element in. `patternIDs` will become the union,
     * and `count` for each patternID will be summed.
     * 
     * WARNING: this operation might break the PATTERN_SIMILARITY_THRESHOLD
     * requirement. We must be sure the elements can be merged before merging.
     */
    public void merge(SequenceElement otherElement) {
        for (Map.Entry<Integer, Integer> patternIDCount : otherElement.patternIDCounts.entrySet()) {
            Integer patternID = patternIDCount.getKey();
            Integer otherCount = patternIDCount.getValue();
            Integer thisCount = this.patternIDCounts.get(patternID);
            if (thisCount == null) thisCount = 0;

            this.patternIDCounts.put(patternID, thisCount + otherCount);
        }
    }

    /**
     * Checks if merging the elements will maintain the 
     * PATTERN_SIMILARITY_THRESHOLD requirement.
     */
    public boolean canMerge(SequenceElement otherElement) {
        for (Integer thisPatternID: this.patternIDCounts.keySet()) {
            for (Integer otherPatternID: otherElement.patternIDCounts.keySet()) {
                if (patternDistances.getDistance(thisPatternID, otherPatternID) 
                    > Constants.PATTERN_SIMILARITY_THRESHOLD) {
                    return false;
                }
            }
        }

        return true;
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
    public double getDistance(SequenceElement otherElement) {
        double leftDistance = 0;
        double rightDistance = 0;

        for (int thisPatternID : this.patternIDCounts.keySet()) {
            double distanceToOther = Integer.MAX_VALUE;
            for (int otherPatternID : otherElement.patternIDCounts.keySet()) {
                double curDistance = patternDistances.getDistance(thisPatternID, otherPatternID);
                if (curDistance < distanceToOther) distanceToOther = curDistance;
            }
            if (distanceToOther > leftDistance) leftDistance = distanceToOther;
        }

        for (int otherPatternID : otherElement.patternIDCounts.keySet()) {
            double distanceToThis = Integer.MAX_VALUE;
            for (int thisPatternID : this.patternIDCounts.keySet()) {
                double curDistance = patternDistances.getDistance(otherPatternID, thisPatternID);
                if (curDistance < distanceToThis) distanceToThis = curDistance;
            }
            if (distanceToThis > rightDistance) rightDistance = distanceToThis;
        }

        return Math.max(leftDistance, rightDistance);
    }

    /**
     * Creates clone with all counts set to 0.
     */
    public SequenceElement createEmptyClone() {
        SequenceElement clone = new SequenceElement(patternDistances);
        for (Integer patternID : patternIDCounts.keySet()) {
            clone.patternIDCounts.put(patternID, 0);
        }
        
        return clone;
    }

    public String toString(Map<Integer, Integer> singleFunctions) {  
        StringBuilder s = new StringBuilder();
        s.append("[");
        ArrayList<String> countStrings = new ArrayList<>();
        for (Integer patternID : patternIDCounts.keySet()) {
            Integer count = patternIDCounts.get(patternID);
            // We want to print only the base functions for patterns that are single functions, but the fully 
            // qualified patternID for standard patterns.
            if (singleFunctions.containsKey(patternID)) {
                countStrings.add(singleFunctions.get(patternID) + " => " + String.valueOf(count));
            } else {
                countStrings.add(String.valueOf(Constants.PATTERN_BASE + patternID) + " => " + String.valueOf(count));
            }
        }

        s.append(String.join(", ", countStrings));
        s.append("]");

        return s.toString();
    }
}
