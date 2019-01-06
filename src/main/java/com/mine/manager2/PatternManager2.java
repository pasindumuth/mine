package com.mine.manager2;

import com.mine.Constants;
import org.json.JSONArray;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PatternManager2 {

    DistanceMap distanceMap;
    List<SubtraceRepresentation> patternRepresentations = new ArrayList<>();
    List<Pattern2> patternInstances = new ArrayList<>();

    public PatternManager2(DistanceMap distanceMap) {
        this.distanceMap = distanceMap;
        initializeNullPattern();
    }

    private void initializeNullPattern() {
        SubtraceRepresentation nullRepresentation = new SubtraceRepresentation(distanceMap, Constants.NULL_FUNCTION_ID);
        nullRepresentation.setDepth(0);
        patternRepresentations.add(nullRepresentation);

        // Update distance map to include null pattern
        List<Double> newDistances = new ArrayList<>();
        newDistances.add(0.0);
        distanceMap.get().add(newDistances);

        // Update instances
        patternInstances.add(new Pattern2(Constants.NULL_PATTERN_ID, nullRepresentation));
    }

    public double getAcceptanceDistance(int patternId) {
        return Constants.NULL_FUNCTION_DISTANCE * patternRepresentations.get(patternId).getDepth();
    }

    public DistanceMap getDistanceMap() {
        return distanceMap;
    }

    public int updatePatterns(RepresentationContainer container) {
        SubtraceRepresentation newRepresentation = container.getRepresentation();

        // Update depth
        int maxDepth = 0;
        for (int patternId : newRepresentation.getPatternIds()) {
            maxDepth = Math.max(maxDepth, patternRepresentations.get(patternId).getDepth());
        }
        newRepresentation.setDepth(maxDepth + 1);

        for (int patternId = 0; patternId < patternRepresentations.size(); patternId++) {
            SubtraceRepresentation representation = patternRepresentations.get(patternId);
            double acceptanceDistance = getAcceptanceDistance(patternId);
            double distance = newRepresentation.getDistance(representation);
            // Accept subtrace only from patterns whose depths which are greater.
            if (distance < acceptanceDistance && representation.getDepth() >= newRepresentation.getDepth()) {
                patternInstances.get(patternId).addInstance(container);
                return patternId;
            }
        }

        // None of the existing patterns accepts newRepresentation
        // Update distanceMap
        List<Double> newDistances = new ArrayList<>();
        for (int patternId = 0; patternId < patternRepresentations.size(); patternId++) {
            SubtraceRepresentation representation = patternRepresentations.get(patternId);
            double distance = newRepresentation.getDistance(representation);
            newDistances.add(distance);
            distanceMap.get().get(patternId).add(distance);
        }
        newDistances.add(0.0);
        distanceMap.get().add(newDistances);

        // Update representation list
        int newPatternId = patternRepresentations.size();
        patternRepresentations.add(newRepresentation);

        // Update instances
        Pattern2 pattern = new Pattern2(newPatternId, newRepresentation);
        pattern.addInstance(container);
        patternInstances.add(pattern);

        return newPatternId;
    }

    public void resetPatterns() {
        for (Pattern2 pattern : patternInstances) {
            pattern.reset();
        }
    }

    public void dumpPatterns(BufferedWriter writer, Long absoluteStartTime) throws IOException {
        verifyInstances();
        Map<Integer, Integer> singleFunctionPatterns = new HashMap<>(); // maps patternIds of single function patterns to their base functions.
        for (int patternId = 0; patternId < patternRepresentations.size(); patternId++) {
            if (patternRepresentations.get(patternId).getDepth() == 1) {
                singleFunctionPatterns.put(patternId, patternRepresentations.get(patternId).getBaseFunction());
            }
        }

        // Since the pattern manager contains pattern shapes from previous threads, the patterns
        // need to have any instances in this thread.
        List<Pattern2> filteredPatternInstances = patternInstances.stream().filter(pattern ->
                !(singleFunctionPatterns.containsKey(pattern.getPatternId()) || pattern.getStartTimes().isEmpty())
        ).collect(Collectors.toList());

        List<Pattern2> sortedPatternInstances = filteredPatternInstances.stream().sorted(
                Comparator.comparing(Pattern2::getDepth)
        ).collect(Collectors.toList());

        JSONArray serializedPatterns = new JSONArray();
        for (Pattern2 pattern : sortedPatternInstances) {
            serializedPatterns.put(pattern.serialize(singleFunctionPatterns, absoluteStartTime));
        }
        writer.write(serializedPatterns.toString());
//        writer.write(serializedPatterns.toString(2)); // Used for pretty printing

//        writer.write("\n");
//        writer.write("\n");
//
//        for (Pattern2 pattern1 : filteredPatternInstances) {
//            for (Pattern2 pattern2 : filteredPatternInstances) {
//                writer.write(String.valueOf(distanceMap.getDistance(pattern1.getPatternId(), pattern2.getPatternId())));
//                writer.write("\t");
//            }
//            writer.write("\n");
//        }
    }

    /**
     * Verifies:
     * 1. Patterns have constituent patterns with strictly lower depth.
     * 1. The pattern instances instances of the patterns for a given depth occur on disjoint time intervals.
     * 2. All patterns occurences are accounted for wherever they occur (except the first pattern).
     */
    private void verifyInstances() {
        System.out.println("Starting pattern verification.");
        verifyLowerDepthReferencing();
        verifyDisjointIntervals();
        verifyPatternOccurenceCounts();
        System.out.println("Pattern verification finished.");
    }

    private void verifyLowerDepthReferencing() {
        for (Pattern2 pattern: patternInstances) {
            for (int patternId : pattern.getPatternIdCounts().keySet()) {
                if (!(patternRepresentations.get(patternId).getDepth() < pattern.getDepth())) {
                    System.out.println("Faiure: depth of constituent pattern is not strictly less that current pattern.");
                }
            }
        }
    }

    private void verifyDisjointIntervals() {
        int maxDepth = 0;
        for (Pattern2 pattern : patternInstances) {
            maxDepth = Math.max(maxDepth, pattern.getDepth());
        }

        List<List<Pattern2>> patternsByDepth = new ArrayList<>();
        // Initialized patternsByDepth
        for (int i = 0; i <= maxDepth; i++) {
            patternsByDepth.add(new ArrayList<>());
        }

        // Populate ordered patterns
        for (Pattern2 pattern : patternInstances) {
            patternsByDepth.get(pattern.getDepth()).add(pattern);
        }

        // Verify patterns of the same depth are disjoint
        for (List<Pattern2> patterns : patternsByDepth) {
            List<Interval> intervals = new ArrayList<>();
            for (Pattern2 pattern : patterns) {
                List<Long> startTimes = pattern.getStartTimes();
                List<Long> durations = pattern.getDurations();
                for (int i = 0; i < startTimes.size(); i++) {
                    intervals.add(new Interval(startTimes.get(i), durations.get(i)));
                }
            }
            intervals.sort((i1, i2) -> {
                if (i1.startTime - i2.startTime > 0) {
                    return 1;
                } else if (i1.startTime == i2.startTime) {
                    return 0;
                } else {
                    return -1;
                }
            });
            for (int i = 0; i < intervals.size() - 1; i++) {
                if (!(intervals.get(i + 1).startTime >= intervals.get(i).startTime + intervals.get(i).duration)) {
                    System.out.println("Failure: intervals of two patterns of the same depth or overlapping.");
                }
            }
        }
    }

    public void verifyPatternOccurenceCounts() {
        List<Integer> patternOccurenceCount = new ArrayList<>();
        for (Pattern2 pattern: patternInstances) {
            patternOccurenceCount.add(pattern.getStartTimes().size());
        }
        for (Pattern2 pattern : patternInstances) {
            for (Map.Entry<Integer, Integer> patternIdCount : pattern.getPatternIdCounts().entrySet()) {
                int patternId = patternIdCount.getKey();
                int currentCount = patternOccurenceCount.get(patternId);
                patternOccurenceCount.set(patternId, currentCount - patternIdCount.getValue());
            }
        }
        // Ignore the null pattern and the base pattern
        for (int patternId = 1; patternId < patternOccurenceCount.size() - 1; patternId++) {
            if (patternOccurenceCount.get(patternId) != 0) {
                System.out.println("Failure: occurences of a non base pattern are not accounted for.");
            }
        }
        if (patternOccurenceCount.get(patternOccurenceCount.size() - 1) != 1) {
            System.out.println("Failure: base pattern has occurence count that is not 1.");
        }
    }

    private static class Interval {
        long startTime;
        long duration;

        Interval(long startTime, long duration) {
            this.startTime = startTime;
            this.duration = duration;
        }
    }
}